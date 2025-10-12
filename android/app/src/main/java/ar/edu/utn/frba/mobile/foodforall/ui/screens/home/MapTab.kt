package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import ar.edu.utn.frba.mobile.foodforall.ui.components.BitmapDescriptorFromEmoji
import ar.edu.utn.frba.mobile.foodforall.ui.components.LocationPermissionGate
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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
const val USER_LOCATION_ZOOM = 14f

@Composable
fun rememberBitmapDescriptorFromRes(@DrawableRes id: Int): BitmapDescriptor {
    val context = LocalContext.current
    return remember(id) {
        BitmapDescriptorFactory.fromResource(id)
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTab(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onRestaurantClick: (String) -> Unit = {}
) {
    var hasLocation by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM)
    }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val restaurants by viewModel.restaurants.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LocationPermissionGate(
        requestOnStart = true,
        onResult = { ok -> hasLocation = ok }
    ) {

    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .debounce(250)
            .distinctUntilChanged()
            .filter { moving -> !moving }
            .collectLatest {
                viewModel.loadRestaurants()
            }
    }

    LaunchedEffect(hasLocation) {
        if (hasLocation) {
            try {
                @SuppressLint("MissingPermission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        viewModel.updateUserLocation(userLatLng)
                        launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(userLatLng, USER_LOCATION_ZOOM, 0f, 0f)
                                ),
                                1000
                            )
                        }
                    } else {
                        viewModel.updateUserLocation(DEFAULT_LOCATION)
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM))
                    }
                }.addOnFailureListener { e ->
                    viewModel.updateUserLocation(DEFAULT_LOCATION)
                    Log.e("MapTab", "Error getting location", e)
                }
            } catch (e: SecurityException) {
                viewModel.updateUserLocation(DEFAULT_LOCATION)
                Log.e("MapTab", "Error getting location", e)
            }
        } else {
            viewModel.updateUserLocation(DEFAULT_LOCATION)
            if (cameraPositionState.position.target != DEFAULT_LOCATION) {
                cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(DEFAULT_LOCATION, DEFAULT_ZOOM, 0f, 0f)
                    )
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocation),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocation),
            onMapLongClick = { }
        ) {
            restaurants.forEach { restaurant ->
                Marker(
                    state = MarkerState(position = restaurant.position),
                    title = restaurant.name,
                    icon = selectIcon(context, restaurant.icon),
                    snippet = restaurant.snippet,
                    onInfoWindowClick = {
                        selectedRestaurant = restaurant
                    },
                    onClick = {
                        selectedRestaurant = restaurant
                        true
                    }
                )
            }
        }

        if (selectedRestaurant != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedRestaurant = null },
                sheetState = bottomSheetState
            ) {
                RestaurantMarkerBottomSheet(
                    restaurant = selectedRestaurant!!,
                    onViewDetails = {
                        onRestaurantClick(selectedRestaurant!!.id)
                        selectedRestaurant = null
                    },
                    onDismiss = { selectedRestaurant = null }
                )
            }
        }
    }
}

@Composable
fun RestaurantMarkerBottomSheet(
    restaurant: Restaurant,
    onViewDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AsyncImage(
            imageUrl = restaurant.imageUrl,
            contentDescription = restaurant.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFA500),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", restaurant.rating),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            restaurant.distanceKm?.let { distance ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Distancia",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f km", distance),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (restaurant.dietaryRestrictions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                restaurant.dietaryRestrictions.forEach { restriction ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = restriction.emoji,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = restriction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (restaurant.snippet?.isNotEmpty() == true) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = restaurant.snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewDetails,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Ver más detalles")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

fun selectIcon(context: Context, icon: Long?): BitmapDescriptor {
    return BitmapDescriptorFromEmoji(context, "🍴", sizeDp = 40f, withBackground = true)
}
