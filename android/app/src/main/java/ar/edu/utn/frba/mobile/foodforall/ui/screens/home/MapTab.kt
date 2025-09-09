package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap


/**
 * Muestra el mapa de la aplicación.
 */
@Composable
fun MapTab(modifier: Modifier = Modifier) {
    GoogleMap(modifier = modifier) {
        //TODO ir a la ubicacion actual del usuario
    }
}