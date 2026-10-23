package com.gasolineras.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasolineras.app.ui.home.HomeUiState
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@Composable
fun HomeStatus(
    state: HomeUiState,
    onLocationAction: () -> Unit,
    onRetry: () -> Unit,
    showLocationInfo: Boolean = true,
    showPriceInfo: Boolean = true
) {
    val position = if (state.manualLocationName != null) {
        "Referencia manual: ${state.manualLocationName}. No es tu posición GPS."
    } else if (state.userLocation.isDefaultLocation) {
        "Madrid como referencia; no es tu ubicación real."
    } else {
        "Última ubicación: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES")).format(Date(state.userLocation.obtainedAtMillis))}."
    }
    val locationText = when {
        state.manualLocationName != null -> position
        !state.hasLocationPermission -> "Permiso de ubicación pendiente. $position Pulsa para activarlo."
        !state.isLocationEnabled -> "Ubicación desactivada. $position Pulsa para activarla."
        state.isLocating -> "Obteniendo tu ubicación… $position"
        state.locationMessage != null -> "${state.locationMessage} $position Pulsa para reintentar."
        state.userLocation.isDefaultLocation -> "$position Pulsa para obtener tu posición."
        else -> position
    }
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            if (showLocationInfo) {
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(enabled = !state.isLocating, onClick = onLocationAction)
                        .padding(vertical = 4.dp)
                )
            }
            if (showPriceInfo && state.isLoading) {
                Text("Consultando precios del Ministerio…", style = MaterialTheme.typography.bodySmall)
            }
            if (showPriceInfo && state.savedAtMillis > 0L) {
                val savedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
                    .format(Date(state.savedAtMillis))
                Text(
                    text = "Publicados por el Ministerio: ${state.officialDate?.takeIf { it.isNotBlank() } ?: "fecha no facilitada"} · Consultados: $savedDate",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (showPriceInfo && com.gasolineras.app.domain.util.DataFreshness.isPublicationOld(state.officialDate)) {
                Text("La publicación del Ministerio tiene más de 24 horas; una nueva consulta puede devolver los mismos precios.", style = MaterialTheme.typography.bodySmall)
            }
            state.dataWarning?.takeIf { showPriceInfo }?.let { warning ->
                Row {
                    Text(warning, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
                    TextButton(onClick = onRetry, enabled = !state.isRefreshing) { Text("Reintentar") }
                }
            }
        }
    }
}
