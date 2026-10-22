# Correcciones locales de Gass — 2.2.5-local

**Autor: Mosqueteroweb.** Registro de la candidata interna 2.2.5-local (código 24), anterior a la publicación 2.2.5 (código 25). Consulta [la publicación](../2.2.5/PUBLICACION.md).

Fecha: 22 de octubre de 2026. Versión local de revisión, código 24. No publicada en GitHub. Se conserva como documento histórico el informe de la 2.2.4.

## Cambios implementados

| Hallazgo de 2.2.4 | Corrección local |
|---|---|
| F01: ubicación antigua en viajes | Se consulta una posición nueva al volver a primer plano y se escuchan cambios durante el uso. Las solicitudes periódicas se retiran al salir de la app; no se pide permiso de ubicación en segundo plano. Se recalculan resultados tras movimientos de al menos 100 metros. |
| F02: salto silencioso a Madrid | Un fallo de GPS conserva la última ubicación válida, que también persiste entre procesos. Madrid solo se usa como referencia inicial, identificada expresamente. Se muestran el estado de permiso/GPS y la fecha de la última posición. Centrar solo ejecuta su callback de éxito si obtiene posición válida. |
| F03: caché solo en memoria | Se guarda la última respuesta válida en almacenamiento interno. Se valida antes de sustituir la anterior y se escribe de forma atómica. Los datos caducados de disco se muestran antes de intentar una recarga en segundo plano. Se muestran fecha oficial y fecha de guardado. |
| F04: carburantes distintos en mapa | Los dos selectores usan los carburantes visibles guardados. El modelo impide activar carburantes ocultos. |
| F05: desaparecen tarjetas al fallar recarga | Se conservan los datos anteriores y se muestra aviso en español con Reintentar. Los errores de primera carga también se traducen y el mapa ya no los describe como ausencia de gasolineras. |
| F06: éxito falso al limpiar caché | El control se llama «Actualizar precios del Ministerio». Informa de que se conserva la última consulta válida y no anuncia una recarga exitosa antes de terminar. No se presenta como un borrado de la caché del mapa. |
| F07: permiso permanentemente denegado | El aviso puede abrir los ajustes de permisos de la app. Con permisos concedidos pero Ubicación apagada se abren los ajustes de Ubicación. Se comprueba de nuevo el estado al volver. |
| F08: favoritas fuera de zona | Se distingue entre no tener favoritas y no tener favoritas que coincidan con radio/filtros. Añadir o quitar favoritas recalcula conjuntamente tarjetas, contador y mínimos. |
| F09: Atrás con leyenda | Atrás cierra primero leyenda, búsqueda o menú. Los diálogos y ajustes conservan su propio cierre. |
| F10: radio cambia al pasar a mapa | Cambiar de vista conserva el radio elegido. |
| F11: APK y documentación no coinciden | Se identifica el código y APK local como 2.2.5-local, manteniendo el enlace de la publicación 2.2.4 claramente separado. Se marca la auditoría anterior como histórica. |

Además se cancelan consultas sustituidas y se verifica su orden antes de aplicar resultados. Esto evita que una respuesta lenta de filtros o ubicaciones anteriores sobrescriba el estado actual. El almacenamiento y las descargas comparten un bloqueo para evitar escrituras y cargas concurrentes inconsistentes. Las cancelaciones no se presentan como errores de red ni como éxito offline.

## Comprobaciones automatizadas

Se ejecutaron:

```sh
./gradlew testDebugUnitTest connectedDebugAndroidTest lintDebug assembleRelease
```

- **23 pruebas unitarias sin fallos**: las 12 existentes más 11 regresiones del repositorio real con API simulada y archivos temporales. Cubren reinicio offline, recarga fallida, caché caducada o dañada, respuesta vacía/inválida, cambio de ciudad sin nueva descarga, favoritos y mínimos, concurrencia y cancelación.
- **6 pruebas en Android 15 sin fallos**: cambios de ciudad recibidos por flujo, vuelta desde segundo plano, GPS apagado preservando posición, primer arranque sin posición real, radio/carburantes personalizados y protección frente a respuestas atrasadas. Las pruebas usan proveedores de ubicación y repositorio controlados; no dependen de la API pública.
- **Lint sin errores**. Permanecen avisos de mantenimiento del proyecto, como dependencias antiguas, recursos sin uso y revisión futura de la configuración de backup/target SDK. No se han ocultado mediante una línea base.
- **APK de release optimizado y firmado generado correctamente** con la configuración local disponible. No se han leído ni copiado claves a la documentación.

## Comprobaciones en la interfaz del APK

Se ejecutaron sobre el emulador Pixel/Android 15 usado en la revisión original. En la revisión de interfaz se verificaron Madrid (38 estaciones con los tres carburantes a 10 km), cambio automático a Zaragoza (6), y actualización al volver de segundo plano en Barcelona (30).

Con la compilación final optimizada se verificó además el **reinicio completo sin conexión**, con Android indicando que no había red predeterminada: se restauraron las 30 estaciones de Barcelona desde disco. Al forzar una recarga sin red, siguieron visibles las 30 tarjetas y apareció «No se han podido actualizar los precios. Se muestran datos guardados» con Reintentar. No apareció un éxito anticipado ni el error DNS en inglés.

Se comprobó también que Atrás cierra la leyenda y mantiene el mapa abierto. Al apagar Ubicación, se conservó el mapa de Barcelona y apareció «Ubicación desactivada» con la fecha de la última posición. Pulsar Centrar abrió los ajustes de Ubicación de Android, sin saltar a Madrid.

La comparación visual final de carburantes personalizados mostró **95, GLP y 98 en las dos vistas**, manteniendo **25 km y 64 estaciones** al cambiar de lista a mapa. También se verificó el nuevo texto de mantenimiento en los ajustes.

Las capturas y los árboles de interfaz seleccionados se guardan en `evidencias/`. Los XML de resultados automatizados se conservan en `resultados/` para poder revisar los resultados sin depender de la carpeta de compilación.

## Archivos para probar

El APK local queda en la raíz como `gasolineras.apk` y con nombre de versión en esta carpeta. Su SHA-256 está en `APK.sha256`; el manifiesto de entrega incluye versión, tamaño y configuración. El APK de la raíz anterior era 2.2.3 y se sustituye por la compilación local verificada. La 2.2.4 publicada en GitHub no se modifica.

## Límites

La revisión cubre un emulador Android 15. Queda pendiente la validación en teléfonos reales, otras versiones de Android y el consumo de batería en viajes prolongados. Se mantiene la integración de navegación existente; no se ha certificado Waze o Petal Maps ni se han publicado cambios remotos. El GPS simulado no sustituye una prueba real de cobertura, túneles y precisión aproximada.

La caché se conserva hasta que se obtiene otra respuesta válida. No garantiza precios actuales cuando no hay conexión; la fecha visible permite identificar ese caso. No se añaden cuentas, telemetría ni servicios de ubicación en segundo plano.

## Estado de entrega

Queda instalada y abierta 2.2.5-local en el emulador, con datos móviles y Ubicación habilitados. Se verificó la firma v2 del APK y la coincidencia de SHA-256 entre la copia de la raíz y la copia con nombre de versión. No se subieron cambios ni se creó una release remota. Las preferencias del emulador reflejan la prueba de 95/GLP/98 y radio 25 km.
