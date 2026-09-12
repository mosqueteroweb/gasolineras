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
    selectedFuel: FuelType,
    onSelectStation: (GasStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
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
            val cheapestStation = stations.minByOrNull { it.priceFor(selectedFuel) ?: Double.MAX_VALUE }
            val nearestStation = stations.minByOrNull { it.distanceMeters ?: Double.MAX_VALUE }

            // 2. Gas Stations Markers with highlights
            stations.forEach { station ->
                val stationPoint = GeoPoint(station.latitude, station.longitude)
                val price = station.formattedPriceFor(selectedFuel)
                val glpInfo = if (station.hasGLP) " • 🟢 GLP: ${station.formattedGlpPrice}" else ""

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
                            title = "🏆 MÁS BARATA: ${station.cleanBrand} ($price)"
                            snippet = "${station.address} • A ${station.formattedDistance}$glpInfo"
                        }
                        isNearest -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(245, 124, 0), // Orange
                                label = "⚡"
                            )
                            title = "⚡ MÁS CERCANA: ${station.cleanBrand} (A ${station.formattedDistance})"
                            snippet = "$price • ${station.address}$glpInfo"
                        }
                        station.hasGLP -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(0, 105, 92), // Teal
                                label = "G"
                            )
                            title = "${station.cleanBrand} ($price)"
                            snippet = "${station.address} • A ${station.formattedDistance}$glpInfo"
                        }
                        else -> {
                            icon = MapMarkerHelper.createGasStationMarker(
                                context = context,
                                pinColor = Color.rgb(25, 118, 210), // Primary Blue
                                label = "⛽"
                            )
                            title = "${station.cleanBrand} ($price)"
                            snippet = "${station.address} • A ${station.formattedDistance}"
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
