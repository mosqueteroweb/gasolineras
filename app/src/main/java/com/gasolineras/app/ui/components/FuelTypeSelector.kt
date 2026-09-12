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

private fun getFuelButtonConfig(fuelType: FuelType): FuelButtonConfig {
    return when (fuelType) {
        FuelType.GASOLEO_A -> FuelButtonConfig(
            fuelType = FuelType.GASOLEO_A,
            label = "Diésel",
            selectedBg = Color(0xFF1565C0),        // Solid Vibrant Blue
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFF90CAF9),   // Light Blue
            unselectedIconTint = Color(0xFF1976D2)  // Blue
        )
        FuelType.GASOLINA_95_E5 -> FuelButtonConfig(
            fuelType = FuelType.GASOLINA_95_E5,
            label = "95",
            selectedBg = Color(0xFFC62828),        // Solid Vibrant Red
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFFFCDD2),   // Light Red
            unselectedIconTint = Color(0xFFD32F2F)  // Red
        )
        FuelType.GLP -> FuelButtonConfig(
            fuelType = FuelType.GLP,
            label = "GLP",
            selectedBg = Color(0xFF00695C),        // Solid Dark Teal Green
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFF80CBC4),   // Light Teal
            unselectedIconTint = Color(0xFF00897B)  // Green
        )
        FuelType.GASOLINA_98_E5 -> FuelButtonConfig(
            fuelType = FuelType.GASOLINA_98_E5,
            label = "98",
            selectedBg = Color(0xFFD84315),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFFFCCBC),
            unselectedIconTint = Color(0xFFE64A19)
        )
        FuelType.GASOLEO_PREMIUM -> FuelButtonConfig(
            fuelType = FuelType.GASOLEO_PREMIUM,
            label = "Diésel+",
            selectedBg = Color(0xFF0D47A1),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFBBDEFB),
            unselectedIconTint = Color(0xFF1565C0)
        )
        FuelType.GASOLEO_B -> FuelButtonConfig(
            fuelType = FuelType.GASOLEO_B,
            label = "Gasóleo B",
            selectedBg = Color(0xFF6D4C41),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFD7CCC8),
            unselectedIconTint = Color(0xFF795548)
        )
        FuelType.ADBLUE -> FuelButtonConfig(
            fuelType = FuelType.ADBLUE,
            label = "AdBlue",
            selectedBg = Color(0xFF0277BD),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFB3E5FC),
            unselectedIconTint = Color(0xFF039BE5)
        )
        FuelType.GNC -> FuelButtonConfig(
            fuelType = FuelType.GNC,
            label = "GNC",
            selectedBg = Color(0xFF6A1B9A),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFE1BEE7),
            unselectedIconTint = Color(0xFF8E24AA)
        )
        FuelType.GNL -> FuelButtonConfig(
            fuelType = FuelType.GNL,
            label = "GNL",
            selectedBg = Color(0xFF00838F),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFB2EBF2),
            unselectedIconTint = Color(0xFF00ACC1)
        )
        FuelType.BIODIESEL -> FuelButtonConfig(
            fuelType = FuelType.BIODIESEL,
            label = "Bio",
            selectedBg = Color(0xFF558B2F),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFDCEDC8),
            unselectedIconTint = Color(0xFF689F38)
        )
        FuelType.DIESEL_RENOVABLE -> FuelButtonConfig(
            fuelType = FuelType.DIESEL_RENOVABLE,
            label = "HVO",
            selectedBg = Color(0xFF2E7D32),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFC8E6C9),
            unselectedIconTint = Color(0xFF388E3C)
        )
        FuelType.HIDROGENO -> FuelButtonConfig(
            fuelType = FuelType.HIDROGENO,
            label = "H2",
            selectedBg = Color(0xFF0288D1),
            selectedContentColor = Color.White,
            selectedIconTint = Color(0xFFB3E5FC),
            unselectedIconTint = Color(0xFF03A9F4)
        )
    }
}

@Composable
fun FuelTypeSelector(
    selectedFuels: Set<FuelType>,
    onToggleFuel: (FuelType) -> Unit,
    visibleFuels: List<FuelType> = listOf(FuelType.GASOLEO_A, FuelType.GASOLINA_95_E5, FuelType.GLP),
    modifier: Modifier = Modifier
) {
    val configs = visibleFuels.take(3).map { getFuelButtonConfig(it) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        configs.forEach { config ->
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
