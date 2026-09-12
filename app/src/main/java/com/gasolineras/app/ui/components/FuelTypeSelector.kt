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
    selectedFuel: FuelType,
    onFuelSelected: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    val mainFuels = listOf(
        FuelType.GASOLEO_A,
        FuelType.GASOLINA_95_E5,
        FuelType.GLP
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        mainFuels.forEach { fuel ->
            val isSelected = fuel == selectedFuel
            FilterChip(
                selected = isSelected,
                onClick = { onFuelSelected(fuel) },
                label = {
                    Text(
                        text = fuel.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
