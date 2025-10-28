package ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.repository.ReviewRepository
import ar.edu.utn.frba.mobile.foodforall.repository.SavedRestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
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
            try {
                val reviews = reviewRepository.getByRestaurantId(restaurantId)
                
                val reviewsWithUsers = reviews.map { review ->
                    val user = userRepository.getById(review.userId)
                    ReviewWithUser(review, user)
                }
                
                _reviews.value = reviewsWithUsers
            } catch (e: Exception) {
                _error.value = "Error al cargar reviews: ${e.message}"
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
                _error.value = "Error al verificar estado de guardado: ${e.message}"
            }
        }
    }

    fun toggleSave(userId: String, restaurantId: String) {
        viewModelScope.launch {
            try {
                val newSavedState = savedRestaurantRepository.toggle(userId, restaurantId)
                _isSaved.value = newSavedState
            } catch (e: Exception) {
                _error.value = "Error al guardar restaurante: ${e.message}"
            }
        }
    }
}