package ar.edu.utn.frba.mobile.foodforall.domain.model

import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Modelo de Review para Firebase y UI
 */
data class Review(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val dietaryRestriction: String = DietaryRestriction.GENERAL.key,
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Retorna la restricción dietética como enum
     */
    val dietaryRestrictionEnum: DietaryRestriction
        get() = DietaryRestriction.fromKey(dietaryRestriction) ?: DietaryRestriction.GENERAL

    /**
     * Convierte el modelo a un Map para guardarlo en Firestore
     */
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "restaurantId" to restaurantId,
        "rating" to rating,
        "comment" to comment,
        "dietaryRestriction" to dietaryRestriction,
        "imageUrls" to imageUrls,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        /**
         * Crea un Review desde un DocumentSnapshot de Firestore
         */
        fun fromFirestore(doc: DocumentSnapshot): Review? {
            return try {
                Review(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    restaurantId = doc.getString("restaurantId") ?: "",
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    comment = doc.getString("comment") ?: "",
                    dietaryRestriction = doc.getString("dietaryRestriction") ?: DietaryRestriction.GENERAL.key,
                    imageUrls = (doc.get("imageUrls") as? List<*>)
                        ?.mapNotNull { it as? String } ?: emptyList(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
