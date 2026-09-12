package com.gasolineras.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasolineras.app.domain.model.FuelType

@Composable
fun FuelTypeSelector(
    selectedFuels: Set<FuelType>,
    onToggleFuel: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainFuels = listOf(
        FuelType.GASOLEO_A to "⛽ Diésel",
        FuelType.GASOLINA_95_E5 to "⛽ Gas 95",
        FuelType.GLP to "🟢 GLP"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mainFuels.forEach { (fuel, label) ->
            val isSelected = selectedFuels.contains(fuel)
            val isGLP = fuel == FuelType.GLP

            FilterChip(
                selected = isSelected,
                onClick = { onToggleFuel(fuel) },
                label = {
                    Text(
                        text = if (isSelected) "✓ $label" else label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = if (isGLP) {
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = androidx.compose.ui.graphics.Color(0xFF00695C),
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                } else {
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
