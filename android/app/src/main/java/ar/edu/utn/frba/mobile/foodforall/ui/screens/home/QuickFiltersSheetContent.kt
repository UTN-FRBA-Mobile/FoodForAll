package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class FilterIcon(
    val key: String,
    val label: String,
    val icon: ImageVector
)

private val quickFilterIcons = listOf(
    FilterIcon("veggie", "Vegetariano", Icons.Outlined.AccountBox),
    FilterIcon("celiac", "Celiaco", Icons.Outlined.AccountCircle),
    FilterIcon("sibo", "Sibo", Icons.Outlined.Face),
    FilterIcon("vegan", "Vegano", Icons.Outlined.Call),
)

/**
 * Muestra un filtro de comida, para listar solamente las dietas seleccionadas
 */
@Composable
fun QuickFiltersSheetContent(
    selectedKeys: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickFilterIcons.forEach { f ->
                val selected = f.key in selectedKeys
                SelectableIcon(
                    selected = selected,
                    label = f.label,
                    icon = f.icon,
                    onClick = { onToggle(f.key) }
                )
            }
        }

    }
}

@Composable
private fun SelectableIcon(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val selectedBg = MaterialTheme.colorScheme.primaryContainer
    val tint = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current

    Surface(
        shape = CircleShape,
        color = if (selected) selectedBg else Color.Transparent,
        tonalElevation = if (selected) 3.dp else 0.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = label, tint = tint)
                Spacer(Modifier.height(2.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}