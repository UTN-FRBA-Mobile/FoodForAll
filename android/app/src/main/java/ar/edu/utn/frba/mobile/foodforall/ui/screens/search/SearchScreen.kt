package ar.edu.utn.frba.mobile.foodforall.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant
import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.HomeViewModel
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.RestaurantCard

@Composable
fun SearchScreen(
    onRestaurantClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDietFilters by remember { mutableStateOf(setOf<String>()) }
    var selectedSort by remember { mutableStateOf<SortOption?>(null) }

    val allRestaurants by viewModel.restaurants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val filteredRestaurants by remember {
        derivedStateOf {
            var restaurants = allRestaurants

            if (searchQuery.isNotBlank()) {
                restaurants = restaurants.filter { restaurant ->
                    restaurant.name.contains(searchQuery, ignoreCase = true) ||
                            restaurant.description.contains(searchQuery, ignoreCase = true)
                }
            }

            if (selectedDietFilters.isNotEmpty()) {
                restaurants = restaurants.filter { restaurant ->
                    selectedDietFilters.any { filterKey ->
                        val restriction = DietaryRestriction.fromKey(filterKey)
                        restriction != null && restriction in restaurant.dietaryRestrictions
                    }
                }
            }

            when (selectedSort) {
                SortOption.NEAREST -> {
                    val withDistance = restaurants.filter { it.distanceKm != null }
                    val withoutDistance = restaurants.filter { it.distanceKm == null }
                    withDistance.sortedBy { it.distanceKm ?: Float.MAX_VALUE } + withoutDistance
                }
                SortOption.BEST_RATED -> restaurants.sortedByDescending { it.rating }
                null -> restaurants
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = "" }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Filtros de dieta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            DietFilterChips(
                selectedFilters = selectedDietFilters,
                onFilterToggle = { filter ->
                    selectedDietFilters = if (filter in selectedDietFilters) {
                        selectedDietFilters - filter
                    } else {
                        selectedDietFilters + filter
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            SortingFilters(
                selectedSort = selectedSort,
                onSortChange = { selectedSort = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            isLoading && allRestaurants.isEmpty() -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando restaurantes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            filteredRestaurants.isEmpty() -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No se encontraron restaurantes",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isNotBlank() || selectedDietFilters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Intenta ajustar los filtros o el término de búsqueda",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                items(filteredRestaurants, key = { it.id }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onRestaurantClick = { onRestaurantClick(restaurant.id) },
                        onReviewClick = { /* No-op for search screen */ }
                    )
                }
            }
        }
    }
}
