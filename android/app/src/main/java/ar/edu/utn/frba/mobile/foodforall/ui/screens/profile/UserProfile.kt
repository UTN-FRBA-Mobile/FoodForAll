package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

data class UserProfile(
    val id: String,
    val fullName: String,
    val username: String,
    val avatarResource: String? = null
)