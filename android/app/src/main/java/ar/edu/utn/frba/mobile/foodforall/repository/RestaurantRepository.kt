package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.text.get

class RestaurantRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = db.collection("restaurants")

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat/2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2.0)
        return 2 * R * asin(sqrt(a))
    }

    private fun approxRadiusFromBounds(b: LatLngBounds): Double {
        val sw = b.southwest; val ne = b.northeast
        val diagMeters = haversine(sw.latitude, sw.longitude, ne.latitude, ne.longitude)
        return diagMeters / 2.0
    }

    private data class BBox(
        val minLat: Double, val maxLat: Double,
        val minLon: Double, val maxLon: Double
    )

    private fun boundingBox(lat: Double, lon: Double, radiusMeters: Double): BBox {
        val latDelta = radiusMeters / 111_000.0
        val lonDelta = radiusMeters / (111_000.0 * cos(Math.toRadians(lat)).coerceAtLeast(1e-6))
        return BBox(lat - latDelta, lat + latDelta, lon - lonDelta, lon + lonDelta)
    }

    suspend fun getById(restaurantId: String): Restaurant? {
        return try {
            val doc = collection.document(restaurantId).get().await()
            Restaurant.fromFirestore(doc)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAll(): List<Restaurant> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { Restaurant.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun fetchInBounds(bounds: LatLngBounds, userLocation: LatLng? = null): List<Restaurant> {
        val center = bounds.center
        val radiusMeters = approxRadiusFromBounds(bounds)

        val centerGeo = GeoLocation(center.latitude, center.longitude)
        val queries = GeoFireUtils.getGeoHashQueryBounds(centerGeo, radiusMeters)

        val tasks = queries.map { q ->
            collection.orderBy("geohash")
                .startAt(q.startHash)
                .endAt(q.endHash)
                .get()
        }

        val docs = tasks.map { it.await() }.flatMap { it.documents }

        val results = docs.mapNotNull { doc ->
            val restaurant = Restaurant.fromFirestore(doc) ?: return@mapNotNull null

            val distance = GeoFireUtils.getDistanceBetween(
                GeoLocation(center.latitude, center.longitude),
                GeoLocation(restaurant.lat, restaurant.lng)
            )

            if (distance <= radiusMeters) {
                val distanceKm = userLocation?.let {
                    val distToUser = haversine(it.latitude, it.longitude, restaurant.lat, restaurant.lng)
                    (distToUser / 1000).toFloat()
                }
                restaurant.copy(distanceKm = distanceKm)
            } else null
        }

        return results
    }

    suspend fun findWithin(lat: Double, lon: Double, radius: Double) =
        withContext(Dispatchers.IO) {
            val box = boundingBox(lat, lon, radius)

            val snap = db.collection("places")
                .whereGreaterThanOrEqualTo("lat", box.minLat)
                .whereLessThanOrEqualTo("lat", box.maxLat)
                .whereGreaterThanOrEqualTo("lon", box.minLon)
                .whereLessThanOrEqualTo("lon", box.maxLon)
                .get().await()

            snap.documents.filter { d ->
                val plat = d.getDouble("lat") ?: return@filter false
                val plon = d.getDouble("lon") ?: return@filter false
                haversine(lat, lon, plat, plon) <= radius
            }
        }

    suspend fun getWithFilters(dietaryRestrictions: Set<DietaryRestriction>): List<Restaurant> {
        if (dietaryRestrictions.isEmpty()) {
            return getAll()
        }

        return try {
            var query: Query = collection

            dietaryRestrictions.forEach { restriction ->
                query = when (restriction) {
                    DietaryRestriction.VEGETARIAN -> query.whereEqualTo("hasVegetarianOption", true)
                    DietaryRestriction.VEGAN -> query.whereEqualTo("hasVeganOption", true)
                    DietaryRestriction.CELIAC -> query.whereEqualTo("hasCeliacOption", true)
                    DietaryRestriction.SIBO -> query.whereEqualTo("hasSiboOption", true)
                    DietaryRestriction.GENERAL -> query
                }
            }

            val snapshot = query.get().await()
            snapshot.documents.mapNotNull { Restaurant.fromFirestore(it) }
        } catch (e: Exception) {
            val all = getAll()
            all.filter { restaurant ->
                dietaryRestrictions.all { restriction ->
                    when (restriction) {
                        DietaryRestriction.VEGETARIAN -> restaurant.hasVegetarianOption
                        DietaryRestriction.VEGAN -> restaurant.hasVeganOption
                        DietaryRestriction.CELIAC -> restaurant.hasCeliacOption
                        DietaryRestriction.SIBO -> restaurant.hasSiboOption
                        DietaryRestriction.GENERAL -> true
                    }
                }
            }
        }
    }

    suspend fun save(restaurant: Restaurant): String {
        return try {
            if (restaurant.id.isEmpty()) {
                val doc = collection.document()
                val restaurantWithId = restaurant.copy(id = doc.id)
                doc.set(restaurantWithId.toFirestoreMap()).await()
                doc.id
            } else {
                collection.document(restaurant.id)
                    .set(restaurant.toFirestoreMap())
                    .await()
                restaurant.id
            }
        } catch (e: Exception) {
            ""
        }
    }
    suspend fun addPoiAt(
        latLng: LatLng,
        name: String = "Nuevo punto",
        snippet: String? = null,
        icon: Long = 0,
    ): String {
        val geohash = GeoFireUtils.getGeoHashForLocation(
            GeoLocation(latLng.latitude, latLng.longitude)
        )

        val restaurant = Restaurant(
            name = name,
            snippet = snippet,
            icon = icon,
            lat = latLng.latitude,
            lng = latLng.longitude,
            geohash = geohash
        )

        return save(restaurant)
    }

    suspend fun delete(restaurantId: String): Boolean {
        return try {
            collection.document(restaurantId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateRating(restaurantId: String, rating: Float): Boolean {
        return try {
            collection.document(restaurantId)
                .update(
                    mapOf(
                        "rating" to rating,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementLikes(restaurantId: String): Boolean {
        return try {
            val doc = collection.document(restaurantId).get().await()
            val currentLikes = doc.getLong("likes") ?: 0
            collection.document(restaurantId)
                .update("likes", currentLikes + 1)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementComments(restaurantId: String): Boolean {
        return try {
            val doc = collection.document(restaurantId).get().await()
            val currentComments = doc.getLong("comments") ?: 0
            collection.document(restaurantId)
                .update("comments", currentComments + 1)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun incrementSaves(restaurantId: String): Boolean {
        return try {
            val doc = collection.document(restaurantId).get().await()
            val currentSaves = doc.getLong("saves") ?: 0
            collection.document(restaurantId)
                .update("saves", currentSaves + 1)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun decrementSaves(restaurantId: String): Boolean {
        return try {
            val doc = collection.document(restaurantId).get().await()
            val currentSaves = doc.getLong("saves") ?: 0
            if (currentSaves > 0) {
                collection.document(restaurantId)
                    .update("saves", currentSaves - 1)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
