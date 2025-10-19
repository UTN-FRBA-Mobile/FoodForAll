package ar.edu.utn.frba.mobile.foodforall.domain.model

import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Modelo de Usuario para Firebase y UI
 */
data class User(
    val id: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val dietaryRestrictions: List<String> = emptyList(), // Keys de DietaryRestriction
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Retorna las restricciones dietéticas como enums
     */
    val dietaryRestrictionsEnums: Set<DietaryRestriction>
        get() = dietaryRestrictions.mapNotNull { DietaryRestriction.fromKey(it) }.toSet()

    /**
     * Convierte el modelo a un Map para guardarlo en Firestore
     */
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "fullName" to fullName,
        "username" to username,
        "email" to email,
        "avatarUrl" to avatarUrl,
        "dietaryRestrictions" to dietaryRestrictions,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        /**
         * Crea un User desde un DocumentSnapshot de Firestore
         */
        fun fromFirestore(doc: DocumentSnapshot): User? {
            return try {
                User(
                    id = doc.id,
                    fullName = doc.getString("fullName") ?: "",
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    avatarUrl = doc.getString("avatarUrl"),
                    dietaryRestrictions = (doc.get("dietaryRestrictions") as? List<*>)
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
