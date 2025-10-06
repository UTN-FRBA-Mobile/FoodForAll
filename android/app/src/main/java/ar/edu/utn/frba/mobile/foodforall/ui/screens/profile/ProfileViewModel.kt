package ar.edu.utn.frba.mobile.foodforall.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.domain.model.Review
import ar.edu.utn.frba.mobile.foodforall.domain.model.User
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.repository.ReviewRepository
import ar.edu.utn.frba.mobile.foodforall.repository.SavedRestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewWithRestaurant(
    val review: Review,
    val restaurant: Restaurant?
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(FirebaseFirestore.getInstance()),
    private val reviewRepository: ReviewRepository = ReviewRepository(FirebaseFirestore.getInstance()),
    private val savedRestaurantRepository: SavedRestaurantRepository = SavedRestaurantRepository(FirebaseFirestore.getInstance()),
    private val restaurantRepository: RestaurantRepository = RestaurantRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val currentUserId = MutableStateFlow<String?>(null)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userReviews = MutableStateFlow<List<ReviewWithRestaurant>>(emptyList())
    val userReviews: StateFlow<List<ReviewWithRestaurant>> = _userReviews.asStateFlow()

    private val _savedRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val savedRestaurants: StateFlow<List<Restaurant>> = _savedRestaurants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Cargar el primer usuario disponible como usuario actual
        loadFirstUser()
    }

    private fun loadFirstUser() {
        viewModelScope.launch {
            try {
                val users = userRepository.getAll()
                if (users.isNotEmpty()) {
                    val user = users.first()
                    currentUserId.value = user.id
                    _currentUser.value = user
                    loadUserData(user.id)
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar usuario: ${e.message}"
            }
        }
    }

    private fun loadUserData(userId: String) {
        loadUserReviews(userId)
        loadSavedRestaurants(userId)
    }

    private fun loadUserReviews(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val reviews = reviewRepository.getByUserId(userId)

                val reviewsWithRestaurants = reviews.map { review ->
                    val restaurant = restaurantRepository.getById(review.restaurantId)
                    ReviewWithRestaurant(review, restaurant)
                }

                _userReviews.value = reviewsWithRestaurants
            } catch (e: Exception) {
                _error.value = "Error al cargar reviews: ${e.message}"
                _userReviews.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSavedRestaurants(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val savedIds = savedRestaurantRepository.getSavedRestaurantIds(userId)

                val restaurants = savedIds.mapNotNull { id ->
                    restaurantRepository.getById(id)
                }

                _savedRestaurants.value = restaurants
            } catch (e: Exception) {
                _error.value = "Error al cargar restaurantes guardados: ${e.message}"
                _savedRestaurants.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSaveRestaurant(restaurantId: String) {
        val userId = currentUserId.value ?: return

        viewModelScope.launch {
            try {
                savedRestaurantRepository.toggle(userId, restaurantId)
                loadSavedRestaurants(userId)
            } catch (e: Exception) {
                _error.value = "Error al guardar restaurante: ${e.message}"
            }
        }
    }

    fun deleteReview(reviewId: String) {
        val userId = currentUserId.value ?: return

        viewModelScope.launch {
            try {
                reviewRepository.delete(reviewId)
                loadUserReviews(userId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar review: ${e.message}"
            }
        }
    }

    fun updateDietaryRestrictions(restrictions: List<String>) {
        val userId = currentUserId.value ?: return

        viewModelScope.launch {
            try {
                userRepository.updateDietaryRestrictions(userId, restrictions)
                val updatedUser = userRepository.getById(userId)
                _currentUser.value = updatedUser
            } catch (e: Exception) {
                _error.value = "Error al actualizar restricciones: ${e.message}"
            }
        }
    }

    fun refresh() {
        currentUserId.value?.let { userId ->
            loadUserData(userId)
        } ?: loadFirstUser()
    }

    fun setCurrentUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getById(userId)
                if (user != null) {
                    currentUserId.value = userId
                    _currentUser.value = user
                    loadUserData(userId)
                }
            } catch (e: Exception) {
                _error.value = "Error al cambiar usuario: ${e.message}"
            }
        }
    }
}
