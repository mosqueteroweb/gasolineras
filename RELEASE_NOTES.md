# Gass 2.2.5 — viajes, consulta sin conexión y coherencia de la interfaz

**Autor: Mosqueteroweb.**

## Ubicación durante los viajes

- La posición se renueva al volver a primer plano y durante el uso de la app, recalculando estaciones y distancias al desplazarse.
- Si el GPS falla o se desactiva, se conserva la última posición válida, también entre reinicios. Madrid solo aparece como referencia inicial explícita cuando aún no hay una ubicación real.
- Se muestran el estado de ubicación y la fecha de la última posición. Los avisos permiten recuperar permisos o abrir los ajustes de Ubicación.
- No se solicita permiso ni seguimiento de ubicación en segundo plano.

## Precios y funcionamiento sin conexión

- La última respuesta válida del Ministerio se guarda de forma persistente y atómica para consultarla después de cerrar y abrir sin Internet.
- Las tarjetas permanecen visibles si falla una recarga; se muestra un aviso en español con «Reintentar», la fecha oficial de los precios y la fecha de guardado.
- Los datos antiguos guardados se muestran antes de intentar una actualización que podría tardar o fallar.
- El mantenimiento pasa a llamarse «Actualizar precios del Ministerio», sin anunciar éxito antes de terminar ni afirmar que borra los mapas.

## Mapa, filtros y favoritos

- Mapa y lista comparten los carburantes personalizados y conservan el radio elegido al cambiar de vista.
- Se impide activar carburantes ocultos y se distinguen las favoritas inexistentes de las que están fuera de zona o no cumplen los filtros.
- Los mínimos y distintivos se recalculan al añadir o quitar favoritas.
- Atrás cierra primero leyenda, búsqueda o menú.
- Se evita que una respuesta atrasada sustituya consultas más recientes de otra ubicación o filtro.

## Validación y archivos

- 23 pruebas unitarias y 6 regresiones Android superadas en el entorno local; comprobaciones de interfaz en Pixel emulado con Android 15.
- Viaje simulado Madrid → Zaragoza → Barcelona, reinicio sin red, recarga fallida, GPS desactivado, carburantes 95/GLP/98 y conservación de 25 km entre vistas.
- APK de release optimizado y firmado: **2.2.5, código 25**. El código 24 se reservó para la candidata interna 2.2.5-local.
- La publicación automática ejecuta pruebas unitarias y lint, verifica la firma y adjunta `gasolineras.apk` y `gasolineras.apk.sha256`.

Quedan pendientes las pruebas en teléfonos físicos, otras versiones de Android, batería en viajes prolongados y validación de Waze/Petal Maps. Los precios guardados pueden estar desactualizados cuando no hay conexión; la fecha visible identifica ese caso.

Detalle por archivo: [CAMBIOS.md](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.5/CAMBIOS.md). Historial: [CHANGELOG.md](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.5/CHANGELOG.md).
