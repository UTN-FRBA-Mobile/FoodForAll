package ar.edu.utn.frba.mobile.foodforall.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ar.edu.utn.frba.mobile.foodforall.MainActivity
import ar.edu.utn.frba.mobile.foodforall.R
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




class StayDetectService : LifecycleService() {
    private lateinit var fused: FusedLocationProviderClient

    private var anchor: Location? = null
    private var anchorSince: Long? = null

    private val dwellMillis = 1 * 60 * 1000L         // 10 minutos
    private val maxStillRadiusBase = 30.0             // 30 m base
    private val accuracyFactor = 2.0                  // tolerancia según precisión
    private var lastSuggestedPlaceId: String? = null
    private var lastSuggestionAtMillis: Long = 0L
    //private val suggestionCooldownMillis = 12 * 60 * 60 * 1000L // 12 horas
    private val repo =  RestaurantRepository(FirebaseFirestore.getInstance())

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW   // LOW/DEFAULT, nunca NONE
                ).apply {
                    description = "Notificación del servicio de ubicación"
                    setShowBadge(false)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun ensureReviewChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = REVIEW_CHANNEL_ID
            if (nm.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(
                    id,
                    "Sugerencias de reseñas",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Te sugiere dejar reseñas cuando detecta que estuviste en un lugar"
                    setShowBadge(true)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        ensureChannel(this)
        ensureReviewChannel(this)
        startForeground(1, buildNotification())
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val req = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            2 * 60 * 1000L // cada ~2 min
        )
            .setMinUpdateDistanceMeters(15f)
            .setWaitForAccurateLocation(false)
            .build()

        fused.requestLocationUpdates(req, callback, Looper.getMainLooper())
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_food_for_all_transparent_full)   // ¡OBLIGATORIO!
            .setContentTitle("Rastreando ubicación")
            .setContentText("Servicio en primer plano activo")
            .setContentIntent(pi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT >= 34) {
            builder.setForegroundServiceBehavior(
                NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            )
        }
        return builder.build()
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            handleLocation(loc)
        }
    }

    private fun handleLocation(loc: Location) {
        val now = System.currentTimeMillis()

        val currentAnchor = anchor
        if (currentAnchor == null) {
            anchor = loc
            anchorSince = now
            return
        }

        val dist = loc.distanceTo(currentAnchor)
        val tol = maxStillRadiusBase.coerceAtLeast((loc.accuracy * accuracyFactor))

        if (dist <= tol) {
            val since = anchorSince ?: now
            if (now - since >= dwellMillis) {
                val lat = currentAnchor.latitude
                val lon = currentAnchor.longitude
                checkNearbyPlaces(lat, lon, 500000.0)
                anchorSince = now
            }
        } else {
            anchor = loc
            anchorSince = now
        }
    }

    private fun checkNearbyPlaces(lat: Double, lon: Double, distance: Double) {
        lifecycleScope.launch {
            try {
                val snaps = repo.findWithin(lat, lon, distance)
                if (snaps.isNotEmpty()) {
                    val nearest = withContext(Dispatchers.Default) {
                        snaps.asSequence()
                            .mapNotNull { doc ->
                                val gp = doc.getGeoPoint("location")
                                val rlat = gp?.latitude
                                    ?: doc.getDouble("latitude")
                                    ?: doc.getDouble("lat")
                                val rlon = gp?.longitude
                                    ?: doc.getDouble("longitude")
                                    ?: doc.getDouble("lon")

                                val rname = doc.getString("name")
                                    ?: doc.getString("title")
                                    ?: doc.getString("nombre")

                                if (rlat == null || rlon == null || rname.isNullOrBlank()) {
                                    null // snapshot incompleto: lo descartamos
                                } else {
                                    val distArr = FloatArray(1)
                                    Location.distanceBetween(lat, lon, rlat, rlon, distArr)
                                    // Guardamos lo necesario para decidir y notificar
                                    Triple(doc.id, rname, distArr[0])
                                }
                            }
                            .minByOrNull { it.third } // por distancia
                    }

                    if (nearest != null) {
                        val (restaurantId, restaurantName, _) = nearest
                        maybeShowReviewSuggestion(restaurantId, restaurantName)
                    }
                }
            } catch (e: Exception) {
                Log.e("StayDetectService", "Error al consultar lugares cercanos", e)
            }
        }
    }

    private fun maybeShowReviewSuggestion(restaurantId: String, place: String) {
        val now = System.currentTimeMillis()
        val samePlace = (restaurantId == lastSuggestedPlaceId)
        //val cooledDown = (now - lastSuggestionAtMillis) >= suggestionCooldownMillis

        if (!samePlace ) { //|| cooledDown
            showReviewSuggestionNotification(restaurantId, place)
            lastSuggestedPlaceId = restaurantId
            lastSuggestionAtMillis = now
        } else {
            Log.d("StayDetectService", "Sugerencia omitida por cooldown")
        }
    }

    private fun showReviewSuggestionNotification(restaurantId: String, place: String) {
        val context = this
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent para abrir la pantalla de reseñas dentro de MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            // Señalamos a MainActivity que navegue a "reviews"
            putExtra("dest", "reviews")
            putExtra("restaurantId", restaurantId)
            putExtra("restaurantName", place)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pi = PendingIntent.getActivity(
            context,
            restaurantId.hashCode(), // requestCode único por restaurante
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = "¿Te gustaría dejar una reseña?"
        val text = "Puede ser que estuviste en \"${place}\", ¿te gustaría dejar una reseña?"

        val notif = NotificationCompat.Builder(context, REVIEW_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_food_for_all_transparent_full)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)              // tocar noti -> abre reseñas
            .setAutoCancel(true)               // se descarta al tocar
            .setCategory(Notification.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // Usamos un ID estable por restaurante para que no se acumulen infinitas notis
        nm.notify(REVIEW_NOTIF_BASE_ID + (restaurantId.hashCode() and 0x0FFFFFFF), notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_FORCE_REVIEW -> {
                val restaurantId = intent.getStringExtra(EXTRA_RESTAURANT_ID)
                val restaurantName = intent.getStringExtra(EXTRA_RESTAURANT_NAME)

                if (!restaurantId.isNullOrBlank() && !restaurantName.isNullOrBlank()) {
                    // Disparamos el mismo flujo que cuando detecta un lugar real
                    maybeShowReviewSuggestion(restaurantId, restaurantName)
                }
            }

            ACTION_STOP -> {
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }




    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "stay_detect_channel"
        private const val CHANNEL_NAME = "Ubicación en segundo plano"
        private const val REVIEW_CHANNEL_ID = "review_suggestions_channel"
        private const val REVIEW_NOTIF_BASE_ID = 10_000
        private const val NOTIF_ID = 1
        private const val ACTION_STOP = "StayDetectService.STOP"
        const val ACTION_FORCE_REVIEW =
            "ar.edu.utn.frba.mobile.foodforall.service.ACTION_FORCE_REVIEW"
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
    }
}

