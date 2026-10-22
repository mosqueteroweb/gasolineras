package com.gasolineras.app.ui.home

import androidx.activity.compose.BackHandler
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
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
    var mapVisibleCount by remember { mutableIntStateOf(0) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showMapLegend by remember { mutableStateOf(false) }

    BackHandler(enabled = !showSettingsSheet && state.selectedStationForDetail == null &&
        (showMapLegend || isSearchExpanded || showOverflowMenu)) {
        when {
            showOverflowMenu -> showOverflowMenu = false
            showMapLegend -> showMapLegend = false
            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.onSearchQueryChanged("")
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing)
        ),
        label = "refresh_angle"
    )

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
                                                    modifier = Modifier.rotate(if (state.isRefreshing) refreshRotation else 0f)
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
                HomeStatus(
                    state = state,
                    onLocationAction = onRequestLocationPermission,
                    onRetry = { viewModel.onRefresh() }
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
                    OsmMapView(
                        userLocation = state.userLocation,
                        stations = state.stations,
                        selectedFuels = state.selectedFuels,
                        minPricePerFuel = state.minPricePerFuel,
                        selectedRadiusKm = state.selectedRadiusKm,
                        onSelectStation = { viewModel.onSelectStation(it) },
                        onVisibleCountChanged = { count -> mapVisibleCount = count },
                        centerTrigger = mapCenterTrigger,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Floating solid fuel selector at top center
                    FuelTypeSelector(
                        selectedFuels = state.selectedFuels,
                        visibleFuels = state.visibleFuels,
                        onToggleFuel = { viewModel.onToggleFuel(it) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )

                    // Floating Legend toggle pill at bottom-start
                    if (!showMapLegend) {
                        Surface(
                            onClick = { showMapLegend = true },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 14.dp, bottom = 114.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Ver leyenda de iconos",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Leyenda",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Floating station count pill at bottom-start (raised above zoom controls)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 14.dp, bottom = 72.dp)
                    ) {
                        Text(
                            text = if (mapVisibleCount > 0) "$mapVisibleCount gasolineras en esta zona" else "0 gasolineras en esta zona",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
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
                            if (!state.hasLocationPermission || !state.isLocationEnabled) {
                                onRequestLocationPermission()
                            } else viewModel.recenterLocation { mapCenterTrigger++ }
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
                }
            } else {
                // ==================== LIST MODE ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. 3 Fuel Carousel
                    FuelTypeSelector(
                        selectedFuels = state.selectedFuels,
                        visibleFuels = state.visibleFuels,
                        onToggleFuel = { viewModel.onToggleFuel(it) }
                    )

                    // 2. Status Bar: Station count & Sort toggle button
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

                        // Clear Sort button (Accessible min-height 42dp, High contrast)
                        val isDark = when (state.themeMode) { "DARK" -> true; "LIGHT" -> false; else -> isSystemInDarkTheme() }
                        val sortBg = if (isDark) Color(0xFF1E3A5F) else Color(0xFFE3F2FD)
                        val sortBorder = if (isDark) Color(0xFF3B82F6) else Color(0xFF90CAF9)
                        val sortContent = if (isDark) Color.White else Color(0xFF0D47A1)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = sortBg,
                            border = BorderStroke(1.dp, sortBorder),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 42.dp)
                                .clickable { viewModel.toggleSort() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = sortContent
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (state.selectedSort == SortOption.CHEAPEST) "💶 Más barata" else "📍 Más cercana",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = sortContent
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
                                        text = when {
                                            state.onlyFavorites && state.favoriteIds.isEmpty() -> "No tienes favoritas guardadas"
                                            state.onlyFavorites -> "No hay favoritas que coincidan con estos filtros"
                                            else -> "No se encontraron gasolineras"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = when {
                                            state.onlyFavorites && state.favoriteIds.isEmpty() -> "Pulsa el corazón en una gasolinera para guardarla."
                                            state.onlyFavorites -> "Tus favoritas siguen guardadas. Prueba a ampliar el radio de ${state.selectedRadiusKm.toInt()} km o cambiar los filtros."
                                            else -> "Prueba a ampliar el radio de distancia o verificar los carburantes marcados."
                                        },
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
                                            onClick = { viewModel.cycleRadius() },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Cambiar radio de búsqueda")
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
                                        val cheapestFuels = state.cheapestFuelsFor(station)
                                        val isCheapest = cheapestFuels.isNotEmpty()

                                        StationCard(
                                            station = station,
                                            selectedFuels = state.selectedFuels,
                                            visibleFuels = state.visibleFuels,
                                            isCheapestInArea = isCheapest,
                                            cheapestForFuels = cheapestFuels,
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
            onDismissRequest = { showSettingsSheet = false }
        )
    }
}
