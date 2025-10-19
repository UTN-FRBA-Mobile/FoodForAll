package ar.edu.utn.frba.mobile.foodforall.domain.model

import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import com.google.firebase.firestore.DocumentSnapshot

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val snippet: String? = null,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val geohash: String = "",

    val hasCeliacOption: Boolean = false,
    val hasSiboOption: Boolean = false,
    val hasVeganOption: Boolean = false,
    val hasVegetarianOption: Boolean = false,

    val imageUrl: String? = null,
    val icon: Long = 0,
    val likes: Long = 0,
    val comments: Long = 0,
    val saves: Long = 0,
    val hasOffer: Boolean = false,
    val rating: Float = 0f,
    val distanceKm: Float? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val position get() = com.google.android.gms.maps.model.LatLng(lat, lng)
    val dietaryRestrictions: Set<DietaryRestriction>
        get() = buildSet {
            if (hasVegetarianOption) add(DietaryRestriction.VEGETARIAN)
            if (hasVeganOption) add(DietaryRestriction.VEGAN)
            if (hasCeliacOption) add(DietaryRestriction.CELIAC)
            if (hasSiboOption) add(DietaryRestriction.SIBO)
        }

    fun withDistance(userLocation: com.google.android.gms.maps.model.LatLng?): Restaurant {
        if (userLocation == null) return this
        return copy(distanceKm = calculateDistance(userLocation.latitude, userLocation.longitude, lat, lng))
    }

    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "description" to description,
        "snippet" to snippet,
        "lat" to lat,
        "lng" to lng,
        "geohash" to geohash,
        "hasCeliacOption" to hasCeliacOption,
        "hasSiboOption" to hasSiboOption,
        "hasVeganOption" to hasVeganOption,
        "hasVegetarianOption" to hasVegetarianOption,
        "imageUrl" to imageUrl,
        "icon" to icon,
        "likes" to likes,
        "comments" to comments,
        "saves" to saves,
        "hasOffer" to hasOffer,
        "rating" to rating,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Restaurant? {
            return try {
                Restaurant(
                    id = doc.id,
                    name = doc.getString("name") ?: doc.getString("title") ?: "Sin nombre",
                    description = doc.getString("description") ?: "",
                    snippet = doc.getString("snippet"),
                    lat = doc.getDouble("lat") ?: 0.0,
                    lng = doc.getDouble("lng") ?: 0.0,
                    geohash = doc.getString("geohash") ?: "",
                    hasCeliacOption = doc.getBoolean("hasCeliacOption") ?: false,
                    hasSiboOption = doc.getBoolean("hasSiboOption") ?: false,
                    hasVeganOption = doc.getBoolean("hasVeganOption") ?: false,
                    hasVegetarianOption = doc.getBoolean("hasVegetarianOption") ?: false,
                    imageUrl = doc.getString("imageUrl"),
                    icon = doc.getLong("icon") ?: 0,
                    likes = doc.getLong("likes") ?: 0,
                    comments = doc.getLong("comments") ?: 0,
                    saves = doc.getLong("saves") ?: 0,
                    hasOffer = doc.getBoolean("hasOffer") ?: false,
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }

        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val earthRadius = 6371.0

            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)

            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

            return (earthRadius * c).toFloat()
        }
    }
}
