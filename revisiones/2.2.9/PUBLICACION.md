# Gass 2.2.9 publicada y verificada

Autor público: Mosqueteroweb. Publicación autorizada después de la validación local.

- Release: [Gass v2.2.9](https://github.com/mosqueteroweb/gasolineras/releases/tag/v2.2.9).
- Commit de la etiqueta y del APK: `406c8ded9eda0a38002459c9132b87677460e94d`.
- Compilación de GitHub: [39965593427](https://github.com/mosqueteroweb/gasolineras/actions/runs/39965593427); validación, firma y publicación satisfactorias.
- APK: `gasolineras.apk`, Android 2.2.9 / código 29, 44,300,243 bytes.
- SHA-256 descargado y contrastado con su archivo de suma y el registro del asset GitHub: `12b29194a44fe05ae7ca6465a212e973874b8c072a74c72b7d38ec6d7ffc307f`.
- Firma válida, certificado coincidente con las versiones anteriores y depuración desactivada.

Se descargó el APK publicado para comprobar esos datos. La copia local de la raíz y `gasolineras.apk.sha256` corresponden ahora al archivo distribuido. La candidata local de pruebas conserva sus datos históricos en `entrega-local.json` y `APK_LOCAL.sha256`; se identifica por separado del binario de GitHub.

## Comprobación de compilación limpia

El primer intento [39964818156](https://github.com/mosqueteroweb/gasolineras/actions/runs/39964818156), sobre `ff836e4`, se detuvo antes de firmar o publicar porque faltaba la suma de un BOM transitivo de Coroutines 1.8.0. Se reprodujo la resolución sin caché anterior y se añadieron ese BOM y los metadatos de Coroutines Core 1.10.2, contrastados con las sumas SHA-256 del editor en Maven Central. Todas las sumas existentes y la verificación estricta permanecen.

La corrección no cambia el código funcional ni las versiones de ejecución. La etiqueta todavía sin release publicada se ajustó una sola vez mediante comprobación de su valor anterior para construir el commit corregido. La ejecución final completó todas sus etapas.

La autoría de los commits y la etiqueta es Mosqueteroweb; la creación de la release la efectúa GitHub Actions. Documentación privada y atribución interna excluidas de Git. Las limitaciones de batería física y accesibilidad siguen documentadas en [validación](VALIDACION.md) y [batería](BATERIA.md).
