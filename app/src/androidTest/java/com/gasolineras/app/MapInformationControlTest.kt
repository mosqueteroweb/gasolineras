package com.gasolineras.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.ui.components.MapInformationControl
import com.gasolineras.app.ui.home.HomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapInformationControlTest {
    @get:Rule val compose = createComposeRule()
    private val expanded = mutableStateOf(false)
    private val state = mutableStateOf(HomeUiState(
        userLocation = UserLocation(40.4168, -3.7038, obtainedAtMillis = 1000L),
        hasLocationPermission = true, savedAtMillis = 1000L, officialDate = "23/10/2026"
    ))

    private fun launch() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                MapInformationControl(state.value, expanded.value, { expanded.value = it }, {}, {},
                    Modifier.fillMaxSize())
            }
        }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun open() {
        compose.onNodeWithContentDescription("Información de ubicación y precios").performClick()
        compose.mainClock.advanceTimeByFrame()
    }

    @Test fun hiddenUntilOpenedAndCanBeClosedManually() {
        launch()
        compose.onNodeWithText("Última ubicación:", substring = true).assertDoesNotExist()
        open()
        compose.onNodeWithText("Última ubicación:", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Precios del Ministerio:", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Cerrar información").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Información", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test fun closesAfterFiveSecondsDespiteDataUpdatesAndReopeningGetsNewTimeout() {
        launch(); open()
        compose.mainClock.advanceTimeBy(3_000)
        compose.runOnIdle { state.value = state.value.copy(officialDate = "Fecha actualizada") }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Fecha actualizada", substring = true).assertIsDisplayed()
        compose.mainClock.advanceTimeBy(1_800)
        compose.onNodeWithContentDescription("Cerrar información").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithContentDescription("Cerrar información").assertDoesNotExist()
        open()
        compose.mainClock.advanceTimeBy(3_000)
        compose.onNodeWithContentDescription("Cerrar información").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(2_100)
        compose.onNodeWithContentDescription("Cerrar información").assertDoesNotExist()
    }

    @Test fun infoIconAlsoClosesThePanelAndWarningsRetainRetryAction() {
        launch()
        compose.runOnIdle { state.value = state.value.copy(dataWarning = "Sin conexión. Datos guardados.") }
        open()
        compose.onNodeWithText("Sin conexión. Datos guardados.").assertIsDisplayed()
        compose.onNodeWithText("Reintentar").assertIsDisplayed()
        compose.onNodeWithContentDescription("Información de ubicación y precios").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithContentDescription("Cerrar información").assertDoesNotExist()
    }
}
