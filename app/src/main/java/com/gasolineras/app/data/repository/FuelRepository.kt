package com.gasolineras.app.data.repository

import com.gasolineras.app.data.api.ApiClient
import com.gasolineras.app.data.api.FuelApiService
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.domain.util.DistanceCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NearbyStationsResult(
    val stations: List<GasStation>,
    val minPrice: Double?,
    val maxPrice: Double?,
    val averagePrice: Double?,
    val totalCountInRadius: Int
)

interface FuelRepository {
    suspend fun getNearbyStations(
        userLat: Double,
        userLon: Double,
        radiusKm: Double,
        selectedFuels: Set<FuelType>,
        sortOption: SortOption,
        searchQuery: String = "",
        onlyFavorites: Boolean = false,
        favoriteIds: Set<String> = emptySet(),
        forceRefresh: Boolean = false
    ): Result<NearbyStationsResult>
}

class FuelRepositoryImpl(
    private val apiService: FuelApiService = ApiClient.fuelApiService
) : FuelRepository {

    // In-memory cache
    private var cachedStations: List<GasStation> = emptyList()
    private var lastFetchTimestamp: Long = 0
    private val cacheDurationMillis = 30 * 60 * 1000L // 30 minutes

    override suspend fun getNearbyStations(
        userLat: Double,
        userLon: Double,
        radiusKm: Double,
        selectedFuels: Set<FuelType>,
        sortOption: SortOption,
        searchQuery: String,
        onlyFavorites: Boolean,
        favoriteIds: Set<String>,
        forceRefresh: Boolean
    ): Result<NearbyStationsResult> = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val needRefresh = forceRefresh ||
                    cachedStations.isEmpty() ||
                    (currentTime - lastFetchTimestamp > cacheDurationMillis)

            if (needRefresh) {
                val response = apiService.getAllStations()
                val rawList = response.stations.orEmpty()
                cachedStations = rawList.mapNotNull { it.toDomain() }
                lastFetchTimestamp = currentTime
            }

            val radiusMeters = radiusKm * 1000.0
            val normalizedQuery = searchQuery.trim().lowercase()

            // 1. Calculate distance & map favorite status
            val processedStations = cachedStations
                .map { station ->
                    val distance = DistanceCalculator.calculateDistanceMeters(
                        userLat, userLon,
                        station.latitude, station.longitude
                    )
                    station.copy(
                        distanceMeters = distance,
                        isFavorite = favoriteIds.contains(station.id)
                    )
                }
                .filter { station ->
                    // Distance check
                    val withinRadius = (station.distanceMeters ?: Double.MAX_VALUE) <= radiusMeters

                    // Fuel availability check: must have at least one of the selected fuels
                    val hasSelectedFuel = if (selectedFuels.isEmpty()) {
                        true
                    } else {
                        selectedFuels.any { fuel -> station.prices.containsKey(fuel) }
                    }

                    // Favorites filter
                    val matchesFavorites = !onlyFavorites || favoriteIds.contains(station.id)

                    // Public sale filter (P = public sale, or blank)
                    val isPublic = station.saleType.isBlank() || station.saleType.equals("P", ignoreCase = true)

                    // Search text filter
                    val matchesSearch = if (normalizedQuery.isBlank()) {
                        true
                    } else {
                        station.brand.lowercase().contains(normalizedQuery) ||
                        station.municipality.lowercase().contains(normalizedQuery) ||
                        station.province.lowercase().contains(normalizedQuery) ||
                        station.address.lowercase().contains(normalizedQuery)
                    }

                    withinRadius && hasSelectedFuel && matchesFavorites && isPublic && matchesSearch
                }

            // Helper for best price among selected fuels
            fun GasStation.bestPrice(): Double? {
                val candidatePrices = if (selectedFuels.isEmpty()) {
                    prices.values
                } else {
                    selectedFuels.mapNotNull { prices[it] }
                }
                return candidatePrices.minOrNull()
            }

            // 2. Compute price stats for selected fuels in this local radius
            val pricesList = processedStations.mapNotNull { it.bestPrice() }
            val minPrice = pricesList.minOrNull()
            val maxPrice = pricesList.maxOrNull()
            val avgPrice = if (pricesList.isNotEmpty()) pricesList.average() else null

            // 3. Sort stations according to user preference
            val sortedStations = when (sortOption) {
                SortOption.CHEAPEST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> { it.bestPrice() ?: Double.MAX_VALUE }
                            .thenBy { it.distanceMeters ?: Double.MAX_VALUE }
                    )
                }
                SortOption.NEAREST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> { it.distanceMeters ?: Double.MAX_VALUE }
                            .thenBy { it.bestPrice() ?: Double.MAX_VALUE }
                    )
                }
            }

            Result.success(
                NearbyStationsResult(
                    stations = sortedStations,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    averagePrice = avgPrice,
                    totalCountInRadius = sortedStations.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
