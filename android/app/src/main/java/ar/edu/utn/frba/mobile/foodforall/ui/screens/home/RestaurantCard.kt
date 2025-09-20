package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlin.math.roundToInt

/**
 * Componente que muestra una tarjeta de restaurante con el diseño de la imagen.
 */
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
        // Cuadrado de reseña (detrás de la tarjeta) - ENFOQUE AGRESIVO
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(60.dp) // Ancho fijo pero pequeño
                .height(if (cardHeight > 0) with(density) { cardHeight.toDp() } else 120.dp) // Altura exacta de la card
                .background(
                    color = Color(0xFFE91E63), // Rosa
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onReviewClick(restaurant) }
        ) {
            Text(
                text = "Reseña",
                modifier = Modifier
                    .align(Alignment.Center),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Tarjeta principal (se puede arrastrar) - MEDICIÓN AGRESIVA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .onSizeChanged { size ->
                    // MEDIR LA ALTURA REAL DE LA CARD
                    cardHeight = size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Si se arrastra más de 30% del ancho, mostrar cuadrado completo
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
            // Contenido principal (izquierda)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del restaurante
                Text(
                    text = restaurant.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Descripción
                Text(
                    text = restaurant.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Iconos de características (simplificados)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (restaurant.hasVegetarianOption) {
                        Text(
                            text = "🌽",
                            fontSize = 16.sp
                        )
                    }
                    if (restaurant.hasRestriction) {
                        Text(
                            text = "🥛",
                            fontSize = 16.sp
                        )
                    }
                    if (restaurant.hasOffer) {
                        Text(
                            text = "🌱",
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Métricas de engagement (simplificadas)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Likes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "❤️",
                            fontSize = 14.sp
                        )
                        Text(
                            text = restaurant.likes.toString(),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Comentarios
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💬",
                            fontSize = 14.sp
                        )
                        Text(
                            text = restaurant.comments.toString(),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Guardados
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔖",
                            fontSize = 14.sp
                        )
                        Text(
                            text = restaurant.saves.toString(),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Imagen del restaurante (derecha)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Text(
                    text = "🍽️",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp
                )
            }
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
        imageResource = "panera_rosa",
        likes = 120,
        comments = 5,
        saves = 5,
        hasVegetarianOption = true,
        hasRestriction = true,
        hasOffer = true
    )
    
    RestaurantCard(restaurant = sampleRestaurant)
}
