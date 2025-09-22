package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.ui.components.BitmapDescriptorFromEmoji
import ar.edu.utn.frba.mobile.foodforall.ui.components.LocationPermissionGate
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

val DEFAULT_LOCATION = LatLng(-34.598666, -58.419950)
const val DEFAULT_ZOOM = 14f
const val USER_LOCATION_ZOOM = 14f // Un zoom más cercano para la ubicación del usuario

@Composable
fun rememberBitmapDescriptorFromRes(@DrawableRes id: Int): BitmapDescriptor {
    val context = LocalContext.current
    return remember(id) {
        BitmapDescriptorFactory.fromResource(id)
    }
}




/**
 * Muestra el mapa de la aplicación.
 */
@Composable
fun MapTab(modifier: Modifier = Modifier) {
    var hasLocation by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
    }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val repo = remember { RestaurantRepository(FirebaseFirestore.getInstance()) }
    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LocationPermissionGate(
        requestOnStart = true,
        onResult = { ok -> hasLocation = ok }
    ) {

    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .debounce(250)                // evita spam
            .distinctUntilChanged()                    // evita duplicados iguales
            .filter { moving -> !moving }              // solo cuando se detiene
            .collectLatest {
                val bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                if (bounds != null) {
                    val result = repo.fetchInBounds(bounds)
                    restaurants = result
                }
            }
    }

    LaunchedEffect(hasLocation) {
        if (hasLocation) {
            try {
                // SuppressMissingPermission se usa porque estamos dentro del chequeo hasLocationPermission
                @SuppressLint("MissingPermission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        // Launch a coroutine to call animate
                        launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(userLatLng, USER_LOCATION_ZOOM, 0f, 0f)
                                ),
                                1000 // Duración de la animación en ms
                            )
                        }
                    } else {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
                    }
                }.addOnFailureListener { e ->
                    Log.e("MapTab", "Error getting location", e)
                    // En caso de error, podría quedarse en la última posición o ir a la default
                    // cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
                }
            } catch (e: SecurityException) {
                Log.e("MapTab", "Error getting location", e)
                // Esto no debería ocurrir si hasLocationPermission es true y el chequeo es correcto
            }
        } else {
            // Si el permiso no está (o se revoca), asegurar que esté en la default
            // Esto es redundante si el estado inicial ya es DEFAULT_LOCATION y no se mueve
            if (cameraPositionState.position.target != DEFAULT_LOCATION) {
                cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(DEFAULT_LOCATION, DEFAULT_ZOOM, 0f, 0f)
                    )
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocation),
        uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocation),
        onMapLongClick = { latLng: LatLng ->
            val temp = Restaurant(
                id = "temp-${System.nanoTime()}",
                title = "Nuevo punto",
                lat = latLng.latitude,
                lng = latLng.longitude,
                icon = 1,
            )
            restaurants = restaurants + temp

            scope.launch {
                try {
                    val newId = repo.addPoiAt(
                        latLng, title = "Nuevo punto"
                    )
                    // Reemplazo el temp por el definitivo con el id real (opcional)
                    restaurants = restaurants
                        .filterNot { it.id == temp.id } +
                            temp.copy(id = newId)
                } catch (e: Exception) {
                    // Si falla, saco el marcador temporal
                    restaurants = restaurants.filterNot { it.id == temp.id }
                    // TODO: mostrar un snackbar/toast con el error
                }
            }
        }
    ) {

        restaurants.forEach { poi ->
            Marker(
                state = MarkerState(position = poi.position),
                title = poi.title,
                icon = selectIcon(context, poi.icon),
                snippet = poi.snippet
            )
        }
    }
}

fun selectIcon(context: Context, icon: Long?): BitmapDescriptor {
    val resId = when (icon) {
        0L -> "🌽"
        1L ->  "🌱"
        2L ->  "🥛"
        else -> "🌽"
    }
    return BitmapDescriptorFromEmoji(context, resId, sizeDp = 36f, withBackground = true)
}
