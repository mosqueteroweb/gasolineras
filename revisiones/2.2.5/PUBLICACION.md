# Publicación de Gass 2.2.5

**Autor: Mosqueteroweb.** Fecha: 22 de octubre de 2026.

## Identificación

- Versión publicada: **2.2.5**; código Android **25**.
- Candidata anterior: 2.2.5-local / código 24. Sus informes y huellas se conservan en `../2.2.5-local/` y no identifican el APK definitivo de esta publicación.
- Etiqueta de esta entrega: [v2.2.5](https://github.com/mosqueteroweb/gasolineras/tree/v2.2.5).
- Release: [Gass v2.2.5](https://github.com/mosqueteroweb/gasolineras/releases/tag/v2.2.5).
- APK: [gasolineras.apk](https://github.com/mosqueteroweb/gasolineras/releases/download/v2.2.5/gasolineras.apk).
- Huella del APK publicado: [gasolineras.apk.sha256](https://github.com/mosqueteroweb/gasolineras/releases/download/v2.2.5/gasolineras.apk.sha256).

## Cambios

El inventario completo de cambios está en [CAMBIOS.md](../../CAMBIOS.md). El resumen público está en [RELEASE_NOTES.md](../../RELEASE_NOTES.md) y [CHANGELOG.md](../../CHANGELOG.md). Autor: Mosqueteroweb.

## Controles previos y publicación reproducible

La candidata superó 23 pruebas unitarias y 6 regresiones en Android 15, con pruebas manuales del APK optimizado. Para preparar la versión pública se recompilan y verifican pruebas unitarias, lint y release con la identificación final. Los resultados locales de la publicación y la huella del APK local se conservan en esta carpeta.

Al subir `v2.2.5`, `.github/workflows/release.yml` ejecuta las pruebas unitarias y lint, genera el APK de release con las claves configuradas en GitHub Secrets, verifica la firma y publica APK y checksum junto con las notas de publicación. La huella que acompaña la release corresponde al binario producido en GitHub Actions; puede diferir de la compilación local por metadatos del entorno de compilación.

No se incluyen claves, tokens ni configuración privada. Se conserva el historial de revisión de la 2.2.4 y de validación de la candidata. La publicación no certifica teléfonos físicos, autonomía en viajes largos ni aplicaciones externas de navegación adicionales.
