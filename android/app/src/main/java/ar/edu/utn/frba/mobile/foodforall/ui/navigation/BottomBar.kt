package ar.edu.utn.frba.mobile.foodforall.ui.navigation

import android.content.Intent
import android.os.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import ar.edu.utn.frba.mobile.foodforall.BuildConfig
import ar.edu.utn.frba.mobile.foodforall.repository.RestaurantRepository
import ar.edu.utn.frba.mobile.foodforall.service.StayDetectService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

@Composable
fun BottomBar(
    items: List<BottomItem>,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    // Contador de taps “secretos” sobre Inicio (solo debug)
    var debugHomeTapCount by remember { mutableIntStateOf(0) }
    val restaurantRepository = remember { RestaurantRepository() }

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route } == true

            val isHomeItem = item.label.equals("Inicio", ignoreCase = true)
            val scope = rememberCoroutineScope()
            val haptics = LocalHapticFeedback.current
            NavigationBarItem(
                selected = selected,
                onClick = {
                    onNavigate(item.route)

                    if (BuildConfig.DEBUG && isHomeItem) {
                        debugHomeTapCount++

                        if (debugHomeTapCount >= 5) {
                            debugHomeTapCount = 0
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                            scope.launch {
                                delay(10_000)
                                val restaurants = restaurantRepository.getAll()
                                val first = restaurants.firstOrNull()

                                if (first != null) {
                                    val intent = Intent(context, StayDetectService::class.java).apply {
                                        action = StayDetectService.ACTION_FORCE_REVIEW
                                        putExtra(
                                            StayDetectService.EXTRA_RESTAURANT_ID,
                                            first.id
                                        )
                                        putExtra(
                                            StayDetectService.EXTRA_RESTAURANT_NAME,
                                            first.name
                                        )
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                }
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                modifier = Modifier
            )
        }
    }
}
