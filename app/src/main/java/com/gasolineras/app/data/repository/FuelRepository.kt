package com.gasolineras.app.data.repository

import com.gasolineras.app.data.api.ApiClient
import com.gasolineras.app.data.local.StationCache
import com.gasolineras.app.data.api.FuelApiService
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.domain.util.DistanceCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NearbyStationsResult(
    val stations: List<GasStation>,
    val mapStations: List<GasStation> = emptyList(),
    val minPrice: Double?,
    val maxPrice: Double?,
    val averagePrice: Double?,
    val minPricePerFuel: Map<FuelType, Double> = emptyMap(),
    val totalCountInRadius: Int,
    val savedAtMillis: Long = 0L,
    val officialDate: String? = null,
    val warningMessage: String? = null,
    val refreshRequired: Boolean = false
)

interface FuelRepository {
    suspend fun clearCache() {}

    suspend fun getNearbyStations(
        userLat: Double,
        userLon: Double,
        radiusKm: Double,
        selectedFuels: Set<FuelType>,
        sortOption: SortOption,
        searchQuery: String = "",
        onlyFavorites: Boolean = false,
        favoriteIds: Set<String> = emptySet(),
        forceRefresh: Boolean = false,
        comparisonFuel: FuelType? = null
    ): Result<NearbyStationsResult>
}

class FuelRepositoryImpl(
    private val apiService: FuelApiService = ApiClient.fuelApiService,
    private val diskCache: StationCache? = null,
    private val now: () -> Long = System::currentTimeMillis
) : FuelRepository {

    private val cacheMutex = Mutex()
    private var cachedStations: List<GasStation> = emptyList()
    private var lastFetchTimestamp = 0L
    private var officialDate: String? = null
    private var cacheRead = false
    private var lastAttempt: Long? = null
    private var warningMessage: String? = null
    private val cacheDurationMillis = 30 * 60 * 1000L

    override suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            diskCache?.clear()
            cachedStations = emptyList()
            lastFetchTimestamp = 0L
            officialDate = null
            warningMessage = null
            lastAttempt = null
        }
    }

    private data class LoadedStations(
        val stations: List<GasStation>, val savedAt: Long,
        val officialDate: String?, val warning: String?, val refreshRequired: Boolean = false
    )

    private suspend fun loadStations(forceRefresh: Boolean): LoadedStations = cacheMutex.withLock {
        val currentTime = now()
        var restoredFromDisk = false
        if (!cacheRead) {
            diskCache?.read()?.takeIf { it.savedAt <= currentTime + 60_000L }?.let { snapshot ->
                val stations = snapshot.response.stations.orEmpty().mapNotNull { it.toDomain() }
                if (stations.isNotEmpty()) {
                    cachedStations = stations
                    lastFetchTimestamp = snapshot.savedAt
                    officialDate = snapshot.response.date
                    restoredFromDisk = true
                }
            }
            cacheRead = true
        }
        val stale = currentTime - lastFetchTimestamp >= cacheDurationMillis
        if (restoredFromDisk && stale && !forceRefresh) {
            // Render saved prices immediately, even if the next network request times out.
            return@withLock LoadedStations(cachedStations, lastFetchTimestamp, officialDate,
                "Se muestran datos guardados mientras se intenta actualizar los precios.", refreshRequired = true)
        }
        val retryAllowed = lastAttempt?.let { currentTime - it >= 30_000L } ?: true
        if (forceRefresh || cachedStations.isEmpty() || (stale && retryAllowed)) {
            lastAttempt = currentTime
            try {
                val response = apiService.getAllStations()
                val stations = response.stations.orEmpty().mapNotNull { it.toDomain() }
                require(stations.isNotEmpty()) { "La respuesta del Ministerio no contiene estaciones válidas." }
                // Persist only a successful, usable response. Never erase valid data before a refresh.
                val persisted = runCatching {
                    diskCache?.write(StationCache.Snapshot(currentTime, response))
                }.isSuccess
                cachedStations = stations
                lastFetchTimestamp = currentTime
                officialDate = response.date
                warningMessage = if (persisted) null else
                    "Precios actualizados, pero no se han podido guardar para usarlos sin conexión."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (cachedStations.isEmpty()) throw e
                warningMessage = "No se han podido actualizar los precios. Se muestran datos guardados."
            }
        }
        LoadedStations(cachedStations, lastFetchTimestamp, officialDate, warningMessage)
    }

    override suspend fun getNearbyStations(
        userLat: Double,
        userLon: Double,
        radiusKm: Double,
        selectedFuels: Set<FuelType>,
        sortOption: SortOption,
        searchQuery: String,
        onlyFavorites: Boolean,
        favoriteIds: Set<String>,
        forceRefresh: Boolean,
        comparisonFuel: FuelType?
    ): Result<NearbyStationsResult> = withContext(Dispatchers.IO) {
        try {
            val loaded = loadStations(forceRefresh)

            val radiusMeters = radiusKm * 1000.0
            val normalizedQuery = searchQuery.trim().lowercase()

            // 1. Calculate distance from user GPS & map favorite status
            val processedStations = loaded.stations
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
                    // Fuel availability check: must have ALL of the selected fuels (conjunction AND)
                    val hasSelectedFuel = if (selectedFuels.isEmpty()) {
                        true
                    } else {
                        selectedFuels.all { fuel -> (station.prices[fuel] ?: 0.0) > 0.0 }
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

                    hasSelectedFuel && matchesFavorites && isPublic && matchesSearch
                }

            // Stations within user-selected radius (for List Mode)
            val inRadiusStations = processedStations.filter { (it.distanceMeters ?: Double.MAX_VALUE) <= radiusMeters }

            val priceFuel = comparisonFuel?.takeIf { it in selectedFuels }
                ?: FuelType.entries.firstOrNull { it in selectedFuels } ?: FuelType.default
            fun GasStation.bestPrice(): Double? = prices[priceFuel]

            // 2. Compute price stats for selected fuels in this local radius
            val pricesList = inRadiusStations.mapNotNull { it.bestPrice() }
            val minPrice = pricesList.minOrNull()
            val maxPrice = pricesList.maxOrNull()
            val avgPrice = if (pricesList.isNotEmpty()) pricesList.average() else null

            // Compute minimum price for EACH selected fuel individually
            val activeFuels = if (selectedFuels.isEmpty()) FuelType.values().toSet() else selectedFuels
            val minPricePerFuel = mutableMapOf<FuelType, Double>()
            for (fuel in activeFuels) {
                val minForFuel = inRadiusStations.mapNotNull { it.prices[fuel] }.minOrNull()
                if (minForFuel != null) {
                    minPricePerFuel[fuel] = minForFuel
                }
            }

            // 3. Sort stations for List mode
            val sortedStations = when (sortOption) {
                SortOption.CHEAPEST -> {
                    inRadiusStations.sortedWith(
                        compareBy<GasStation> {
                            val p = it.bestPrice()
                            if (p != null) Math.round(p * 1000.0) else Long.MAX_VALUE
                        }.thenBy {
                            it.distanceMeters ?: Double.MAX_VALUE
                        }
                    )
                }
                SortOption.NEAREST -> {
                    inRadiusStations.sortedWith(
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
                    mapStations = sortedStations,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    averagePrice = avgPrice,
                    minPricePerFuel = minPricePerFuel,
                    totalCountInRadius = sortedStations.size,
                    savedAtMillis = loaded.savedAt,
                    officialDate = loaded.officialDate,
                    warningMessage = loaded.warning,
                    refreshRequired = loaded.refreshRequired
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
