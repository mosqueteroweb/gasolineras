package com.gasolineras.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gasolineras.app.data.local.FavoritesManager
import com.gasolineras.app.data.location.LocationClient
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.data.repository.FuelRepository
import com.gasolineras.app.data.repository.FuelRepositoryImpl
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: FuelRepository = FuelRepositoryImpl(),
    private val locationClient: LocationClient = LocationClient(application)
) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)

    private val _uiState = MutableStateFlow(
        HomeUiState(favoriteIds = favoritesManager.getFavoriteIds())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        checkLocationAndLoad(forceRefresh = false)
    }

    /**
     * Checks location permissions, immediately triggers station loading with current coordinates,
     * and asynchronously fetches GPS location to refine distances without blocking UI.
     */
    fun checkLocationAndLoad(forceRefresh: Boolean = false) {
        val hasPermission = locationClient.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }

        // Start loading stations immediately so the user never waits on a blank screen
        fetchStations(forceRefresh = forceRefresh)

        // If permission is already granted, refine with current GPS coordinates concurrently
        if (hasPermission) {
            viewModelScope.launch {
                val userLocation = locationClient.getCurrentLocation()
                if (userLocation != _uiState.value.userLocation) {
                    _uiState.update { it.copy(userLocation = userLocation) }
                    fetchStations(forceRefresh = false)
                }
            }
        }
    }

    /**
     * Called when location permission is granted by the user.
     */
    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(hasLocationPermission = true) }
        viewModelScope.launch {
            val userLocation = locationClient.getCurrentLocation()
            _uiState.update { it.copy(userLocation = userLocation) }
            fetchStations(forceRefresh = false)
        }
    }

    /**
     * Toggles one of the 3 main fuels (Diésel, Gasolina 95, GLP).
     * Ensures at least one fuel remains selected.
     */
    fun onToggleFuel(fuel: FuelType) {
        val current = _uiState.value.selectedFuels
        val next = if (current.contains(fuel)) {
            if (current.size > 1) current - fuel else current
        } else {
            current + fuel
        }
        _uiState.update { it.copy(selectedFuels = next) }
        fetchStations(forceRefresh = false)
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
        val updatedFavorites = favoritesManager.getFavoriteIds()
        _uiState.update { state ->
            val updatedStations = state.stations.map { s ->
                if (s.id == stationId) s.copy(isFavorite = updatedFavorites.contains(s.id)) else s
            }
            val filteredStations = if (state.onlyFavorites) {
                updatedStations.filter { it.isFavorite }
            } else {
                updatedStations
            }
            val updatedMapStations = state.mapStations.map { s ->
                if (s.id == stationId) s.copy(isFavorite = updatedFavorites.contains(s.id)) else s
            }
            val filteredMapStations = if (state.onlyFavorites) {
                updatedMapStations.filter { it.isFavorite }
            } else {
                updatedMapStations
            }
            val updatedDetail = state.selectedStationForDetail?.let { detail ->
                if (detail.id == stationId) detail.copy(isFavorite = updatedFavorites.contains(detail.id)) else detail
            }
            state.copy(
                favoriteIds = updatedFavorites,
                stations = filteredStations,
                mapStations = filteredMapStations,
                selectedStationForDetail = updatedDetail
            )
        }
    }

    fun onToggleMapView() {
        setMapView(!_uiState.value.isMapView)
    }

    fun setMapView(isMap: Boolean) {
        if (_uiState.value.isMapView == isMap) return
        if (isMap) {
            viewModelScope.launch {
                val state = _uiState.value
                val optimalRadius = repository.findOptimalRadius(
                    userLat = state.userLocation.latitude,
                    userLon = state.userLocation.longitude,
                    selectedFuels = state.selectedFuels,
                    candidateRadii = listOf(3.0, 10.0, 25.0, 100.0)
                )
                _uiState.update { it.copy(isMapView = true, selectedRadiusKm = optimalRadius) }
                fetchStations(forceRefresh = false)
            }
        } else {
            _uiState.update { it.copy(isMapView = false) }
        }
    }

    fun onRefresh() {
        fetchStations(forceRefresh = true)
    }

    fun onSelectStation(station: GasStation?) {
        _uiState.update { it.copy(selectedStationForDetail = station) }
    }

    private fun fetchStations(forceRefresh: Boolean) {
        val currentState = _uiState.value
        val isFirstLoad = currentState.stations.isEmpty()

        _uiState.update {
            it.copy(
                isLoading = isFirstLoad && !forceRefresh,
                isRefreshing = forceRefresh,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = repository.getNearbyStations(
                userLat = currentState.userLocation.latitude,
                userLon = currentState.userLocation.longitude,
                radiusKm = currentState.selectedRadiusKm,
                selectedFuels = currentState.selectedFuels,
                sortOption = currentState.selectedSort,
                searchQuery = currentState.searchQuery,
                onlyFavorites = currentState.onlyFavorites,
                favoriteIds = currentState.favoriteIds,
                forceRefresh = forceRefresh
            )

            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            stations = data.stations,
                            mapStations = data.mapStations,
                            minPrice = data.minPrice,
                            maxPrice = data.maxPrice,
                            averagePrice = data.averagePrice,
                            minPricePerFuel = data.minPricePerFuel,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.localizedMessage
                                ?: "No se pudieron obtener los precios de las gasolineras."
                        )
                    }
                }
            )
        }
    }
}
