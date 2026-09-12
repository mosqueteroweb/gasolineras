package com.gasolineras.app.domain.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates great-circle distance between two geographic coordinates using Haversine formula.
     * @return Distance in meters.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Formats distance in meters into human-readable string (e.g. "350 m" or "2.4 km").
     */
    fun formatDistance(meters: Double?): String {
        if (meters == null) return "-- km"
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(java.util.Locale.US, "%.1f km", meters / 1000.0)
        }
    }
}
