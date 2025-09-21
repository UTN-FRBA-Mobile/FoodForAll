package ar.edu.utn.frba.mobile.foodforall.domain.model

data class Restaurant(
    val id: String,
    val title: String,
    val snippet: String? = null,
    val icon: Long? = 0,
    val lat: Double,
    val lng: Double,
    val hasCeliacOption: Boolean = false,
    val hasSiboOption: Boolean = false,
    val hasVeganOption: Boolean = false,
    val hasVegetarianOption: Boolean = false,
    val description: String? = null,
    val imageResource: String? = null,
    val likes: Long? = 0,
    val comments: Long? = 0,
    val saves: Long? = 0,
) {
    val position get() = com.google.android.gms.maps.model.LatLng(lat, lng)
}