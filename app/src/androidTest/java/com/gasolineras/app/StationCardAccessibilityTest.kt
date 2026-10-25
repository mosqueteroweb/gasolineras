package com.gasolineras.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.ui.components.StationCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationCardAccessibilityTest {
    @get:Rule val compose = createComposeRule()

    private val station = GasStation("1", "ESTACION DE SERVICIO REPSOL", "AVENIDA DE LA CONSTITUCIÓN, 3", "28001",
        "MADRID", "MADRID", "L-D: 24H", 40.4, -3.7,
        mapOf(FuelType.GASOLEO_A to 1.5, FuelType.GLP to 0.9), distanceMeters = 1500.0)

    @Test fun pricesAreReadAsWordsAndAvatarIsSilentAtTwoHundredPercent() {
        var opened = 0
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MaterialTheme {
                    StationCard(station, setOf(FuelType.GASOLEO_A, FuelType.GLP),
                        listOf(FuelType.GASOLEO_A, FuelType.GLP), cheapestForFuels = listOf(FuelType.GASOLEO_A),
                        onClick = { opened++ }, onNavigateClick = {}, onToggleFavorite = {})
                }
            }
        }
        compose.onNodeWithContentDescription("Diésel: 1,500 euros por litro, la más barata de la zona").assertExists()
        compose.onNodeWithContentDescription("GLP: 0,900 euros por litro").assertExists()
        compose.onNodeWithText("Avenida de la Constitución, 3, Madrid").assertExists()
        compose.onAllNodesWithText("RE").assertCountEquals(0)
        compose.onNodeWithText("Repsol").performClick()
        compose.runOnIdle { assertEquals(1, opened) }
    }
}
