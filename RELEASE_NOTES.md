# Gass 2.2.9 — mapa actualizado, autoría y segundo plano

**Autor: Mosqueteroweb. Versión Android: 2.2.9 / 29.** Incluye las mejoras de 2.2.7 y 2.2.8.

- MapLibre Native 13.7.1 sustituye a osmdroid, conservando OpenStreetMap, filtros, radio, marcadores, selección de estación y búsqueda por zona. El encuadre muestra el círculo del radio elegido y el seguimiento se pausa al explorar el mapa.
- Nueva opción **Autoría** en el menú de tres puntos: Mosqueteroweb, fuentes de datos y avisos de las 86 bibliotecas de ejecución. Las licencias completas se pueden leer sin conexión. Se mantiene visible la atribución obligatoria de OpenStreetMap.
- El mapa y el GPS se pausan al abandonar la pantalla o bloquear el teléfono; se cancelan las consultas pendientes y se recuperan al volver, conservando filtros y referencias manuales.
- Eliminados la animación permanente de actualización y el sondeo periódico del estado GPS; los cambios de ubicación se reciben mediante avisos de Android. Corregida también la carrera de registro y cancelación del seguimiento.
- Compilación actualizada y dependencias verificadas mediante SHA-256; comprobación automática del inventario de licencias.

**Validación:** 36 pruebas unitarias y 21 pruebas Android superadas, análisis sin errores y comprobación de firma. Viaje simulado Madrid–Barcelona–Zaragoza, datos guardados sin conexión y controles con texto grande.

**Batería:** las muestras del emulador mostraron GPS detenido y actividad mínima de CPU en segundo plano. No se reprodujo el consumo elevado comunicado en el móvil; falta confirmar la autonomía en el dispositivo afectado y no se promete un porcentaje de ahorro.

**Descarga:** APK universal de aproximadamente 44,3 MB para cuatro arquitecturas. El aumento de tamaño corresponde principalmente a las bibliotecas nativas del mapa. Firma compatible con las versiones anteriores.

[Todos los cambios](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.9/revisiones/2.2.9/CAMBIOS.md) · [Pruebas y límites](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.9/revisiones/2.2.9/VALIDACION.md) · [Mediciones de consumo](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.9/revisiones/2.2.9/BATERIA.md).
