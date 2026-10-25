package com.gasolineras.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gasolineras.app.BuildConfig
import com.gasolineras.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AuthorshipDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var licenses by remember { mutableStateOf<List<Pair<String,List<String>>>>(emptyList()) }
    LaunchedEffect(Unit) {
        licenses = withContext(Dispatchers.IO) {
            context.assets.list("licenses").orEmpty().sorted().map { file ->
                file.removeSuffix(".txt").replace('_', ' ') to context.assets.open("licenses/$file").bufferedReader().use { it.readText().chunked(3000) }
            }
        }
    }
    LaunchedEffect(licenses) { if (licenses.isNotEmpty()) loading = false }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).semantics { paneTitle = "Autoría" }) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Autoría", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
                LazyColumn(Modifier.weight(1f).testTag("authorship_content")) {
                    item {
                        Image(painterResource(R.drawable.ic_mosqueteroweb), contentDescription = "Mosqueteroweb", modifier = Modifier.size(48.dp))
                        Text("Gass ${BuildConfig.VERSION_NAME} · Mosqueteroweb", style = MaterialTheme.typography.titleMedium)
                        Text("Aplicación de código libre · GNU GPLv3")
                        TextButton(onClick = { NavigationHelper.openLink(context, "https://github.com/mosqueteroweb/gasolineras") }) { Text("Código fuente y contacto") }
                        Text("Datos de gasolineras: Ministerio MITECO. Se conserva la fecha de publicación proporcionada por el Ministerio.")
                        TextButton(onClick = { NavigationHelper.openLink(context, "https://geoportalgasolineras.es/") }) { Text("Fuente de los precios") }
                        Text("Cartografía © OpenStreetMap contributors · Datos bajo ODbL 1.0. Servicio de mapas de OpenStreetMap Foundation.")
                        TextButton(onClick = { NavigationHelper.openLink(context, "https://www.openstreetmap.org/copyright") }) { Text("OpenStreetMap: autoría y licencia") }
                        Text("Bibliotecas y licencias", style = MaterialTheme.typography.titleLarge)
                        Text("Los avisos y textos completos siguientes se incluyen en la aplicación y pueden consultarse sin conexión.")
                    }
                    if (loading) item { Text("Cargando licencias…") }
                    licenses.forEach { (title, sections) ->
                        item(key = title) {
                            TextButton(onClick = { expanded[title] = expanded[title] != true }, modifier = Modifier.fillMaxWidth()) {
                                Text((if (expanded[title] == true) "Ocultar " else "Ver ") + title)
                            }
                        }
                        if (expanded[title] == true) items(sections.size, key = { "$title-$it" }) { index ->
                            Text(sections[index], style = MaterialTheme.typography.bodySmall)
                        }
                        item(key = "$title-divider") { HorizontalDivider() }
                    }
                }
            }
        }
    }
}
