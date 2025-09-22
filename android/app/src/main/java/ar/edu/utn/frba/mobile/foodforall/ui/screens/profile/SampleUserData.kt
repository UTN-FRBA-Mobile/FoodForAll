package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.SampleRestaurants

object SampleUserData {
    val currentUser = UserProfile(
        id = "1",
        fullName = "Juan Perez",
        username = "@juanperez"
    )

    val userReviews = listOf(
        Review(
            id = "1",
            restaurant = SampleRestaurants.restaurants[0],
            rating = 4.5f,
            comment = "Excelente café vegano y ambiente acogedor. El 2x1 está genial!",
            date = "Hace 2 días",
            userId = currentUser.id,
            restriction = DietaryRestriction.VEGAN
        ),
        Review(
            id = "2",
            restaurant = SampleRestaurants.restaurants[1],
            rating = 4.0f,
            comment = "Me gustó mucho la nueva sucursal, opciones vegetarianas excelentes.",
            date = "Hace 5 días",
            userId = currentUser.id,
            restriction = DietaryRestriction.VEGETARIAN
        ),
        Review(
            id = "3",
            restaurant = SampleRestaurants.restaurants[4],
            rating = 5.0f,
            comment = "¡Increíble experiencia! Opciones sin gluten perfectas.",
            date = "Hace 1 semana",
            userId = currentUser.id,
            restriction = DietaryRestriction.CELIAC
        ),
        Review(
            id = "4",
            restaurant = SampleRestaurants.restaurants[8],
            rating = 4.8f,
            comment = "Perfecto para dieta SIBO, comida simple y bien preparada.",
            date = "Hace 10 días",
            userId = currentUser.id,
            restriction = DietaryRestriction.SIBO
        ),
        Review(
            id = "5",
            restaurant = SampleRestaurants.restaurants[7],
            rating = 4.2f,
            comment = "Las carnes están perfectas, muy recomendado para cenar.",
            date = "Hace 2 semanas",
            userId = currentUser.id,
            restriction = DietaryRestriction.GENERAL
        )
    )

    val savedRestaurants = listOf(
        SampleRestaurants.restaurants[0],
        SampleRestaurants.restaurants[2],
        SampleRestaurants.restaurants[5],
        SampleRestaurants.restaurants[8],
        SampleRestaurants.restaurants[9]
    )
}