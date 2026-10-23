package com.gasolineras.app

import com.gasolineras.app.data.api.FuelApiResponse
import com.gasolineras.app.data.api.FuelApiService
import com.gasolineras.app.data.api.FuelStationDto
import com.gasolineras.app.data.local.StationCache
import com.gasolineras.app.data.repository.FuelRepositoryImpl
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FuelRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val madrid = station("madrid", "40,416775", "-3,703790")
    private val barcelona = station("barcelona", "41,3874", "2,1686")
    private val response = FuelApiResponse("22/10/2026 22:48:28", listOf(madrid, barcelona))
    private var time = 1_800_000_000_000L

    private class Api(var fetch: suspend () -> FuelApiResponse) : FuelApiService {
        val calls = AtomicInteger()
        override suspend fun getAllStations(): FuelApiResponse { calls.incrementAndGet(); return fetch() }
        override suspend fun getStationsByProvince(idProvincia: String) = getAllStations()
    }

    private fun station(id: String, lat: String, lon: String, price: String = "0,95") = FuelStationDto(
        id = id, rotulo = "REPSOL", latitud = lat, longitud = lon,
        precioGLP = price, precioG95 = "1,70", tipoVenta = "P"
    )

    private suspend fun FuelRepositoryImpl.near(lat: Double = 40.416775, lon: Double = -3.703790,
        refresh: Boolean = false, favorites: Set<String> = emptySet(), onlyFavorites: Boolean = false) =
        getNearbyStations(lat, lon, 10.0, setOf(FuelType.GLP), SortOption.CHEAPEST,
            onlyFavorites = onlyFavorites, favoriteIds = favorites, forceRefresh = refresh)

    @Test fun restartWithoutNetworkRestoresStationsAndDate() = runBlocking {
        val cache = StationCache(folder.newFile("stations.json"))
        val online = Api { response }
        FuelRepositoryImpl(online, cache) { time }.near().getOrThrow()
        val offline = Api { throw IOException("offline") }
        val data = FuelRepositoryImpl(offline, cache) { time }.near().getOrThrow()
        assertEquals(listOf("madrid"), data.stations.map { it.id })
        assertEquals(response.date, data.officialDate)
        assertEquals(time, data.savedAtMillis)
        assertEquals(0, offline.calls.get())
    }

    @Test fun failedForcedRefreshKeepsPersistentResponseAndWarns() = runBlocking {
        val cache = StationCache(folder.newFile("stations.json"))
        val api = Api { response }
        val repo = FuelRepositoryImpl(api, cache) { time }
        repo.near().getOrThrow()
        api.fetch = { throw IOException("DNS details must not reach the UI") }
        time += 60_000L
        val data = repo.near(refresh = true).getOrThrow()
        assertNotNull(data.warningMessage)
        assertFalse(data.warningMessage!!.contains("DNS"))
        assertEquals(listOf("madrid"), data.stations.map { it.id })
        assertEquals(time - 60_000L, cache.read()!!.savedAt)
        assertNotNull(repo.near().getOrThrow().warningMessage)
    }

    @Test fun expiredDiskCacheRemainsUsableOfflineAndRetriesWhenRequested() = runBlocking {
        val cache = StationCache(folder.newFile("stations.json"))
        cache.write(StationCache.Snapshot(time - 86_400_000, response))
        val api = Api { throw IOException("offline") }
        val repo = FuelRepositoryImpl(api, cache) { time }
        val restored = repo.near().getOrThrow()
        assertTrue(restored.refreshRequired)
        assertEquals(0, api.calls.get()) // Show disk data before attempting a possibly slow network request.
        assertNotNull(repo.near(refresh = true).getOrThrow().warningMessage)
        repo.near().getOrThrow()
        assertEquals(1, api.calls.get()) // Local filters do not repeatedly retry a failed download.
        api.fetch = { response }
        val recovered = repo.near(refresh = true).getOrThrow()
        assertNull(recovered.warningMessage)
        assertEquals(time, recovered.savedAtMillis)
    }

    @Test fun emptyResponseDoesNotOverwriteValidCache() = runBlocking {
        val cache = StationCache(folder.newFile("stations.json"))
        val api = Api { response }
        val repo = FuelRepositoryImpl(api, cache) { time }
        repo.near().getOrThrow()
        api.fetch = { FuelApiResponse("bad", emptyList()) }
        assertNotNull(repo.near(refresh = true).getOrThrow().warningMessage)
        assertEquals(response.date, cache.read()!!.response.date)
    }

    @Test fun malformedResponseDoesNotOverwriteValidCache() = runBlocking {
        val api = Api { response }
        val repo = FuelRepositoryImpl(api)
        repo.near().getOrThrow()
        api.fetch = { FuelApiResponse(null, listOf(madrid.copy(latitud = "invalid"))) }
        assertEquals("madrid", repo.near(refresh = true).getOrThrow().stations.single().id)
    }

    @Test fun corruptedCacheAllowsOnlineRecovery() = runBlocking {
        val file = folder.newFile("broken.json").apply { writeText("{not valid JSON") }
        val api = Api { response }
        val data = FuelRepositoryImpl(api, StationCache(file)) { time }.near().getOrThrow()
        assertEquals("madrid", data.stations.single().id)
        assertNotNull(StationCache(file).read())
    }

    @Test fun noCacheAndNoNetworkReturnsFailure() = runBlocking {
        val api = Api { throw IOException("offline") }
        assertTrue(FuelRepositoryImpl(api).near().isFailure)
    }

    @Test fun movingCitiesRecalculatesCachedDistancesAndBothViews() = runBlocking {
        val api = Api { response }
        val repo = FuelRepositoryImpl(api)
        val first = repo.near().getOrThrow()
        val next = repo.near(41.3874, 2.1686).getOrThrow()
        assertEquals("madrid", first.stations.single().id)
        assertEquals("barcelona", next.stations.single().id)
        assertEquals(0.0, next.stations.single().distanceMeters!!, 0.01)
        assertEquals(next.stations, next.mapStations)
        assertEquals(1, api.calls.get())
    }

    @Test fun favoriteRemovalRecalculatesMinimumPrice() = runBlocking {
        val cheap = madrid.copy(id = "cheap", precioGLP = "0,80")
        val api = Api { FuelApiResponse(null, listOf(cheap, madrid)) }
        val repo = FuelRepositoryImpl(api)
        assertEquals(0.8, repo.near().getOrThrow().minPricePerFuel[FuelType.GLP]!!, 0.001)
        val favorites = repo.near(favorites = setOf("madrid"), onlyFavorites = true).getOrThrow()
        assertEquals(0.95, favorites.minPricePerFuel[FuelType.GLP]!!, 0.001)
        assertTrue(repo.near(onlyFavorites = true).getOrThrow().stations.isEmpty())
    }

    @Test fun concurrentQueriesShareOneDownload() = runBlocking {
        val api = Api { delay(100); response }
        val repo = FuelRepositoryImpl(api)
        val first = async { repo.near() }
        val second = async { repo.near(41.3874, 2.1686) }
        assertEquals("madrid", first.await().getOrThrow().stations.single().id)
        assertEquals("barcelona", second.await().getOrThrow().stations.single().id)
        assertEquals(1, api.calls.get())
    }

    @Test fun cancellationIsNotReportedAsAnOfflineSuccess() = runBlocking {
        val api = Api { response }
        val repo = FuelRepositoryImpl(api)
        repo.near().getOrThrow()
        api.fetch = { throw CancellationException("superseded query") }
        try {
            repo.near(refresh = true)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    @Test fun comparisonUsesChosenFuelInsteadOfCheapestDifferentProduct() = runBlocking {
        val first = madrid.copy(id = "cheapGas", precioG95 = "1,40", precioGLP = "1,10")
        val second = madrid.copy(id = "cheapGlp", precioG95 = "1,80", precioGLP = "0,80")
        val repo = FuelRepositoryImpl(Api { response.copy(stations = listOf(first, second)) }) { time }
        suspend fun sorted(fuel: FuelType) = repo.getNearbyStations(40.416775, -3.703790, 10.0,
            setOf(FuelType.GLP, FuelType.GASOLINA_95_E5), SortOption.CHEAPEST,
            comparisonFuel = fuel).getOrThrow()
        assertEquals("cheapGas", sorted(FuelType.GASOLINA_95_E5).stations.first().id)
        assertEquals("cheapGlp", sorted(FuelType.GLP).stations.first().id)
        assertEquals(1.4, sorted(FuelType.GASOLINA_95_E5).minPrice!!, 0.0001)
    }

    @Test fun explicitClearRemovesDiskAndMemoryAndNextQueryReloads() = runBlocking {
        val cache = StationCache(folder.newFile("clear.json"))
        val api = Api { response }
        val repo = FuelRepositoryImpl(api, cache) { time }
        repo.near().getOrThrow()
        repo.clearCache()
        assertNull(cache.read())
        repo.near().getOrThrow()
        assertEquals(2, api.calls.get())
    }
}
