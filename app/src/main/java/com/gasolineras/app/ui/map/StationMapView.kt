package com.gasolineras.app.ui.map

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gasolineras.app.data.location.UserLocation
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.util.DistanceCalculator
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.math.*

private fun zoomForRadius(km: Double, latitude: Double, context: android.content.Context): Double {
    val metrics = context.resources.displayMetrics
    val sideDp = min(metrics.widthPixels, metrics.heightPixels) / metrics.density
    return log2(cos(Math.toRadians(latitude)) * 78271.51696 * sideDp / (km * 2400)).coerceIn(0.0, 19.0)
}

private fun centerOnRadius(map: MapLibreMap, context: android.content.Context, location: UserLocation, radiusKm: Double) {
    val bounds = LatLngBounds.Builder().includes(radiusCircle(location.latitude, location.longitude, radiusKm * 1000)).build()
    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, (16 * context.resources.displayMetrics.density).toInt()))
}

internal fun radiusCircle(lat: Double, lon: Double, meters: Double): List<LatLng> {
    val latitude = Math.toRadians(lat)
    val longitude = Math.toRadians(lon)
    val distance = meters / 6378137.0
    return (0..64).map { index ->
        val bearing = 2 * PI * index / 64
        val a = asin(sin(latitude) * cos(distance) + cos(latitude) * sin(distance) * cos(bearing))
        val b = longitude + atan2(sin(bearing) * sin(distance) * cos(latitude), cos(distance) - sin(latitude) * sin(a))
        LatLng(Math.toDegrees(a), Math.toDegrees(b))
    }
}

// Local raster style: no remote style, fonts, sprites, key, analytics or bulk downloads.
private const val OSM_STYLE = """{"version":8,"sources":{"osm":{"type":"raster","tiles":["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],"tileSize":256,"maxzoom":19,"attribution":"© OpenStreetMap contributors"}},"layers":[{"id":"osm","type":"raster","source":"osm","paint":{"raster-fade-duration":0}}]}"""

@Suppress("DEPRECATION")
@Composable
fun StationMapView(
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
    onMapCenterChanged: (Double, Double) -> Unit = { _, _ -> },
    mapStyleJson: String = OSM_STYLE
) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val select by rememberUpdatedState(onSelectStation)
    val visibleCount by rememberUpdatedState(onVisibleCountChanged)
    val centerChanged by rememberUpdatedState(onMapCenterChanged)
    var followGps by rememberSaveable { mutableStateOf(true) }
    var cameraLat by rememberSaveable { mutableDoubleStateOf(userLocation.latitude) }
    var cameraLon by rememberSaveable { mutableDoubleStateOf(userLocation.longitude) }
    var cameraZoom by rememberSaveable { mutableDoubleStateOf(zoomForRadius(selectedRadiusKm, userLocation.latitude, context)) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var idle by remember { mutableIntStateOf(0) }
    var mapError by remember { mutableStateOf(false) }
    var lastRadius by rememberSaveable { mutableDoubleStateOf(selectedRadiusKm) }
    val view = remember {
        MapResources.initialize(context)
        MapView(context).apply {
            onCreate(null)
            contentDescription = "Mapa de gasolineras. La misma información está disponible en Ver lista."
            setMaximumFps(30)
            var downX = 0f
            var downY = 0f
            val slop = android.view.ViewConfiguration.get(context).scaledTouchSlop
            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y }
                    android.view.MotionEvent.ACTION_MOVE -> if (hypot(event.x - downX, event.y - downY) > slop) followGps = false
                }
                false
            }
        }
    }
    DisposableEffect(view, owner) {
        val lifecycle = MapLifecycle(object : MapLifecycle.Target {
            override fun start() = view.onStart()
            override fun resume() = view.onResume()
            override fun pause() = view.onPause()
            override fun stop() { view.onStop(); MapResources.pauseRequests() }
            override fun destroy() = view.onDestroy()
        })
        owner.lifecycle.addObserver(lifecycle)
        lifecycle.sync(owner.lifecycle.currentState)
        val memory = object : android.content.ComponentCallbacks {
            override fun onConfigurationChanged(config: android.content.res.Configuration) {}
            override fun onLowMemory() = view.onLowMemory()
        }
        context.registerComponentCallbacks(memory)
        onDispose {
            owner.lifecycle.removeObserver(lifecycle)
            context.unregisterComponentCallbacks(memory)
            lifecycle.dispose()
        }
    }
    DisposableEffect(view) {
        val failed = MapView.OnDidFailLoadingMapListener { mapError = true }
        view.addOnDidFailLoadingMapListener(failed)
        view.getMapAsync { ready ->
            ready.uiSettings.apply {
                isLogoEnabled = false
                isAttributionEnabled = false // Visible OSM link supplied by HomeScreen, full licenses in Autoría.
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
            }
            ready.setPrefetchZoomDelta(0)
            ready.cameraPosition = CameraPosition.Builder().target(LatLng(cameraLat, cameraLon)).zoom(cameraZoom).build()
            ready.addOnCameraMoveStartedListener { reason ->
                if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) followGps = false
            }
            ready.addOnCameraIdleListener {
                val camera = ready.cameraPosition
                camera.target?.let { cameraLat = it.latitude; cameraLon = it.longitude; centerChanged(it.latitude, it.longitude) }
                cameraZoom = camera.zoom
                idle++
            }
            ready.setStyle(Style.Builder().fromJson(mapStyleJson)) {
                map = ready
                mapError = false
                ready.cameraPosition.target?.let { centerChanged(it.latitude, it.longitude) }
                idle++
            }
        }
        onDispose { view.removeOnDidFailLoadingMapListener(failed) }
    }
    val selectedStationByMarker = remember { mutableMapOf<Long, GasStation>() }
    DisposableEffect(map) {
        map?.setOnMarkerClickListener { marker ->
            selectedStationByMarker[marker.id]?.let { select(it) }
            true
        }
        onDispose { map?.setOnMarkerClickListener(null) }
    }
    LaunchedEffect(map, userLocation.latitude, userLocation.longitude, referenceName, selectedRadiusKm, centerTrigger) {
        val ready = map ?: return@LaunchedEffect
        if (followGps || referenceName != null) {
            centerOnRadius(ready, context, userLocation, selectedRadiusKm)
        }
    }
    LaunchedEffect(selectedRadiusKm) {
        if (lastRadius != selectedRadiusKm) {
            lastRadius = selectedRadiusKm
            map?.let { centerOnRadius(it, context, userLocation, selectedRadiusKm) }
        }
    }
    LaunchedEffect(centerTrigger) {
        if (centerTrigger > 0) {
            followGps = true
            map?.let { centerOnRadius(it, context, userLocation, selectedRadiusKm) }
        }
    }
    LaunchedEffect(map, idle, stations, selectedFuels, minPricePerFuel, userLocation.latitude, userLocation.longitude, referenceName, selectedRadiusKm, comparisonFuel) {
        val ready = map ?: return@LaunchedEffect
        val box = ready.projection.visibleRegion.latLngBounds
        val visible = stations.filter { box.contains(LatLng(it.latitude, it.longitude)) }
        visibleCount(visible.size)
        val displayed = visible.sortedWith(compareBy<GasStation> { it.prices[comparisonFuel] ?: Double.MAX_VALUE }
            .thenBy { DistanceCalculator.calculateDistanceMeters(cameraLat, cameraLon, it.latitude, it.longitude) }).take(80)
        ready.clear()
        selectedStationByMarker.clear()
        ready.addPolygon(PolygonOptions().addAll(radiusCircle(userLocation.latitude, userLocation.longitude, selectedRadiusKm * 1000))
            .fillColor(Color.rgb(33,150,243)).alpha(0.12f).strokeColor(Color.rgb(25,118,210)))
        val factory = IconFactory.getInstance(context)
        ready.addMarker(MarkerOptions().position(LatLng(userLocation.latitude, userLocation.longitude))
            .title(referenceName?.let { "Referencia: $it" } ?: "Última posición GPS")
            .icon(factory.fromBitmap((MapMarkerHelper.createUserLocationMarker(context) as BitmapDrawable).bitmap)))
        val nearest = displayed.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }?.id
        // Only three shared bitmap icons per redraw, rather than one allocation per station.
        val icons = listOf(Color.rgb(46,125,50), Color.rgb(245,124,0), Color.rgb(25,118,210)).mapIndexed { i, color ->
            factory.fromBitmap((MapMarkerHelper.createGasStationMarker(context, color, listOf("🏆","📍","⛽")[i]) as BitmapDrawable).bitmap)
        }
        displayed.forEach { station ->
            val cheapest = selectedFuels.any { fuel -> station.prices[fuel]?.let { it <= (minPricePerFuel[fuel] ?: -1.0) } == true }
            val icon = if (cheapest) icons[0] else if (station.id == nearest) icons[1] else icons[2]
            val marker = ready.addMarker(MarkerOptions().position(LatLng(station.latitude, station.longitude))
                .title(station.cleanBrand).snippet(selectedFuels.mapNotNull { fuel -> station.prices[fuel]?.let { "${fuel.shortName}: ${fuel.formatPrice(it)}" } }.joinToString(" · "))
                .icon(icon))
            selectedStationByMarker[marker.id] = station
        }
    }
    androidx.compose.foundation.layout.Box(modifier.fillMaxSize()) {
        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().semantics { contentDescription = "Mapa de gasolineras. La misma información está disponible en Ver lista." })
        if (mapError) androidx.compose.material3.Text("No se pudo cargar el mapa. Puedes consultar las gasolineras en Ver lista.")
    }
}
