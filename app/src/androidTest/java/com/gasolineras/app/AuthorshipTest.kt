package com.gasolineras.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gasolineras.app.ui.components.AuthorshipDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthorshipTest {
    @get:Rule val compose = createComposeRule()

    @Test fun authorshipIsReadableAtLargeTextAndCloseRemainsAvailableWhileReadingLicenses() {
        var closed = false
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MaterialTheme { AuthorshipDialog { closed = true } }
            }
        }
        compose.onNodeWithText("Autoría").assertIsDisplayed()
        compose.onNodeWithText("Cerrar").assertIsDisplayed()
        compose.onNodeWithText("Gass ${BuildConfig.VERSION_NAME} · Mosqueteroweb").assertExists()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Cargando licencias…").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("authorship_content").performScrollToNode(hasText("Ver Apache 2.0"))
        compose.onNodeWithText("Ver Apache 2.0").performClick()
        compose.onNodeWithText("Ocultar Apache 2.0").assertIsDisplayed()
        compose.onNodeWithText("Cerrar").assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(closed) }
    }
}
