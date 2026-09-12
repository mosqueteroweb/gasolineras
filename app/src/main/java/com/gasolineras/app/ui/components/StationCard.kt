package com.gasolineras.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.ui.theme.CheapGreen
import com.gasolineras.app.ui.theme.CheapGreenContainer

@Composable
fun StationCard(
    station: GasStation,
    selectedFuels: Set<FuelType>,
    visibleFuels: List<FuelType> = emptyList(),
    isCheapestInArea: Boolean = false,
    cheapestForFuels: List<FuelType> = emptyList(),
    onClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row: Brand icon/avatar, Name, Open 24h tag, Favorite button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Brand Logo avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(getBrandColor(station.cleanBrand)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = station.cleanBrand.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = station.shortBrand,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (station.isOpen24Hours) {
                            val isDark = isSystemInDarkTheme()
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color(0xFF1E3A5F) else Color(0xFFE3F2FD)
                            ) {
                                Text(
                                    text = "24H",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0D47A1),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${station.address}, ${station.municipality}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Heart favorite button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (station.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (station.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-row: Distance badge & Cheapest badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = station.formattedDistance,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val showCheapest = isCheapestInArea || cheapestForFuels.isNotEmpty()
                if (showCheapest) {
                    val fuelsLabel = if (cheapestForFuels.isNotEmpty()) {
                        "🏆 " + cheapestForFuels.joinToString(", ") { fuel ->
                            when (fuel) {
                                FuelType.GASOLEO_A -> "Diésel"
                                FuelType.GASOLINA_95_E5 -> "Gas 95"
                                FuelType.GLP -> "GLP"
                                else -> fuel.displayName
                            }
                        }
                    } else {
                        "🏆 ¡MÁS BARATA!"
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CheapGreenContainer,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = fuelsLabel,
                            color = CheapGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fuel Prices Row for selected fuels (up to 3)
            val fuelsToShow = if (visibleFuels.isNotEmpty()) {
                visibleFuels.filter { selectedFuels.contains(it) }.take(3)
            } else {
                FuelType.entries.filter { selectedFuels.contains(it) }.take(3)
            }

            if (fuelsToShow.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fuelsToShow.forEach { fuel ->
                        val p = station.priceFor(fuel)
                        val pText = if (p != null) String.format(java.util.Locale.US, "%.3f €", p) else "—"
                        val label = when (fuel) {
                            FuelType.GASOLEO_A -> "Diésel"
                            FuelType.GASOLINA_95_E5 -> "Gas 95"
                            FuelType.GLP -> "GLP"
                            FuelType.GASOLINA_98_E5 -> "Gas 98"
                            FuelType.GASOLEO_PREMIUM -> "Diésel+"
                            FuelType.GASOLEO_B -> "Gasóleo B"
                            FuelType.ADBLUE -> "AdBlue"
                            FuelType.GNC -> "GNC"
                            FuelType.GNL -> "GNL"
                            FuelType.BIODIESEL -> "Bio"
                            FuelType.DIESEL_RENOVABLE -> "HVO"
                            FuelType.HIDROGENO -> "H2"
                        }
                        val isGLP = fuel == FuelType.GLP
                        val isCheapestThisFuel = cheapestForFuels.contains(fuel)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isCheapestThisFuel -> Color(0xFFC8E6C9)
                                isGLP -> Color(0xFFE0F2F1)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when {
                                        isCheapestThisFuel -> "🏆 $label"
                                        isGLP -> "🟢 $label"
                                        else -> label
                                    },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = when {
                                        isCheapestThisFuel -> Color(0xFF1B5E20)
                                        isGLP -> Color(0xFF004D40)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = pText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    color = when {
                                        isCheapestThisFuel -> Color(0xFF2E7D32)
                                        isGLP -> Color(0xFF00695C)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Destacado especial si tiene GLP pero GLP no estaba entre los seleccionados
            if (station.hasGLP && !selectedFuels.contains(FuelType.GLP)) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE0F2F1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🟢 GLP / Autogas disponible:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C)
                        )
                        Text(
                            text = station.formattedGlpPrice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Schedule + "Cómo llegar" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = station.schedule,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onNavigateClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ir",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getBrandColor(brand: String): Color {
    val b = brand.uppercase()
    return when {
        b.contains("REPSOL") -> Color(0xFFF05023)
        b.contains("CEPSA") -> Color(0xFFD61A22)
        b.contains("BP") -> Color(0xFF009900)
        b.contains("GALP") -> Color(0xFFFF6600)
        b.contains("SHELL") -> Color(0xFFFBCE07)
        b.contains("PLENOIL") -> Color(0xFF005691)
        b.contains("BALLENOIL") -> Color(0xFF0088CC)
        b.contains("PETROPRIX") -> Color(0xFF00A651)
        b.contains("AVIA") -> Color(0xFFE20613)
        b.contains("CARREFOUR") -> Color(0xFF00387B)
        b.contains("ALCAMPO") -> Color(0xFFE2001A)
        else -> Color(0xFF546E7A)
    }
}
