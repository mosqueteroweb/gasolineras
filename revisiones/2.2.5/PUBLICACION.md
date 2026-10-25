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

El inventario completo de cambios está en [CAMBIOS.md](../historico/CAMBIOS-2.2.5-a-2.2.11.md). El resumen público está en [RELEASE_NOTES.md](../../RELEASE_NOTES.md) y [CHANGELOG.md](../../CHANGELOG.md). Autor: Mosqueteroweb.

## Controles previos y publicación reproducible

La candidata superó 23 pruebas unitarias y 6 regresiones en Android 15, con pruebas manuales del APK optimizado. Para preparar la versión pública se recompilan y verifican pruebas unitarias, lint y release con la identificación final. Los resultados locales de la publicación y la huella del APK local se conservan en esta carpeta.

Al subir `v2.2.5`, `.github/workflows/release.yml` ejecuta las pruebas unitarias y lint, genera el APK de release con las claves configuradas en GitHub Secrets, verifica la firma y publica APK y checksum junto con las notas de publicación. La huella que acompaña la release corresponde al binario producido en GitHub Actions; puede diferir de la compilación local por metadatos del entorno de compilación.

No se incluyen claves, tokens ni configuración privada. Se conserva el historial de revisión de la 2.2.4 y de validación de la candidata. La publicación no certifica teléfonos físicos, autonomía en viajes largos ni aplicaciones externas de navegación adicionales.

## Publicación terminada y verificada

La release **Gass v2.2.5 está publicada**, sin marcarse como borrador ni prerelease. GitHub Actions terminó con éxito todas las etapas: pruebas unitarias/lint, compilación, verificación de firma, generación de checksum y publicación de los dos adjuntos.

- Commit de código y etiqueta: [`c89129f01c40ed898d8b011c1edc40df8d107a57`](https://github.com/mosqueteroweb/gasolineras/commit/c89129f01c40ed898d8b011c1edc40df8d107a57).
- Ejecución comprobada: [Build & Release APK #39860744304](https://github.com/mosqueteroweb/gasolineras/actions/runs/39860744304).
- APK publicado: **2.025.031 bytes**, versión **2.2.5**, código **25**, sin indicador de depuración.
- SHA-256 publicado y verificado tras descargar el APK: `008010d8dac2116735bcbae196dd74166c401081a57176b7121bd8bb70a3a23b`.
- La huella descargada en `gasolineras.apk.sha256` coincide con el APK y con el digest del adjunto de GitHub.
- La firma es válida y el certificado coincide con el APK local y con la release 2.2.4 comprobada, conservando la identidad de firma para actualizaciones.
- Autor público: **Mosqueteroweb**.

Se ha copiado el APK publicado a `gasolineras.apk` en la raíz y a `gasolineras.apk` de esta carpeta, junto a sus checksums. Esas copias son idénticas al adjunto de GitHub. Los APK siguen excluidos del seguimiento de Git. La compilación local previa se conserva como `gass-2.2.5.apk` de esta carpeta, con su huella independiente en `APK_LOCAL.sha256` y `entrega-local.json`; no debe confundirse con el binario generado en GitHub Actions.

El registro estructurado está en [publicacion-verificada.json](publicacion-verificada.json). Este cierre documental se guarda en un commit posterior de documentación; la etiqueta `v2.2.5` identifica el código de esa entrega. La corrección posterior de autoría conserva el código y el APK publicado.
