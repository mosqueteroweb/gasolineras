package com.gasolineras.app.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapView(
    userLocation: UserLocation,
    stations: List<GasStation>,
    selectedFuels: Set<FuelType>,
    onSelectStation: (GasStation) -> Unit,
    modifier: Modifier = Modifier,
    centerTrigger: Int = 0
) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.5)
            val center = GeoPoint(userLocation.latitude, userLocation.longitude)
            controller.setCenter(center)
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
        }
    }

    // Auto-center when GPS location updates or when user taps recenter button
    androidx.compose.runtime.LaunchedEffect(userLocation) {
        if (!userLocation.isDefaultLocation) {
            mapView.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude))
        }
    }

    androidx.compose.runtime.LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            mapView.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude))
        }
    }

    // Helper to calculate best price among selected fuels
    fun bestPriceFor(station: GasStation): Double? {
        val candidates = if (selectedFuels.isEmpty()) {
            station.prices.values
        } else {
            selectedFuels.mapNotNull { station.prices[it] }
        }
        return candidates.minOrNull()
    }

    // Helper for marker price string
    fun priceStringFor(station: GasStation): String {
        val parts = mutableListOf<String>()
        if (selectedFuels.contains(FuelType.GASOLEO_A) && station.priceFor(FuelType.GASOLEO_A) != null) {
            parts.add("D: ${station.formattedPriceFor(FuelType.GASOLEO_A)}")
        }
        if (selectedFuels.contains(FuelType.GASOLINA_95_E5) && station.priceFor(FuelType.GASOLINA_95_E5) != null) {
            parts.add("95: ${station.formattedPriceFor(FuelType.GASOLINA_95_E5)}")
        }
        if (selectedFuels.contains(FuelType.GLP) && station.priceFor(FuelType.GLP) != null) {
            parts.add("GLP: ${station.formattedPriceFor(FuelType.GLP)}")
        }
        return if (parts.isNotEmpty()) {
            parts.joinToString(" | ")
        } else {
            station.prices.values.minOrNull()?.let { String.format(java.util.Locale.US, "%.3f €/L", it) } ?: "--"
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            view.overlays.clear()

            // 1. Distinctive User Position Marker (Blue GPS circle with white ring & halo)
            val userPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
            val userMarker = Marker(view).apply {
                position = userPoint
                title = "📍 Tu posición actual"
                snippet = if (userLocation.isDefaultLocation) "Ubicación de referencia (Madrid)" else "Ubicación GPS en tiempo real"
                icon = MapMarkerHelper.createUserLocationMarker(context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            view.overlays.add(userMarker)

            // Calculate cheapest and nearest in the current list
            val cheapestStation = stations.minByOrNull { bestPriceFor(it) ?: Double.MAX_VALUE }
            val nearestStation = stations.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }

            // 2. Gas Stations Markers with highlights
            stations.forEach { station ->
                val stationPoint = GeoPoint(station.latitude, station.longitude)
                val priceDesc = priceStringFor(station)
                val glpInfo = if (station.hasGLP && !selectedFuels.contains(FuelType.GLP)) " • 🟢 GLP: ${station.formattedGlpPrice}" else ""

                val isCheapest = cheapestStation != null && station.id == cheapestStation.id
                val isNearest = nearestStation != null && station.id == nearestStation.id && !isCheapest

                val marker = Marker(view).apply {
                    position = stationPoint

                    when {
                        isCheapest -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(46, 125, 50), // Green
                                label = "🏆"
                            )
                            title = "🏆 MÁS BARATA: ${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}$glpInfo"
                        }
                        isNearest -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(245, 124, 0), // Orange
                                label = "⚡"
                            )
                            title = "⚡ MÁS CERCANA: ${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}$glpInfo"
                        }
                        station.hasGLP -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(0, 105, 92), // Teal
                                label = "G"
                            )
                            title = "${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}$glpInfo"
                        }
                        else -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(25, 118, 210), // Primary Blue
                                label = "⛽"
                            )
                            title = "${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}"
                        }
                    }

                    setAnchor(Marker.ANCHOR_CENTER, 0.86f)
                    setOnMarkerClickListener { clickedMarker, _ ->
                        clickedMarker.showInfoWindow()
                        onSelectStation(station)
                        true
                    }
                }
                view.overlays.add(marker)
            }

            view.invalidate()
        }
    )
}
