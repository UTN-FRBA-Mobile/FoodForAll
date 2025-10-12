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
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ar.edu.utn.frba.mobile.foodforall.MainActivity
import ar.edu.utn.frba.mobile.foodforall.R
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

    private val dwellMillis = 10 * 60 * 1000L         // 10 minutos
    private val maxStillRadiusBase = 30.0             // 30 m base
    private val accuracyFactor = 2.0                  // tolerancia según precisión
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

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        ensureChannel(this)
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
            .setSmallIcon(R.drawable.ic_launcher_foreground)   // ¡OBLIGATORIO!
            .setContentTitle("Rastreando ubicación")
            .setContentText("Servicio en primer plano activo")
            .setContentIntent(pi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Android 14+ aconsejable:
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
        Log.d("STAYSERVICE","HandleLocation " + loc.latitude + "," + loc.longitude)
        val now = System.currentTimeMillis()

        // Si no hay ancla, la fijamos
        if (anchor == null) {
            anchor = loc
            anchorSince = now
            return
        }

        val dist = loc.distanceTo(anchor!!) // en metros
        val tol = maxStillRadiusBase.coerceAtLeast((loc.accuracy * accuracyFactor))

        if (dist <= tol) {
            // seguimos quietos respecto del ancla
            val since = anchorSince ?: now
            if (now - since >= dwellMillis) {
                // ¡Estancia detectada!
                // Consultar Firestore alrededor del punto ancla
                val lat = anchor!!.latitude
                val lon = anchor!!.longitude
                checkNearbyPlaces(lat, lon, 100.0)
                // Reinicia la ventana para no disparar infinitamente
                anchorSince = now
            }
        } else {
            // nos movimos: nueva ancla
            anchor = loc
            anchorSince = now
        }
    }

    private fun checkNearbyPlaces(lat: Double, lon: Double, distance: Double) {
        lifecycleScope.launch {
            try {
                val places = repo.findWithin(lat, lon, distance)
                if (places.isNotEmpty()) {
                    // TODO: tu acción (notificar, registrar, etc.)
                    // Por ejemplo, mostrar notificación secundaria o emitir un callback.
                }
            } catch (e: Exception) {
                // TODO: log/retry
            }
        }
    }




    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "stay_detect_channel"
        private const val CHANNEL_NAME = "Ubicación en segundo plano"
        private const val NOTIF_ID = 1
        private const val ACTION_STOP = "StayDetectService.STOP"
    }
}

