package ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.repository.ReviewRepository
import ar.edu.utn.frba.mobile.foodforall.repository.SavedRestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewWithUser(
    val review: Review,
    val user: User?
)

class RestaurantProfileViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(FirebaseFirestore.getInstance()),
    private val savedRestaurantRepository: SavedRestaurantRepository = SavedRestaurantRepository(FirebaseFirestore.getInstance()),
    private val userRepository: UserRepository = UserRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _reviews = MutableStateFlow<List<ReviewWithUser>>(emptyList())
    val reviews: StateFlow<List<ReviewWithUser>> = _reviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadReviews(restaurantId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val reviews = reviewRepository.getByRestaurantId(restaurantId)
                
                val reviewsWithUsers = reviews.map { review ->
                    async {
                        val user = userRepository.getById(review.userId)
                        ReviewWithUser(review, user)
                    }
                }.awaitAll()
                
                _reviews.value = reviewsWithUsers
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Sin conexión a internet. Verificá tu conexión e intentá de nuevo."
                _reviews.value = emptyList()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                _error.value = "Error al conectar con el servidor. Intentá más tarde."
                _reviews.value = emptyList()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las reseñas. Intentá de nuevo."
                _reviews.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkIfSaved(userId: String, restaurantId: String) {
        viewModelScope.launch {
            try {
                val saved = savedRestaurantRepository.isSaved(userId, restaurantId)
                _isSaved.value = saved
            } catch (e: Exception) {
                _isSaved.value = false
            }
        }
    }

    fun toggleSave(userId: String, restaurantId: String) {
        viewModelScope.launch {
            try {
                val newSavedState = savedRestaurantRepository.toggle(userId, restaurantId)
                _isSaved.value = newSavedState
                _error.value = null
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Sin conexión. No se pudo guardar el restaurante."
            } catch (e: Exception) {
                _error.value = "No se pudo guardar el restaurante. Intentá de nuevo."
            }
        }
    }
}