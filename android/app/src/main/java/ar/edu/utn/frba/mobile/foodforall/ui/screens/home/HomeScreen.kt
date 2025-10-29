package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.utn.frba.mobile.foodforall.ui.components.FilterPillButton
import ar.edu.utn.frba.mobile.foodforall.domain.model.DietaryRestriction
import ar.edu.utn.frba.mobile.foodforall.domain.model.Restaurant

sealed class HomeTab (val title: String) {
    data object Map : HomeTab("Mapa")
    data object Restaurants : HomeTab("Restaurantes")
}

private val homeTabs = listOf(HomeTab.Restaurants, HomeTab.Map)

private val setSaver: Saver<Set<String>, ArrayList<String>> =
    Saver(
        save = { s -> ArrayList(s) },
        restore = { list -> list.toSet() }
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRestaurantClick: (String) -> Unit,
    onCreateReviewClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(),
    authViewModel: ar.edu.utn.frba.mobile.foodforall.ui.viewmodel.AuthViewModel = viewModel()
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedTab = homeTabs[selectedTabIndex]

    var showFilters by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var selectedFilters by rememberSaveable(stateSaver = setSaver) {
        mutableStateOf(emptySet<String>())
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeTabRow(
                selected = selectedTab,
                onSelected = { selectedTabIndex = homeTabs.indexOf(it) }
            )
            when (selectedTab) {
                is HomeTab.Map -> MapTab(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onRestaurantClick = onCreateReviewClick,
                    authViewModel = authViewModel
                )
                is HomeTab.Restaurants -> RestaurantListTab(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onRestaurantClick = onRestaurantClick,
                    onReviewClick = onCreateReviewClick
                )
            }
        }
        FilterPillButton(
            onClick = { showFilters = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .navigationBarsPadding()
        )
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState
        ) {
            QuickFiltersSheetContent(
                selectedKeys = selectedFilters,
                onToggle = { key ->
                    selectedFilters = if (key in selectedFilters) {
                        selectedFilters - key
                    } else {
                        selectedFilters + key
                    }

                    val filters = selectedFilters.mapNotNull { DietaryRestriction.fromKey(it) }.toSet()
                    viewModel.applyFilters(filters)
                },
                onClear = {
                    selectedFilters = emptySet()
                    viewModel.clearFilters()
                },
                onApply = { showFilters = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun HomeTabRow(
    selected: HomeTab,
    onSelected: (HomeTab) -> Unit
) {
    TabRow(selectedTabIndex = homeTabs.indexOf(selected)) {
        homeTabs.forEachIndexed { index, tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                text = { Text(tab.title) }
            )
        }
    }
}