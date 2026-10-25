package com.gasolineras.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val BluePrimary = Color(0xFF1976D2)
val BlueDark = Color(0xFF0D47A1)
val BlueLight = Color(0xFFBBDEFB)

val CheapGreen = Color(0xFF2E7D32)
val CheapGreenContainer = Color(0xFFE8F5E9)

val ModerateOrange = Color(0xFFF57C00)
val ModerateOrangeContainer = Color(0xFFFFF3E0)

val ExpensiveRed = Color(0xFFC62828)
val ExpensiveRedContainer = Color(0xFFFFEBEE)

val SurfaceLight = Color(0xFFFFFFFF)
val BackgroundLight = Color(0xFFF8F9FA)
val TextPrimaryLight = Color(0xFF1C1B1F)
val TextSecondaryLight = Color(0xFF49454F)

val CardBorder = Color(0xFFE0E0E0)

/** Highlight colours for prices: the light-mode pastels glare on a dark background. */
data class PriceHighlightColors(val container: Color, val label: Color, val price: Color)

@Composable
fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
fun cheapestHighlight(): PriceHighlightColors = if (isAppInDarkTheme())
    PriceHighlightColors(Color(0xFF1E3B22), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
else PriceHighlightColors(Color(0xFFC8E6C9), Color(0xFF1B5E20), Color(0xFF2E7D32))

@Composable
fun glpHighlight(): PriceHighlightColors = if (isAppInDarkTheme())
    PriceHighlightColors(Color(0xFF123A37), Color(0xFFB2DFDB), Color(0xFF80CBC4))
else PriceHighlightColors(Color(0xFFE0F2F1), Color(0xFF004D40), Color(0xFF00695C))
