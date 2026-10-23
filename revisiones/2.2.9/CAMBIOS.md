# Gass 2.2.9 / 29

Autor público: Mosqueteroweb. Trabajo autorizado para el 23/10/2026 a las 15:00, Europe/Madrid. Validada primero en local y publicada posteriormente con autorización; véase [PUBLICACION.md](PUBLICACION.md).

## Mapa

- Sustitución de osmdroid 6.1.18 por MapLibre Native Android 13.7.1, variante OpenGL ES por compatibilidad. Sin claves, alta de servicios ni costes contratados.
- Se conserva OpenStreetMap y su proveedor raster actual, con estilo local, User-Agent identificable, caché HTTP de 64 MiB respetando cabeceras y caché ambiental del motor. No se añaden descargas de áreas ni anticipación de niveles de zoom.
- Se conservan radio, círculo, marcadores de estaciones y referencia, colores de mínima/nearest, límite de 80 visibles, selección de ficha, precios/unidades en la ficha, seguimiento GPS, pausa al explorar, recentrado y búsqueda en la zona. Un toque en una estación abre directamente su ficha con precios y acciones.
- La cámara ignora cambios que solo afectan a la fecha del GPS. El radio y recentrado son acciones explícitas. Se guarda la cámara durante recreaciones de la pantalla.
- El ciclo de vida del mapa se enlaza a la actividad: pausa, parada y destrucción equilibradas; límite de 30 FPS. Las peticiones del mapa se cancelan al pararlo. Se evita un segundo proveedor GPS dentro del motor.
- Borrado de caché adaptado a MapLibre y eliminación de la caché antigua de osmdroid.

## Autoría

- Nueva opción «Autoría» en el menú de tres puntos, disponible en lista y mapa. La autoría de Mosqueteroweb se traslada desde Ajustes.
- Versión, GPLv3, proyecto/contacto, Ministerio MITECO, OpenStreetMap, inventario exacto de dependencias de ejecución, licencias completas, avisos de los binarios y componentes nativos de MapLibre incluidos en el APK y consultables sin conexión.
- Avisos largos fragmentados en bloques desplazables para no componer un texto de más de un megabyte de una vez. Cierre fijo, panel identificado para accesibilidad y enlaces a las fuentes.
- Se mantiene visible y enlazada en el mapa la atribución requerida por el proveedor; Autoría amplía la información, no la sustituye.

## Consumo y segundo plano

- Seguimiento GPS limitado a actividad RESUMED: se detiene también al perder el primer plano o bloquear pantalla. El retorno reactiva el seguimiento y conserva filtros y referencias manuales.
- Cancelación de consultas de precios, búsqueda aplazada y carga de municipios al salir. Se rechazan respuestas atrasadas; una actualización forzada interrumpida se reintenta al volver.
- Corrección de la carrera de registro GPS: una suscripción que acaba de registrarse tras la cancelación se elimina también.
- Eliminación de la animación infinita de recarga que existía aunque no se actualizasen los precios. Se conserva la barra de progreso durante las consultas.
- Eliminación del sondeo del estado GPS cada dos segundos. Los cambios se reciben mediante avisos Android registrados solo mientras se usa la pantalla.
- No se añaden servicios, trabajos periódicos, alarmas, bloqueos de activación ni permisos de ubicación en segundo plano.

## Herramientas

MapLibre usa metadatos Kotlin recientes. Para mantener compilador, optimización y análisis compatibles se actualizan Kotlin/Compose Compiler a 2.2.10, AGP a 8.10.1, Gradle a 8.11.1 y BOM Compose a 2025.08.01. Coroutines queda explícito en 1.10.2, ya requerido por MapLibre. Se mantiene Java 17, minSDK 26 y target/compileSDK 35. Descargas fijadas por SHA-256, incluido el ZIP oficial de Gradle. La verificación estricta permanece activada.

Las correcciones de 2.2.7 y 2.2.8 permanecen. Validación y límites en [VALIDACION.md](VALIDACION.md); detalle de consumo en [BATERIA.md](BATERIA.md).
