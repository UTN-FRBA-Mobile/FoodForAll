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

enum class SortOption(val key: String, val label: String) {
    NEAREST("nearest", "Más cercanos"),
    BEST_RATED("best_rated", "Mejor puntuados")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortingFilters(
    selectedSort: SortOption?,
    onSortChange: (SortOption?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SortOption.entries.forEach { sortOption ->
            FilterChip(
                onClick = {
                    if (selectedSort == sortOption) {
                        onSortChange(null)
                    } else {
                        onSortChange(sortOption)
                    }
                },
                label = {
                    Text(
                        text = sortOption.label,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = selectedSort == sortOption
            )
        }
    }
}