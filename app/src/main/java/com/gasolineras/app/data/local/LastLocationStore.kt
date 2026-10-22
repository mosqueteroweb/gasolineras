package com.gasolineras.app.data.local

import android.content.Context
import com.gasolineras.app.data.location.UserLocation

class LastLocationStore(context: Context) {
    private val prefs = context.getSharedPreferences("last_location", Context.MODE_PRIVATE)

    fun read(): UserLocation {
        val lat = Double.fromBits(prefs.getLong("latitude", Double.NaN.toBits()))
        val lon = Double.fromBits(prefs.getLong("longitude", Double.NaN.toBits()))
        val time = prefs.getLong("time", 0L)
        return if (lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0 && time > 0L) {
            UserLocation(lat, lon, obtainedAtMillis = time)
        } else UserLocation.DEFAULT
    }

    fun write(location: UserLocation) {
        if (location.isDefaultLocation) return
        prefs.edit().putLong("latitude", location.latitude.toBits())
            .putLong("longitude", location.longitude.toBits())
            .putLong("time", location.obtainedAtMillis).apply()
    }
}
