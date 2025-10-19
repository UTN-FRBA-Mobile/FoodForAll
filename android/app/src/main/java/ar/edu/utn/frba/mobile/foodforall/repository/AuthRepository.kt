package ar.edu.utn.frba.mobile.foodforall.repository

import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(FirebaseFirestore.getInstance())
) {

    val currentUserFlow: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val user = userRepository.getById(firebaseUser.uid)
                    trySend(user)
                }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = userRepository.getById(firebaseUser.uid)
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Usuario no encontrado en la base de datos")
                }
            } else {
                AuthResult.Error("Error al iniciar sesión")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        username: String,
        dietaryRestrictions: List<String> = emptyList()
    ): AuthResult {
        return try {
            if (!isUsernameAvailable(username)) {
                return AuthResult.Error("El username ya está en uso")
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    fullName = fullName,
                    username = username,
                    email = email,
                    avatarUrl = null,
                    dietaryRestrictions = dietaryRestrictions,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                userRepository.save(user)

                AuthResult.Success(user)
            } else {
                AuthResult.Error("Error al crear usuario")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        val existingUser = userRepository.getByUsername(username)
        return existingUser == null
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
}
