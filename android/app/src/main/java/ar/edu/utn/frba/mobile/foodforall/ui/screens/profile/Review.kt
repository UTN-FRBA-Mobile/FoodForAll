package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.Restaurant


data class Review(
    val id: String,
    val restaurant: Restaurant,
    val rating: Float,
    val comment: String,
    val date: String,
    val userId: String,
    val restriction: DietaryRestriction = DietaryRestriction.GENERAL
)