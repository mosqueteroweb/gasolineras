package com.gasolineras.app

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gasolineras.app.data.local.LastLocationStore
import com.gasolineras.app.data.local.UserPreferencesManager
import com.gasolineras.app.data.location.LocationProvider
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.data.repository.FuelRepository
import com.gasolineras.app.data.repository.NearbyStationsResult
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.ui.home.HomeViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelRegressionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var app: Application
    private lateinit var store: ViewModelStore
    private lateinit var location: FakeLocation
    private lateinit var repo: FakeRepository
    private lateinit var vm: HomeViewModel
    private val madrid = UserLocation(40.416775, -3.703790, obtainedAtMillis = 1000L)
    private val barcelona = UserLocation(41.3874, 2.1686, obtainedAtMillis = 2000L)

    private class TestApplication(context: Context) : Application() {
        init {
            attachBaseContext(object : ContextWrapper(context) {
                override fun getSharedPreferences(name: String, mode: Int) =
                    super.getSharedPreferences("regression-$name", mode)
            })
        }
    }

    private class FakeLocation : LocationProvider {
        var permission = true
        var enabled = true
        var current: UserLocation? = null
        val updates = MutableSharedFlow<UserLocation>(extraBufferCapacity = 8)
        override fun hasLocationPermission() = permission
        override fun isLocationEnabled() = enabled
        override suspend fun getCurrentLocation(forceFresh: Boolean) = current
        override fun observeLocations(): Flow<UserLocation> = updates
    }

    private class FakeRepository : FuelRepository {
        var latestLatitude = 0.0
        var calls = 0
        var load: suspend (Double) -> NearbyStationsResult = { radius ->
            NearbyStationsResult(emptyList(), emptyList(), null, null, null,
                emptyMap(), radius.toInt(), savedAtMillis = 1000L)
        }
        override suspend fun getNearbyStations(userLat: Double, userLon: Double, radiusKm: Double,
            selectedFuels: Set<FuelType>, sortOption: SortOption, searchQuery: String,
            onlyFavorites: Boolean, favoriteIds: Set<String>, forceRefresh: Boolean): Result<NearbyStationsResult> {
            calls++
            latestLatitude = userLat
            return Result.success(load(radiusKm))
        }
    }

    @Before fun setup() {
        app = TestApplication(instrumentation.targetContext)
        listOf("last_location", "gasolineras_preferences", "gasolineras_user_preferences").forEach {
            app.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit()
        }
        store = ViewModelStore()
        location = FakeLocation()
        repo = FakeRepository()
    }

    private fun create() = main {
        vm = HomeViewModel(app, repo, location)
        store.put("home", vm)
    }

    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun await(condition: () -> Boolean) {
        val end = System.currentTimeMillis() + 5000L
        while (System.currentTimeMillis() < end) {
            var ready = false
            main { ready = condition() }
            if (ready) return
            Thread.sleep(20)
        }
        fail("Expected state was not reached within five seconds")
    }

    @After fun teardown() = main { store.clear() }

    @Test fun continuousLocationChangesCityWithoutManualRefresh() {
        location.current = madrid
        create()
        main { vm.onForeground() }
        await { vm.uiState.value.userLocation.latitude == madrid.latitude }
        location.updates.tryEmit(barcelona)
        await { vm.uiState.value.userLocation.latitude == barcelona.latitude && repo.latestLatitude == barcelona.latitude }
    }

    @Test fun returningFromBackgroundAcquiresNewPosition() {
        location.current = madrid
        create()
        main { vm.onForeground() }
        await { vm.uiState.value.userLocation.latitude == madrid.latitude }
        main { vm.onBackground() }
        location.current = barcelona
        main { vm.onForeground() }
        await { vm.uiState.value.userLocation.latitude == barcelona.latitude }
    }

    @Test fun failedOrDisabledGpsPreservesLastValidPositionAndDoesNotSignalSuccess() {
        LastLocationStore(app).write(barcelona)
        location.enabled = false
        create()
        var succeeded = false
        main { vm.recenterLocation { succeeded = true } }
        await { vm.uiState.value.locationMessage != null && !vm.uiState.value.isLocating }
        main {
            assertEquals(barcelona, vm.uiState.value.userLocation)
            assertFalse(vm.uiState.value.isLocationEnabled)
            assertFalse(succeeded)
        }
    }

    @Test fun noGpsOnFirstLaunchKeepsExplicitReference() {
        create()
        main { vm.recenterLocation() }
        await { vm.uiState.value.locationMessage != null }
        main { assertTrue(vm.uiState.value.userLocation.isDefaultLocation) }
    }

    @Test fun mapKeepsRadiusAndHiddenFuelsCannotBeActivated() {
        val prefs = UserPreferencesManager(app)
        prefs.setVisibleFuels(listOf(FuelType.GASOLINA_95_E5, FuelType.GLP, FuelType.GASOLINA_98_E5))
        prefs.setDefaultSelectedFuels(setOf(FuelType.GLP))
        create()
        main {
            vm.onRadiusSelected(25.0)
            vm.setMapView(true)
            vm.onToggleFuel(FuelType.GASOLEO_A)
            assertEquals(25.0, vm.uiState.value.selectedRadiusKm, 0.0)
            assertEquals(setOf(FuelType.GLP), vm.uiState.value.selectedFuels)
            vm.onToggleFuel(FuelType.GASOLINA_98_E5)
            assertTrue(FuelType.GASOLINA_98_E5 in vm.uiState.value.selectedFuels)
        }
    }

    @Test fun lateResponseCannotOverwriteLatestFilter() {
        val firstStarted = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        repo.load = { radius ->
            if (radius == 10.0) withContext(NonCancellable) {
                firstStarted.complete(Unit)
                releaseFirst.await()
            }
            NearbyStationsResult(emptyList(), emptyList(), radius, null, null,
                emptyMap(), 0, savedAtMillis = 1000L)
        }
        create()
        await { firstStarted.isCompleted }
        main { vm.onRadiusSelected(25.0) }
        await { vm.uiState.value.minPrice == 25.0 }
        releaseFirst.complete(Unit)
        instrumentation.waitForIdleSync()
        main { assertEquals(25.0, vm.uiState.value.minPrice!!, 0.0) }
    }
}
