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
        fuelType: FuelType,
        sortOption: SortOption,
        searchQuery: String = "",
        onlyGLP: Boolean = false,
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
        fuelType: FuelType,
        sortOption: SortOption,
        searchQuery: String,
        onlyGLP: Boolean,
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

            // 1. Calculate distance & filter stations
            val processedStations = cachedStations
                .map { station ->
                    val distance = DistanceCalculator.calculateDistanceMeters(
                        userLat, userLon,
                        station.latitude, station.longitude
                    )
                    station.copy(distanceMeters = distance)
                }
                .filter { station ->
                    // Distance check
                    val withinRadius = (station.distanceMeters ?: Double.MAX_VALUE) <= radiusMeters

                    // Fuel availability check
                    val hasFuel = station.prices.containsKey(fuelType)

                    // GLP special filter
                    val matchesGLP = !onlyGLP || station.hasGLP

                    // Public sale filter (exclude wholesale or restricted cooperative stations)
                    val isPublic = station.saleType.equals("P", ignoreCase = true)

                    // Search text filter
                    val matchesSearch = if (normalizedQuery.isBlank()) {
                        true
                    } else {
                        station.brand.lowercase().contains(normalizedQuery) ||
                        station.municipality.lowercase().contains(normalizedQuery) ||
                        station.province.lowercase().contains(normalizedQuery) ||
                        station.address.lowercase().contains(normalizedQuery)
                    }

                    withinRadius && hasFuel && matchesGLP && isPublic && matchesSearch
                }

            // 2. Compute price stats for selected fuel in this local radius
            val prices = processedStations.mapNotNull { it.prices[fuelType] }
            val minPrice = prices.minOrNull()
            val maxPrice = prices.maxOrNull()
            val avgPrice = if (prices.isNotEmpty()) prices.average() else null

            // 3. Sort stations according to user preference
            val sortedStations = when (sortOption) {
                SortOption.CHEAPEST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> { it.prices[fuelType] ?: Double.MAX_VALUE }
                            .thenBy { it.distanceMeters ?: Double.MAX_VALUE }
                    )
                }
                SortOption.NEAREST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> { it.distanceMeters ?: Double.MAX_VALUE }
                            .thenBy { it.prices[fuelType] ?: Double.MAX_VALUE }
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
