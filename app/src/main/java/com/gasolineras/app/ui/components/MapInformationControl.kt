package com.gasolineras.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasolineras.app.ui.home.HomeUiState
import kotlinx.coroutines.delay

@Composable
fun MapInformationControl(
    state: HomeUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onLocationAction: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnExpandedChange = rememberUpdatedState(onExpandedChange)
    // Data/GPS updates do not restart the five seconds counted from opening.
    LaunchedEffect(expanded) {
        if (expanded) {
            delay(5_000L)
            currentOnExpandedChange.value(false)
        }
    }
    Box(modifier) {
        if (expanded) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
                    .widthIn(max = 360.dp)
                    .heightIn(max = 300.dp)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp)
                    ) {
                        Text("Información", style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { onExpandedChange(false) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar información")
                        }
                    }
                    HomeStatus(state, onLocationAction, onRetry)
                }
            }
        }
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(end = 80.dp, bottom = 16.dp).size(52.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = "Información de ubicación y precios")
        }
    }
}
