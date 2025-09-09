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
import ar.edu.utn.frba.mobile.foodforall.ui.components.FilterPillButton

sealed class HomeTab (val title: String) {
    data object Map : HomeTab("Mapa")
    data object Restaurants : HomeTab("Restoranes")
}

private val homeTabs = listOf(HomeTab.Restaurants, HomeTab.Map)

private val setSaver: Saver<Set<String>, ArrayList<String>> =
    Saver(
        save = { s -> ArrayList(s) },
        restore = { list -> list.toSet() }
    )

/**
 * Muestra la pantalla principal de la aplicación.
 * Contiene un mapa y una lista de restaurantes.
 */
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen () {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedTab = homeTabs[selectedTabIndex]

    var showFilters by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val selectedFilters by rememberSaveable(stateSaver = setSaver) { // Use 'by' for delegation
        mutableStateOf(emptySet<String>())
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            HomeTabRow(
                selected = selectedTab,
                onSelected = { selectedTabIndex = homeTabs.indexOf(it) }
            )
            when (selectedTab) {
                is HomeTab.Map -> MapTab(modifier = Modifier.fillMaxSize())
                is HomeTab.Restaurants -> RestaurantListTab(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        //FIXME el boton queda no es flotante, por lo cual corta el contenido que hay abajo.
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
                    //TODO()
                },
                onClear = { },
                onApply = { showFilters = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Header con los tabs
 */
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
