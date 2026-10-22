package com.gasolineras.app.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val isDefaultLocation: Boolean = false
) {
    companion object {
        // Madrid Puerta del Sol as fallback coordinate
        val DEFAULT = UserLocation(
            latitude = 40.416775,
            longitude = -3.703790,
            isDefaultLocation = true
        )
    }
}

class LocationClient(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient? by lazy {
        try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (t: Throwable) {
            null
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Attempts to obtain real device GPS/network location.
     * When forceFresh is true, it skips any stale cached lastLocation and directly queries
     * the FusedLocationProviderClient with PRIORITY_HIGH_ACCURACY.
     */
    suspend fun getCurrentLocation(forceFresh: Boolean = false): UserLocation {
        if (!hasLocationPermission()) {
            return UserLocation.DEFAULT
        }

        val client = fusedLocationClient ?: return UserLocation.DEFAULT

        return try {
            kotlinx.coroutines.withTimeoutOrNull(5000L) {
                if (!forceFresh) {
                    val lastKnown = try { client.lastLocation.await() } catch (e: Exception) { null }
                    if (lastKnown != null) {
                        return@withTimeoutOrNull UserLocation(
                            latitude = lastKnown.latitude,
                            longitude = lastKnown.longitude,
                            isDefaultLocation = false
                        )
                    }
                }

                val cancellationTokenSource = CancellationTokenSource()
                val location = client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()

                if (location != null) {
                    UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isDefaultLocation = false
                    )
                } else {
                    val lastFallback = try { client.lastLocation.await() } catch (e: Exception) { null }
                    if (lastFallback != null) {
                        UserLocation(
                            latitude = lastFallback.latitude,
                            longitude = lastFallback.longitude,
                            isDefaultLocation = false
                        )
                    } else {
                        UserLocation.DEFAULT
                    }
                }
            } ?: UserLocation.DEFAULT
        } catch (e: Throwable) {
            UserLocation.DEFAULT
        }
    }
}
