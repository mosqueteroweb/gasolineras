package com.gasolineras.app.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val isDefaultLocation: Boolean = false,
    val obtainedAtMillis: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT = UserLocation(40.416775, -3.703790, true, 0L)
    }
}

interface LocationProvider {
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    suspend fun getCurrentLocation(forceFresh: Boolean = false): UserLocation?
    fun observeLocations(): Flow<UserLocation>
}

class LocationClient(private val context: Context) : LocationProvider {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    override fun hasLocationPermission(): Boolean = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    override fun isLocationEnabled(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    override suspend fun getCurrentLocation(forceFresh: Boolean): UserLocation? {
        if (!hasLocationPermission() || !isLocationEnabled()) return null
        val token = CancellationTokenSource()
        return try {
            withTimeoutOrNull(10_000L) {
                if (!forceFresh) {
                    client.lastLocation.await()?.takeIf { it.isRecent(120_000L) }
                        ?.let { return@withTimeoutOrNull it.toUserLocation() }
                }
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(0L)
                    .setDurationMillis(8_000L)
                    .build()
                client.getCurrentLocation(request, token.token).await()
                    ?.takeIf { it.isRecent(30_000L) }?.toUserLocation()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: SecurityException) {
            null // The user can revoke permission between the check and the asynchronous request.
        } catch (_: Exception) {
            null
        } finally {
            token.cancel()
        }
    }

    // Collected only while the activity is STARTED; no background location permission.
    override fun observeLocations(): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission() || !isLocationEnabled()) {
            close()
            return@callbackFlow
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.takeIf { it.isRecent(30_000L) }
                    ?.let { trySend(it.toUserLocation()) }
            }
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(10_000L)
            .setMinUpdateDistanceMeters(100f)
            .build()
        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { close(it) }
        } catch (e: SecurityException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun Location.isRecent(maxAgeMillis: Long): Boolean =
        latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000 in 0..maxAgeMillis

    private fun Location.toUserLocation() = UserLocation(
        latitude, longitude, obtainedAtMillis = System.currentTimeMillis() -
            ((SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000).coerceAtLeast(0L)
    )
}
