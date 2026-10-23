# Publicación de Gass 2.2.9

Autor público: Mosqueteroweb. Publicación autorizada después de validar la candidata local. El primer intento de compilación limpia de GitHub [39964818156](https://github.com/mosqueteroweb/gasolineras/actions/runs/39964818156), sobre `ff836e4`, detectó una suma pendiente de un BOM transitivo de Kotlin Coroutines 1.8.0 y no llegó a firmar ni publicar una release.

Se reprodujo la resolución sin caché anterior y se añadieron el BOM y los metadatos de Coroutines Core 1.10.2, contrastados con las sumas SHA-256 del editor en Maven Central. Se conservan todas las sumas existentes y la verificación estricta. La compilación local con caché nueva, pruebas unitarias y lint pasó, seguida de otra comprobación estricta satisfactoria. La corrección afecta a los metadatos de compilación, no al código funcional ni a las versiones de ejecución. Se reintentará la publicación sobre el commit corregido; la etiqueta todavía sin release publicada se ajustará con comprobación de su valor anterior.

El APK local de validación conserva su identidad histórica en `entrega-local.json`, `APK_LOCAL.sha256` y `fuentes-sha256.json`. El APK publicado y su commit se registrarán por separado. La atribución interna y documentación privada permanecen excluidas de Git.
