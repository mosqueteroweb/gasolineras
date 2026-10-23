# Validación local de Gass 2.2.8

**Autor público: Mosqueteroweb. Fecha: 23/10/2026. Versión 2.2.8 / código 28.**

Esta entrega incorpora la 2.2.7 y las mejoras descritas en [CAMBIOS.md](CAMBIOS.md). Esta validación se completó primero en local. Después se autorizó la publicación; el resultado de la compilación remota y la comprobación de sus adjuntos se registra en [PUBLICACION.md](PUBLICACION.md).

## Resultado final

**33 pruebas unitarias y 16 de Android superadas, sin fallos ni errores.** Compilación y lint terminados correctamente. Lint: **0 errores, 42 advertencias y 1 observación**, principalmente recursos, actualización de dependencias/SDK, iconos y estilo. Resumen por caso en [PRUEBAS.json](PRUEBAS.json).

Se repitió la batería tras corregir los casos detectados y reiniciar el emulador, cuyo sistema había retenido una geometría horizontal incoherente con el bloqueo de rotación. Los tests esperan a que se muestre la ventana inicial y desplazan el contenido del panel cuando hace falta; los controles de cierre quedan fuera del contenido desplazable. La batería completa finalizó correctamente. Después se ajustó únicamente el texto para una fecha oficial ausente («fecha no facilitada»); se repitieron las 33 unitarias, lint y la compilación de release, también correctamente.

## Comprobaciones

- Compilación optimizada de release, pruebas unitarias, pruebas Android y lint con verificación estricta de las sumas de dependencias.
- Pruebas nuevas de números no finitos, coordenadas fuera de rango, precios inválidos, límite de texto/caché, unidades y formato, distancia antipodal, antigüedad de publicación, orden de comparación y borrado de precios.
- Regresiones Android de ciudad/reanudación, errores GPS, respuestas atrasadas, ubicación manual, caducidad de posición, controles seleccionables, ajustes al 200 %, información temporal/persistente y referencia municipal sin elegir una estación periférica arbitraria.
- Doce fondos seleccionados de carburantes superan 4,5:1 con texto blanco; mínimo calculado 4,52. El árbol Android real confirma casillas seleccionadas con acción de pulsación. Esto no certifica todos los estados visuales ni un recorrido hablado de lector de pantalla.
- APK identificado como 2.2.8/28, firma válida y certificado igual al de 2.2.6, sin indicador de depuración. El certificado debug es distinto. Suma del APK local en `APK_LOCAL.sha256`; metadatos en `entrega-local.json`.

## Prueba de uso en emulador

Pixel virtual Android 15, 1080 × 1920, densidad 420. Red, posición y configuración del emulador se han controlado para las pruebas; no se han alterado datos de un móvil físico.

- Lista con precios oficiales: Madrid, 38 estaciones con Diésel/95/GLP y radio de 10 km. Al simular el desplazamiento a Barcelona, actualización automática a 30, sin cambiar filtros ni pulsar actualizar.
- Elección de Zaragoza mientras el GPS continúa en Barcelona: referencia manual explícita, resultados y distancias calculados desde la referencia. Se detectó que una primera estación periférica desplazaba la búsqueda; la selección final usa la estación más próxima a la mediana de las coordenadas municipales, con regresión automática específica.
- Información del Ministerio tras la última tarjeta, con publicación y consulta diferenciadas. No consume un pie fijo en horizontal.
- Recarga sin red mantiene los precios, fechas y estación mostrada y añade aviso con «Reintentar». Red restaurada al terminar.
- Texto al 200 % en horizontal: desplazamiento del conjunto para llegar a la tarjeta, precios y navegación. Ajustes al 200 % también comprobados automáticamente: X y guardado disponibles.
- La información del mapa conserva los cinco segundos por defecto y tiene una prueba adicional de «Mantener abierta». No se han repetido manualmente todas las combinaciones de navegación externa de campañas históricas.

Las capturas de lista, viaje, pie, offline y horizontal se conservaron durante la prueba de la candidata firmada, antes del último ajuste de referencia municipal y del singular «1 encontrada». Por eso las capturas municipales antiguas pueden mostrar una referencia periférica y «1 encontradas»; no se presentan como prueba del algoritmo municipal final. La prueba específica del algoritmo y la validación final del APK cubren ese último ajuste.

Después de reinstalar el APK definitivo se observó también un aviso «System UI isn't responding» del sistema del emulador. Al elegir «Wait», la interfaz se recuperó y Gass mostró las 38 estaciones de Madrid. La captura del aviso se conserva solo con el diagnóstico local; no se atribuye una causa raíz demostrada ni se considera resuelta la estabilidad del emulador por esta entrega. Se requiere contrastar el comportamiento en dispositivos físicos.

## Límites y mantenimiento

Lint puede mantener advertencias preexistentes de actualización de bibliotecas/SDK, recursos, iconos y estilo; el resumen final detalla el número. No se han actualizado masivamente bibliotecas sin probar compatibilidad. El motor osmdroid sigue archivado: [plan de mantenimiento](MANTENIMIENTO_MAPA.md).

La consulta autorizada de las 75 versiones Maven a OSV no devolvió avisos. Ausencia de avisos no significa ausencia de vulnerabilidades.

Pendientes fuera de la entrega local: medidas de la cuenta GitHub y protecciones administrativas, ejecución de CI en Linux, migración del mapa, validación en dispositivos físicos, batería prolongada y recorrido hablado completo con TalkBack/Switch Access. No se afirma certificación de seguridad o accesibilidad.
