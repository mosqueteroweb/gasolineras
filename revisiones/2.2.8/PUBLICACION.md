# Publicación verificada de Gass 2.2.8

**Autor: Mosqueteroweb. Fecha: 23/10/2026.**

[Release 2.2.8](https://github.com/mosqueteroweb/gasolineras/releases/tag/v2.2.8) publicada y marcada como versión más reciente. Incluye los cambios de [2.2.7](../2.2.7/PUBLICACION.md).

[Compilación 39931277944](https://github.com/mosqueteroweb/gasolineras/actions/runs/39931277944) terminada correctamente en Linux: validación de versión, pruebas unitarias, lint, verificación estricta de dependencias, compilación, firma y publicación. Firma y publicación se ejecutan en tareas separadas con permisos limitados.

El APK se ha descargado de la release y comprobado de forma independiente:

- Versión **2.2.8 / código 28**, 2067307 bytes, sin depuración.
- Firma válida y mismo certificado que las versiones anteriores.
- SHA-256 coincidente con el archivo adjunto y con el digest de GitHub:

`6cb1fdfb05ec069d222aa49df7ead05e50ab65fd5806672c6c4b0d6ed2b24e1c`

Fuente del binario: commit `1785415437344959b9a856c80444bae3db5bd402`. Datos de verificación en [publicacion.json](publicacion.json).

La compilación distribuida es la de GitHub; la candidata local conserva su propia suma en `APK_LOCAL.sha256`. El archivo local de la raíz `gasolineras.apk` se ha actualizado con el APK publicado y `gasolineras.apk.sha256` identifica ese archivo.

## Verificación de compilaciones limpias

Los intentos iniciales se detuvieron antes de firmar por sumas ausentes en metadatos auxiliares: archivos `.module` de JUnit BOM y POM de Kotlin Coroutines BOM 1.7.3/1.7.1. Se incorporaron desde Maven Central y se contrastaron con las sumas del editor. Se completó una resolución local renovada antes de repetir la publicación; la ejecución final en Linux también superó la comprobación estricta. Los intentos fallidos no publicaron un APK 2.2.8.

La validación funcional local consta de **33 pruebas unitarias y 16 de Android**. Sus límites, las incidencias del emulador y las tareas pendientes en dispositivos físicos se conservan en [VALIDACION.md](VALIDACION.md). La auditoría completa y la atribución privada local permanecen excluidas de Git.
