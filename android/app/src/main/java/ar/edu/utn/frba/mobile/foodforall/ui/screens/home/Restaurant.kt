package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction

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
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val hasOffer: Boolean = false,
    val rating: Float = 0f, // Calificación de 0 a 5
    val distanceKm: Float = 0f // Distancia en kilómetros
)
