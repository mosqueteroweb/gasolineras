package com.gasolineras.app

import androidx.core.net.toUri
import androidx.core.content.edit
import android.Manifest
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.gasolineras.app.ui.home.HomeScreen
import com.gasolineras.app.ui.home.HomeViewModel
import com.gasolineras.app.ui.theme.GasolinerasTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) = viewModel.onLocationEnvironmentChanged()
                }
                ContextCompat.registerReceiver(this@MainActivity, receiver, IntentFilter().apply {
                    addAction(LocationManager.MODE_CHANGED_ACTION)
                    addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
                }, ContextCompat.RECEIVER_NOT_EXPORTED)
                viewModel.onForeground()
                try { awaitCancellation() } finally {
                    unregisterReceiver(receiver)
                    viewModel.onBackground()
                }
            }
        }
        val permissionHistory = getSharedPreferences("permission_history", MODE_PRIVATE)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            GasolinerasTheme(themeMode = state.themeMode) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                    if (fineGranted || coarseGranted) {
                        viewModel.onLocationPermissionGranted()
                    }
                }

                val requestPermission = {
                    val requested = permissionHistory.getBoolean("location_requested", false)
                    val canExplain = ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (requested && !canExplain) {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            "package:$packageName".toUri()))
                    } else {
                        permissionHistory.edit { putBoolean("location_requested", true) }
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                }
                LaunchedEffect(Unit) {
                    if (!state.hasLocationPermission &&
                        !permissionHistory.getBoolean("location_requested", false)) {
                        requestPermission()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        viewModel = viewModel,
                        onRequestLocationPermission = {
                            when {
                                !state.hasLocationPermission -> requestPermission()
                                !state.isLocationEnabled -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                else -> viewModel.recenterLocation()
                            }
                        }
                    )
                }
            }
        }
    }
}
