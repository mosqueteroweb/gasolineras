# Validación del procedimiento de publicación

Fecha: 23 de octubre de 2026. Autor público: Mosqueteroweb.

## Resultado

El procedimiento exige compilar desde cero en Linux antes de crear una etiqueta nueva. No modifica el código Android, la versión ni el APK publicado de Gass 2.2.9.

- Las 17 pruebas del publicador pasan localmente y en GitHub. Comprueban, entre otros casos, que una comprobación ausente, fallida, antigua o de otro commit impida crear la etiqueta; también verifican que no se sobrescriban etiquetas y que el modo de consulta no publique nada.
- Actionlint 1.7.12 valida la configuración final del workflow.
- La [ejecución 39969628055](https://github.com/mosqueteroweb/gasolineras/actions/runs/39969628055), del commit `e6e1b0aae603cb37c399890eea8401bbeaca28fd`, termina correctamente: pruebas del publicador, pruebas unitarias Android, lint y compilación de release sin firma, con directorio Gradle vacío y verificación estricta de dependencias. La firma y la publicación quedan omitidas, como corresponde a una subida a `main`.
- La consulta real `python3 scripts/publish_release.py --check-only --timeout 0` acepta esa ejecución sin crear ni subir etiquetas. La comprobación negativa del commit anterior a esta política rechaza la publicación al faltar la validación previa exigida.

## Incidencia detectada durante la implantación

La [primera ejecución 39969087029](https://github.com/mosqueteroweb/gasolineras/actions/runs/39969087029) detectó un contexto de expresión no permitido en las variables de entorno del trabajo. Se corrigió configurando el directorio temporal en un paso del trabajo, antes de preparar Java. Actionlint reproduce el error de la configuración inicial y acepta la corregida. No se llegó a firmar ni publicar nada con esa configuración fallida.

## Release conservada

La release sigue siendo [Gass 2.2.9](https://github.com/mosqueteroweb/gasolineras/releases/tag/v2.2.9). No se recrea su etiqueta ni se reemplazan sus archivos.

- Objeto de etiqueta: `3d07498e43164dacd3a3350f9a11a02556226a73`.
- Commit de la release: `406c8ded9eda0a38002459c9132b87677460e94d`.
- SHA-256 del APK: `12b29194a44fe05ae7ca6465a212e973874b8c072a74c72b7d38ec6d7ffc307f`.

Una dependencia nueva todavía puede requerir incorporar su suma contrastada con la fuente oficial. El cambio hace que ese problema se detecte en `main`, antes de crear la siguiente etiqueta, y bloquea la publicación hasta corregirlo. No se aceptan sumas automáticamente ni se desactiva la verificación.

Procedimiento: [PUBLICACION.md](../../PUBLICACION.md).
