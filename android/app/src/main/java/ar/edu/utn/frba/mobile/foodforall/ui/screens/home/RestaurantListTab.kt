package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Muestra una lista scrolleable de restaurantes.
 */
@Composable
fun RestaurantListTab(
    modifier: Modifier = Modifier,
    onRestaurantClick: (String) -> Unit = {},
    onReviewClick: (Restaurant) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo gris claro como en la imagen
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            items(SampleRestaurants.restaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    onRestaurantClick = { onRestaurantClick(restaurant.id) },
                    onReviewClick = onReviewClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantListTabPreview() {
    RestaurantListTab()
}