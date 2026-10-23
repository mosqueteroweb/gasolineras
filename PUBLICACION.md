# Publicación con validación previa en Linux

Autor público: Mosqueteroweb. Procedimiento incorporado después de publicar 2.2.9 para detectar metadatos sin verificar antes de crear la siguiente etiqueta. No modifica el APK 2.2.9 ni su versión.

Resultado de las comprobaciones: [validación del 23 de octubre de 2026](revisiones/proceso-publicacion-2026-10-23/VALIDACION.md).

## Comprobación automática

Cada subida a `main`, pull request y ejecución manual lanza el trabajo `validate` en Ubuntu con Java 17. Cada ejecución usa un directorio Gradle nuevo dentro del espacio temporal del runner; comprueba que no existan cachés de dependencias, `.gradle` ni `app/build`. No se restaura caché de Gradle.

Se ejecutan las pruebas del publicador, las pruebas unitarias Android, lint y la compilación completa de release sin firma de producción. Gradle usa `--no-build-cache --no-configuration-cache --refresh-dependencies --dependency-verification strict`. La compilación debe conservar intacto `verification-metadata.xml`. No se generan ni aceptan sumas automáticamente en GitHub. Los diagnósticos de una comprobación fallida se guardan siete días como artefactos de Actions; la fase previa no recibe claves de firma.

## Publicar una versión nueva

Por instrucción del usuario, subir código de la aplicación a GitHub incluye preparar una nueva versión, subir su etiqueta mediante el publicador y comprobar la release con su APK. No terminar la entrega únicamente con el código en `main`, salvo indicación expresa en contra. La comprobación previa sigue siendo obligatoria: la etiqueta se envía después de que termine correctamente.

Preparar su versión, cambios y notas; confirmar los archivos en la rama `main`. Con Python 3 y las credenciales normales de Git configuradas, ejecutar desde la raíz:

```sh
python3 scripts/publish_release.py
```

El publicador:

1. Exige un árbol de trabajo limpio y una versión nueva; rechaza etiquetas locales o remotas existentes.
2. Sube `main` sin forzar y espera a la comprobación Linux del SHA completo que se quiere publicar.
3. Solo acepta la última ejecución de ese commit, originada por una subida a `main` en este repositorio, con `validate` y sus comprobaciones limpias completadas correctamente. Una comprobación de otra rama, un PR, otro SHA, una comprobación antigua sin la nueva política, una fase omitida o una ejecución más reciente fallida no sirven como autorización.
4. Comprueba de nuevo que no hayan cambiado el árbol de trabajo, el commit ni `main` remoto. Entonces crea una etiqueta anotada nueva con autoría Mosqueteroweb y la sube. No sobrescribe etiquetas.
5. GitHub vuelve a exigir la validación previa de `main` para ese mismo SHA, repite la comprobación limpia y solo después permite firmar y publicar. La fase de firma usa también un directorio Gradle nuevo. La firma, versión y checksum se comprueban como antes.

Para comprobar el estado sin subir código ni crear etiquetas:

```sh
python3 scripts/publish_release.py --check-only --timeout 0
```

El plazo normal de espera es de quince minutos; se puede ampliar con `--timeout 1800`. Si hay un fallo, falta información o se agota el tiempo, se interrumpe la publicación antes de crear la etiqueta. Las lecturas locales de Actions usan la API pública; no se extraen tokens de la URL remota de Git. En el trabajo de etiquetas se usa el token temporal de Actions con permisos de lectura.

Git permite que alguien cree manualmente una etiqueta fuera de este publicador: este procedimiento no instala una prohibición administrativa de Git. Una etiqueta manual no elude el control de firma/publicación de GitHub, que exige el resultado previo del mismo commit. El procedimiento habitual debe ser el publicador anterior, no subir `main` y la etiqueta a la vez.

## Si falta una suma

Consultar los diagnósticos, identificar el artefacto exacto y contrastarlo con el editor o repositorio oficial antes de actualizar las sumas. Confirmar la corrección y subirla a `main`: GitHub repetirá automáticamente la comprobación sin caché. No usar una verificación desactivada ni reescribir una etiqueta ya publicada para sortear el fallo.

Las pruebas del publicador se ejecutan también localmente:

```sh
python3 -m unittest discover -s scripts/tests -v
```

Fuentes de funcionamiento: [eventos de GitHub Actions](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow) y [API de ejecuciones](https://docs.github.com/en/rest/actions/workflow-runs).
