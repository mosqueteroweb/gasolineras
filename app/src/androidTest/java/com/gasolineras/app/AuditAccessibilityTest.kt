package com.gasolineras.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.SortOption
import com.gasolineras.app.ui.components.FuelTypeSelector
import com.gasolineras.app.ui.components.SettingsBottomSheet
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuditAccessibilityTest {
    @get:Rule val compose = createComposeRule()

    @Test fun fuelControlsExposeNamesAndSelection() {
        val selected = mutableStateOf(setOf(FuelType.GNC))
        compose.setContent {
            MaterialTheme { FuelTypeSelector(selected.value, { fuel -> selected.value = setOf(fuel) }, listOf(FuelType.GNC, FuelType.GNL)) }
        }
        compose.onNodeWithContentDescription(FuelType.GNC.displayName).assertIsOn()
        compose.onNodeWithContentDescription(FuelType.GNL.displayName).assertIsOff().performClick().assertIsOn()
        compose.onNodeWithContentDescription(FuelType.GNC.displayName).assertIsOff()
    }

    @Test fun settingsCloseAndSaveRemainAvailableAtTwoHundredPercent() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MaterialTheme {
                    SettingsBottomSheet(listOf(FuelType.GASOLEO_A, FuelType.GASOLINA_95_E5, FuelType.GLP),
                        setOf(FuelType.GASOLEO_A), "LIST", 10.0, SortOption.CHEAPEST, "SYSTEM",
                        { _, _, _, _, _, _ -> }, {}, {})
                }
            }
        }
        compose.waitUntil(10_000) { compose.onNodeWithContentDescription("Cerrar").isDisplayed() && compose.onNodeWithText("Guardar preferencias").isDisplayed() }
        compose.onNodeWithContentDescription("Cerrar").assertIsDisplayed().assertHasClickAction()
        compose.onNodeWithText("Guardar preferencias").assertIsDisplayed().assertHasClickAction()
        compose.onNodeWithText("Cancelar y cerrar").assertIsDisplayed()
    }
}
