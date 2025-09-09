package ar.edu.utn.frba.mobile.foodforall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ar.edu.utn.frba.mobile.foodforall.ui.navigation.AppRoot
import ar.edu.utn.frba.mobile.foodforall.ui.theme.FoodForAllTheme

/**
 * Punto de Entrada
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodForAllTheme {
                AppRoot()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodForAllPreview() {
    FoodForAllTheme {
        AppRoot()
    }
}