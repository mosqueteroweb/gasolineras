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
import android.view.View
import android.view.ViewGroup
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

@Composable
fun OsmMapView(
    userLocation: UserLocation,
    stations: List<GasStation>,
    selectedFuels: Set<FuelType>,
    minPricePerFuel: Map<FuelType, Double> = emptyMap(),
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

    // Auto-center camera on GPS location once acquired or when centerTrigger changes
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

            // Determine cheapest prices per fuel across the displayed stations
            val activeFuels = if (selectedFuels.isEmpty()) FuelType.values().toSet() else selectedFuels
            val effectiveMinPrices = if (minPricePerFuel.isNotEmpty()) {
                minPricePerFuel
            } else {
                activeFuels.associateWith { fuel -> stations.mapNotNull { it.prices[fuel] }.minOrNull() }
                    .filterValues { it != null } as Map<FuelType, Double>
            }

            // Find nearest station
            val nearestStation = stations.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }

            // Configure the shared InfoWindow so that tapping the legend (bubble) opens the full detail card
            val sampleMarker = Marker(view)
            sampleMarker.infoWindow?.view?.let { infoView ->
                bindInfoWindowClicks(infoView) {
                    val activeMarker = (sampleMarker.infoWindow as? MarkerInfoWindow)?.markerReference
                    val currentStation = activeMarker?.relatedObject as? GasStation
                    if (currentStation != null) {
                        sampleMarker.infoWindow?.close()
                        onSelectStation(currentStation)
                    }
                }
            }

            // 2. Gas Stations Markers with highlights
            stations.forEach { station ->
                val stationPoint = GeoPoint(station.latitude, station.longitude)
                val priceDesc = priceStringFor(station)
                val glpInfo = if (station.hasGLP && !selectedFuels.contains(FuelType.GLP)) " • 🟢 GLP: ${station.formattedGlpPrice}" else ""

                // Check which fuels this station is cheapest for
                val cheapestFuels = activeFuels.filter { fuel ->
                    val p = station.prices[fuel]
                    val minP = effectiveMinPrices[fuel]
                    p != null && minP != null && p <= minP
                }
                val isCheapest = cheapestFuels.isNotEmpty()
                val isNearest = nearestStation != null && station.id == nearestStation.id && !isCheapest

                val marker = Marker(view).apply {
                    position = stationPoint
                    relatedObject = station

                    when {
                        isCheapest -> {
                            val fuelsLabel = cheapestFuels.joinToString(", ") { fuel ->
                                when (fuel) {
                                    FuelType.GASOLEO_A -> "Diésel"
                                    FuelType.GASOLINA_95_E5 -> "Gas 95"
                                    FuelType.GLP -> "GLP"
                                    else -> fuel.displayName
                                }
                            }
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(46, 125, 50), // Green
                                label = "🏆"
                            )
                            title = "🏆 ${station.cleanBrand}"
                            snippet = "Más barata en: $fuelsLabel\n$priceDesc • A ${station.formattedDistance}$glpInfo\n👉 Toca la leyenda para ver detalles"
                        }
                        isNearest -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(245, 124, 0), // Orange
                                label = "⚡"
                            )
                            title = "⚡ ${station.cleanBrand}"
                            snippet = "Más cercana • $priceDesc • A ${station.formattedDistance}$glpInfo\n👉 Toca la leyenda para ver detalles"
                        }
                        station.hasGLP -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(0, 105, 92), // Teal
                                label = "G"
                            )
                            title = "${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}$glpInfo\n👉 Toca la leyenda para ver detalles"
                        }
                        else -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(25, 118, 210), // Primary Blue
                                label = "⛽"
                            )
                            title = "${station.cleanBrand}"
                            snippet = "$priceDesc • A ${station.formattedDistance}\n👉 Toca la leyenda para ver detalles"
                        }
                    }

                    setAnchor(Marker.ANCHOR_CENTER, 0.86f)
                    // On marker click: ONLY show the info window (leyenda), do NOT open full card yet
                    setOnMarkerClickListener { clickedMarker, _ ->
                        clickedMarker.showInfoWindow()
                        true
                    }
                }
                view.overlays.add(marker)
            }

            view.invalidate()
        }
    )
}

private fun bindInfoWindowClicks(view: View, onClick: () -> Unit) {
    view.setOnClickListener { onClick() }
    view.setOnTouchListener { _, event ->
        if (event.action == android.view.MotionEvent.ACTION_UP) {
            onClick()
            true
        } else {
            false
        }
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            bindInfoWindowClicks(view.getChildAt(i), onClick)
        }
    }
}
