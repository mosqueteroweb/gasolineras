package com.gasolineras.app.ui.home

import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val stations: List<GasStation> = emptyList(),
    val userLocation: UserLocation = UserLocation.DEFAULT,
    val selectedFuels: Set<FuelType> = setOf(
        FuelType.GASOLEO_A,
        FuelType.GASOLINA_95_E5,
        FuelType.GLP
    ),
    val selectedRadiusKm: Double = 15.0,
    val selectedSort: SortOption = SortOption.CHEAPEST,
    val searchQuery: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val averagePrice: Double? = null,
    val selectedStationForDetail: GasStation? = null,
    val hasLocationPermission: Boolean = false,
    val onlyFavorites: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val isMapView: Boolean = false
)
