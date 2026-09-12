package com.gasolineras.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasolineras.app.domain.model.FuelType

private data class FuelButtonConfig(
    val fuelType: FuelType,
    val label: String,
    val selectedBg: Color,
    val selectedContentColor: Color,
    val selectedIconTint: Color,
    val unselectedIconTint: Color
)

private val fuelConfigs = listOf(
    FuelButtonConfig(
        fuelType = FuelType.GASOLEO_A,
        label = "Diésel",
        selectedBg = Color(0xFF1565C0),        // Solid Vibrant Blue
        selectedContentColor = Color.White,
        selectedIconTint = Color(0xFF90CAF9),   // Light Blue
        unselectedIconTint = Color(0xFF1976D2)  // Blue
    ),
    FuelButtonConfig(
        fuelType = FuelType.GASOLINA_95_E5,
        label = "95",
        selectedBg = Color(0xFFC62828),        // Solid Vibrant Red
        selectedContentColor = Color.White,
        selectedIconTint = Color(0xFFFFCDD2),   // Light Red
        unselectedIconTint = Color(0xFFD32F2F)  // Red
    ),
    FuelButtonConfig(
        fuelType = FuelType.GLP,
        label = "GLP",
        selectedBg = Color(0xFF00695C),        // Solid Dark Teal Green
        selectedContentColor = Color.White,
        selectedIconTint = Color(0xFF80CBC4),   // Light Teal
        unselectedIconTint = Color(0xFF00897B)  // Green
    )
)

@Composable
fun FuelTypeSelector(
    selectedFuels: Set<FuelType>,
    onToggleFuel: (FuelType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        fuelConfigs.forEach { config ->
            val isSelected = selectedFuels.contains(config.fuelType)
            val backgroundColor = if (isSelected) config.selectedBg else MaterialTheme.colorScheme.surface
            val contentColor = if (isSelected) config.selectedContentColor else MaterialTheme.colorScheme.onSurface
            val iconTint = if (isSelected) config.selectedIconTint else config.unselectedIconTint
            val borderColor = if (isSelected) config.selectedBg else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

            Surface(
                onClick = { onToggleFuel(config.fuelType) },
                shape = RoundedCornerShape(10.dp),
                color = backgroundColor,
                contentColor = contentColor,
                border = BorderStroke(1.dp, borderColor),
                shadowElevation = if (isSelected) 3.dp else 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 38.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = config.label,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
