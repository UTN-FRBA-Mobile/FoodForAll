package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant

@Composable
fun SavedRestaurantsTab(
    savedRestaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (savedRestaurants.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🔖",
                    fontSize = 48.sp
                )
                Text(
                    text = "No hay restaurantes guardados",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "Guarda tus favoritos para encontrarlos fácil!",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(savedRestaurants) { restaurant ->
                SavedRestaurantCard(
                    restaurant = restaurant,
                    onRestaurantClick = onRestaurantClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedRestaurantsTabPreview() {
    val sampleRestaurants = listOf(
        Restaurant(
            id = "1",
            name = "Panera Rosa",
            description = "2X1 en cafes HOY",
            lat = -34.603722,
            lng = -58.381592,
            hasVegetarianOption = true,
            hasVeganOption = true,
            rating = 4.5f
        ),
        Restaurant(
            id = "2",
            name = "Tomate",
            description = "Especialidades vegetarianas",
            lat = -34.603722,
            lng = -58.381592,
            hasVegetarianOption = true,
            rating = 4.8f
        )
    )

    SavedRestaurantsTab(savedRestaurants = sampleRestaurants)
}