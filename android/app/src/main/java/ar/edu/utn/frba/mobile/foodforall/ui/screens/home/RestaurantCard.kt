package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import kotlin.math.roundToInt


@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onRestaurantClick: (Restaurant) -> Unit = {},
    onReviewClick: (Restaurant) -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var cardHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(60.dp)
                .height(if (cardHeight > 0) with(density) { cardHeight.toDp() } else 120.dp)
                .background(
                    color = Color(0xFFE91E63), // Rosa
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onReviewClick(restaurant) }
        ) {
            Text(
                text = "Reseña",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .onSizeChanged { size ->
                    cardHeight = size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            if (offsetX < -150f) {
                                offsetX = -150f
                            } else {
                                offsetX = 0f
                            }
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount.x).coerceIn(-150f, 0f)
                    }
                }
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = restaurant.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = restaurant.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        restaurant.dietaryRestrictions.forEach { restriction ->
                            if (restriction.emoji.isNotEmpty()) {
                                Text(
                                    text = restriction.emoji,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⭐",
                            fontSize = 14.sp
                        )
                        Text(
                            text = String.format("%.1f", restaurant.rating),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${restaurant.comments} reseñas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                AsyncImage(
                    imageUrl = restaurant.imageUrl,
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RestaurantCardPreview() {
    val sampleRestaurant = Restaurant(
        id = "1",
        name = "Panera Rosa",
        description = "2X1 en cafes HOY",
        likes = 120,
        comments = 5,
        saves = 5,
        hasVegetarianOption = true,
        hasCeliacOption = true,
        hasOffer = true,
        rating = 4.5f,
        distanceKm = 0.8f
    )

    RestaurantCard(restaurant = sampleRestaurant)
}
