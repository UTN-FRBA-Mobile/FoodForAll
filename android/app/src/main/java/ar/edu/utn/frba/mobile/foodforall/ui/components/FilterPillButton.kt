package ar.edu.utn.frba.mobile.foodforall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Boton Flotante
 */
@Composable
fun FilterPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = modifier
            .height(44.dp)
            .wrapContentWidth()
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.Transparent)
                .padding(horizontal = 16.dp)
                .height(44.dp)
                .wrapContentWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Abrir filtros")
            Spacer(Modifier.width(6.dp))
            Text("Filtro Rápido", style = MaterialTheme.typography.labelLarge)
        }
    }
}