package com.gasolineras.app.ui.components

import androidx.core.net.toUri
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.gasolineras.app.domain.model.GasStation

object NavigationHelper {

    private const val GOOGLE_MAPS = "com.google.android.apps.maps"

    /**
     * Starts driving directions to the station in the user's navigation app.
     *
     * A plain `geo:` link only shows the place, and Google Maps ignores it while a route is
     * already running, so Google Maps gets its turn-by-turn intent, which replaces the active
     * route. Another navigator set as default for maps links (Waze, OsmAnd…) keeps `geo:`.
     */
    fun navigateToStation(context: Context, station: GasStation) {
        val destination = "${station.latitude},${station.longitude}"
        val label = Uri.encode(station.cleanBrand + " - " + station.address)
        val geoIntent = Intent(Intent.ACTION_VIEW, "geo:$destination?q=$destination($label)".toUri())
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, "google.navigation:q=$destination&mode=d".toUri())
            .setPackage(GOOGLE_MAPS)
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=driving&dir_action=navigate".toUri()
        )

        val attempts = if (prefersOtherNavigator(context, geoIntent)) {
            listOf(geoIntent, googleMapsIntent, webIntent)
        } else {
            listOf(googleMapsIntent, geoIntent, webIntent)
        }
        for (intent in attempts) {
            try {
                context.startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                // Try the next way of opening directions.
            }
        }
        Toast.makeText(context, "No se encontró ninguna aplicación de mapas.", Toast.LENGTH_SHORT).show()
    }

    /** True when the user chose a navigator other than Google Maps as default for maps links. */
    private fun prefersOtherNavigator(context: Context, geoIntent: Intent): Boolean {
        val defaultApp = context.packageManager
            .resolveActivity(geoIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
        // With no default the system resolves to its chooser ("android" or a resolver package).
        return defaultApp != null && defaultApp != GOOGLE_MAPS && defaultApp != "android" &&
            !defaultApp.contains("resolver") && !defaultApp.contains("intentresolver")
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
