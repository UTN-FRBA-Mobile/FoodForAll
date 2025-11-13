package ar.edu.utn.frba.mobile.foodforall.ui.screens.review

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.repository.ReviewRepository
import ar.edu.utn.frba.mobile.foodforall.ui.components.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Vista para creación de reseñas.
 *
 * @param restaurantId ID del restaurante a reseñar
 * @param currentUserId ID del usuario que crea la reseña
 * @param onSubmit se dispara con el Review creado (aquí podés persistir en Firestore)
 * @param onCancel se dispara al cancelar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReviewScreen(
    restaurantId: String,
    currentUserId: String,
    onDone: (Review) -> Unit,
    onCancel: () -> Unit,
    restaurantName: String? = null,
    repo: ReviewRepository = ReviewRepository()
) {
    val repository = remember { RestaurantRepository(FirebaseFirestore.getInstance()) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    var rating by remember { mutableStateOf(4.0f) }
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    var selectedRestriction by remember { mutableStateOf(DietaryRestriction.GENERAL) }
    var imageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var restaurant by remember { mutableStateOf<Restaurant?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(restaurantId) {
        try {
            loadError = null
            restaurant = repository.getById(restaurantId)
        } catch (e: Exception) {
            loadError = "No se pudo cargar el restaurante. Revisá tu conexión."
        } finally {
            isLoading = false
        }
    }
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentRestaurant = restaurant ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Restaurante no encontrado")
        }
        return
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(restaurant = currentRestaurant, onBack = onCancel)
            Spacer(modifier = Modifier.height(16.dp))
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                errorMsg = validate(rating, comment.text)
                                if (errorMsg == null) {
                                    isSubmitting = true
                                    val now = System.currentTimeMillis()
                                    val review = Review(
                                        id = "",
                                        userId = currentUserId,
                                        restaurantId = restaurantId,
                                        rating = rating,
                                        comment = comment.text.trim(),
                                        dietaryRestriction = selectedRestriction.key,
                                        imageUrls = imageUrls,
                                        createdAt = now,
                                        updatedAt = now
                                    )

                                    val newId = repo.save(review)

                                    if (newId.isNotEmpty()) {
                                        val saved = review.copy(id = newId)
                                        val title = restaurantName ?: "Reseña publicada"
                                        val shortComment = if (saved.comment.length > 60)
                                            saved.comment.take(57) + "…"
                                        else saved.comment
                                        Toast
                                            .makeText(
                                                ctx,
                                                "$title: ${"%.1f".format(saved.rating)}★ • $shortComment",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                        onDone(saved)
                                    } else {
                                        errorMsg = "No se pudo guardar la reseña. Verificá tu conexión e intentá de nuevo."
                                    }
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSubmitting) "Guardando..." else "Publicar reseña")
                    }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Calificación", fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StarRating(
                        value = rating,
                        onChange = { rating = it }
                    )
                    Text(
                        text = String.format("%.1f / 5.0", rating),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de reseña (restricción)", fontWeight = FontWeight.SemiBold)
                FlowRestrictions(
                    selected = selectedRestriction,
                    onSelect = { selectedRestriction = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Comentario", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.text.length <= 800) comment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    placeholder = { Text("Contanos tu experiencia…") },
                    supportingText = {
                        Text("${comment.text.length}/800", fontSize = 11.sp)
                    }
                )
            }

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun validate(rating: Float, comment: String): String? {
    if (rating <= 0f) return "La calificación debe ser mayor a 0."
    if (comment.trim().length < 5) return "El comentario es muy corto."
    return null
}

@Composable
fun StarRating(
    value: Float,
    onChange: (Float) -> Unit,
    max: Int = 5
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..max) {
            val filled = i <= value.toInt()
            val symbol = if (filled) "⭐" else "☆"
            Text(
                text = symbol,
                fontSize = 22.sp,
                modifier = Modifier.clickable { onChange(i.toFloat()) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRestrictions(
    selected: DietaryRestriction,
    onSelect: (DietaryRestriction) -> Unit,
    modifier: Modifier = Modifier
) {
    val all = remember { DietaryRestriction.values().toList() }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        all.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = {
                    val hasEmoji = option.emoji.isNotEmpty()
                    Text(
                        text = (if (hasEmoji) "${option.emoji} " else "") + option.description
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
fun CustomTopAppBar(
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
