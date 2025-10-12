package ar.edu.utn.frba.mobile.foodforall.ui.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.edu.utn.frba.mobile.foodforall.service.StayDetectService
import ar.edu.utn.frba.mobile.foodforall.ui.components.LocationPermissionGate
import ar.edu.utn.frba.mobile.foodforall.ui.screens.home.HomeScreen
import ar.edu.utn.frba.mobile.foodforall.ui.screens.profile.ProfileScreen
import ar.edu.utn.frba.mobile.foodforall.ui.screens.restaurantprofile.RestaurantProfileScreen
import ar.edu.utn.frba.mobile.foodforall.ui.screens.search.SearchScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val RESTAURANT_PROFILE = "restaurant_profile/{restaurantId}"
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

    val ctx = LocalContext.current
    var hasLocation by remember { mutableStateOf(false) }

    fun startStayDetectService(ctx: Context) {
        val i = Intent(ctx, StayDetectService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
        else ctx.startService(i)
    }

    LocationPermissionGate(
        requestOnStart = true,
        onResult = { ok -> hasLocation = ok }
    ) {
        startStayDetectService(ctx)
    }

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
                HomeScreen(
                    onRestaurantClick = { restaurantId ->
                        navController.navigate("restaurant_profile/$restaurantId")
                    }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onRestaurantClick = { restaurantId ->
                        navController.navigate("restaurant_profile/$restaurantId")
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                )
            }
            composable(
                route = Routes.RESTAURANT_PROFILE,
                arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString("restaurantId")
                if (restaurantId != null) {
                    RestaurantProfileScreen(
                        restaurantId = restaurantId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}