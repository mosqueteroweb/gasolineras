# Mantenimiento del mapa

**Responsable del proyecto: Mosqueteroweb. Fecha: 23/10/2026.**

La versión 2.2.8 conserva osmdroid 6.1.18. Su repositorio está archivado; estas correcciones no convierten la biblioteca en un proyecto mantenido. Se fija el contenido de las dependencias mediante SHA-256 y se comprueban seguimiento GPS, exploración, filtros, atribución y liberación del mapa. No se añaden descargas masivas de teselas.

El proveedor utiliza `https://tile.openstreetmap.org/`, atribución visible y un agente con versión y enlace al proyecto. La caché HTTP la gestiona osmdroid. Deben respetarse la [política de teselas](https://operations.osmfoundation.org/policies/tiles/) y las limitaciones del servicio público.

Antes de una nueva distribución amplia se revisarán los avisos de seguridad y la compatibilidad con el proveedor. Si una incidencia no tiene corrección viable, se desactivará el mapa y se mantendrá la lista mientras se prepara una alternativa.

La siguiente revisión de mantenimiento se fija antes del 23/01/2027. El trabajo de sustitución requiere un prototipo que conserve círculo de radio, marcadores/precios, selección de estación, seguimiento y exploración, funcionamiento sin GPS y alternativa accesible por lista. Se compararán MapLibre y una capa propia mantenida sobre osmdroid, revisando licencia, proveedor, límites de uso y coste. La elección y la migración necesitan validación en dispositivos físicos; no se presentan como realizadas en 2.2.8.

## Evolución posterior en local

La candidata 2.2.9 sustituye osmdroid por MapLibre Native; véase [su registro](../2.2.9/CAMBIOS.md). Este documento conserva la situación histórica de la 2.2.8 publicada.
