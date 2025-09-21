package ar.edu.utn.frba.mobile.foodforall.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.edu.utn.frba.mobile.foodforall.ui.model.DietaryRestriction

private val dietFilters = listOf(
    DietaryRestriction.VEGETARIAN,
    DietaryRestriction.CELIAC,
    DietaryRestriction.SIBO,
    DietaryRestriction.VEGAN
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DietFilterChips(
    selectedFilters: Set<String>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dietFilters.forEach { restriction ->
            FilterChip(
                onClick = { onFilterToggle(restriction.key) },
                label = {
                    Text(
                        text = "${restriction.emoji} ${restriction.description}",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = restriction.key in selectedFilters
            )
        }
    }
}