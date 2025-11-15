package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import ar.edu.utn.frba.mobile.foodforall.ui.components.BitmapDescriptorFromEmoji
import ar.edu.utn.frba.mobile.foodforall.ui.components.LocationPermissionGate
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.GalleryTabContent
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.MenuTabContent
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.ProfileOption
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.ProfileSection
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.RestaurantProfileViewModel
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.RestaurantReviewCard
import ar.edu.utn.frba.mobile.foodforall.ui.viewmodel.AuthViewModel
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

fun shareRestaurant(context: Context, restaurant: Restaurant) {
    val dietaryText = if (restaurant.dietaryRestrictions.isNotEmpty()) {
        restaurant.dietaryRestrictions.joinToString(" ") { it.emoji }
    } else {
        "🍽️"
    }

    val shareText = buildString {
        append("🍽️ ${restaurant.name}\n")
        restaurant.snippet?.let {
            append("$it\n")
        }
        append("$dietaryText\n")
        append("\nDescubrí más restaurantes en FoodForAll ✨")
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    val chooser = Intent.createChooser(shareIntent, "Compartir restaurante")

    try {
        if (context is Activity) {
            context.startActivity(chooser)
        } else {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        Log.e("RestaurantShare", "Error al compartir restaurante", e)
    }
}

@Composable
fun FullRestaurantProfileInSheet(
    restaurant: Restaurant,
    authViewModel: AuthViewModel,
    onBackToCompact: () -> Unit,
    onCreateReview: (String) -> Unit,
    viewModel: RestaurantProfileViewModel = viewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val reviewsLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val error by viewModel.error.collectAsState()
    val authUser by authViewModel.currentUser.collectAsState()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    val profileSections = listOf(
        ProfileSection.Menu,
        ProfileSection.Gallery,
        ProfileSection.Reviews
    )

    // LaunchedEffects para cargar datos y verificar estado de guardado
    LaunchedEffect(restaurant.id) {
        viewModel.loadReviews(restaurant.id)
    }

    LaunchedEffect(restaurant.id, authUser?.id) {
        authUser?.id?.let { userId ->
            viewModel.checkIfSaved(userId, restaurant.id)
        }
    }

    // Estado para cambiar entre pestañas del perfil
    var selectedSectionIndex by rememberSaveable { mutableStateOf(2) } // Reviews es el índice 2
    val context = LocalContext.current
    // Contenedor principal para el contenido desplazable
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = WindowInsets.ime.asPaddingValues()
        ) {
            item {
                // Header con Imagen, Nombre y botón para volver a la vista compacta
                RestaurantProfileSheetHeader(
                    restaurant = restaurant,
                    onBack = onBackToCompact, // Llama a la función para volver a vista compacta
                    onSave = {
                        authUser?.id?.let { userId ->
                            viewModel.toggleSave(userId, restaurant.id)
                        }
                    },
                    isSaved = isSaved,
                    showSaveButton = authUser != null,
                    onShare = { shareRestaurant(context, restaurant) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Av. Libertador 5000\n12:00 - 00:00 hs",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp, // Agregado .sp
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Opciones del Perfil (Tabs)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    profileSections.forEachIndexed { index, section ->
                        ProfileOption(
                            icon = section.icon,
                            label = section.title,
                            isSelected = selectedSectionIndex == index,
                            onClick = { selectedSectionIndex = index }
                        )
                    }
                }
            }

            // Contenido de la Sección Seleccionada
            when (profileSections[selectedSectionIndex]) {
                is ProfileSection.Menu -> item {
                    Box(Modifier.padding(16.dp)) { MenuTabContent() }
                }
                is ProfileSection.Gallery -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { GalleryTabContent() }
                }
                is ProfileSection.Reviews -> {
                    if (reviewsLoading) {
                        item {
                            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (reviews.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("⭐", fontSize = 48.sp) // Agregado .sp
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No hay reseñas aún", fontSize = 16.sp) // Agregado .sp
                                }
                            }
                        }
                    } else {
                        items(reviews.size) { index ->
                            RestaurantReviewCard(
                                reviewWithUser = reviews[index]
                            )
                        }
                    }
                }
            }

            // Espacio para el FAB (se debe mover el FAB fuera del LazyColumn)
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        ExtendedFloatingActionButton(
            onClick = { onCreateReview(restaurant.id) },
            icon = { Icon(Icons.Default.ThumbUp, contentDescription = "Crear reseña") },
            text = { Text("Crear reseña") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alineado al fondo del Box
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

// Composable del Header del Sheet de Perfil Completo (sin cambios, solo se asegura la accesibilidad)
@Composable
fun RestaurantProfileSheetHeader(
    restaurant: Restaurant,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    showSaveButton: Boolean,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        AsyncImage(
            imageUrl = restaurant.imageUrl,
            contentDescription = restaurant.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onShare,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartir restaurante",
                tint = Color.White
            )
        }

        if (showSaveButton) {
            IconButton(
                onClick = onSave,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Quitar de guardados" else "Guardar restaurante",
                    tint = if (isSaved) Color.White else Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = restaurant.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "@${restaurant.name.replace(" ", "").lowercase()}resto",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTab(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onRestaurantClick: (String) -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    restaurantProfileViewModel: RestaurantProfileViewModel = viewModel()
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
    // El skipPartiallyExpanded = false es crucial para permitir el estado compacto
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Estado para controlar si estamos en la vista Compacta (false) o Perfil Completo (true)
    var isFullProfile by rememberSaveable { mutableStateOf(false) }

    LocationPermissionGate(
        requestOnStart = true,
        onResult = { ok -> hasLocation = ok }
    ) {
        // Lógica de permisos
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
                }
            } catch (e: SecurityException) {
                viewModel.updateUserLocation(DEFAULT_LOCATION)
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

    // Efecto para expandir/colapsar el sheet cuando cambia el modo de vista
    LaunchedEffect(isFullProfile) {
        if (selectedRestaurant != null) {
            if (isFullProfile) {
                // Si es Perfil Completo, se expande a pantalla completa
                bottomSheetState.expand()
            } else {
                // Si es Compacto, se queda en parcialmente expandido (default)
                bottomSheetState.partialExpand()
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
                        // Asegura que siempre comience en vista compacta al abrir
                        isFullProfile = false
                    },
                    onClick = {
                        selectedRestaurant = restaurant
                        isFullProfile = false
                        true
                    }
                )
            }
        }

        if (selectedRestaurant != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    selectedRestaurant = null
                    isFullProfile = false // Reinicia el estado al cerrar
                },
                sheetState = bottomSheetState,
                // Asegura que no haya padding extra si el teclado está visible
                //windowInsets = WindowInsets.ime
            ) {
                selectedRestaurant?.let { restaurant ->
                    if (isFullProfile) {
                        FullRestaurantProfileInSheet(
                            restaurant = restaurant,
                            authViewModel = authViewModel,
                            onBackToCompact = {
                                scope.launch {
                                    isFullProfile = false
                                }
                            },
                            onCreateReview = { restaurantId ->
                                selectedRestaurant = null
                                isFullProfile = false
                                onRestaurantClick(restaurantId)
                            },
                            viewModel = restaurantProfileViewModel
                        )
                    } else {
                        RestaurantMarkerBottomSheet(
                            restaurant = restaurant,
                            onViewDetails = {
                                isFullProfile = true
                            },
                            onDismiss = {
                                selectedRestaurant = null
                                isFullProfile = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// Mantenemos RestaurantMarkerBottomSheet como estaba (ya usa onViewDetails para cambiar el estado)
@Composable
fun RestaurantMarkerBottomSheet(
    restaurant: Restaurant,
    onViewDetails: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth() // Cambiado a fillMaxWidth para consistencia
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
            onClick = onViewDetails, // <--- Esto es lo que activa el cambio de estado a FullProfile
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