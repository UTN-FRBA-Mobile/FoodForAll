package ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

sealed class ProfileSection(val title: String, val icon: ImageVector) {
    data object Menu : ProfileSection("Carta", Icons.Default.List)
    data object Gallery : ProfileSection("Galería", Icons.Default.Favorite)
    data object Reviews : ProfileSection("Reseñas", Icons.Default.ThumbUp)
}

private val profileSections = listOf(
    ProfileSection.Menu,
    ProfileSection.Gallery,
    ProfileSection.Reviews
)


@Composable
fun RestaurantProfileScreen(
    restaurantId: String,
    onBack: () -> Unit,
    viewModel: RestaurantProfileViewModel = viewModel()
) {
    val repository = remember { RestaurantRepository(FirebaseFirestore.getInstance()) }
    val scope = rememberCoroutineScope()
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSectionIndex by remember { mutableIntStateOf(0) }

    val reviews by viewModel.reviews.collectAsState()
    val reviewsLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(restaurantId) {
        scope.launch {
            try {
                restaurant = repository.getById(restaurantId)
            } finally {
                isLoading = false
            }
        }
        viewModel.loadReviews(restaurantId)
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (restaurant == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Restaurant not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(restaurant = restaurant!!, onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Av. Libertador 5000\n12:00 - 00:00 hs",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (profileSections[selectedSectionIndex]) {
                is ProfileSection.Menu -> MenuTabContent()
                is ProfileSection.Gallery -> GalleryTabContent()
                is ProfileSection.Reviews -> ReviewsTabContent(
                    reviews = reviews,
                    isLoading = reviewsLoading
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    restaurant: Restaurant,
    onBack: () -> Unit
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
                contentDescription = "Back",
                tint = Color.White
            )
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

@Composable
fun ProfileOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
        )
    }
}

@Composable
fun MenuTabContent() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Entradas",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = "Potato Skins: $910")
        Text(text = "Chicago Style Spinach: $970")
        Text(text = "Kansas Chicken Tenders: $970")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Principal",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = "Grilled Chicken Salad: $1350")
        Text(text = "Club Sandwich: $1250")
        Text(text = "Caesar Salad: $1040")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bebidas",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = "Coca Cola: $200")
        Text(text = "Agua sin gas")
        Text(text = "Agua con gas")
    }
}

@Composable
fun GalleryTabContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Galería de fotos del restaurante")
    }
}

@Composable
fun ReviewsTabContent(
    reviews: List<ReviewWithUser>,
    isLoading: Boolean
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        reviews.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay reseñas aún",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Sé el primero en dejar una reseña",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(reviews.size) { index ->
                    RestaurantReviewCard(reviewWithUser = reviews[index])
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantProfileScreenPreview() {
    RestaurantProfileScreen(restaurantId = "6", onBack = {})
}