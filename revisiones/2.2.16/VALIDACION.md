# Gass 2.2.16 — validación

Fecha: 3 de noviembre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.16 / código 36.

Origen: auditoría de UX en Galaxy Z Fold8 (hallazgo G1). Desde el mapa, el botón o gesto «atrás» cerraba la app en vez de volver a la lista.

- Cambio: `BackHandler` en `HomeScreen` que vuelve a la lista cuando se está en el mapa, salvo que el mapa sea la vista inicial elegida en Ajustes (entonces el mapa es la portada y «atrás» sale, como antes). Se declara antes del manejador de los paneles, así que una leyenda, menú o búsqueda abiertos se cierran primero.
- Pruebas unitarias correctas y lint sin errores.
- En emulador arm64 (Android 16), perfil de pantalla exterior del Fold (1080×2520): lista → mapa → atrás vuelve a la lista; atrás otra vez sale de la app.
