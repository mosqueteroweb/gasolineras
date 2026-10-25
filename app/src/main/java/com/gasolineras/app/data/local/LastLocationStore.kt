package com.gasolineras.app.data.local

import androidx.core.content.edit
import android.content.Context
import com.gasolineras.app.data.location.UserLocation

class LastLocationStore(context: Context) {
    private val prefs = context.getSharedPreferences("last_location", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean("remember", true)

    fun setEnabled(enabled: Boolean) {
        clear()
        prefs.edit { putBoolean("remember", enabled) }
    }

    fun read(): UserLocation {
        if (!isEnabled()) return UserLocation.DEFAULT
        val lat = Double.fromBits(prefs.getLong("latitude", Double.NaN.toBits()))
        val lon = Double.fromBits(prefs.getLong("longitude", Double.NaN.toBits()))
        val time = prefs.getLong("time", 0L)
        return if (lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0 && time > 0L && System.currentTimeMillis() - time in 0..86_400_000L) {
            UserLocation(lat, lon, obtainedAtMillis = time)
        } else {
            if (time > 0L) clear()
            UserLocation.DEFAULT
        }
    }

    fun clear() { prefs.edit { remove("latitude"); remove("longitude"); remove("time") } }

    fun write(location: UserLocation) {
        if (location.isDefaultLocation || !isEnabled()) return
        prefs.edit {
            putLong("latitude", location.latitude.toBits())
            putLong("longitude", location.longitude.toBits())
            putLong("time", location.obtainedAtMillis)
        }
    }
}
