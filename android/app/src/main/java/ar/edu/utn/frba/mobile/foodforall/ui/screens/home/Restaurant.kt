package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

/**
 * Data class que representa un restaurante en la aplicación.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val imageResource: String, // Nombre del recurso de imagen
    val likes: Int,
    val comments: Int,
    val saves: Int,
    val hasVegetarianOption: Boolean = false,
    val hasRestriction: Boolean = false,
    val hasOffer: Boolean = false
)
