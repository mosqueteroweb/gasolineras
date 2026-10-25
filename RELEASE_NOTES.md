# Gass 2.2.12 — APK más ligero y mantenimiento

**Autor: Mosqueteroweb. Versión Android: 2.2.12 / 32.** Sin cambios visibles en el uso de la app.

- **APK de 44 MB a 22 MB**: se incluye solo el código nativo del mapa para móviles y tablets ARM (`arm64-v8a` y `armeabi-v7a`). Se retiran `x86` y `x86_64`, que solo usaban emuladores.
- **Android 16 (SDK 36)** como objetivo y compilación con Gradle 8.14.6 y Android Gradle Plugin 8.13.2.
- **Dependencias actualizadas**: AndroidX Core 1.18.0, Lifecycle 2.10.0, Activity 1.13.0, Play Services Location 21.4.0. Inventario de licencias regenerado; la única biblioteca nueva, AndroidX NavigationEvent, es Apache 2.0.
- **Privacidad en Android 12 o superior**: los datos de la app quedan excluidos también de las transferencias entre dispositivos, igual que de las copias de seguridad.
- Limpieza interna: recursos sin uso eliminados y código adaptado a las extensiones de AndroidX.

**Comprobación local:** 36 pruebas unitarias y 21 pruebas en emulador correctas, lint sin errores, compilación release con verificación estricta de dependencias.

**Publicación:** comprobación previa del commit exacto en Linux, firma con el certificado de siempre y verificación de versión y suma SHA-256. Se instala encima de la versión anterior conservando favoritas y ajustes.

[Cambios y comprobaciones](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.12/revisiones/2.2.12/VALIDACION.md).
