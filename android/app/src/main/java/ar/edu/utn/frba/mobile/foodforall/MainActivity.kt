package ar.edu.utn.frba.mobile.foodforall

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.utn.frba.mobile.foodforall.ui.navigation.AppRoot
import ar.edu.utn.frba.mobile.foodforall.ui.theme.FoodForAllTheme
import ar.edu.utn.frba.mobile.foodforall.utils.DeepLinkEvents

/**
 * Punto de Entrada
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DeepLinkEvents.publish(intent)
        setContent {
            FoodForAllTheme {
                AppRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Publicamos los intents que llegan cuando la Activity ya está viva (FLAG_ACTIVITY_SINGLE_TOP)
        DeepLinkEvents.publish(intent)
    }
}

@Preview(showBackground = true)
@Composable
fun FoodForAllPreview() {
    FoodForAllTheme {
        AppRoot()
    }
}