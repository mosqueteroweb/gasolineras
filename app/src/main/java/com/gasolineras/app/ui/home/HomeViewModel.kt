package com.gasolineras.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gasolineras.app.data.local.FavoritesManager
import com.gasolineras.app.data.local.LastLocationStore
import com.gasolineras.app.data.local.StationCache
import com.gasolineras.app.data.location.LocationProvider
import com.gasolineras.app.domain.util.DistanceCalculator
import java.io.File
import kotlinx.coroutines.isActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import com.gasolineras.app.data.location.LocationClient
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.data.repository.FuelRepository
import com.gasolineras.app.data.repository.FuelRepositoryImpl
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.data.local.UserPreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: FuelRepository = FuelRepositoryImpl(
        diskCache = StationCache(File(application.filesDir, "stations-v1.json"))
    ),
    private val locationClient: LocationProvider = LocationClient(application)
) : AndroidViewModel(application) {

    private val lastLocationStore = LastLocationStore(application)
    private val favoritesManager = FavoritesManager(application)
    private val preferencesManager = UserPreferencesManager(application)

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userLocation = lastLocationStore.read(),
            rememberLocation = lastLocationStore.isEnabled(),
            comparisonFuel = preferencesManager.getComparisonFuel(),
            favoriteIds = favoritesManager.getFavoriteIds(),
            visibleFuels = preferencesManager.getVisibleFuels(),
            selectedFuels = preferencesManager.getDefaultSelectedFuels(),
            isMapView = preferencesManager.getDefaultView() == "MAP",
            selectedRadiusKm = preferencesManager.getDefaultRadiusKm(),
            selectedSort = preferencesManager.getDefaultSort(),
            themeMode = preferencesManager.getThemeMode()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var latestGpsLocation = _uiState.value.userLocation

    private var searchDebounceJob: Job? = null
    private var fetchJob: Job? = null
    private var fetchRevision = 0L
    private var locationJob: Job? = null
    private var locationRevision = 0L
    private var acceptedLocationRevision = 0L
    private var trackingJob: Job? = null
    private var statusJob: Job? = null
    private var foreground = false

    init {
        updateLocationStatus()
        fetchStations(forceRefresh = false)
    }

    fun onForeground() {
        if (foreground) return
        foreground = true
        updateLocationStatus()
        if (_uiState.value.savedAtMillis > 0L || _uiState.value.errorMessage != null) {
            fetchStations(forceRefresh = false)
        }
        restartTracking()
        locate(forceFresh = true)
        statusJob = viewModelScope.launch {
            while (isActive) {
                delay(2_000L)
                val before = _uiState.value
                updateLocationStatus()
                val after = _uiState.value
                if (before.hasLocationPermission != after.hasLocationPermission ||
                    before.isLocationEnabled != after.isLocationEnabled) {
                    restartTracking()
                    locate(forceFresh = true)
                }
            }
        }
    }

    fun onBackground() {
        foreground = false
        statusJob?.cancel()
        trackingJob?.cancel()
        locationRevision++
        locationJob?.cancel()
        _uiState.update { it.copy(isLocating = false) }
    }

    private fun updateLocationStatus() {
        _uiState.update { it.copy(
            hasLocationPermission = locationClient.hasLocationPermission(),
            isLocationEnabled = locationClient.isLocationEnabled()
        ) }
    }

    private fun restartTracking() {
        trackingJob?.cancel()
        if (_uiState.value.manualLocationName != null || !foreground || !locationClient.hasLocationPermission() || !locationClient.isLocationEnabled()) return
        trackingJob = viewModelScope.launch {
            locationClient.observeLocations().catch { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(locationMessage = "No se puede seguir tu ubicación. Pulsa para reintentar.") }
            }.collect { acceptLocation(it) }
        }
    }

    private fun acceptLocation(location: UserLocation) {
        if (location.isDefaultLocation || _uiState.value.manualLocationName != null) return
        val previous = _uiState.value.userLocation
        if (location.obtainedAtMillis < previous.obtainedAtMillis) return
        val moved = previous.isDefaultLocation || DistanceCalculator.calculateDistanceMeters(
            previous.latitude, previous.longitude, location.latitude, location.longitude
        ) >= 100.0
        latestGpsLocation = location
        acceptedLocationRevision++
        lastLocationStore.write(location)
        _uiState.update { it.copy(
            userLocation = if (moved) location else previous.copy(obtainedAtMillis = location.obtainedAtMillis),
            locationMessage = null
        ) }
        if (moved) fetchStations(forceRefresh = false)
    }

    private suspend fun acquireLocation(forceFresh: Boolean, revision: Long): UserLocation? {
        updateLocationStatus()
        _uiState.update { it.copy(isLocating = true) }
        val acceptanceBeforeRequest = acceptedLocationRevision
        return try {
            val location = locationClient.getCurrentLocation(forceFresh)
                ?: _uiState.value.userLocation.takeIf { !it.isDefaultLocation && acceptedLocationRevision > acceptanceBeforeRequest }
            if (revision != locationRevision) return null
            if (location != null && !location.isDefaultLocation) {
                acceptLocation(location)
                location
            } else {
                _uiState.update { it.copy(locationMessage =
                    "No se ha podido obtener tu ubicación. Se conserva la última posición disponible.") }
                null
            }
        } finally {
            if (revision == locationRevision) _uiState.update { it.copy(isLocating = false) }
        }
    }

    private fun locate(forceFresh: Boolean, onSuccess: (UserLocation) -> Unit = {}) {
        if (_uiState.value.manualLocationName != null) return
        val revision = ++locationRevision
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            acquireLocation(forceFresh, revision)?.let(onSuccess)
        }
    }

    fun onLocationPermissionGranted() {
        updateLocationStatus()
        restartTracking()
        locate(forceFresh = true)
    }

    /**
     * Toggles one of the 3 main fuels (Diésel, Gasolina 95, GLP).
     * Ensures at least one fuel remains selected.
     */
    fun onToggleFuel(fuel: FuelType) {
        if (fuel !in _uiState.value.visibleFuels) return
        val current = _uiState.value.selectedFuels
        val next = if (current.contains(fuel)) {
            if (current.size > 1) current - fuel else current
        } else {
            if (current.size < 3) current + fuel else current
        }
        _uiState.update { it.copy(selectedFuels = next, comparisonFuel = it.comparisonFuel.takeIf { fuel -> fuel in next } ?: next.first()) }
        fetchStations(forceRefresh = false)
    }

    fun onComparisonFuelSelected(fuel: FuelType) {
        if (fuel !in _uiState.value.selectedFuels) return
        preferencesManager.setComparisonFuel(fuel)
        _uiState.update { it.copy(comparisonFuel = fuel) }
        fetchStations(false)
    }

    fun loadLocalities() {
        _uiState.update { it.copy(isLoadingLocalities = true) }
        viewModelScope.launch {
            val result = repository.getNearbyStations(40.0, -3.0, 20_050.0,
                emptySet(), SortOption.NEAREST)
            _uiState.update { state -> state.copy(isLoadingLocalities = false,
                localityChoices = result.getOrNull()?.stations.orEmpty()
                    .groupBy { "${it.municipality}/${it.province}" }.values.map { stations ->
                        val lat = stations.map { it.latitude }.sorted()[stations.size / 2]
                        val lon = stations.map { it.longitude }.sorted()[stations.size / 2]
                        stations.minByOrNull { DistanceCalculator.calculateDistanceMeters(lat, lon, it.latitude, it.longitude) }!!
                    }.sortedBy { it.municipality }) }
        }
    }

    fun useManualLocation(latitude: Double, longitude: Double, name: String) {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return
        locationRevision++
        locationJob?.cancel()
        trackingJob?.cancel()
        _uiState.update { it.copy(userLocation = UserLocation(latitude, longitude),
            manualLocationName = name, locationMessage = null, isLocating = false, searchQuery = "") }
        fetchStations(false)
    }

    fun resumeGps() = recenterLocation()


    fun clearPrivateData(kind: String) {
        when (kind) {
            "location" -> lastLocationStore.clear()
            "remember" -> {
                val enabled = !_uiState.value.rememberLocation
                lastLocationStore.setEnabled(enabled)
                _uiState.update { it.copy(rememberLocation = enabled) }
            }
            "map" -> {
                _uiState.update { it.copy(isMapView = false) }
                viewModelScope.launch {
                    delay(500)
                    val cleared = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        File(getApplication<Application>().cacheDir, "osmdroid").deleteRecursively()
                    }
                    if (!cleared) _uiState.update { it.copy(dataWarning = "No se pudo borrar toda la caché del mapa.") }
                }
            }
            "favorites" -> {
                getApplication<Application>().getSharedPreferences("gasolineras_preferences", 0)
                    .edit().remove("favorite_station_ids").apply()
                _uiState.update { it.copy(favoriteIds = emptySet(), onlyFavorites = false) }
                fetchStations(false)
            }
            "prices" -> {
                fetchRevision++
                fetchJob?.cancel()
                viewModelScope.launch {
                    runCatching { repository.clearCache() }.onSuccess {
                        _uiState.update { it.copy(stations = emptyList(), mapStations = emptyList(),
                            savedAtMillis = 0L, officialDate = null, dataWarning = null,
                            minPricePerFuel = emptyMap(), isLoading = false, isRefreshing = false,
                            errorMessage = "Precios borrados. Pulsa Reintentar para consultarlos de nuevo.") }
                    }.onFailure { _uiState.update { it.copy(dataWarning = "No se pudieron borrar los precios guardados.") } }
                }
            }
        }
    }

    fun onRadiusSelected(radiusKm: Double) {
        if (_uiState.value.selectedRadiusKm == radiusKm) return
        _uiState.update { it.copy(selectedRadiusKm = radiusKm) }
        fetchStations(forceRefresh = false)
    }

    /**
     * Cycles through the available radius options: 3km -> 10km -> 25km -> 100km -> 3km
     * Returns the newly selected radius in km.
     */
    fun cycleRadius(): Double {
        val options = listOf(3.0, 10.0, 25.0, 100.0)
        val current = _uiState.value.selectedRadiusKm
        val currentIndex = options.indexOf(current).takeIf { it >= 0 } ?: 1
        val nextRadius = options[(currentIndex + 1) % options.size]
        onRadiusSelected(nextRadius)
        return nextRadius
    }

    fun onSortOptionSelected(sortOption: SortOption) {
        if (_uiState.value.selectedSort == sortOption) return
        _uiState.update { it.copy(selectedSort = sortOption) }
        fetchStations(forceRefresh = false)
    }

    fun toggleSort() {
        val nextSort = if (_uiState.value.selectedSort == SortOption.CHEAPEST) {
            SortOption.NEAREST
        } else {
            SortOption.CHEAPEST
        }
        onSortOptionSelected(nextSort)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(300) // Debounce typing
            fetchStations(forceRefresh = false)
        }
    }

    fun onToggleOnlyFavorites(onNoFavorites: () -> Unit = {}) {
        val nextFavoritesState = !_uiState.value.onlyFavorites
        if (nextFavoritesState) {
            val favs = favoritesManager.getFavoriteIds()
            if (favs.isEmpty()) {
                onNoFavorites()
                return
            }
        }
        _uiState.update { it.copy(onlyFavorites = nextFavoritesState) }
        fetchStations(forceRefresh = false)
    }

    fun onToggleFavorite(stationId: String) {
        favoritesManager.toggleFavorite(stationId)
        val ids = favoritesManager.getFavoriteIds()
        _uiState.update { state -> state.copy(
            favoriteIds = ids,
            selectedStationForDetail = state.selectedStationForDetail?.let {
                it.copy(isFavorite = it.id in ids)
            }
        ) }
        // Recompute counts, filters and cheapest badges together, including removal of the last favorite.
        fetchStations(forceRefresh = false)
    }

    fun onToggleMapView() {
        setMapView(!_uiState.value.isMapView)
    }

    fun setMapView(isMap: Boolean) {
        _uiState.update { it.copy(isMapView = isMap) }
    }

    fun recenterLocation(onSuccess: (UserLocation) -> Unit = {}) {
        if (_uiState.value.manualLocationName != null) {
            _uiState.update { it.copy(manualLocationName = null, userLocation = latestGpsLocation) }
            fetchStations(false)
        }
        restartTracking()
        locate(forceFresh = true, onSuccess = onSuccess)
    }

    fun onRefresh() {
        // Price refresh does not wait for GPS; a failed GPS request must not block offline data.
        locate(forceFresh = true)
        fetchStations(forceRefresh = true)
    }

    fun onSelectStation(station: GasStation?) {
        _uiState.update { it.copy(selectedStationForDetail = station) }
    }

    private fun fetchStations(forceRefresh: Boolean) {
        val refreshing = forceRefresh || _uiState.value.isRefreshing
        fetchJob?.cancel()
        val revision = ++fetchRevision
        val currentState = _uiState.value
        _uiState.update { it.copy(
            isLoading = it.savedAtMillis == 0L,
            isRefreshing = refreshing,
            errorMessage = null
        ) }
        fetchJob = viewModelScope.launch {
            val result = repository.getNearbyStations(
                userLat = currentState.userLocation.latitude,
                userLon = currentState.userLocation.longitude,
                radiusKm = currentState.selectedRadiusKm,
                selectedFuels = currentState.selectedFuels,
                sortOption = currentState.selectedSort,
                searchQuery = currentState.searchQuery,
                onlyFavorites = currentState.onlyFavorites,
                favoriteIds = currentState.favoriteIds,
                forceRefresh = refreshing,
                comparisonFuel = currentState.comparisonFuel
            )
            // A slow response for an earlier location/filter may never replace the latest query.
            if (revision != fetchRevision) return@launch
            result.fold(
                onSuccess = { data ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        stations = data.stations,
                        mapStations = data.mapStations,
                        minPrice = data.minPrice,
                        maxPrice = data.maxPrice,
                        averagePrice = data.averagePrice,
                        minPricePerFuel = data.minPricePerFuel,
                        savedAtMillis = data.savedAtMillis,
                        officialDate = data.officialDate,
                        dataWarning = data.warningMessage,
                        errorMessage = null
                ) }
                    if (data.refreshRequired) fetchStations(forceRefresh = true)
                },
                onFailure = { _ -> _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "No se han podido consultar los precios. Comprueba tu conexión y vuelve a intentarlo."
                ) } }
            )
        }
    }

    fun updatePreferences(
        visibleFuels: List<FuelType>,
        defaultSelectedFuels: Set<FuelType>,
        defaultView: String,
        defaultRadiusKm: Double,
        defaultSort: SortOption,
        themeMode: String
    ) {
        preferencesManager.setVisibleFuels(visibleFuels)
        preferencesManager.setDefaultSelectedFuels(defaultSelectedFuels)
        preferencesManager.setDefaultView(defaultView)
        preferencesManager.setDefaultRadiusKm(defaultRadiusKm)
        preferencesManager.setDefaultSort(defaultSort)
        preferencesManager.setThemeMode(themeMode)

        val updatedVisible = preferencesManager.getVisibleFuels()
        val currentSelected = _uiState.value.selectedFuels
        val updatedSelected = currentSelected.filter { updatedVisible.contains(it) }.toSet().ifEmpty {
            preferencesManager.getDefaultSelectedFuels()
        }

        _uiState.update {
            it.copy(
                visibleFuels = updatedVisible,
                selectedFuels = updatedSelected,
                comparisonFuel = it.comparisonFuel.takeIf { f -> f in updatedSelected } ?: updatedSelected.first(),
                themeMode = themeMode,
                selectedSort = defaultSort,
                selectedRadiusKm = defaultRadiusKm
            )
        }
        fetchStations(forceRefresh = false)
    }

    fun getDefaultSelectedFuelsPreference() = preferencesManager.getDefaultSelectedFuels()
    fun getDefaultViewPreference() = preferencesManager.getDefaultView()
    fun getDefaultSortPreference() = preferencesManager.getDefaultSort()

    fun getDefaultRadiusPreference(): Double = preferencesManager.getDefaultRadiusKm()
}
