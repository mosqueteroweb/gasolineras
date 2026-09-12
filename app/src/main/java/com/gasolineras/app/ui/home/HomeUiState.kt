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
    val mapStations: List<GasStation> = emptyList(),
    val userLocation: UserLocation = UserLocation.DEFAULT,
    val visibleFuels: List<FuelType> = listOf(
        FuelType.GASOLEO_A,
        FuelType.GASOLINA_95_E5,
        FuelType.GLP
    ),
    val selectedFuels: Set<FuelType> = setOf(
        FuelType.GASOLEO_A,
        FuelType.GASOLINA_95_E5,
        FuelType.GLP
    ),
    val selectedRadiusKm: Double = 10.0,
    val selectedSort: SortOption = SortOption.CHEAPEST,
    val themeMode: String = "SYSTEM",
    val searchQuery: String = "",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val averagePrice: Double? = null,
    val minPricePerFuel: Map<FuelType, Double> = emptyMap(),
    val selectedStationForDetail: GasStation? = null,
    val hasLocationPermission: Boolean = false,
    val onlyFavorites: Boolean = false,
    val favoriteIds: Set<String> = emptySet(),
    val isMapView: Boolean = false
) {
    /**
     * Returns the list of selected fuels for which this station has the lowest price in the area.
     */
    fun cheapestFuelsFor(station: GasStation): List<FuelType> {
        if (minPricePerFuel.isEmpty()) return emptyList()
        val active = if (selectedFuels.isEmpty()) minPricePerFuel.keys else selectedFuels
        return active.filter { fuel ->
            val p = station.prices[fuel]
            val minP = minPricePerFuel[fuel]
            p != null && minP != null && p <= minP
        }
    }
}
