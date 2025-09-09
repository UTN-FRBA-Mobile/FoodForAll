package ar.edu.utn.frba.mobile.foodforall.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.HomeScreen
import ar.edu.utn.frba.mobile.foodforall.ui.screens.profile.ProfileScreen
import ar.edu.utn.frba.mobile.foodforall.ui.screens.search.SearchScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PROFILE = "profile"
}

data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "Inicio", Icons.Filled.Home),
    BottomItem(Routes.SEARCH, "Buscar", Icons.Filled.Search),
    BottomItem(Routes.PROFILE, "Perfil", Icons.Filled.Person),
)

/**
 * Contiene la barra de navegación y el contenido principal de la aplicación.
 */
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(
                items = bottomItems,
                currentDestination = navController
                    .currentBackStackEntryAsState().value?.destination,
            ) { targetRoute ->
                navController.navigate(targetRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.then(Modifier.padding(innerPadding))
        ) {
            composable(Routes.HOME) {
                HomeScreen()
            }
            composable(Routes.SEARCH) {
                SearchScreen()
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }
        }
    }
}