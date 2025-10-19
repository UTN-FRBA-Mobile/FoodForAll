package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction

@Composable
fun ReviewCard(
    reviewWithRestaurant: ReviewWithRestaurant,
    modifier: Modifier = Modifier
) {
    val review = reviewWithRestaurant.review
    val restaurant = reviewWithRestaurant.restaurant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                imageUrl = restaurant?.imageUrl,
                contentDescription = restaurant?.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant?.name ?: "Restaurante desconocido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = formatTimeAgo(review.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stars = (1..5).joinToString("") { index ->
                        if (index <= review.rating.toInt()) "⭐" else "☆"
                    }

                    Text(
                        text = stars,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format("%.1f", review.rating),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = review.comment,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    val restriction = review.dietaryRestrictionEnum
                    if (restriction != DietaryRestriction.GENERAL && restriction.emoji.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = restriction.emoji,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        weeks > 0 -> "Hace ${weeks}sem"
        days > 0 -> "Hace ${days}d"
        hours > 0 -> "Hace ${hours}h"
        minutes > 0 -> "Hace ${minutes}min"
        else -> "Ahora"
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewCardPreview() {
    val sampleRestaurant = Restaurant(
        id = "1",
        name = "Panera Rosa",
        description = "2X1 en cafes HOY"
    )

    val sampleReview = Review(
        id = "1",
        restaurantId = "1",
        userId = "1",
        rating = 4.5f,
        comment = "Excelente café y ambiente acogedor. El 2x1 está genial!",
        dietaryRestriction = DietaryRestriction.VEGAN.key,
        createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000) // Hace 2 días
    )

    ReviewCard(
        reviewWithRestaurant = ReviewWithRestaurant(
            review = sampleReview,
            restaurant = sampleRestaurant
        )
    )
}
