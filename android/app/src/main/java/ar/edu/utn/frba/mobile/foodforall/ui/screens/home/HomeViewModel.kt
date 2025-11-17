package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RestaurantRepository = RestaurantRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedFilters = MutableStateFlow<Set<DietaryRestriction>>(emptySet())
    val selectedFilters: StateFlow<Set<DietaryRestriction>> = _selectedFilters.asStateFlow()

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _mapBounds = MutableStateFlow<LatLngBounds?>(null)

    init {
        loadRestaurants()
    }

    fun setMapBounds(bounds: LatLngBounds) {
        _mapBounds.value = bounds
        loadRestaurants()
    }

    fun clearMapBounds() {
        if (_mapBounds.value != null) {
            _mapBounds.value = null
            loadRestaurants()
        }
    }

    fun updateUserLocation(location: LatLng?) {
        _userLocation.value = location
        _restaurants.value = _restaurants.value.map { it.withDistance(location) }
    }

    fun loadRestaurants() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = _mapBounds.value?.let { bounds ->
                    val inBounds = repository.fetchInBounds(bounds, _userLocation.value)
                    if (_selectedFilters.value.isEmpty()) {
                        inBounds
                    } else {
                        inBounds.filter { restaurant ->
                            _selectedFilters.value.all { filter ->
                                when (filter) {
                                    DietaryRestriction.VEGETARIAN -> restaurant.hasVegetarianOption
                                    DietaryRestriction.VEGAN -> restaurant.hasVeganOption
                                    DietaryRestriction.CELIAC -> restaurant.hasCeliacOption
                                    DietaryRestriction.SIBO -> restaurant.hasSiboOption
                                    DietaryRestriction.GENERAL -> true
                                }
                            }
                        }
                    }
                } ?: if (_selectedFilters.value.isEmpty()) {
                    repository.getAll()
                } else {
                    repository.getWithFilters(_selectedFilters.value)
                }

                _restaurants.value = result
                    .map { it.withDistance(_userLocation.value) }
                    .sortedByDescending { it.rating }
            } catch (e: java.net.UnknownHostException) {
                _error.value = "Sin conexión a internet. Verificá tu conexión."
                _restaurants.value = emptyList()
            } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
                _error.value = "Error al conectar con el servidor. Intentá más tarde."
                _restaurants.value = emptyList()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los restaurantes. Intentá de nuevo."
                _restaurants.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilters(filters: Set<DietaryRestriction>) {
        _selectedFilters.value = filters
        loadRestaurants()
    }

    fun clearFilters() {
        _selectedFilters.value = emptySet()
        loadRestaurants()
    }

    fun toggleFilter(filter: DietaryRestriction) {
        val current = _selectedFilters.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        applyFilters(current)
    }


    fun refresh() {
        loadRestaurants()
    }
}
