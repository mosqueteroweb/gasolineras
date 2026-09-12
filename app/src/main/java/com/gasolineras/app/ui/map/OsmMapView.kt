package com.gasolineras.app.ui.map

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.util.DistanceCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

private fun zoomLevelForRadius(radiusKm: Double): Double = when {
    radiusKm <= 3.5 -> 15.8
    radiusKm <= 12.0 -> 13.8
    radiusKm <= 30.0 -> 12.0
    else -> 9.8
}

@Composable
fun OsmMapView(
    userLocation: UserLocation,
    stations: List<GasStation>,
    selectedFuels: Set<FuelType>,
    minPricePerFuel: Map<FuelType, Double> = emptyMap(),
    selectedRadiusKm: Double = 10.0,
    onSelectStation: (GasStation) -> Unit,
    onVisibleCountChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    centerTrigger: Int = 0
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    var mapMoveTrigger by remember { mutableIntStateOf(0) }

    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.filesDir
            osmdroidTileCache = java.io.File(context.cacheDir, "osmdroid")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            val initialZoom = zoomLevelForRadius(selectedRadiusKm)
            controller.setZoom(initialZoom)
            val center = GeoPoint(userLocation.latitude, userLocation.longitude)
            controller.setCenter(center)

            // Listen to user map movements (pan & zoom) with debounce
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(350)
                        mapMoveTrigger++
                    }
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    debounceJob?.cancel()
                    debounceJob = coroutineScope.launch {
                        delay(350)
                        mapMoveTrigger++
                    }
                    return false
                }
            })
        }
    }

    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            debounceJob?.cancel()
            mapView.onPause()
        }
    }

    // On initial display, guarantee camera is centered and at optimal zoom for the initial radius
    LaunchedEffect(Unit) {
        val center = GeoPoint(userLocation.latitude, userLocation.longitude)
        val zoom = zoomLevelForRadius(selectedRadiusKm)
        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(center)
    }

    // Auto-center camera on GPS location once acquired if starting from default
    LaunchedEffect(userLocation) {
        if (!userLocation.isDefaultLocation) {
            val center = GeoPoint(userLocation.latitude, userLocation.longitude)
            mapView.controller.animateTo(center)
        }
    }

    // Re-center and adjust zoom ONLY when explicitly requested (centerTrigger / GPS recenter FAB)
    LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            val center = GeoPoint(userLocation.latitude, userLocation.longitude)
            val zoom = zoomLevelForRadius(selectedRadiusKm)
            mapView.controller.setZoom(zoom)
            mapView.controller.animateTo(center)
            mapMoveTrigger++
        }
    }

    // Helper for marker price string with large, clean numbers and no clutter
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
            parts.joinToString("   ")
        } else {
            station.prices.values.minOrNull()?.let { String.format(java.util.Locale.US, "%.3f €", it) } ?: "--"
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            // Reading mapMoveTrigger ensures this block re-runs whenever the user finishes zooming or panning
            @Suppress("UNUSED_VARIABLE")
            val trigger = mapMoveTrigger

            view.overlays.clear()

            // 1. User Position Marker
            val userPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
            val userMarker = Marker(view).apply {
                position = userPoint
                title = "📍 Tu posición"
                snippet = if (userLocation.isDefaultLocation) "Madrid (referencia)" else "GPS en tiempo real"
                icon = MapMarkerHelper.createUserLocationMarker(context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            view.overlays.add(userMarker)

            // 2. Filter stations within current visible BoundingBox (with 8% margin for seamless edge panning)
            val box = view.boundingBox
            val visibleStations = if (box != null && box.latNorth != box.latSouth) {
                val latMargin = (box.latNorth - box.latSouth) * 0.08
                val lonMargin = (box.lonEast - box.lonWest) * 0.08
                val minLat = box.latSouth - latMargin
                val maxLat = box.latNorth + latMargin
                val minLon = box.lonWest - lonMargin
                val maxLon = box.lonEast + lonMargin

                stations.filter { station ->
                    station.latitude in minLat..maxLat && station.longitude in minLon..maxLon
                }
            } else {
                stations
            }

            // Report visible station count back to HomeScreen for the pill
            onVisibleCountChanged(visibleStations.size)

            val activeFuels = if (selectedFuels.isEmpty()) FuelType.values().toSet() else selectedFuels

            // Calculate cheapest prices per fuel among VISIBLE stations
            val effectiveMinPrices = activeFuels.mapNotNull { fuel ->
                val minP = visibleStations.mapNotNull { it.prices[fuel] }.minOrNull()
                if (minP != null) fuel to minP else null
            }.toMap()

            // 3. Performance capping: If more than 80 stations are visible, select top 80
            // prioritizing cheapest prices, stations with GLP and proximity to screen center
            val centerGeo = view.mapCenter
            val displayStations = if (visibleStations.size > 80) {
                visibleStations.sortedWith(
                    compareBy<GasStation> { s ->
                        activeFuels.mapNotNull { s.prices[it] }.minOrNull() ?: Double.MAX_VALUE
                    }.thenBy { s ->
                        DistanceCalculator.calculateDistanceMeters(
                            centerGeo.latitude, centerGeo.longitude,
                            s.latitude, s.longitude
                        )
                    }
                ).take(80)
            } else {
                visibleStations
            }

            // Find nearest station to user GPS among displayed stations
            val nearestStation = displayStations.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }

            // Configure shared InfoWindow for tap-to-open detail
            val sampleMarker = Marker(view)
            sampleMarker.infoWindow?.view?.let { infoView ->
                val titleView = infoView.findViewById<android.widget.TextView>(org.osmdroid.library.R.id.bubble_title)
                val descView = infoView.findViewById<android.widget.TextView>(org.osmdroid.library.R.id.bubble_description)

                titleView?.textSize = 15f
                titleView?.setTypeface(null, android.graphics.Typeface.BOLD)

                descView?.textSize = 17f
                descView?.setTypeface(null, android.graphics.Typeface.BOLD)
                descView?.setTextColor(android.graphics.Color.parseColor("#1B5E20"))

                bindInfoWindowClicks(infoView) {
                    val activeMarker = (sampleMarker.infoWindow as? MarkerInfoWindow)?.markerReference
                    val currentStation = activeMarker?.relatedObject as? GasStation
                    if (currentStation != null) {
                        sampleMarker.infoWindow?.close()
                        onSelectStation(currentStation)
                    }
                }
            }

            // 4. Gas Stations Markers
            displayStations.forEach { station ->
                val stationPoint = GeoPoint(station.latitude, station.longitude)
                val priceDesc = priceStringFor(station)

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
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(46, 125, 50), // Green
                                label = "🏆"
                            )
                            title = "🏆 ${station.cleanBrand} (${station.formattedDistance})"
                            snippet = priceDesc
                        }
                        isNearest -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(245, 124, 0), // Orange
                                label = "⚡"
                            )
                            title = "⚡ ${station.cleanBrand} (${station.formattedDistance})"
                            snippet = priceDesc
                        }
                        station.hasGLP -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(0, 105, 92), // Teal
                                label = "G"
                            )
                            title = "🟢 ${station.cleanBrand} (${station.formattedDistance})"
                            snippet = priceDesc
                        }
                        else -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(25, 118, 210), // Primary Blue
                                label = "⛽"
                            )
                            title = "${station.cleanBrand} (${station.formattedDistance})"
                            snippet = priceDesc
                        }
                    }

                    setAnchor(Marker.ANCHOR_CENTER, 0.86f)
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
