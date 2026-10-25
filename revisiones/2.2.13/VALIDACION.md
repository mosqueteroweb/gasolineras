# Gass 2.2.13 — validación

Fecha: 25 de octubre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.13 / código 33.

- Compilación con Gradle vacío, `--refresh-dependencies` y `--dependency-verification strict` (`testDebugUnitTest`, `lintDebug`, `assembleRelease`): correcta y sin cambios en `verification-metadata.xml`, ya sin Gradle Plugin Portal.
- 36 pruebas unitarias y 21 instrumentadas (emulador arm64, Android 16) correctas. Lint: 0 errores.
- Permisos del APK de release: `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_NETWORK_STATE`. Ya no aparece `ACCESS_WIFI_STATE`.
- Instalación sobre 2.2.12 conservando datos; mapa con teselas, marcadores y ubicación en Madrid. Un primer intento de descarga de precios falló coincidiendo con un cambio de red del emulador (Wi-Fi a móvil); «Reintentar» cargó los datos.
