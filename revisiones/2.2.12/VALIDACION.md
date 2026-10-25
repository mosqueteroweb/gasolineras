# Gass 2.2.12 — cambios y comprobaciones

Fecha: 25 de octubre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.12 / código 32.

## Cambios

- **Tamaño del APK.** `abiFilters` limitado a `arm64-v8a` y `armeabi-v7a`. MapLibre aporta unos 11 MB de código nativo por arquitectura; `x86` y `x86_64` solo servían a emuladores. Release: 44,3 MB → 21,6 MB. Lint avisa de la falta de `x86_64` para ChromeOS (`ChromeOsAbiSupport`); se acepta.
- **Herramientas y SDK.** Gradle 8.11.1 → 8.14.6 (suma oficial de la distribución), Android Gradle Plugin 8.10.1 → 8.13.2, `compileSdk` y `targetSdk` 35 → 36. Se retira `android.suppressUnsupportedCompileSdk`.
- **Dependencias.** AndroidX Core 1.13.1 → 1.18.0, Lifecycle 2.8.4 → 2.10.0, Activity Compose 1.9.1 → 1.13.0, Play Services Location 21.3.0 → 21.4.0, AndroidX Test JUnit 1.2.1 → 1.3.0, Espresso 3.6.1 → 3.7.0. Core 1.19 y Lifecycle 2.11 exigen Android Gradle Plugin 9.1 y SDK 37; quedan para una migración propia.
- **Sumas de verificación.** 312 artefactos nuevos en `verification-metadata.xml`. Cada SHA-256 coincide con el archivo descargado, y su SHA-1 con el publicado por Google Maven o Maven Central (los 42 artefactos con nombre distinto se localizaron mediante su metadato `.module`). Ninguna discrepancia.
- **Licencias.** Inventario regenerado con `scripts/export-runtime-licenses.gradle` y `scripts/update-license-notices.py`: 90 artefactos, 38 cambios de versión y una familia nueva, AndroidX NavigationEvent (Apache 2.0).
- **Privacidad.** `dataExtractionRules` excluye todos los datos de la copia en la nube y de la transferencia entre dispositivos en Android 12+, coherente con `allowBackup="false"`.
- **Limpieza.** 18 cadenas y 3 colores sin uso eliminados; carpeta `mipmap-anydpi-v26` duplicada retirada; logotipo de autoría movido a `drawable-nodpi`; `SharedPreferences.edit {}`, `toUri()`, `createBitmap()` y `toDrawable()` de AndroidX en lugar de las llamadas equivalentes. Lint: de 62 a 18 advertencias, todas de versiones que requieren la migración anterior, del aviso de ChromeOS, del icono monocromo y del toque del mapa.
- **Documentación.** README reescrito. `AUDITORIA.md` y `CAMBIOS.md` pasan a `revisiones/historico/` con sus enlaces corregidos.

## Validación local

- `./gradlew --dependency-verification strict :app:testDebugUnitTest :app:lintDebug :app:assembleRelease` desde una carpeta `app/build` vacía: correcto. 36 pruebas unitarias sin fallos; lint sin errores.
- `connectedDebugAndroidTest` en emulador arm64 (Android 16): 21 pruebas correctas. En una primera ejecución falló `NativeMapTest` por tiempo de espera tras el gesto de arrastre simulado; pasó en dos repeticiones aisladas y en la ejecución completa siguiente.

## Publicación

El publicador comprueba el commit exacto en Linux con Gradle vacío y verificación estricta antes de crear la etiqueta. El workflow de la etiqueta repite la validación, firma con el certificado habitual y publica la release con su suma SHA-256.
