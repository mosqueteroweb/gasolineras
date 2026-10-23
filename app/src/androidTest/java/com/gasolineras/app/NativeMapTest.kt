package com.gasolineras.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.ui.map.StationMapView
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class NativeMapTest {
    @get:Rule val compose = createComposeRule()
    private val madrid = UserLocation(40.4168, -3.7038)
    private val barcelona = UserLocation(41.3874, 2.1686)
    private val zaragoza = UserLocation(41.6488, -0.8891)
    private fun station(id: String, point: UserLocation) = GasStation(id, id, "Calle", "", id, "", "24H",
        point.latitude, point.longitude, mapOf(FuelType.GASOLEO_A to 1.5))

    @Test fun nativeMapFollowsCitiesPausesAfterDragRecentersAndCanBeDisposedAndReopened() {
        val location = mutableStateOf(madrid)
        val shown = mutableStateOf(true)
        val trigger = mutableIntStateOf(0)
        var centerLat = 0.0
        var centerLon = 0.0
        var count = -1
        compose.setContent {
            MaterialTheme {
                if (shown.value) StationMapView(location.value,
                    listOf(station("Madrid", madrid), station("Madrid 9 km", madrid.copy(latitude = madrid.latitude + .08)), station("Barcelona", barcelona), station("Zaragoza", zaragoza)),
                    setOf(FuelType.GASOLEO_A), onSelectStation = {}, modifier = Modifier.fillMaxSize(),
                    onVisibleCountChanged = { count = it }, centerTrigger = trigger.intValue,
                    onMapCenterChanged = { a, b -> centerLat = a; centerLon = b },
                    // Exercise the native renderer and annotations without automated requests to OSM.
                    mapStyleJson = """{"version":8,"sources":{},"layers":[{"id":"background","type":"background","paint":{"background-color":"#eeeeee"}}]}""")
            }
        }
        compose.waitUntil(15_000) { count == 2 && abs(centerLat - madrid.latitude) < .001 }
        compose.runOnIdle { location.value = barcelona }
        compose.waitUntil(10_000) { abs(centerLat - barcelona.latitude) < .001 && count == 1 }
        val bounds = compose.onNodeWithContentDescription("Mapa de gasolineras. La misma información está disponible en Ver lista.").fetchSemanticsNode().boundsInWindow
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "input swipe ${(bounds.left + bounds.width * .8f).toInt()} ${bounds.center.y.toInt()} ${(bounds.left + bounds.width * .2f).toInt()} ${bounds.center.y.toInt()} 500"
        ).use { descriptor -> java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() } }
        compose.waitUntil(10_000) { abs(centerLon - barcelona.longitude) > .001 }
        compose.waitForIdle()
        val pannedLat = centerLat
        val pannedLon = centerLon
        compose.runOnIdle { location.value = zaragoza }
        compose.waitForIdle()
        assertEquals(pannedLat, centerLat, .001)
        assertEquals(pannedLon, centerLon, .001)
        compose.runOnIdle { trigger.intValue++ }
        compose.waitUntil(10_000) { abs(centerLat - zaragoza.latitude) < .001 && count == 1 }
        compose.runOnIdle { shown.value = false }
        compose.waitForIdle()
        compose.runOnIdle { count = -1; shown.value = true }
        compose.waitUntil(15_000) { count == 1 }
    }
}
