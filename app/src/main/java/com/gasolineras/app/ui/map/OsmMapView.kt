package com.gasolineras.app.ui.map

import android.graphics.Color
import android.view.View
import android.view.MotionEvent
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
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

private fun zoomLevelForRadius(radiusKm: Double): Double = when {
    radiusKm <= 3.5 -> 14.4
    radiusKm <= 12.0 -> 12.5
    radiusKm <= 30.0 -> 10.9
    else -> 8.9
}

private fun createGeodesicCircle(
    center: GeoPoint,
    radiusMeters: Double,
    pointsCount: Int = 64
): ArrayList<GeoPoint> {
    val circlePoints = ArrayList<GeoPoint>(pointsCount)
    val earthRadius = 6378137.0 // WGS84 major axis in meters
    val dR = radiusMeters / earthRadius
    val centerLatRad = Math.toRadians(center.latitude)
    val centerLonRad = Math.toRadians(center.longitude)

    for (i in 0 until pointsCount) {
        val bearing = 2.0 * Math.PI * i / pointsCount
        val latRad = Math.asin(
            Math.sin(centerLatRad) * Math.cos(dR) +
            Math.cos(centerLatRad) * Math.sin(dR) * Math.cos(bearing)
        )
        val lonRad = centerLonRad + Math.atan2(
            Math.sin(bearing) * Math.sin(dR) * Math.cos(centerLatRad),
            Math.cos(dR) - Math.sin(centerLatRad) * Math.sin(latRad)
        )
        circlePoints.add(GeoPoint(Math.toDegrees(latRad), Math.toDegrees(lonRad)))
    }
    return circlePoints
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
    centerTrigger: Int = 0,
    referenceName: String? = null,
    comparisonFuel: FuelType = FuelType.default,
    onMapCenterChanged: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    var followGps by remember { mutableStateOf(true) }
    var mapMoveTrigger by remember { mutableIntStateOf(0) }

    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = "${context.packageName}/${com.gasolineras.app.BuildConfig.VERSION_NAME} (https://github.com/mosqueteroweb/gasolineras)"
            osmdroidBasePath = context.filesDir
            osmdroidTileCache = java.io.File(context.cacheDir, "osmdroid")
        }
        MapView(context).apply {
            setTileSource(org.osmdroid.tileprovider.tilesource.XYTileSource("OpenStreetMap", 0, 19, 256, ".png", arrayOf("https://tile.openstreetmap.org/"), "© OpenStreetMap contributors"))
            setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_MOVE) followGps = false
                false
            }
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
            mapView.onDetach()
        }
    }

    // On initial display, guarantee camera is centered and at optimal zoom for the initial radius
    LaunchedEffect(Unit) {
        val center = GeoPoint(userLocation.latitude, userLocation.longitude)
        val zoom = zoomLevelForRadius(selectedRadiusKm)
        mapView.controller.setZoom(zoom)
        mapView.controller.setCenter(center)
    }

    LaunchedEffect(mapMoveTrigger) {
        onMapCenterChanged(mapView.mapCenter.latitude, mapView.mapCenter.longitude)
    }

    LaunchedEffect(userLocation.latitude, userLocation.longitude, referenceName) {
        if (followGps || referenceName != null) {
            mapView.controller.animateTo(GeoPoint(userLocation.latitude, userLocation.longitude))
        }
    }

    // Timestamp-only GPS updates never move the camera; touching the map pauses following.
    // Re-center and adjust zoom when distance radius changes
    LaunchedEffect(selectedRadiusKm) {
        val center = GeoPoint(userLocation.latitude, userLocation.longitude)
        val zoom = zoomLevelForRadius(selectedRadiusKm)
        mapView.controller.setZoom(zoom)
        mapView.controller.animateTo(center)
        mapMoveTrigger++
    }

    // Re-center and adjust zoom ONLY when explicitly requested (centerTrigger / GPS recenter FAB)
    LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            followGps = true
            val center = GeoPoint(userLocation.latitude, userLocation.longitude)
            val zoom = zoomLevelForRadius(selectedRadiusKm)
            mapView.controller.setZoom(zoom)
            mapView.controller.animateTo(center)
            mapMoveTrigger++
        }
    }

    // Helper for marker price string dynamically formatting selected fuels
    fun priceStringFor(station: GasStation): String {
        val parts = selectedFuels.mapNotNull { fuel ->
            val price = station.priceFor(fuel)
            if (price != null && price > 0.0) {
                "${fuel.shortName}: ${station.formattedPriceFor(fuel)}"
            } else null
        }
        return if (parts.isNotEmpty()) {
            parts.joinToString("   ")
        } else {
            station.prices[comparisonFuel]?.let { comparisonFuel.formatPrice(it) } ?: "--"
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

            val userPoint = GeoPoint(userLocation.latitude, userLocation.longitude)

            // 1. Radius Circle Overlay (Visual delimitator for selected distance mode)
            val circleOverlay = Polygon(view).apply {
                points = createGeodesicCircle(userPoint, selectedRadiusKm * 1000.0)
                fillPaint.color = Color.argb(22, 33, 150, 243) // Translucent soft blue (~8.5%)
                outlinePaint.color = Color.argb(160, 25, 118, 210) // Subtle blue border
                outlinePaint.strokeWidth = 2.0f * context.resources.displayMetrics.density
                infoWindow = null
                setOnClickListener { _, _, _ -> false }
            }
            view.overlays.add(circleOverlay)

            // 2. User Position Marker
            val userMarker = Marker(view).apply {
                position = userPoint
                title = if (referenceName != null) "Referencia: $referenceName" else "📍 Última posición"
                snippet = if (referenceName != null) "Ubicación manual; no es tu posición GPS" else if (userLocation.isDefaultLocation) "Madrid (referencia)" else "Última posición obtenida; consulta su fecha en Información"
                icon = MapMarkerHelper.createUserLocationMarker(context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            view.overlays.add(userMarker)

            // 3. Filter stations within current visible BoundingBox (with 8% margin for seamless edge panning)
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

            // Calculate cheapest prices per fuel (respecting minPricePerFuel from area or visible stations)
            val effectiveMinPrices = activeFuels.mapNotNull { fuel ->
                val minP = minPricePerFuel[fuel] ?: visibleStations.mapNotNull { it.prices[fuel] }.minOrNull()
                if (minP != null) fuel to minP else null
            }.toMap()

            // 3. Performance capping: If more than 80 stations are visible, select top 80
            // prioritizing cheapest prices, stations with GLP and proximity to screen center
            val centerGeo = view.mapCenter
            val displayStations = if (visibleStations.size > 80) {
                visibleStations.sortedWith(
                    compareBy<GasStation> { s ->
                        s.prices[comparisonFuel] ?: Double.MAX_VALUE
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
                        station.hasGLP && selectedFuels.contains(FuelType.GLP) -> {
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
