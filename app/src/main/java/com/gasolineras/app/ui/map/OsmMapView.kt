package com.gasolineras.app.ui.map

import android.content.Context
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
            controller.setZoom(13.5)
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

            // 1. User Position Marker
            val userPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
            val userMarker = Marker(view).apply {
                position = userPoint
                title = "Tu posición"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            view.overlays.add(userMarker)

            // 2. Gas Stations Markers
            stations.forEach { station ->
                val stationPoint = GeoPoint(station.latitude, station.longitude)
                val price = station.formattedPriceFor(selectedFuel)
                val glpInfo = if (station.hasGLP) " [🟢 GLP: ${station.formattedGlpPrice}]" else ""

                val marker = Marker(view).apply {
                    position = stationPoint
                    title = "${station.cleanBrand} - $price$glpInfo"
                    snippet = station.address
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
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
