package com.gasolineras.app.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.ui.components.FuelTypeSelector
import com.gasolineras.app.ui.components.NavigationHelper
import com.gasolineras.app.ui.components.RadiusFilterBar
import com.gasolineras.app.ui.components.StationCard
import com.gasolineras.app.ui.components.StationDetailDialog
import com.gasolineras.app.ui.map.OsmMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var mapCenterTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = {
                                Text(
                                    "Buscar marca o localidad...",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Gasolineras",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isSearchExpanded) {
                        IconButton(onClick = {
                            isSearchExpanded = false
                            viewModel.onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cerrar búsqueda",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (isSearchExpanded) {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar texto",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    } else {
                        // 1. Favorites Heart Button in TopBar
                        IconButton(onClick = {
                            viewModel.onToggleOnlyFavorites {
                                Toast.makeText(
                                    context,
                                    "Aún no tienes gasolineras favoritas. Toca el corazón ❤️ en cualquier gasolinera para añadirla.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (state.onlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (state.onlyFavorites) "Ver todas" else "Ver favoritas",
                                tint = if (state.onlyFavorites) Color(0xFFFF5252) else MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // 2. Sort button in TopBar
                        IconButton(onClick = {
                            viewModel.toggleSort()
                            val nextSortName = if (state.selectedSort == SortOption.CHEAPEST) "más cercana" else "más barata"
                            Toast.makeText(context, "Ordenando por $nextSortName", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Cambiar orden (precio / distancia)",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // 3. Search Lupa icon button
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar gasolinera",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // 4. Refresh button
                        IconButton(onClick = { viewModel.onRefresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar precios",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = !state.isMapView,
                    onClick = { viewModel.setMapView(false) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Ver lista"
                        )
                    },
                    label = {
                        Text(
                            text = "Lista",
                            fontWeight = if (!state.isMapView) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                NavigationBarItem(
                    selected = state.isMapView,
                    onClick = { viewModel.setMapView(true) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Ver mapa"
                        )
                    },
                    label = {
                        Text(
                            text = "Mapa",
                            fontWeight = if (state.isMapView) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isMapView) {
                // ==================== MAP MODE: 100% CLEAN FULL SCREEN ====================
                Box(modifier = Modifier.fillMaxSize()) {
                    OsmMapView(
                        userLocation = state.userLocation,
                        stations = state.stations,
                        selectedFuels = state.selectedFuels,
                        onSelectStation = { viewModel.onSelectStation(it) },
                        centerTrigger = mapCenterTrigger,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Lightweight floating fuel chips at top center
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val fuels = listOf(
                            FuelType.GASOLEO_A to "⛽ Diésel",
                            FuelType.GASOLINA_95_E5 to "⛽ Gas 95",
                            FuelType.GLP to "🟢 GLP"
                        )
                        fuels.forEach { (fuel, label) ->
                            val isSelected = state.selectedFuels.contains(fuel)
                            val isGLP = fuel == FuelType.GLP
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onToggleFuel(fuel) },
                                label = {
                                    Text(
                                        text = if (isSelected) "✓ $label" else label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = if (isGLP) {
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00695C),
                                        selectedLabelColor = Color.White
                                    )
                                } else {
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }
                    }

                    // Floating station count pill at bottom-start
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 14.dp)
                    ) {
                        Text(
                            text = "${state.stations.size} gasolineras",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Floating GPS recenter button at bottom-end
                    FloatingActionButton(
                        onClick = { mapCenterTrigger++ },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 14.dp, bottom = 14.dp)
                            .size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Centrar en mi posición",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                // ==================== LIST MODE ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Location Permission Warning Banner (if not granted)
                    if (!state.hasLocationPermission) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRequestLocationPermission() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pulsa aquí para activar el GPS y calcular distancias reales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Active Favorites Filter Banner
                    if (state.onlyFavorites) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "❤️ Mostrando solo tus ${state.stations.size} favoritas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                TextButton(onClick = { viewModel.onToggleOnlyFavorites() }) {
                                    Text("Ver todas", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 1. 3 Fuel Carousel (Diésel, Gas 95, GLP) - All checked by default
                    FuelTypeSelector(
                        selectedFuels = state.selectedFuels,
                        onToggleFuel = { viewModel.onToggleFuel(it) }
                    )

                    // 2. Distance Radius Filter (fits screen)
                    RadiusFilterBar(
                        selectedRadiusKm = state.selectedRadiusKm,
                        onRadiusSelected = { viewModel.onRadiusSelected(it) }
                    )

                    // 3. Status Bar: Station count & Sort toggle button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.stations.size} encontradas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        // Clear Sort button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { viewModel.toggleSort() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.selectedSort == SortOption.CHEAPEST) "💶 Más barata" else "📍 Más cercana",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 4. List / State content area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when {
                            state.isLoading -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Consultando precios oficiales del Ministerio...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            state.errorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = state.errorMessage ?: "",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { viewModel.onRefresh() },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }

                            state.stations.isEmpty() -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (state.onlyFavorites) "No tienes favoritas guardadas" else "No se encontraron gasolineras",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (state.onlyFavorites) "Pulsa el corazón ❤️ en cualquier gasolinera para añadirla aquí." else "Prueba a ampliar el radio de distancia o verificar los carburantes marcados.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    if (state.onlyFavorites) {
                                        Button(
                                            onClick = { viewModel.onToggleOnlyFavorites() },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Ver todas las gasolineras")
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.onRadiusSelected(25.0) },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Ampliar radio a 25 km")
                                        }
                                    }
                                }
                            }

                            else -> {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(
                                        items = state.stations,
                                        key = { index, station -> "${station.id}_$index" }
                                    ) { _, station ->
                                        val candidatePrices = if (state.selectedFuels.isEmpty()) {
                                            station.prices.values
                                        } else {
                                            state.selectedFuels.mapNotNull { station.prices[it] }
                                        }
                                        val stationBestPrice = candidatePrices.minOrNull()
                                        val isCheapest = state.minPrice != null &&
                                                stationBestPrice != null &&
                                                stationBestPrice == state.minPrice

                                        StationCard(
                                            station = station,
                                            selectedFuels = state.selectedFuels,
                                            isCheapestInArea = isCheapest,
                                            onClick = { viewModel.onSelectStation(station) },
                                            onNavigateClick = {
                                                NavigationHelper.navigateToStation(context, station)
                                            },
                                            onToggleFavorite = {
                                                viewModel.onToggleFavorite(station.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail dialog popup (works for both Map and List modes)
    state.selectedStationForDetail?.let { station ->
        StationDetailDialog(
            station = station,
            onDismiss = { viewModel.onSelectStation(null) },
            onNavigate = {
                viewModel.onSelectStation(null)
                NavigationHelper.navigateToStation(context, station)
            },
            onToggleFavorite = {
                viewModel.onToggleFavorite(station.id)
            }
        )
    }
}
