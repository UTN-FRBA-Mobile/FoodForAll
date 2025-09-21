package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.Restaurant
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.SampleRestaurants

@Composable
fun SavedRestaurantCard(
    restaurant: Restaurant,
    onRestaurantClick: (Restaurant) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onRestaurantClick(restaurant) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val imageResource = when (restaurant.name.lowercase()) {
                    "panera rosa" -> R.drawable.panera_rosa
                    "tomate" -> R.drawable.tomate
                    "mi barrio" -> R.drawable.mi_barrio
                    else -> null
                }

                if (imageResource != null) {
                    Image(
                        painter = painterResource(id = imageResource),
                        contentDescription = restaurant.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val backgroundColor = when (restaurant.name.lowercase()) {
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
                            text = restaurant.name.take(2).uppercase(),
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
                Text(
                    text = restaurant.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        restaurant.dietaryRestrictions.forEach { restriction ->
                            if (restriction.emoji.isNotEmpty()) {
                                Text(
                                    text = restriction.emoji,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "🔖",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedRestaurantCardPreview() {
    SavedRestaurantCard(
        restaurant = SampleRestaurants.restaurants[0]
    )
}