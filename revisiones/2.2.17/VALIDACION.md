# Gass 2.2.17 — validación

Fecha: 3 de noviembre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.17 / código 37.

Origen: aviso del usuario. «Ir» (lista) y «Cómo llegar con GPS» (ficha, también desde el mapa) no llevaban bien a la gasolinera, sobre todo con una ruta ya activa en Google Maps.

Causa: se abría `geo:lat,lng?q=lat,lng(etiqueta)`. En Google Maps ese enlace solo muestra el sitio (no calcula la ruta) y, si ya hay una navegación en marcha, Maps vuelve a la ruta anterior y descarta el destino nuevo.

Cambio en `NavigationHelper.navigateToStation`:

- Con Google Maps instalado se abre `google.navigation:q=lat,lng&mode=d` en el paquete de Maps: navegación en coche paso a paso que sustituye la ruta activa.
- Si el usuario tiene otro navegador como app predeterminada para enlaces de mapas (Waze, OsmAnd…), se mantiene `geo:` para respetar su elección.
- Respaldo final: enlace web de Google Maps con `dir_action=navigate`.
- `<queries>` del manifiesto incluye el paquete de Google Maps para poder abrirlo en Android 11+.

Comprobación:

- Pruebas unitarias correctas y lint sin errores.
- Emulador arm64 (Android 16) con Google Maps y una navegación previa activa: «Ir» desde la lista y «Cómo llegar con GPS» desde la ficha lanzan `google.navigation:` en `MapsActivity` (registro de `ActivityTaskManager`). El emulador no dibuja la navegación de Maps, así que el cambio de ruta en pantalla queda por confirmar en el teléfono.
