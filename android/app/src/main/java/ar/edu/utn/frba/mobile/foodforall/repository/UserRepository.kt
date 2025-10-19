package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para operaciones CRUD de usuarios
 */
class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = db.collection("users")

    /**
     * Obtiene un usuario por su ID
     */
    suspend fun getById(userId: String): User? {
        return try {
            val doc = collection.document(userId).get().await()
            User.fromFirestore(doc)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene todos los usuarios
     */
    suspend fun getAll(): List<User> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { User.fromFirestore(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun save(user: User): String {
        return try {
            if (user.id.isEmpty()) {
                val doc = collection.document()
                val userWithId = user.copy(id = doc.id)
                doc.set(userWithId.toFirestoreMap()).await()
                doc.id
            } else {
                collection.document(user.id)
                    .set(user.toFirestoreMap())
                    .await()
                user.id
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Actualiza las restricciones dietéticas de un usuario
     */
    suspend fun updateDietaryRestrictions(userId: String, restrictions: List<String>): Boolean {
        return try {
            collection.document(userId)
                .update(
                    mapOf(
                        "dietaryRestrictions" to restrictions,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina un usuario
     */
    suspend fun delete(userId: String): Boolean {
        return try {
            collection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Busca un usuario por username
     */
    suspend fun getByUsername(username: String): User? {
        return try {
            val snapshot = collection
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { User.fromFirestore(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Busca un usuario por email
     */
    suspend fun getByEmail(email: String): User? {
        return try {
            val snapshot = collection
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.let { User.fromFirestore(it) }
        } catch (e: Exception) {
            null
        }
    }
}
