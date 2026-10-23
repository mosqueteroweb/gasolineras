# Gass 2.2.8 — cambios

**Autor público: Mosqueteroweb. Versión Android: 2.2.8 / 28.**

Estado: publicación autorizada después de terminar la validación local. Incluye todos los cambios locales de 2.2.7: botones uniformes y datos del Ministerio debajo de las gasolineras.

- Precios con coma decimal y unidad por combustible: GNC, GNL e hidrógeno por kilogramo; el resto por litro. Tarjetas, detalle y mapa comparten formato.
- Comparación de precios por un combustible elegido explícitamente. Los otros seleccionados siguen siendo requisitos de disponibilidad, sin mezclar sus precios para ordenar.
- Un único desplazamiento en lista permite acceder a filtros, tarjetas y acciones con texto ampliado y en horizontal. La información del Ministerio queda después de la última tarjeta.
- Elección de municipio a partir de los datos oficiales, sin servicios de geocodificación adicionales. La referencia corresponde a la estación más cercana a la mediana de coordenadas del municipio, no a un centro urbano geocodificado ni al GPS del usuario. El índice de búsqueda se prepara una vez y prioriza el nombre municipal exacto. La búsqueda sigue aplicando el radio y los filtros.
- Al volver desde una referencia manual al GPS, un fallo conserva la última posición GPS conocida y no presenta las coordenadas manuales como una obtención real.
- Búsqueda en el centro explorado del mapa. El desplazamiento manual pausa el seguimiento; el botón GPS lo recupera. Una renovación de la fecha de posición no mueve la cámara.
- Cuenta explícita de marcadores: hasta 80 del total de la zona visible. La lista conserva todas las coincidencias.
- Información del mapa con cinco segundos por defecto, tiempo recomendado por Android y opción «Mantener abierta». Cierre con X, icono o Atrás; nombre de panel y recuperación del foco de teclado.
- Mayor contraste para 98, Bio e H2; carburantes con nombre completo y selección accesible. Controles principales de al menos 48 dp, favoritas y navegación con contexto de estación.
- Ajustes con espacio reservado para cerrar, guardar/cancelar siempre disponibles y aviso de cambios pendientes. Las acciones de mantenimiento se aplican directamente y no dependen de guardar preferencias.
- Fechas de publicación y consulta diferenciadas; aviso de publicación de más de 24 horas; última posición identificada como tal, distancias en línea recta y carga sin un falso cero de resultados.
- Última ubicación con caducidad de 24 horas, descarte al leerla si ha caducado y opción para no recordarla. Borrado separado de posición, favoritas, precios y mapa; explicación de comunicaciones con terceros.
- Validación de números finitos, coordenadas, precios y longitudes de texto. Límite de 32 MiB de respuesta descomprimida y caché; una recarga fallida conserva la consulta válida.
- Firma debug independiente; acciones fijadas por SHA, tareas de validación/firma/publicación separadas, permisos mínimos y retirada del keystore al terminar. Comprobación de versión, código, firma y ausencia de depuración. Wrapper y dependencias con SHA-256, incluidos AAPT2 para Linux y los metadatos Gradle oficiales de JUnit BOM.
- Atribución visible de OpenStreetMap, fuente de teselas oficial y agente identificado con versión/contacto del proyecto. Plan de mantenimiento en `MANTENIMIENTO_MAPA.md`.

Los ajustes de seguridad de la cuenta GitHub, protecciones administrativas, pruebas en dispositivos físicos y migración del motor de mapa no se presentan como ejecutados. La validación y sus límites se registran en `VALIDACION.md`.
