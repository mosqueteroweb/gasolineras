package com.gasolineras.app.ui.components

import androidx.core.net.toUri
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.gasolineras.app.domain.model.GasStation

object NavigationHelper {

    /**
     * Launches external map navigation app (Google Maps, Waze, etc.) to guide the driver to the gas station.
     */
    fun navigateToStation(context: Context, station: GasStation) {
        val label = Uri.encode(station.cleanBrand + " - " + station.address)
        // Intent URI format that works across Google Maps, Waze and OsmAnd
        val geoUri = "geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}($label)".toUri()
        val intent = Intent(Intent.ACTION_VIEW, geoUri)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser Google Maps directions
            val webUri = "https://www.google.com/maps/dir/?api=1&destination=${station.latitude},${station.longitude}".toUri()
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            try {
                context.startActivity(webIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "No se encontró ninguna aplicación de mapas.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Opens an external web page without crashing when the device has no browser. */
    fun openLink(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay ningún navegador para abrir el enlace.", Toast.LENGTH_SHORT).show()
        }
    }
}
