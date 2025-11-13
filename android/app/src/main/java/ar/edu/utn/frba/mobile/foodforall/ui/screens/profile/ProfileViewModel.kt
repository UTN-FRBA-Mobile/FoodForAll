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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _userReviews = MutableStateFlow<List<ReviewWithRestaurant>>(emptyList())
    val userReviews: StateFlow<List<ReviewWithRestaurant>> = _userReviews.asStateFlow()

    private val _savedRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val savedRestaurants: StateFlow<List<Restaurant>> = _savedRestaurants.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _isLoadingSaved = MutableStateFlow(false)
    val isLoadingSaved: StateFlow<Boolean> = _isLoadingSaved.asStateFlow()

    val isLoading: StateFlow<Boolean> = combine(
        _isLoadingReviews,
        _isLoadingSaved
    ) { reviews, saved -> reviews || saved }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadUserData(userId: String) {
        loadUserReviews(userId)
        loadSavedRestaurants(userId)
    }

    private fun loadUserReviews(userId: String) {
        viewModelScope.launch {
            _isLoadingReviews.value = true
            try {
                val reviews = reviewRepository.getByUserId(userId)

                val reviewsWithRestaurants = reviews.map { review ->
                    async {
                        val restaurant = restaurantRepository.getById(review.restaurantId)
                        ReviewWithRestaurant(review, restaurant)
                    }
                }.awaitAll()

                _userReviews.value = reviewsWithRestaurants
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Sin conexión a internet. Verificá tu conexión."
                _userReviews.value = emptyList()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                _error.value = "Error al conectar con el servidor. Intentá más tarde."
                _userReviews.value = emptyList()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar las reseñas. Intentá de nuevo."
                _userReviews.value = emptyList()
            } finally {
                _isLoadingReviews.value = false
            }
        }
    }

    private fun loadSavedRestaurants(userId: String) {
        viewModelScope.launch {
            _isLoadingSaved.value = true
            try {
                val savedIds = savedRestaurantRepository.getSavedRestaurantIds(userId)

                val restaurants = savedIds.map { id ->
                    async {
                        restaurantRepository.getById(id)
                    }
                }.awaitAll().filterNotNull()

                _savedRestaurants.value = restaurants
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Sin conexión a internet. Verificá tu conexión."
                _savedRestaurants.value = emptyList()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                _error.value = "Error al conectar con el servidor. Intentá más tarde."
                _savedRestaurants.value = emptyList()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los restaurantes guardados. Intentá de nuevo."
                _savedRestaurants.value = emptyList()
            } finally {
                _isLoadingSaved.value = false
            }
        }
    }

    fun toggleSaveRestaurant(userId: String, restaurantId: String) {
        viewModelScope.launch {
            try {
                savedRestaurantRepository.toggle(userId, restaurantId)
                loadSavedRestaurants(userId)
            } catch (e: Exception) {
                _error.value = "Error al guardar restaurante: ${e.message}"
            }
        }
    }

    fun deleteReview(userId: String, reviewId: String) {
        viewModelScope.launch {
            try {
                reviewRepository.delete(reviewId)
                loadUserReviews(userId)
            } catch (e: Exception) {
                _error.value = "Error al eliminar review: ${e.message}"
            }
        }
    }

    fun updateDietaryRestrictions(userId: String, restrictions: List<String>) {
        viewModelScope.launch {
            try {
                userRepository.updateDietaryRestrictions(userId, restrictions)
            } catch (e: Exception) {
                _error.value = "Error al actualizar restricciones: ${e.message}"
            }
        }
    }
}
