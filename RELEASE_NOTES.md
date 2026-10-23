# Gass 2.2.8 — accesibilidad, privacidad y búsqueda por ubicación

**Autor: Mosqueteroweb. Versión Android: 2.2.8 / 28.**

Incluye los cambios de 2.2.7 y las mejoras posteriores de la auditoría.

- Botones de opciones de Ajustes con altura uniforme y acciones de guardar/cancelar siempre disponibles.
- Información del Ministerio después de la última gasolinera de la lista; publicación y consulta diferenciadas.
- Precios con coma decimal y unidad correcta: GNC, GNL e hidrógeno en €/kg; los demás en €/L.
- Elección explícita del combustible con el que se comparan los precios.
- Búsqueda por municipio sin GPS y búsqueda en la zona explorada del mapa, con referencia manual identificada.
- Seguimiento GPS que se pausa al explorar el mapa y se recupera desde su botón. Una referencia manual nunca se presenta como una obtención GPS.
- Mejor contraste, selección accesible, controles ampliados y desplazamiento de la lista con letra grande y en horizontal.
- Información del mapa con cinco segundos por defecto, tiempo recomendado por Android y opción de mantenerla abierta; atribución de OpenStreetMap visible.
- Última ubicación con caducidad, opción de no recordarla y borrado separado de datos locales. Validación más estricta de datos y compilación/firma reforzadas.

Validación local: **33 pruebas unitarias y 16 de Android superadas**. El APK conserva el certificado de firma de las versiones anteriores y no permite depuración. La publicación ejecuta nuevamente pruebas unitarias y lint y verifica versión, código y firma.

Se documentan los límites de las pruebas en emulador, el trabajo pendiente en dispositivos físicos y el plan de mantenimiento del motor de mapa. Detalle en [cambios](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.8/revisiones/2.2.8/CAMBIOS.md) y [validación](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.8/revisiones/2.2.8/VALIDACION.md).
