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
    val minPricePerFuel: Map<FuelType, Double> = emptyMap(),
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

    suspend fun findOptimalRadius(
        userLat: Double,
        userLon: Double,
        selectedFuels: Set<FuelType>,
        candidateRadii: List<Double> = listOf(3.0, 10.0, 25.0, 100.0)
    ): Double
}

class FuelRepositoryImpl(
    private val apiService: FuelApiService = ApiClient.fuelApiService
) : FuelRepository {

    // In-memory cache (only stores stations within 100 km of the user)
    private var cachedStations: List<GasStation> = emptyList()
    private var lastFetchTimestamp: Long = 0
    private var lastFetchLat: Double? = null
    private var lastFetchLon: Double? = null
    private val cacheDurationMillis = 30 * 60 * 1000L // 30 minutes
    private val maxCacheRadiusMeters = 100_000.0 // 100 km max API radius

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
            val movedSignificantly = lastFetchLat == null || lastFetchLon == null ||
                    DistanceCalculator.calculateDistanceMeters(userLat, userLon, lastFetchLat!!, lastFetchLon!!) > 10_000.0

            val needRefresh = forceRefresh ||
                    cachedStations.isEmpty() ||
                    movedSignificantly ||
                    (currentTime - lastFetchTimestamp > cacheDurationMillis)

            if (needRefresh) {
                val response = apiService.getAllStations()
                val rawList = response.stations.orEmpty()
                // Eliminate stations beyond 100 km from the user when querying the API
                cachedStations = rawList.mapNotNull { it.toDomain() }.filter { station ->
                    DistanceCalculator.calculateDistanceMeters(
                        userLat, userLon,
                        station.latitude, station.longitude
                    ) <= maxCacheRadiusMeters
                }
                lastFetchTimestamp = currentTime
                lastFetchLat = userLat
                lastFetchLon = userLon
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

            // Compute minimum price for EACH selected fuel individually
            val activeFuels = if (selectedFuels.isEmpty()) FuelType.values().toSet() else selectedFuels
            val minPricePerFuel = mutableMapOf<FuelType, Double>()
            for (fuel in activeFuels) {
                val minForFuel = processedStations.mapNotNull { it.prices[fuel] }.minOrNull()
                if (minForFuel != null) {
                    minPricePerFuel[fuel] = minForFuel
                }
            }

            // 3. Sort stations according to user preference (breaking price ties by distance ascending)
            val sortedStations = when (sortOption) {
                SortOption.CHEAPEST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> {
                            val p = it.bestPrice()
                            if (p != null) Math.round(p * 1000.0) else Long.MAX_VALUE
                        }.thenBy {
                            it.distanceMeters ?: Double.MAX_VALUE
                        }
                    )
                }
                SortOption.NEAREST -> {
                    processedStations.sortedWith(
                        compareBy<GasStation> { it.distanceMeters ?: Double.MAX_VALUE }
                            .thenBy {
                                val p = it.bestPrice()
                                if (p != null) Math.round(p * 1000.0) else Long.MAX_VALUE
                            }
                    )
                }
            }

            Result.success(
                NearbyStationsResult(
                    stations = sortedStations,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    averagePrice = avgPrice,
                    minPricePerFuel = minPricePerFuel,
                    totalCountInRadius = sortedStations.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findOptimalRadius(
        userLat: Double,
        userLon: Double,
        selectedFuels: Set<FuelType>,
        candidateRadii: List<Double>
    ): Double = withContext(Dispatchers.IO) {
        if (cachedStations.isEmpty()) {
            try {
                val response = apiService.getAllStations()
                val rawList = response.stations.orEmpty()
                cachedStations = rawList.mapNotNull { it.toDomain() }.filter { station ->
                    DistanceCalculator.calculateDistanceMeters(
                        userLat, userLon,
                        station.latitude, station.longitude
                    ) <= maxCacheRadiusMeters
                }
                lastFetchTimestamp = System.currentTimeMillis()
                lastFetchLat = userLat
                lastFetchLon = userLon
            } catch (e: Exception) {
                return@withContext candidateRadii.firstOrNull() ?: 10.0
            }
        }

        val stationsWithDistance = cachedStations.map { station ->
            val dist = DistanceCalculator.calculateDistanceMeters(
                userLat, userLon,
                station.latitude, station.longitude
            )
            station.copy(distanceMeters = dist)
        }.filter { station ->
            val isPublic = station.saleType.isBlank() || station.saleType.equals("P", ignoreCase = true)
            val hasFuel = if (selectedFuels.isEmpty()) true else selectedFuels.any { station.prices.containsKey(it) }
            isPublic && hasFuel
        }

        for (radius in candidateRadii) {
            val radiusMeters = radius * 1000.0
            val count = stationsWithDistance.count { (it.distanceMeters ?: Double.MAX_VALUE) <= radiusMeters }
            if (count > 0) {
                return@withContext radius
            }
        }

        candidateRadii.lastOrNull() ?: 100.0
    }
}
