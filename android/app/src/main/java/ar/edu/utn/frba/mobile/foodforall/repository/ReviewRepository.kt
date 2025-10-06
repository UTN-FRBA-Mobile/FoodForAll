package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para operaciones CRUD de reviews
 */
class ReviewRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = db.collection("reviews")

    /**
     * Obtiene una review por su ID
     */
    suspend fun getById(reviewId: String): Review? {
        return try {
            val doc = collection.document(reviewId).get().await()
            Review.fromFirestore(doc)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene todas las reviews de un restaurante
     */
    suspend fun getByRestaurantId(restaurantId: String): List<Review> {
        return try {
            val snapshot = collection
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { Review.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene todas las reviews de un usuario
     */
    suspend fun getByUserId(userId: String): List<Review> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { Review.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calcula el rating promedio de un restaurante
     */
    suspend fun getAverageRating(restaurantId: String): Float {
        val reviews = getByRestaurantId(restaurantId)
        if (reviews.isEmpty()) return 0f
        return reviews.map { it.rating }.average().toFloat()
    }

    /**
     * Obtiene el conteo de reviews de un restaurante
     */
    suspend fun getReviewCount(restaurantId: String): Int {
        return try {
            val snapshot = collection
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Crea o actualiza una review
     */
    suspend fun save(review: Review): String {
        return try {
            if (review.id.isEmpty()) {
                // Crear nueva
                val doc = collection.document()
                val reviewWithId = review.copy(id = doc.id)
                doc.set(reviewWithId.toFirestoreMap()).await()
                doc.id
            } else {
                // Actualizar existente
                collection.document(review.id)
                    .set(review.toFirestoreMap())
                    .await()
                review.id
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Elimina una review
     */
    suspend fun delete(reviewId: String): Boolean {
        return try {
            collection.document(reviewId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene las últimas N reviews de un restaurante
     */
    suspend fun getLatestByRestaurant(restaurantId: String, limit: Int = 10): List<Review> {
        return try {
            val snapshot = collection
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            snapshot.documents.mapNotNull { Review.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Verifica si un usuario ya dejó una review en un restaurante
     */
    suspend fun hasUserReviewed(userId: String, restaurantId: String): Boolean {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("restaurantId", restaurantId)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
