# Validación de Gass 2.2.6

Autor: Mosqueteroweb. Fecha: 23 de octubre de 2026.

## Cambio de interfaz

El bloque de ubicación y precios se muestra fijo únicamente en la lista. En el mapa se accede mediante el icono de información inferior, situado a la izquierda del botón de centrado. El panel conserva el estado real de GPS/permisos, la fecha de la última ubicación, la fecha oficial del Ministerio, el guardado de precios y los avisos con reintento.

Se cierra con X, pulsando de nuevo el icono, Atrás o tras 5 segundos. Las actualizaciones de datos no reinician la cuenta. Reabrirlo inicia una cuenta nueva. Información y leyenda son excluyentes, y cambiar a lista cierra el panel.

## Comprobaciones automatizadas

- 23 pruebas unitarias superadas.
- 9 pruebas instrumentadas superadas en el emulador Pixel con Android 15: 6 regresiones del modelo de pantalla y 3 nuevas de interfaz.
- Las nuevas pruebas comprueban que los datos están ocultos inicialmente, la apertura, el cierre manual, el cierre por icono, la presencia de avisos/reintento, el cierre automático a los 5 segundos, las actualizaciones sin reiniciar el plazo y un nuevo plazo al reabrir.

## Publicación y documentación

Versión 2.2.6 / código Android 26. La documentación pública identifica al autor como Mosqueteroweb. Se han corregido los dos commits posteriores a 2.2.4 y la etiqueta 2.2.5 para reflejar esa autoría. Se conserva el código funcional de 2.2.5 y su APK original; únicamente se actualizan sus notas públicas.

Las copias privadas de los documentos y del historial anterior quedan fuera del seguimiento de Git. Las evidencias de la revisión anterior mantienen su fecha y alcance.

- Compilación de release y lint completados sin errores. El análisis conserva 41 advertencias y una observación; ninguna corresponde al nuevo componente.
- Firma del APK local válida; versión y código verificados directamente en el paquete.

## Comprobación visual del APK instalado

Se ha probado el APK optimizado y firmado en Android 15 con precios reales y 38 estaciones en Madrid. Se comprueba el mapa sin bloque superior, la apertura de ubicación/precios, el cierre automático, X y Atrás, la alternancia con leyenda y la vuelta de lista a mapa sin reabrir el panel. Capturas en [evidencias](evidencias/).
