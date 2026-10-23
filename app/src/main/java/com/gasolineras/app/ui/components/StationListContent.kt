package com.gasolineras.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.ui.home.HomeUiState
import com.gasolineras.app.ui.home.HomeViewModel

/** One scrolling surface keeps stations reachable in landscape and with large text. */
@Composable
fun StationListContent(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onLocationAction: () -> Unit,
    onNavigate: (GasStation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item("location") { HomeStatus(state, onLocationAction, { viewModel.onRefresh() }, showPriceInfo = false) }
        item("fuels") { FuelTypeSelector(state.selectedFuels, viewModel::onToggleFuel, state.visibleFuels) }
        item("sorting") {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(if (state.isLoading) "Buscando gasolineras…" else if (state.stations.size == 1) "1 encontrada" else "${state.stations.size} encontradas",
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                TextButton(onClick = viewModel::toggleSort) {
                    Text(if (state.selectedSort == SortOption.CHEAPEST) "Orden: precio de ${state.comparisonFuel.displayName}" else "Orden: distancia en línea recta")
                }
                Text("Comparar precio de:", style = MaterialTheme.typography.labelMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.visibleFuels.filter { it in state.selectedFuels }.forEach { fuel ->
                        FilterChip(selected = state.comparisonFuel == fuel,
                            onClick = { viewModel.onComparisonFuelSelected(fuel) },
                            label = { Text(fuel.displayName) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                }
            }
        }
        if (state.isLoading) {
            item("loading") { CircularProgressIndicator(Modifier.padding(16.dp)) }
        } else if (state.errorMessage != null) {
            item("error") {
                Column(Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text(state.errorMessage)
                    Button(onClick = { viewModel.onRefresh() }) { Text("Reintentar") }
                }
            }
        } else if (state.stations.isEmpty()) {
            item("empty") {
                Column(Modifier.padding(16.dp)) {
                    Text(if (state.onlyFavorites) "No hay favoritas que coincidan con estos filtros" else "No se encontraron gasolineras")
                    Text("Prueba a ampliar el radio o cambiar los carburantes. Los seleccionados deben estar disponibles en la misma gasolinera.")
                    TextButton(onClick = { viewModel.cycleRadius() }) { Text("Ampliar o cambiar radio") }
                    if (state.onlyFavorites) TextButton(onClick = viewModel::onToggleOnlyFavorites) { Text("Ver todas las gasolineras") }
                }
            }
        } else {
            itemsIndexed(state.stations, key = { index, station -> "${station.id}_$index" }) { _, station ->
                val cheapest = state.cheapestFuelsFor(station)
                Box(Modifier.padding(horizontal = 16.dp)) {
                    StationCard(station = station, selectedFuels = state.selectedFuels,
                        visibleFuels = state.visibleFuels, isCheapestInArea = cheapest.isNotEmpty(),
                        cheapestForFuels = cheapest, onClick = { viewModel.onSelectStation(station) },
                        onNavigateClick = { onNavigate(station) },
                        onToggleFavorite = { viewModel.onToggleFavorite(station.id) })
                }
            }
        }
        item("ministry") { HomeStatus(state, onLocationAction, { viewModel.onRefresh() }, showLocationInfo = false) }
    }
}
