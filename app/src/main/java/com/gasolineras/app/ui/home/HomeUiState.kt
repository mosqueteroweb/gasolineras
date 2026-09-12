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
    val selectedFuel: FuelType = FuelType.default,
    val selectedRadiusKm: Double = 10.0,
    val selectedSort: SortOption = SortOption.CHEAPEST,
    val searchQuery: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val averagePrice: Double? = null,
    val selectedStationForDetail: GasStation? = null,
    val hasLocationPermission: Boolean = false,
    val onlyGLP: Boolean = false,
    val isMapView: Boolean = false
)
