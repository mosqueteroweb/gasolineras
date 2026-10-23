# Validación — Gass 2.2.9 / 29, candidata local

Fecha: 23/10/2026. Autor público: Mosqueteroweb. Esta validación corresponde a la candidata local previa a la publicación autorizada posteriormente. El estado de la entrega en GitHub se registra por separado en [PUBLICACION.md](PUBLICACION.md).

## Resultado automático

- **36 pruebas unitarias y 21 instrumentadas: 57 superadas, cero fallos, errores u omisiones.**
- Compilación y optimización de release correctas; análisis Android: **0 errors, 62 warnings, 1 hint**. Las advertencias incluyen mantenimiento de dependencias/SDK y recursos, no se han desactivado análisis para obtener el resultado.
- Última ejecución completa en modo offline de Gradle y verificación estricta de dependencias: `testDebugUnitTest connectedDebugAndroidTest lintDebug assembleRelease`. Offline de Gradle se refiere a sus repositorios, no a una restricción global de red de la aplicación.
- Inventario incorporado de **86 bibliotecas de ejecución distintas** verificado frente al grafo resuelto. Textos y avisos completos empaquetados. Las cuatro bibliotecas MapLibre se contrastaron además con sus SHA-256 publicados en Maven Central.

Entorno: Java 17, Gradle 8.11.1, AGP 8.10.1, Kotlin/Compose Compiler 2.2.10, BOM Compose 2025.08.01. Emulador test_pixel Android 15/API 35 arm64. MinSDK 26, target/compileSDK 35.

Pruebas nuevas: inicio/parada/destrucción equilibrados del mapa; cancelación GPS y consultas al ocultar la app; reanudación conservando referencia manual; cambios de estado GPS sin sondeo; Autoría con texto al 200 % y cierre visible al leer licencias; mapa nativo con cambio de ciudades, arrastre Android real, seguimiento pausado, recentrado y reapertura. La regresión del encuadre comprueba también una estación a unos nueve kilómetros dentro de un radio de diez. El mapa automatizado usa un estilo de prueba local sin peticiones al servidor OSM.

Se conservan las pruebas de DTO, caché, ordenación, unidades, filtros, referencias, municipios, respuestas atrasadas, controles accesibles, Ajustes e Información con cierre manual, cinco segundos y opción de mantener abierta. Los informes exactos están en [pruebas](pruebas/).

## Comprobación visual del APK firmado

- Mapa OpenStreetMap cargado con radio de diez kilómetros y sus marcadores; encuadre corregido para la escala de MapLibre. Madrid: 38 de 38 estaciones, con Diésel, 95 y GLP activos.
- Viaje simulado Madrid → Barcelona → Zaragoza: 38 → 30 → 6 resultados, sin pulsar actualizar precios. Se respetan los intervalos normales de GPS; una captura tomada a los cinco segundos aún mostraba la ciudad anterior y la captura confirmada posterior recogió el cambio. No se interpreta aquella captura intermedia como un fallo permanente.
- Un toque en Repsol, Alberto Aguilera, Madrid, abre la ficha correcta: GLP, resto de precios/unidades, dirección, horario y distancia en línea recta. Favorita añadida desde la ficha.
- Retorno a lista en Zaragoza: seis estaciones y mismas condiciones de búsqueda; precios y comparación visibles.
- «Autoría» aparece en lista y mapa, muestra Mosqueteroweb, versión, fuentes y avisos. Licencia Apache abierta y desplazada sin conexión, con cierre fijo. Autoría propia retirada de Ajustes.
- Actualización forzada sin red: tarjetas conservadas; aviso de datos guardados, fecha y reintento después de la última estación. Arranque posterior sin conexión conserva las seis estaciones de Zaragoza.
- Conectividad del emulador restaurada después de las pruebas sin conexión.
- En el APK final, desactivar GPS mediante Android actualiza inmediatamente el aviso sin salir de la app y conserva las 38 estaciones de Madrid; se reactiva la ubicación y se vuelve a entregar una posición válida.
- Mediciones de mapa, Inicio, pantalla apagada y retorno documentadas en [BATERIA.md](BATERIA.md). No se ha medido autonomía real ni se ha reproducido el alto consumo físico comunicado.

Evidencias seleccionadas en [evidencias](evidencias/). Corresponden a las iteraciones indicadas por su nombre; los informes finales corresponden al código entregado. El APK final se reinstala después de las pruebas de depuración.

## Artefacto local

- Archivo: `gass-2.2.9-local.apk`.
- Tamaño: **44,300,243 bytes**, aproximadamente 44.3 MB; universal para arm64-v8a, armeabi-v7a, x86 y x86_64.
- SHA-256: `53e2ceec5572d588ed3b01e4738cb8be82c84cbd5b03c691f0d476a7d80ca76b`.
- Certificado SHA-256: `68a1531afbc6de0f84ac61eaf990579a4f867c261e168a53579abd09214f3774`; coincide con la 2.2.8 publicada.
- Firma verificada y depuración desactivada. No incorpora permisos de ubicación en segundo plano, servicios en primer plano ni WAKE_LOCK.
- La copia raíz `gasolineras.apk` y su checksum se han actualizado después a la 2.2.9 publicada y verificada. La candidata local de validación se conserva por separado.

La ampliación respecto a los 2.067.307 bytes de la 2.2.8 procede principalmente de las bibliotecas nativas para cuatro arquitecturas. No se presenta MapLibre como una reducción de tamaño. Si se quiere reducir el fichero de descarga, queda como propuesta distribuir APK por arquitectura o un bundle, conservando compatibilidad y firma; no se han eliminado arquitecturas en esta entrega.

Huella de los archivos fuente y recursos en [fuentes-sha256.json](fuentes-sha256.json); datos de entrega en [entrega-local.json](entrega-local.json).

## Límites y mantenimiento

Pendiente confirmar batería en el teléfono afectado, viajes largos, señal débil, fabricantes/versiones Android, consumo físico y TalkBack/Switch Access con lectores reales. Las pruebas de semántica y texto ampliado no son una certificación completa de accesibilidad. La API de anotaciones utilizada sigue disponible en MapLibre 13.7.1 y está deprecada por el proveedor; una futura actualización mayor debe revisar su sustitución por capas GeoJSON. No se ha repetido OSV para el nuevo grafo de dependencias; el resultado anterior de 2.2.8 no se extiende a esta candidata. No se han alterado credenciales ni protecciones de GitHub.

## Publicación posterior

La publicación autorizada pasó de nuevo las pruebas unitarias y lint en Linux, compiló y verificó el APK firmado y publicó sus archivos. El primer intento se detuvo por un metadato sin suma registrada; se completó con sumas oficiales y se mantuvo la verificación estricta. Esa corrección no cambia el código funcional ni las versiones runtime. Los hashes de `fuentes-sha256.json` conservan la candidata local previa a ese ajuste; el commit del APK distribuido se identifica en [PUBLICACION.md](PUBLICACION.md).
