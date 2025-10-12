package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
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
    private val col get() = db.collection("restaurants")

    // Distancia Haversine en metros
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

    suspend fun fetchInBounds(bounds: LatLngBounds): List<Restaurant> {
        val center = bounds.center
        val radiusMeters = approxRadiusFromBounds(bounds)

        val centerGeo = GeoLocation(center.latitude, center.longitude)
        val queries = GeoFireUtils.getGeoHashQueryBounds(centerGeo, radiusMeters)

        val tasks = queries.map { q ->
            col.orderBy("geohash")
                .startAt(q.startHash)
                .endAt(q.endHash)
                .get()
        }

        val docs = tasks.map { it.await() }.flatMap { it.documents }

        // Filtro final por distancia real (porque geohash crea una "envolvente")
        return docs.mapNotNull { d ->
            val lat = d.getDouble("lat") ?: return@mapNotNull null
            val lng = d.getDouble("lng") ?: return@mapNotNull null

            val distance = GeoFireUtils.getDistanceBetween(
                GeoLocation(center.latitude, center.longitude),
                GeoLocation(lat, lng)
            )
            if (distance <= radiusMeters) {
                Restaurant(
                    id = d.id,
                    title = d.getString("title") ?: "POI",
                    snippet = d.getString("snippet"),
                    description = d.getString("description"),
                    icon = d.getLong("icon"),
                    likes = d.getLong("icon"),
                    comments = d.getLong("icon"),
                    saves = d.getLong("icon"),
                    imageResource = d.getString("imageResource"),
                    hasSiboOption = d.getBoolean("hasSiboOption") == true,
                    hasVeganOption = d.getBoolean("hasVeganOption") == true,
                    hasCeliacOption = d.getBoolean("hasCeliacOption") == true,
                    lat = lat,
                    lng = lng
                )
            } else null
        }
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

    suspend fun addPoiAt(
        latLng: LatLng,
        title: String = "Nuevo punto",
        snippet: String? = null,
        icon: Int = 0,
    ): String {
        val geohash = GeoFireUtils.getGeoHashForLocation(
            GeoLocation(latLng.latitude, latLng.longitude)
        )
        val doc = col.document()
        val data = mapOf(
            "title" to title,
            "snippet" to snippet,
            "icon" to icon,
            "lat" to latLng.latitude,
            "lng" to latLng.longitude,
            "geohash" to geohash
        )
        doc.set(data).await()
        return doc.id
    }
}