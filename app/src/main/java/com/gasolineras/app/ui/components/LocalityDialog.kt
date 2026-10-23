package com.gasolineras.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasolineras.app.ui.home.HomeUiState
import com.gasolineras.app.ui.home.HomeViewModel
import java.text.Normalizer

@Composable
fun LocalityDialog(state: HomeUiState, viewModel: HomeViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    fun normalized(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").lowercase(java.util.Locale.ROOT)
    val index = remember(state.localityChoices) {
        state.localityChoices.map { Triple(it, normalized(it.municipality), normalized("${it.municipality} ${it.province}")) }
    }
    val results = remember(query, index) {
        val text = normalized(query.trim())
        index.filter { text.isNotBlank() && it.third.contains(text) }
            .sortedBy { if (it.second == text) 0 else if (it.second.startsWith(text)) 1 else 2 }
            .map { it.first }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Elegir municipio") },
        text = {
            Column {
                Text("Referencia aproximada de una gasolinera del municipio. No es una ubicación GPS.")
                OutlinedTextField(value = query, onValueChange = { query = it.take(100) }, label = { Text("Municipio o provincia") }, singleLine = true)
                if (state.isLoadingLocalities) CircularProgressIndicator()
                LazyColumn(Modifier.heightIn(max = 280.dp)) {
                    items(results, key = { "${it.municipality}/${it.province}" }) { station ->
                        TextButton(onClick = {
                            viewModel.useManualLocation(station.latitude, station.longitude, "${station.municipality}, ${station.province}")
                            onDismiss()
                        }) { Text("${station.municipality}, ${station.province}") }
                    }
                    if (!state.isLoadingLocalities && query.isNotBlank() && results.isEmpty()) item { Text("Sin coincidencias. Necesitas una consulta de precios guardada o conexión.") }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}
