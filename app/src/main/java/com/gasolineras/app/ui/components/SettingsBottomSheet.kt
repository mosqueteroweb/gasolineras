package com.gasolineras.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsBottomSheet(
    visibleFuels: List<FuelType>,
    defaultSelectedFuels: Set<FuelType>,
    defaultView: String,
    defaultRadiusKm: Double,
    defaultSort: SortOption,
    themeMode: String,
    onSavePreferences: (
        visibleFuels: List<FuelType>,
        defaultSelectedFuels: Set<FuelType>,
        defaultView: String,
        defaultRadiusKm: Double,
        defaultSort: SortOption,
        themeMode: String
    ) -> Unit,
    onClearCache: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tempVisibleFuels by remember { mutableStateOf(visibleFuels.take(3)) }
    var tempDefaultSelectedFuels by remember {
        mutableStateOf(
            defaultSelectedFuels.filter { visibleFuels.contains(it) }.take(3).toSet().ifEmpty {
                setOf(visibleFuels.firstOrNull() ?: FuelType.GASOLEO_A)
            }
        )
    }
    var tempDefaultView by remember { mutableStateOf(defaultView) }
    var tempDefaultRadius by remember { mutableStateOf(defaultRadiusKm) }
    var tempDefaultSort by remember { mutableStateOf(defaultSort) }
    var tempThemeMode by remember { mutableStateOf(themeMode) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Ajustes y Preferencias",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personaliza carburantes y comportamiento",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Carburantes visibles en pantalla (máximo 3)
            Text(
                text = "⛽ Carburantes en pantalla (Máximo 3)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona hasta 3 carburantes para la pantalla principal (${tempVisibleFuels.size}/3 seleccionados):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FuelType.entries.forEach { fuel ->
                    val isChecked = tempVisibleFuels.contains(fuel)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) {
                                if (tempVisibleFuels.size > 1) {
                                    val nextList = tempVisibleFuels - fuel
                                    tempVisibleFuels = nextList
                                    tempDefaultSelectedFuels = tempDefaultSelectedFuels - fuel
                                    if (tempDefaultSelectedFuels.isEmpty()) {
                                        tempDefaultSelectedFuels = setOf(nextList.first())
                                    }
                                } else {
                                    Toast.makeText(context, "Debes mantener al menos 1 carburante", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (tempVisibleFuels.size < 3) {
                                    val nextList = tempVisibleFuels + fuel
                                    tempVisibleFuels = nextList
                                    tempDefaultSelectedFuels = tempDefaultSelectedFuels + fuel
                                } else {
                                    Toast.makeText(context, "Máximo 3 carburantes en pantalla para asegurar la legibilidad", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        label = {
                            Text(
                                text = fuel.displayName,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Carburante(s) activos al abrir la app (máximo 3)
            Text(
                text = "🎯 Carburante(s) marcados al iniciar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "De los elegidos arriba, ¿cuáles quieres que aparezcan marcados al abrir la app?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tempVisibleFuels.forEach { fuel ->
                    val isChecked = tempDefaultSelectedFuels.contains(fuel)
                    FilterChip(
                        selected = isChecked,
                        onClick = {
                            if (isChecked) {
                                if (tempDefaultSelectedFuels.size > 1) {
                                    tempDefaultSelectedFuels = tempDefaultSelectedFuels - fuel
                                } else {
                                    Toast.makeText(context, "Debe quedar al menos 1 carburante marcado", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (tempDefaultSelectedFuels.size < 3) {
                                    tempDefaultSelectedFuels = tempDefaultSelectedFuels + fuel
                                }
                            }
                        },
                        label = {
                            Text(
                                text = fuel.shortName,
                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // 3. Vista y Búsqueda por defecto
            Text(
                text = "🗺️ Vista y Búsqueda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Vista inicial: Lista o Mapa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { tempDefaultView = "LIST" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tempDefaultView == "LIST") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (tempDefaultView == "LIST") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lista",
                        fontWeight = if (tempDefaultView == "LIST") FontWeight.Bold else FontWeight.Normal,
                        color = if (tempDefaultView == "LIST") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { tempDefaultView = "MAP" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tempDefaultView == "MAP") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (tempDefaultView == "MAP") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mapa",
                        fontWeight = if (tempDefaultView == "MAP") FontWeight.Bold else FontWeight.Normal,
                        color = if (tempDefaultView == "MAP") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radio inicial (Modo lista)
            Text(
                text = "Radio inicial de búsqueda (Modo lista):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3.0, 10.0, 25.0, 100.0).forEach { radius ->
                    val isSelected = tempDefaultRadius == radius
                    FilterChip(
                        selected = isSelected,
                        onClick = { tempDefaultRadius = radius },
                        label = { Text("${radius.toInt()} km", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ordenación inicial
            Text(
                text = "Criterio de ordenación inicial:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { tempDefaultSort = SortOption.CHEAPEST },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tempDefaultSort == SortOption.CHEAPEST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (tempDefaultSort == SortOption.CHEAPEST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "💶 Más barata",
                        fontWeight = if (tempDefaultSort == SortOption.CHEAPEST) FontWeight.Bold else FontWeight.Normal,
                        color = if (tempDefaultSort == SortOption.CHEAPEST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { tempDefaultSort = SortOption.NEAREST },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tempDefaultSort == SortOption.NEAREST) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (tempDefaultSort == SortOption.NEAREST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "📍 Más cercana",
                        fontWeight = if (tempDefaultSort == SortOption.NEAREST) FontWeight.Bold else FontWeight.Normal,
                        color = if (tempDefaultSort == SortOption.NEAREST) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // 4. Tema de la aplicación
            Text(
                text = "🎨 Tema de la aplicación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("SYSTEM" to "⚙️ Sistema", "LIGHT" to "☀️ Claro", "DARK" to "🌙 Oscuro").forEach { (mode, label) ->
                    val isSelected = tempThemeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { tempThemeMode = mode },
                        label = { Text(label, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Mantenimiento y Caché
            Text(
                text = "ℹ️ Mantenimiento y Datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    onClearCache()
                    Toast.makeText(context, "Caché vaciada y datos recargados del Ministerio", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vaciar caché y forzar recarga oficial")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Gass v2.1.0 • Código Libre (GPLv3) • Datos oficiales del Ministerio MITECO",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar
            Button(
                onClick = {
                    onSavePreferences(
                        tempVisibleFuels,
                        tempDefaultSelectedFuels,
                        tempDefaultView,
                        tempDefaultRadius,
                        tempDefaultSort,
                        tempThemeMode
                    )
                    Toast.makeText(context, "Preferencias guardadas correctamente", Toast.LENGTH_SHORT).show()
                    onDismissRequest()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Preferencias", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
