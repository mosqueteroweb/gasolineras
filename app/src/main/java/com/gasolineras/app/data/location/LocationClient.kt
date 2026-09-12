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

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

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
     * Returns UserLocation or fallback if location cannot be retrieved or permissions are missing.
     */
    suspend fun getCurrentLocation(): UserLocation {
        if (!hasLocationPermission()) {
            return UserLocation.DEFAULT
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    isDefaultLocation = false
                )
            } else {
                // Fallback to last known location
                val lastKnown = fusedLocationClient.lastLocation.await()
                if (lastKnown != null) {
                    UserLocation(
                        latitude = lastKnown.latitude,
                        longitude = lastKnown.longitude,
                        isDefaultLocation = false
                    )
                } else {
                    UserLocation.DEFAULT
                }
            }
        } catch (e: SecurityException) {
            UserLocation.DEFAULT
        } catch (e: Exception) {
            UserLocation.DEFAULT
        }
    }
}
