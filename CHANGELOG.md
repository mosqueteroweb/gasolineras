# Historial de cambios

## 2.2.14 — 25 de octubre de 2026

**Autor público: Mosqueteroweb.** Auditoría de UX. Nombres de carburante unificados, carburante de ordenación visible, marcas completas, direcciones en minúsculas legibles, provincia solo cuando aporta, origen de la distancia correcto, colores propios para modo oscuro, «10 km» con espacio e iconos en todo el menú. Versión Android 2.2.14 / código 34. [Auditoría](revisiones/2.2.14/AUDITORIA_UX.md) y [validación](revisiones/2.2.14/VALIDACION.md).

## 2.2.13 — 25 de octubre de 2026

**Autor público: Mosqueteroweb.** Auditoría de seguridad. Plugins de Gradle resueltos solo desde Google Maven y Maven Central (el Plugin Portal hizo fallar la publicación de 2.2.12). Retirado el permiso `ACCESS_WIFI_STATE`, que la app no usa. Los enlaces externos ya no cierran la app si no hay navegador. Eliminado código sin uso. Versión Android 2.2.13 / código 33. [Auditoría](revisiones/2.2.13/AUDITORIA_SEGURIDAD.md) y [validación](revisiones/2.2.13/VALIDACION.md).

## 2.2.12 — 25 de octubre de 2026

**Autor público: Mosqueteroweb.** APK de 44 a 22 MB al incluir solo el código nativo ARM del mapa. SDK objetivo 36, Gradle 8.14.6, Android Gradle Plugin 8.13.2 y AndroidX Core 1.18.0, Lifecycle 2.10.0, Activity 1.13.0 y Play Services Location 21.4.0, con sumas e inventario de licencias regenerados. Datos excluidos de las transferencias entre dispositivos en Android 12+. Recursos sin uso eliminados y avisos de lint corregidos. Documentación simplificada: README nuevo y documentos históricos movidos a `revisiones/historico/`. Versión Android 2.2.12 / código 32. [Cambios y comprobaciones](revisiones/2.2.12/VALIDACION.md).

## 2.2.11 — 23 de octubre de 2026

**Autor público: Mosqueteroweb.** Restaurado el botón «💶 Más barata / 📍 Más cercana» junto al contador de resultados. Retirados el texto de ordenación por combustible y las opciones «Comparar precio de». Se mantienen los filtros de carburantes y las mejoras del mapa de 2.2.10. Versión Android 2.2.11 / código 31. [Cambios y comprobaciones](revisiones/2.2.11/VALIDACION.md).

## 2.2.10 — 23 de octubre de 2026

**Autor público: Mosqueteroweb.** Mapa simplificado: retirados el contador flotante de gasolineras y el botón duplicado de leyenda; esta permanece en los tres puntos. La búsqueda de zona se representa mediante una lupa con diana debajo de GLP, con área táctil de 48 dp y etiqueta accesible. Conserva la búsqueda por el centro del mapa. Versión Android 2.2.10 / código 30. [Cambios y validación](revisiones/2.2.10/VALIDACION.md).

Las publicaciones exigen compilar desde cero en Linux y verificar las dependencias antes de crear su etiqueta. Cada subida de código de la app incluye ahora la publicación de su versión y APK, salvo indicación expresa del usuario.

## 2.2.9 — 23 de octubre de 2026

**Autor público: Mosqueteroweb.** MapLibre Native 13.7.1 con OpenStreetMap, menú Autoría con licencias sin conexión, mapa detenido con el ciclo de vida, cancelación de GPS/consultas al salir y eliminación de sondeos y animaciones permanentes. Publicada con APK firmado y SHA-256; [publicación verificada](revisiones/2.2.9/PUBLICACION.md). Véanse [cambios](revisiones/2.2.9/CAMBIOS.md), [pruebas](revisiones/2.2.9/VALIDACION.md) y [consumo](revisiones/2.2.9/BATERIA.md).

## 2.2.8 — 23 de octubre de 2026

**Autor: Mosqueteroweb.** Correcciones de unidades, comparación por combustible, ubicación manual y exploración del mapa, accesibilidad, privacidad y compilación verificable. Incluye 2.2.7. Detalle en [cambios](revisiones/2.2.8/CAMBIOS.md). Publicada con APK firmado y suma SHA-256; véase [publicación verificada](revisiones/2.2.8/PUBLICACION.md).

## 2.2.7 — 23 de octubre de 2026

**Autor: Mosqueteroweb.**

- Botones de ajustes con altura mínima común de 64 dp.
- Todas las opciones comparten una altura calculada para dos líneas según el tamaño de letra.
- Radio, ordenación y tema con etiquetas centradas en dos líneas; «3 km» y «Claro» dejan de verse más pequeños que sus vecinos.
- Botón de guardar sin altura fija para admitir texto ampliado.
- Información de precios y actualización del Ministerio debajo de la lista, con fechas, avisos y reintento; ubicación conservada arriba.
- Versión Android 2.2.7 / código 27.

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

Véanse [notas de release](RELEASE_NOTES.md), [cambios por archivo](revisiones/historico/CAMBIOS-2.2.5-a-2.2.11.md) y [publicación](revisiones/2.2.5/PUBLICACION.md).

## 2.2.4 y versiones anteriores

La evaluación del APK 2.2.4 está documentada en [la revisión funcional](revisiones/2.2.4-2026-10-22/INFORME.md).
