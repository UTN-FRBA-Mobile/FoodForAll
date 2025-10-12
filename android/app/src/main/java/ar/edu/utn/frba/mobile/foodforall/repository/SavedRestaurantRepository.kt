package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.SavedRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para operaciones de restaurantes guardados/favoritos
 */
class SavedRestaurantRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = db.collection("savedRestaurants")

    /**
     * Obtiene todos los restaurantes guardados de un usuario
     */
    suspend fun getByUserId(userId: String): List<SavedRestaurant> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { SavedRestaurant.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtiene los IDs de restaurantes guardados de un usuario
     */
    suspend fun getSavedRestaurantIds(userId: String): List<String> {
        val saved = getByUserId(userId)
        return saved.map { it.restaurantId }
    }

    /**
     * Verifica si un usuario tiene guardado un restaurante
     */
    suspend fun isSaved(userId: String, restaurantId: String): Boolean {
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

    /**
     * Guarda un restaurante para un usuario
     */
    suspend fun save(userId: String, restaurantId: String): String {
        return try {
            // Verificar si ya existe
            val existing = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("restaurantId", restaurantId)
                .limit(1)
                .get()
                .await()

            if (!existing.isEmpty) {
                // Ya existe, retornar el ID existente
                existing.documents.first().id
            } else {
                // Crear nuevo
                val doc = collection.document()
                val savedRestaurant = SavedRestaurant(
                    id = doc.id,
                    userId = userId,
                    restaurantId = restaurantId,
                    savedAt = System.currentTimeMillis()
                )
                doc.set(savedRestaurant.toFirestoreMap()).await()
                doc.id
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Elimina un restaurante guardado
     */
    suspend fun unsave(userId: String, restaurantId: String): Boolean {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .await()

            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Alterna el estado de guardado de un restaurante (toggle)
     */
    suspend fun toggle(userId: String, restaurantId: String): Boolean {
        return if (isSaved(userId, restaurantId)) {
            unsave(userId, restaurantId)
            false // Ahora NO está guardado
        } else {
            save(userId, restaurantId)
            true // Ahora SÍ está guardado
        }
    }

    /**
     * Cuenta cuántos usuarios han guardado un restaurante
     */
    suspend fun getSaveCount(restaurantId: String): Int {
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
}
