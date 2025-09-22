package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.R
import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.SampleRestaurants

@Composable
fun ReviewCard(
    review: Review,
    modifier: Modifier = Modifier
) {
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
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val imageResource = when (review.restaurant.name.lowercase()) {
                    "panera rosa" -> R.drawable.panera_rosa
                    "tomate" -> R.drawable.tomate
                    "mi barrio" -> R.drawable.mi_barrio
                    else -> null
                }

                if (imageResource != null) {
                    Image(
                        painter = painterResource(id = imageResource),
                        contentDescription = review.restaurant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val backgroundColor = when (review.restaurant.name.lowercase()) {
                        "mc donalds" -> Color(0xFFFFC107)
                        "roldán" -> Color(0xFF9C27B0)
                        "kansas" -> Color(0xFFFF9800)
                        "la parrilla" -> Color(0xFFF44336)
                        "sushi zen" -> Color(0xFF00BCD4)
                        "pizza corner" -> Color(0xFF795548)
                        else -> Color(0xFFE0E0E0)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                    ) {
                        Text(
                            text = review.restaurant.name.take(2).uppercase(),
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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
                        text = review.restaurant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = review.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stars = (1..5).joinToString("") { index ->
                        if (index <= review.rating) "⭐" else "☆"
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

                    if (review.restriction != DietaryRestriction.GENERAL) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = review.restriction.emoji,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewCardPreview() {
    val sampleReview = Review(
        id = "1",
        restaurant = SampleRestaurants.restaurants[0],
        rating = 4.5f,
        comment = "Excelente café y ambiente acogedor. El 2x1 está genial!",
        date = "Hace 2 días",
        userId = "1",
        restriction = DietaryRestriction.VEGAN
    )

    ReviewCard(review = sampleReview)
}