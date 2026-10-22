# Historial de cambios

## 2.2.6 — 23 de octubre de 2026

**Autor: Mosqueteroweb.**

- El mapa deja de mostrar el bloque fijo de última ubicación y precios del Ministerio.
- Nuevo icono de información inferior, junto al centrado de ubicación, que abre el mismo contenido y conserva los avisos y sus acciones.
- Cierre con X, pulsación del icono, Atrás o a los 5 segundos desde la apertura. Las actualizaciones no prolongan el temporizador.
- Leyenda e información se muestran por separado; al salir del mapa se cierra el panel.
- Documentación y autoría pública corregidas a Mosqueteroweb.
- Versión Android 2.2.6 / código 26.

## 2.2.5 — 22 de octubre de 2026

**Autor: Mosqueteroweb.**

- Actualización de ubicación durante el uso y al reanudar la app, limitada al primer plano.
- Persistencia de la última posición válida; eliminación del salto a Madrid ante fallos de GPS y aviso explícito de referencia inicial.
- Caché persistente y escritura atómica de precios; arranque offline con datos guardados y fechas visibles.
- Conservación de tarjetas ante errores de actualización y mensajes comprensibles con reintento.
- Mantenimiento descrito como actualización de precios y eliminación del aviso anticipado de éxito.
- Recuperación de permisos denegados y acceso a ajustes de Ubicación.
- Carburantes personalizados y radio coherentes entre mapa y lista.
- Mensajes de favoritas según filtros/zona y recálculo de mínimos al modificarlas.
- Cierre de leyenda, búsqueda y menú mediante Atrás.
- Control de consultas concurrentes, cancelación y rechazo de resultados atrasados.
- 11 pruebas unitarias nuevas y 6 regresiones instrumentadas; conservación de evidencias de la revisión funcional y la validación local.
- Documentación completa de archivos modificados y límites de la validación.
- Automatización de release con pruebas unitarias, lint, comprobación de firma, checksum y notas detalladas.
- Versión pública 2.2.5 / código 25, posterior a la candidata interna 2.2.5-local / código 24.

Véanse [notas de release](RELEASE_NOTES.md), [cambios por archivo](CAMBIOS.md) y [publicación](revisiones/2.2.5/PUBLICACION.md).

## 2.2.4 y versiones anteriores

La evaluación del APK 2.2.4 está documentada en [la revisión funcional](revisiones/2.2.4-2026-10-22/INFORME.md).
