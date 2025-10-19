package ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction

@Composable
fun RestaurantReviewCard(
    reviewWithUser: ReviewWithUser,
    modifier: Modifier = Modifier
) {
    val review = reviewWithUser.review
    val user = reviewWithUser.user

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (user?.avatarUrl != null) {
                        AsyncImage(
                            imageUrl = user.avatarUrl,
                            contentDescription = user.fullName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initial = user?.fullName?.firstOrNull()?.uppercase() ?: "?"
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user?.fullName ?: "Usuario desconocido",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = user?.username ?: "",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = formatTimeAgo(review.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stars = (1..5).joinToString("") { index ->
                    if (index <= review.rating.toInt()) "⭐" else "☆"
                }

                Text(
                    text = stars,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format("%.1f", review.rating),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
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
    val months = days / 30

    return when {
        months > 0 -> "Hace ${months}m"
        weeks > 0 -> "Hace ${weeks}sem"
        days > 0 -> "Hace ${days}d"
        hours > 0 -> "Hace ${hours}h"
        minutes > 0 -> "Hace ${minutes}min"
        else -> "Ahora"
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantReviewCardPreview() {
    val sampleUser = User(
        id = "1",
        fullName = "Juan Pérez",
        username = "@juanperez",
        email = "juan@example.com"
    )

    val sampleReview = Review(
        id = "1",
        restaurantId = "1",
        userId = "1",
        rating = 4.5f,
        comment = "Excelente lugar, muy recomendable! La comida es deliciosa y el servicio impecable.",
        dietaryRestriction = DietaryRestriction.VEGAN.key,
        createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
    )

    RestaurantReviewCard(
        reviewWithUser = ReviewWithUser(
            review = sampleReview,
            user = sampleUser
        )
    )
}
