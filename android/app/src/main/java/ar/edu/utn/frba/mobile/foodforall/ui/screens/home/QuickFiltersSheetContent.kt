package ar.edu.utn.frba.mobile.foodforall.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction

private val quickFilterOptions = listOf(
    DietaryRestriction.VEGETARIAN,
    DietaryRestriction.CELIAC,
    DietaryRestriction.SIBO,
    DietaryRestriction.VEGAN,
)

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
            quickFilterOptions.forEach { restriction ->
                val selected = restriction.key in selectedKeys
                SelectableFilterOption(
                    selected = selected,
                    label = restriction.description,
                    emoji = restriction.emoji,
                    onClick = { onToggle(restriction.key) }
                )
            }
        }

    }
}

@Composable
private fun SelectableFilterOption(
    selected: Boolean,
    label: String,
    emoji: String,
    onClick: () -> Unit
) {
    val selectedBg = MaterialTheme.colorScheme.primaryContainer

    Surface(
        shape = CircleShape,
        color = if (selected) selectedBg else Color.Transparent,
        tonalElevation = if (selected) 3.dp else 0.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
    }
}