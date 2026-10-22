# Cambios

**Autor: Mosqueteroweb.** **Fecha:** 22 de octubre de 2026. **Entrega:** Gass 2.2.5, código 25.

Este documento recoge la revisión del APK 2.2.4, las propuestas, la implementación, las pruebas y la documentación de Gass 2.2.5.

## Código y configuración modificados

| Archivo | Trabajo realizado |
|---|---|
| `app/src/main/java/com/gasolineras/app/MainActivity.kt` | Activación y retirada del seguimiento por ciclo de vida; solicitud inicial de permiso y recuperación desde ajustes de aplicación/ubicación. |
| `data/location/LocationClient.kt` | Contrato LocationProvider, consultas de ubicación con cancelación, validación de antigüedad, flujo de cambios en primer plano y ausencia de referencia Madrid ante errores. Ruta relativa al paquete `com/gasolineras/app` de `app/src/main/java`. |
| `data/local/LastLocationStore.kt` (nuevo) | Persistencia local de la última posición válida y su fecha. |
| `data/local/StationCache.kt` (nuevo) | Persistencia de respuesta oficial y fecha; lectura tolerante a daños y escritura atómica. |
| `data/repository/FuelRepository.kt` | Caché disco/memoria, conservación de datos válidos ante fallos, fechas/avisos, bloqueo de descargas concurrentes y resultados de mapa limitados al mismo conjunto de la lista. Se elimina el cambio automático de radio al entrar al mapa. |
| `ui/home/HomeUiState.kt` | Estado de adquisición de ubicación, permisos/GPS, fechas y avisos de datos guardados. |
| `ui/home/HomeViewModel.kt` | Renovación de ubicación al volver y desplazarse; conservación de la posición válida; seguimiento cancelable; protección de respuestas atrasadas; radio compartido; selección coherente y recálculo de favoritas/mínimos. |
| `ui/home/HomeScreen.kt` | Selectores de carburantes compartidos, panel de estado en ambas vistas, recuperación de GPS/permisos, mensajes de favoritas y errores, cierre por Atrás y preferencias predeterminadas separadas del estado temporal. |
| `ui/components/HomeStatus.kt` (nuevo) | Fechas en formato español, estado de referencia/última posición y avisos de recarga con reintento sin ocultar tarjetas. |
| `ui/components/SettingsBottomSheet.kt` | Sustitución del aviso falso de limpieza/éxito por un control de actualización con explicación de conservación de datos. |
| `app/build.gradle.kts` | Identificación de candidata 2.2.5-local/código 24 y promoción a publicación 2.2.5/código 25. |
| `.gitignore` | Exclusión del estado generado por el compilador Kotlin; se mantienen las exclusiones de APK, configuración privada y claves. |
| `.github/workflows/release.yml` | Pruebas unitarias y lint antes de compilar; comprobación de firma, checksum, notas de RELEASE_NOTES.md y comprobación de existencia de adjuntos. |

Salvo MainActivity, las rutas abreviadas de código de la tabla pertenecen a `app/src/main/java/com/gasolineras/app/`.

## Pruebas añadidas

- `app/src/test/java/com/gasolineras/app/FuelRepositoryTest.kt`: 11 regresiones de caché, reinicio offline, recargas fallidas/vacías/inválidas, datos dañados, cambios de ciudad, favoritos, concurrencia y cancelación.
- `app/src/androidTest/java/com/gasolineras/app/HomeViewModelRegressionTest.kt`: 6 regresiones de cambio de ciudad por flujo, reanudación, fallo de GPS, referencia inicial, radio/carburantes y respuestas atrasadas.
- Se ejecutaron las 12 pruebas unitarias existentes además de las 11 nuevas: 23 en total. Las 6 pruebas instrumentadas se ejecutaron en Android 15.
- Pruebas de interfaz del APK optimizado: Madrid (38), Zaragoza (6), Barcelona (30); reinicio sin conexión y recarga fallida conservando 30 estaciones; GPS apagado sin salto a Madrid; selector 95/GLP/98 y 25 km/64 estaciones en mapa y lista; cierre de leyenda con Atrás.
- Las evidencias y resultados de la candidata se preservan en `revisiones/2.2.5-local/`. La preparación de la publicación está en `revisiones/2.2.5/`.

## Documentación escrita o actualizada

- `README.md`: versión pública, resumen funcional, autoría explícita, enlaces a cambios, compilación y límites de navegación comprobada.
- `AUDITORIA.md`: contexto histórico y enlaces a revisiones posteriores.
- `CHANGELOG.md` y `RELEASE_NOTES.md`: historial y notas publicadas de la entrega.
- `revisiones/2.2.4-2026-10-22/INFORME.md`: revisión funcional original, 11 hallazgos, propuestas y evidencias, sin cambios de código en aquella fase.
- `revisiones/2.2.5-local/VALIDACION.md`: implementación y validación de la candidata interna, se conserva como historial anterior a la publicación.
- `revisiones/2.2.5/PUBLICACION.md`: versión final, controles previos, enlaces de GitHub, generación de artefactos y alcance de la publicación.

## Alcance y límites

Se conserva la arquitectura Kotlin/Compose, la fuente de datos MITECO y la integración de navegación existente. No se añaden cuentas, telemetría ni permisos de ubicación en segundo plano. El control de mantenimiento actualiza precios: no implementa un borrado manual de mapas. La compatibilidad con Waze/Petal Maps y las pruebas en teléfonos físicos, otras versiones de Android y viajes largos siguen pendientes.

Las claves y la configuración local privada no forman parte del commit ni de las notas. Los APK se entregan como archivos de release; no se incorporan a los archivos de código de Git. Los informes históricos conservan los resultados y fechas originales, identificando cuándo describen una candidata todavía no publicada.
