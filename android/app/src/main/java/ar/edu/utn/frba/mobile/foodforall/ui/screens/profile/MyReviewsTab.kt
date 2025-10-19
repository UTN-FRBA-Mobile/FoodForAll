package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction

@Composable
fun MyReviewsTab(
    reviews: List<ReviewWithRestaurant>,
    modifier: Modifier = Modifier
) {
    if (reviews.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📝",
                    fontSize = 48.sp
                )
                Text(
                    text = "No hay reseñas aún",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "¡Escribe tu primera reseña!",
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
            items(reviews) { reviewWithRestaurant ->
                ReviewCard(reviewWithRestaurant = reviewWithRestaurant)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyReviewsTabPreview() {
    val sampleRestaurant = Restaurant(
        id = "1",
        name = "Panera Rosa",
        description = "2X1 en cafes HOY",
        lat = -34.603722,
        lng = -58.381592,
        rating = 4.5f
    )

    val sampleReview = Review(
        id = "1",
        restaurantId = "1",
        userId = "1",
        rating = 4.5f,
        comment = "Excelente café y ambiente acogedor. El 2x1 está genial!",
        dietaryRestriction = DietaryRestriction.VEGAN.key,
        createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
    )

    val sampleReviews = listOf(
        ReviewWithRestaurant(
            review = sampleReview,
            restaurant = sampleRestaurant
        )
    )

    MyReviewsTab(reviews = sampleReviews)
}