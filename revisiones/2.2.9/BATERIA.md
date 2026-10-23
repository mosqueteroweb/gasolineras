# Consumo y segundo plano — candidata local 2.2.9

## Resultado y alcance

Se han corregido el ciclo de vida del mapa y la cancelación de trabajo al salir, eliminando además el sondeo del estado GPS y una animación continua innecesaria. En las muestras cortas del emulador la candidata quedó sin solicitud GPS activa en segundo plano y con pantalla apagada. No se reprodujo el consumo elevado comunicado en el teléfono: la 2.2.8 tampoco consumió CPU apreciable en su muestra de segundo plano. No se atribuye por tanto una causa única ni se afirma que el problema físico esté definitivamente resuelto.

## Método

Emulador test_pixel, Android 15/API 35 arm64, mismo AVD para referencia publicada 2.2.8 y candidata firmada 2.2.9. Muestras de aproximadamente 30 segundos en cada estado, sin compilaciones durante las muestras de la candidata. CPU de proceso medida con utime+stime de `/proc/PID/stat`; Android confirmó CLK_TCK=100. El intervalo incluye la pequeña demora de captura de diagnósticos, por lo que se informa el tiempo observado entre lecturas. No equivale a potencia eléctrica ni autonomía.

| Versión | Estado | Intervalo (s) | CPU de proceso (s) |
|---|---|---:|---:|
| 2.2.8 | list | 30.986 | 0.39 |
| 2.2.8 | map | 30.945 | 0.44 |
| 2.2.8 | background | 31.104 | 0.01 |
| 2.2.9 | map | 31.574 | 0.04 |
| 2.2.9 | background | 30.397 | 0.01 |
| 2.2.9 | screenoff | 30.436 | 0.03 |
| 2.2.9 | return | 30.354 | 0.03 |

`map`: mapa quieto de Madrid tras carga; `background`: botón Inicio; `screenoff`: bloqueo desde la aplicación visible; `return`: regreso con pantalla encendida. Son muestras ilustrativas, sin repetición estadística ni igualdad garantizada del contenido visual o la caché entre motores. No deben convertirse en un porcentaje de ahorro. [Datos resumidos](bateria-muestras.json).

## GPS, red y activación

- Android mostró `ProviderRequest[OFF]` en el proveedor GPS tanto al dejar la candidata en segundo plano como con pantalla apagada. En primer plano se verificó la solicitud de la app y el cambio de ciudad al entregar nuevas posiciones.
- En las dos muestras ocultas de la candidata, `dumpsys power` informó cero bloqueos de activación globales. El único bloqueo global observado en la referencia pertenecía a `com.android.phone` (`telephony-radio`), no a Gass.
- No hay servicios de Gass registrados en `dumpsys activity services`. El manifiesto no incorpora permiso de ubicación en segundo plano, servicio en primer plano ni WAKE_LOCK. Las pruebas comprueban cancelación de suscripción, petición pendiente y conservación de la referencia manual.
- Los contadores por UID de `dumpsys netstats` permanecieron en 12.566.470 bytes recibidos y 69.602 enviados entre inicio y fin de las muestras de segundo plano y pantalla apagada, todos en el conjunto FOREGROUND; no apareció un conjunto DEFAULT de tráfico de fondo para la app durante esas muestras. Son contadores agregados de Android y pueden reflejarse con demora; no equivalen a una captura continua de paquetes. La inspección individual de sockets mediante `ss` no estuvo disponible por permisos del Android del emulador, y no se usa como evidencia de ausencia de conexiones.
- La caché, consultas y carga inicial generan red legítima al abrir la aplicación. No se han añadido alarmas, trabajadores periódicos ni descargas de mapas por adelantado.

## Confirmación pendiente en el móvil afectado

Instalar la candidata, reproducir la forma habitual de uso y comparar periodos suficientemente largos con Inicio, pantalla apagada y cierre mediante Atrás; revisar el apartado de batería Android y un informe de diagnóstico del mismo dispositivo. Registrar duración, modelo/versión Android, señal GPS y móvil, tiempo de pantalla y versión instalada. Medir un viaje real y la entrega a una aplicación de navegación. No se han medido mAh, horas de autonomía ni GPU/consumo energético físico en este emulador. Tampoco se certifican todos los fabricantes.

Las capturas de diagnóstico completas y los registros de trabajo se conservan solo en la documentación local excluida de Git. Este informe contiene únicamente el resumen pertinente.
