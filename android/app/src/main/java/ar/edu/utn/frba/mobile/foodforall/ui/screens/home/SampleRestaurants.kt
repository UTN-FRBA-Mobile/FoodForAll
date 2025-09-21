package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction

/**
 * Datos hardcodeados de restaurantes de ejemplo basados en la imagen.
 */
object SampleRestaurants {
    val restaurants = listOf(
        Restaurant(
            id = "1",
            name = "Panera Rosa",
            description = "2X1 en cafes HOY",
            imageResource = "panera_rosa",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.CELIAC),
            hasOffer = true,
            rating = 4.5f,
            distanceKm = 0.8f
        ),
        Restaurant(
            id = "2",
            name = "Tomate",
            description = "Veni a la nueva sucursal de Tomate",
            imageResource = "tomate",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.VEGAN),
            hasOffer = false,
            rating = 4.2f,
            distanceKm = 1.2f
        ),
        Restaurant(
            id = "3",
            name = "Mc Donalds",
            description = "Nueva hamburguesa",
            imageResource = "mcdonalds",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN),
            hasOffer = true,
            rating = 3.8f,
            distanceKm = 2.1f
        ),
        Restaurant(
            id = "4",
            name = "Mi Barrio",
            description = "Mañana 21/09 cerrado",
            imageResource = "mi_barrio",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.SIBO),
            hasOffer = true,
            rating = 4.7f,
            distanceKm = 0.5f
        ),
        Restaurant(
            id = "5",
            name = "Roldán",
            description = "Obtené un 5% off en tu cena de hoy mostrando este mensaje!",
            imageResource = "roldan",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.CELIAC, DietaryRestriction.VEGAN),
            hasOffer = true,
            rating = 4.3f,
            distanceKm = 1.8f
        ),
        Restaurant(
            id = "6",
            name = "Kansas",
            description = "Nuevo Plato!",
            imageResource = "kansas",
            likes = 120,
            comments = 5,
            saves = 5,
            dietaryRestrictions = emptySet(),
            hasOffer = false,
            rating = 4.1f,
            distanceKm = 3.2f
        ),
        Restaurant(
            id = "7",
            name = "Mi Barrio",
            description = "Platos tradicionales de la casa",
            imageResource = "mi_barrio_2",
            likes = 95,
            comments = 3,
            saves = 8,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN),
            hasOffer = true,
            rating = 4.4f,
            distanceKm = 0.7f
        ),
        Restaurant(
            id = "8",
            name = "La Parrilla",
            description = "Carnes a la parrilla todos los días",
            imageResource = "la_parrilla",
            likes = 150,
            comments = 12,
            saves = 20,
            dietaryRestrictions = emptySet(),
            hasOffer = true,
            rating = 4.6f,
            distanceKm = 1.5f
        ),
        Restaurant(
            id = "9",
            name = "Sushi Zen",
            description = "Sushi fresco y auténtico",
            imageResource = "sushi_zen",
            likes = 200,
            comments = 8,
            saves = 15,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.SIBO),
            hasOffer = false,
            rating = 4.8f,
            distanceKm = 2.3f
        ),
        Restaurant(
            id = "10",
            name = "Pizza Corner",
            description = "Pizza artesanal con ingredientes premium",
            imageResource = "pizza_corner",
            likes = 180,
            comments = 6,
            saves = 12,
            dietaryRestrictions = setOf(DietaryRestriction.VEGETARIAN, DietaryRestriction.VEGAN),
            hasOffer = true,
            rating = 4.0f,
            distanceKm = 1.9f
        )
    )
}
