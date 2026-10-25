package com.gasolineras.app.ui.home

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.BackHandler
import com.gasolineras.app.ui.components.MapInformationControl
import com.gasolineras.app.ui.components.StationListContent
import com.gasolineras.app.ui.components.LocalityDialog
import com.gasolineras.app.ui.components.HomeStatus
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import com.gasolineras.app.ui.components.SettingsBottomSheet
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Info
import com.gasolineras.app.ui.components.FuelTypeSelector
import com.gasolineras.app.ui.components.MapLegendCard
import com.gasolineras.app.ui.components.NavigationHelper
import com.gasolineras.app.ui.components.RadiusFilterBar
import com.gasolineras.app.ui.components.StationCard
import com.gasolineras.app.ui.components.StationDetailDialog
import com.gasolineras.app.ui.map.StationMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var mapCenterTrigger by remember { mutableIntStateOf(0) }
    var mapVisibleCount by remember { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showMapLegend by remember { mutableStateOf(false) }
    var mapCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showLocalities by remember { mutableStateOf(false) }
    var showMapInfo by remember { mutableStateOf(false) }
    var showAuthorship by remember { mutableStateOf(false) }
    val requestGpsCenter = {
        mapCenterTrigger++
        viewModel.recenterLocation()
        if (!state.hasLocationPermission || !state.isLocationEnabled) onRequestLocationPermission()
    }

    BackHandler(enabled = !showSettingsSheet && state.selectedStationForDetail == null &&
        (showMapInfo || showMapLegend || isSearchExpanded || showOverflowMenu)) {
        when {
            showMapInfo -> showMapInfo = false
            showOverflowMenu -> showOverflowMenu = false
            showMapLegend -> showMapLegend = false
            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.onSearchQueryChanged("")
            }
        }
    }


    Scaffold(
        topBar = {
            Column {
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
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gass",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    softWrap = false
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Toggle Lista / Mapa (Accesible 48dp touch target, 24dp icon)
                                IconButton(
                                    onClick = {
                                        showMapInfo = false
                                        val willBeMap = !state.isMapView
                                        if (willBeMap) mapCenterTrigger++
                                        viewModel.setMapView(willBeMap)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.isMapView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.Map,
                                        contentDescription = if (state.isMapView) "Ver lista" else "Ver mapa",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // 2. Selector de Radio (Botón cápsula con icono y texto claro "10km")
                                Surface(
                                    onClick = {
                                        val nextRadius = viewModel.cycleRadius()
                                        Toast.makeText(context, "Radio: ${nextRadius.toInt()} km", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .defaultMinSize(minHeight = 36.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = "Cambiar radio de distancia (actual: ${state.selectedRadiusKm.toInt()} km)",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${state.selectedRadiusKm.toInt()}km",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                // 3. Favoritos (Accesible 48dp touch target, 24dp icon)
                                IconButton(
                                    onClick = {
                                        viewModel.onToggleOnlyFavorites {
                                            Toast.makeText(
                                                context,
                                                "Aún no tienes gasolineras favoritas. Toca el corazón ❤️ en cualquier gasolinera para añadirla.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (state.onlyFavorites) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = if (state.onlyFavorites) "Ver todas" else "Ver solo favoritas",
                                        tint = if (state.onlyFavorites) Color(0xFFFF5252) else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // 4. Menú de 3 puntos (Overflow Menu) (Accesible 48dp touch target, 24dp icon)
                                Box {
                                    IconButton(onClick = { showOverflowMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Más opciones",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { showOverflowMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Actualizar precios") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                viewModel.onRefresh()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Buscar gasolinera") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                isSearchExpanded = true
                                            }
                                        )
                                        DropdownMenuItem(
                                        text = { Text("Elegir municipio") },
                                        onClick = { showOverflowMenu = false; showLocalities = true; viewModel.loadLocalities() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Usar mi ubicación GPS") },
                                        onClick = {
                                            showOverflowMenu = false
                                            requestGpsCenter()
                                        }
                                    )
                                    DropdownMenuItem(
                                            text = { Text("Ajustes y preferencias") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                showSettingsSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Autoría") },
                                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                            onClick = { showOverflowMenu = false; showAuthorship = true }
                                        )
                                        if (state.isMapView) {
                                            DropdownMenuItem(
                                                text = { Text("Leyenda del mapa") },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null
                                                    )
                                                },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    showMapInfo = false
                                                    showMapLegend = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
                if (state.isLoading || state.isRefreshing || state.isLocating) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }
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
                    StationMapView(
                        userLocation = state.userLocation,
                        stations = state.stations,
                        selectedFuels = state.selectedFuels,
                        minPricePerFuel = state.minPricePerFuel,
                        selectedRadiusKm = state.selectedRadiusKm,
                        onSelectStation = { viewModel.onSelectStation(it) },
                        onVisibleCountChanged = { count -> mapVisibleCount = count },
                        centerTrigger = mapCenterTrigger,
                        referenceName = state.manualLocationName,
                        comparisonFuel = state.comparisonFuel,
                        onMapCenterChanged = { lat, lon -> mapCenter = lat to lon },
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(modifier = Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.surface) {
                        TextButton(onClick = { NavigationHelper.openLink(context, "https://www.openstreetmap.org/copyright") }) {
                            Text("© OpenStreetMap contributors", fontSize = 10.sp)
                        }
                    }

                    // Keep the zone action below the fuel row, aligned with GLP.
                    Column(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FuelTypeSelector(
                            selectedFuels = state.selectedFuels,
                            visibleFuels = state.visibleFuels,
                            onToggleFuel = { viewModel.onToggleFuel(it) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary,
                                shadowElevation = 4.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        mapCenter?.let { (lat, lon) ->
                                            viewModel.useManualLocation(lat, lon, "Zona del mapa")
                                        }
                                    },
                                    enabled = mapCenter != null,
                                    modifier = Modifier.size(48.dp).semantics {
                                        contentDescription = "Buscar en esta zona"
                                    }
                                ) {
                                    Box(modifier = Modifier.size(32.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.TopStart).size(28.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                                .size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating Legend Card when expanded
                    if (showMapLegend) {
                        MapLegendCard(
                            radiusKm = state.selectedRadiusKm,
                            onDismiss = { showMapLegend = false },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 14.dp, end = 14.dp, bottom = 72.dp)
                                .widthIn(max = 340.dp)
                        )
                    }

                    // Floating GPS recenter button at bottom-end (ergonomic 52dp)
                    FloatingActionButton(
                        onClick = {
                            requestGpsCenter()
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                            .size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Centrar en mi posición",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Floating empty state banner in map mode (when current map viewport has no stations)
                    if (mapVisibleCount == 0 && !state.isLoading) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = when {
                                        state.errorMessage != null -> state.errorMessage.orEmpty()
                                        state.onlyFavorites -> "No hay favoritas que coincidan con los filtros en esta zona"
                                        state.stations.isEmpty() -> "No hay gasolineras en un radio de ${state.selectedRadiusKm.toInt()} km"
                                        else -> "No hay gasolineras en este encuadre"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        when {
                                            state.errorMessage != null -> viewModel.onRefresh()
                                            state.stations.isEmpty() -> viewModel.cycleRadius()
                                            !state.hasLocationPermission || !state.isLocationEnabled -> onRequestLocationPermission()
                                            else -> viewModel.recenterLocation { mapCenterTrigger++ }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = when {
                                            state.errorMessage != null -> "Reintentar"
                                            state.stations.isEmpty() -> "Cambiar radio de búsqueda"
                                            else -> "Centrar en mi ubicación"
                                        },
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    MapInformationControl(
                        state = state,
                        expanded = showMapInfo,
                        onExpandedChange = { expanded ->
                            showMapInfo = expanded
                            if (expanded) showMapLegend = false
                        },
                        onLocationAction = onRequestLocationPermission,
                        onRetry = { viewModel.onRefresh() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                StationListContent(state, viewModel, onRequestLocationPermission,
                    onNavigate = { NavigationHelper.navigateToStation(context, it) })
            }
        }
    }

    if (showAuthorship) com.gasolineras.app.ui.components.AuthorshipDialog { showAuthorship = false }

    if (showLocalities) LocalityDialog(state, viewModel) { showLocalities = false }

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

    // Settings & Preferences BottomSheet
    if (showSettingsSheet) {
        SettingsBottomSheet(
            visibleFuels = state.visibleFuels,
            defaultSelectedFuels = viewModel.getDefaultSelectedFuelsPreference(),
            defaultView = viewModel.getDefaultViewPreference(),
            defaultRadiusKm = viewModel.getDefaultRadiusPreference(),
            defaultSort = viewModel.getDefaultSortPreference(),
            themeMode = state.themeMode,
            onSavePreferences = { vis, sel, view, rad, sort, theme ->
                viewModel.updatePreferences(vis, sel, view, rad, sort, theme)
            },
            onRefreshPrices = {
                viewModel.onRefresh()
            },
            onClearData = viewModel::clearPrivateData,
            rememberLocation = state.rememberLocation,
            onDismissRequest = { showSettingsSheet = false }
        )
    }
}
