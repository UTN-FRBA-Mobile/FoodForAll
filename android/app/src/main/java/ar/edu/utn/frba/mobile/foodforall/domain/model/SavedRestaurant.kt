package ar.edu.utn.frba.mobile.foodforall.domain.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Modelo de SavedRestaurant (relación usuario-restaurante guardado)
 */
data class SavedRestaurant(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val savedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convierte el modelo a un Map para guardarlo en Firestore
     */
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "restaurantId" to restaurantId,
        "savedAt" to savedAt
    )

    companion object {
        /**
         * Crea un SavedRestaurant desde un DocumentSnapshot de Firestore
         */
        fun fromFirestore(doc: DocumentSnapshot): SavedRestaurant? {
            return try {
                SavedRestaurant(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    restaurantId = doc.getString("restaurantId") ?: "",
                    savedAt = doc.getLong("savedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
