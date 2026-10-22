# Revisión funcional de Gass 2.2.4

**Autor: Mosqueteroweb.** Evaluación histórica del APK 2.2.4; las correcciones posteriores están en [CAMBIOS.md](../../CAMBIOS.md).

Fecha: 22 de octubre de 2026. Pruebas manuales sobre el APK publicado, ejecutado en un Pixel emulado con Android 15 (API 35, arm64, 1080 × 1920). No se ha modificado código, compilado la aplicación ni cambiado los APK del proyecto. Esta carpeta contiene únicamente documentación y evidencias de la revisión.

## Resultado

La consulta de precios, los filtros básicos, las fichas, favoritos, la navegación hacia Google Maps y la persistencia de preferencias funcionan en los recorridos comprobados. **La aplicación necesita mejoras importantes para un viaje: no sigue los cambios de ubicación automáticamente y su gestión de GPS y de pérdida de conexión puede confundir al usuario.** No se observaron excepciones de cierre en el registro de errores de Android durante la sesión. Esto no equivale a una certificación de ausencia de fallos.

## Versión y método

- Los dos APK locales, `gasolineras.apk` y `app/build/outputs/apk/release/gasolineras.apk`, son **2.2.3 / código 22**, aunque README y configuración del proyecto indican 2.2.4.
- Se descargó e instaló la [última release publicada](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk). Se verificaron **2.2.4 / código 23** tanto en el APK como en el paquete instalado y la pantalla de ajustes.
- SHA-256 del APK probado: `2845e7a59f92d7295e9efedb207803850fe02ee8270b69d3aa6018fddba33bda`.
- Se consultaron README.md y AUDITORIA.md. Se leyó el código local para explicar los comportamientos observados, sin modificarlo. Referencia local: `385f7e4`.
- Se usaron pulsaciones, escritura, desplazamientos, apertura/cierre de la aplicación, permisos de Android, GPS simulado y desconexión real de la red del emulador. Las capturas corresponden al APK de release, sin instrumentarlo.
- El emulador empezó con una posición antigua en California. Antes de interpretar las pruebas de viaje se verificaron las coordenadas entregadas por Android. Para los cambios de ciudad se usaron el GPS del emulador y proveedores de ubicación de prueba de Android, con señal repetida durante las adquisiciones. Los fallos transitorios de inyección no se consideran por sí solos defectos de Gass.
- Los precios se contrastaron con una [consulta al servicio oficial](https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/), cuya fecha de respuesta fue `22/10/2026 22:48:28`, con 11.394 estaciones. El extracto comprobado y la huella de la respuesta están en `contraste-datos.json`.

## Viaje simulado y contraste de resultados

| Parada | Coordenadas | Selección y radio | Resultado comprobado |
|---|---|---|---|
| Madrid, Puerta del Sol | 40.416775, -3.703790 | Diésel + 95 + GLP; 10 km | 38 estaciones; coincide con el servicio oficial |
| Madrid | mismas | Solo GLP; 3 / 10 / 25 / 100 km | 1 / 41 / 107 / 173 estaciones; el cambio de radio actualiza resultados |
| Zaragoza | 41.6488, -0.8891 | Diésel + 95 + GLP; 10 km | 6 estaciones; coincide con el servicio oficial |
| Zaragoza | mismas | Solo GLP; 25 km | 9 estaciones; coincide con el servicio oficial |
| Barcelona, centro | 41.3874, 2.1686 | Solo GLP; 25 km | 64 estaciones en lista y mapa después de centrar; coincide con el servicio oficial |
| Barcelona | mismas | Solo GLP; 3 km | Ninguna; coincide con el servicio oficial; se ofrece ampliar a 25 km |
| Barcelona | mismas | Solo GLP; 10 km | 30 estaciones al entrar al mapa; coincide con el servicio oficial |

La llegada a Barcelona con la app ya abierta **no actualiza la zona de Zaragoza**. Enviar la app a segundo plano y volver tampoco actualiza esa zona. Centrar con señal de ubicación reciente sí lleva el mapa a Barcelona. Esta diferencia es el principal problema del recorrido.

## Hallazgos y propuestas

### F01 — Alta: la ubicación queda desactualizada durante un viaje

**Reproducción:** abrir en Zaragoza con GLP y 25 km; cambiar el dispositivo a Barcelona; esperar con la app abierta; salir con Inicio y volver a la misma actividad. Android ya tiene Barcelona, pero Gass conserva las 9 estaciones y el mapa de Zaragoza. Pulsar centrar con señal GPS reciente cambia a las 64 de Barcelona.

**Impacto:** se muestran estaciones y distancias de una parada anterior sin advertencia. El usuario necesita saber que debe actualizar o centrar.

**Propuesta:** actualizar ubicación al volver a primer plano y, mientras se usa la app, con un umbral razonable de tiempo/distancia. Mostrar la antigüedad de la ubicación y un estado de adquisición. Evitar que una respuesta anterior sobrescriba una consulta posterior.

**Apoyo en código:** MainActivity no renueva ubicación al reanudar; LocationClient hace consultas puntuales; HomeViewModel actualiza principalmente al iniciar, actualizar o centrar.

Evidencias: [antes de actualizar](evidencias/41-travel-no-update.png), [vuelta desde segundo plano](evidencias/42-resume-stale.png), [Barcelona después de centrar](evidencias/44-center-live.png).

### F02 — Alta: centrar puede sustituir la ubicación por Madrid sin avisar claramente

**Observado:** una adquisición fallida durante la prueba Barcelona devolvió el mapa a Madrid, con 107 estaciones GLP a 25 km; repetir con señal reciente devolvió Barcelona y 64 estaciones. Este primer fallo de adquisición fue provocado en el entorno de simulación; sirve para comprobar cómo responde la app al fallo, no para medir la fiabilidad del GPS físico.

**Reproducción adicional con el interruptor de ubicación:** desde el mapa de Barcelona, desactivar Ubicación en Android; cerrar el aviso del sistema con «Close» manteniendo la ubicación apagada; pulsar centrar. Gass cambia a Madrid y muestra 41 estaciones GLP a 10 km, sin identificar claramente que ha cambiado a una referencia. El aviso previo de Android sí informa de que la ubicación está apagada, pero Gass no explica el salto de ciudad.

**Impacto:** Madrid puede parecer la posición real del usuario. Con permisos concedidos no aparece el aviso de permiso de la lista.

**Propuesta:** conservar la última posición válida cuando falle una adquisición. Avisar «No se ha podido obtener tu ubicación» y permitir reintentar. Usar Madrid solo como referencia inicial explícitamente identificada. No anunciar «Ubicación actualizada» cuando se haya usado una referencia.

**Apoyo en código:** LocationClient devuelve UserLocation.DEFAULT ante errores o al vencer 5 segundos; recenterLocation asigna ese resultado sin comprobar isDefaultLocation. onRefresh, en cambio, sí evita sustituir la posición por la referencia: los dos controles tienen comportamientos distintos.

Evidencias: [salto a Madrid](evidencias/43-center-barcelona.png), [GPS desactivado: resultado en Madrid](evidencias/71-gps-off-result.png), [recuperación con señal reciente](evidencias/44-center-live.png). El nombre del primer archivo identifica el paso de la prueba, no la ciudad que se ve: la captura muestra Madrid.

### F03 — Alta para uso en viaje: no hay caché de estaciones persistente

**Reproducción:** consultar estaciones con Internet; quitar la conexión; cerrar completamente el proceso y abrir. La lista queda con 0 estaciones y error de red, pese a haber consultado antes esa zona. Dentro de la misma sesión, los datos en memoria sí permiten ordenar y filtrar sin red.

**Propuesta:** persistir la última respuesta válida y su fecha; permitir consultar datos guardados tras reiniciar, indicando claramente su antigüedad. Revisar AUDITORIA.md, que describe una caché offline en preferencias que no se verifica en esta versión. El repositorio consultado conserva las estaciones solo en memoria.

Evidencias: [consulta aún disponible en memoria](evidencias/53-offline-cached.png), [arranque sin red](evidencias/61-offline-cold-list.png).

### F04 — Media: el mapa ignora los carburantes visibles personalizados

**Reproducción:** en ajustes quitar Diésel y añadir Gasolina 98, dejando como visibles 95, GLP y 98. Guardar. La lista muestra esos tres botones; cambiar a mapa vuelve a mostrar Diésel, 95 y GLP. No se puede activar 98 desde el selector del mapa y reaparece un carburante que se había ocultado.

**Propuesta:** usar la misma configuración de carburantes visibles en las dos vistas y evitar activar un carburante que no pertenece a esa configuración.

**Apoyo en código:** HomeScreen pasa visibleFuels al selector de lista, pero omite ese argumento en el selector del mapa.

Evidencias: [lista personalizada](evidencias/65-alt-fuel-list.png), [mapa con botones predeterminados](evidencias/66-alt-fuel-map.png).

### F05 — Media: una actualización fallida oculta datos que siguen disponibles

**Reproducción:** con 64 estaciones cargadas, desactivar datos y Wi-Fi. Confirmar que Android no tiene red predeterminada. Pulsar Actualizar. La cabecera conserva «64 encontradas», pero las tarjetas se sustituyen por `Unable to resolve host ... No address associated with hostname`. Cambiar la ordenación vuelve a mostrar las tarjetas de memoria. Restaurar red y pulsar Reintentar recupera la consulta.

**Propuesta:** conservar las tarjetas y mostrar un aviso en español: «No se han podido actualizar los precios. Mostrando datos guardados de…». Separar error de primera carga y error de actualización. Mantener coherentes contador y contenido.

Evidencias: [error y contador contradictorio](evidencias/54-offline-refresh.png), [datos recuperados al ordenar](evidencias/55-offline-cache-reappears.png), [recuperación con Internet](evidencias/62-online-recovery.png).

### F06 — Media: limpiar caché anuncia éxito aunque no hay conexión

**Reproducción:** sin red, pulsar «Vaciar caché y forzar recarga oficial». Aparece inmediatamente «Caché vaciada y datos recargados del Ministerio», aunque la operación termina en error.

**Propuesta:** anunciar éxito solo cuando termine una recarga válida. Mientras tanto mostrar progreso; al fallar, conservar datos válidos y explicar lo ocurrido. Definir si el control recarga precios o también libera espacio: el callback consultado solo llama a onRefresh, sin limpiar la caché de mapas.

Evidencias: [éxito falso sin red](evidencias/59-false-cache-success.png), [error posterior](evidencias/60-cache-offline-error.png).

### F07 — Media: el aviso de permiso no recupera una denegación permanente

**Reproducción:** denegar la ubicación repetidamente hasta que Android deja de mostrar la solicitud. Pulsar «Pulsa aquí para activar el GPS…» no abre un camino útil para conceder el permiso. Para seguir las pruebas se concedió desde el sistema.

**Propuesta:** distinguir permiso denegado, denegado permanentemente y ubicación del sistema desactivada. Ofrecer acceso a Ajustes de la app o a Ubicación según el caso. Si se usa Madrid, indicarlo también en lista; mostrar distancias de referencia sin identificar la ciudad induce a error.

Evidencia: [aviso tras denegación](evidencias/03-denied.png). La captura prueba el estado del aviso; el bloqueo al pulsarlo se observó durante la interacción.

### F08 — Media: favoritas fuera del radio se describen como inexistentes

**Reproducción:** guardar BP de Madrid; viajar a Barcelona; filtrar favoritas. La lista muestra «No tienes favoritas guardadas», aunque la favorita de Madrid sigue guardada. Añadir una de Barcelona y filtrarla funciona; al quitarla vuelve el estado vacío local.

**Propuesta:** mostrar «No hay favoritas dentro de 25 km» y ofrecer ampliar radio o ver las favoritas guardadas fuera de la zona. Reservar el mensaje de no tener favoritas para cuando el conjunto guardado esté realmente vacío.

Evidencias: [favorita guardada inicialmente](evidencias/06-detail.png), [mensaje incorrecto en Barcelona](evidencias/46-fav-outside.png), [favorita local](evidencias/47-fav-barcelona.png).

### F09 — Baja: Atrás sale de la app con la leyenda abierta

Se abrió la leyenda y se pulsó Atrás de Android: se volvió al lanzador en vez de cerrar primero la leyenda. La ficha de estación sí se cierra con Atrás.

**Propuesta:** consumir Atrás para cerrar elementos superpuestos antes de salir. Revisar el mismo patrón para la búsqueda expandida cuando el teclado ya esté oculto. Este último caso se propone para revisión adicional; no se califica aquí como fallo independiente.

### F10 — Baja: cambiar de vista cambia el radio elegido

Al pasar de la lista de Barcelona con 25 km al mapa, el radio cambia a 10 km y el conjunto pasa de 64 a 30 estaciones. El código elige el primer radio con resultados. No es una pérdida de datos; sí un cambio de criterio que el usuario no ha pedido explícitamente.

**Propuesta:** conservar el radio al alternar vistas o presentar claramente la búsqueda automática como una opción. Facilita comparar lista y mapa y coincide mejor con la promesa de sincronización del README.

Evidencias: [lista a 25 km](evidencias/65-alt-fuel-list.png), [mapa a 10 km](evidencias/66-alt-fuel-map.png).

### F11 — Baja: artefactos locales y documentación inducen a probar otra versión

Los APK locales son 2.2.3; el README dice 2.2.4 y la release publicada sí es 2.2.4. AUDITORIA.md evalúa 2.2.1 y mezcla hallazgos históricos con correcciones marcadas, además de describir un funcionamiento offline que esta sesión no confirma.

**Propuesta:** identificar cada APK con versión y huella, y fechar claramente las revisiones. Mantener el enlace a la release como fuente verificable. No se han sustituido los APK locales en esta revisión.

## Cobertura de las opciones

| Área | Comprobación y resultado |
|---|---|
| Instalación y apertura | Release 2.2.4 instalada y abierta; versión verificada |
| Permiso denegado | La consulta usa Madrid; problema de recuperación tras denegación repetida |
| Ubicación precisa | Madrid, Zaragoza y Barcelona; actualización manual válida con señal reciente |
| Viaje en primer plano y segundo plano | Zona antigua persiste; F01 |
| Fallo de adquisición y GPS desactivado | Centrar sustituye Barcelona por Madrid; F02 |
| Selector principal | Tres carburantes, solo GLP y protección del último seleccionado comprobados |
| Catálogo de ajustes | Se muestran 12 opciones; límite de tres comprobado; sustitución por 98 guardada |
| Personalización entre vistas | Lista correcta, mapa usa botones predeterminados; F04 |
| Radios | 3, 10, 25 y 100 km; contador y resultados cambian |
| Orden | Más barata y más cercana; resultados visibles y distancias coherentes |
| Trofeos | Mínimos por carburante visibles, incluidos mínimos distintos entre BP y Área 117; cambian al filtrar favoritas |
| Búsqueda | Marca Repsol: 3 coincidencias en Zaragoza; localidad Zaragoza y texto sin coincidencias comprobados; limpiar/restablecer funciona |
| Lista vacía | Barcelona GLP 3 km: sin resultados; control de ampliar radio disponible |
| Favoritos | Añadir, filtrar, persistir tras cierre y quitar; mensaje de zona vacía incorrecto |
| Fichas | Apertura desde lista y desde globo del mapa; dirección, horario, distancia, GLP y resto de precios presentes |
| Mapa | Teselas OSM, círculo, posición, marcadores, leyenda y centrado; comparación de 64 estaciones con lista y fuente oficial |
| Marcador | Globo Repsol 3,0 km / GLP 1,175 €/L; toque del globo abre la ficha correcta |
| Navegación | Ir abre Google Maps con coordenadas y etiqueta correctas; se comprobó la pantalla del destino y su acceso a rutas |
| Preferencias | Inicio en mapa, radio 25 km, cercanía, solo GLP y tema oscuro sobreviven al cierre completo |
| Tema | Inicio siguiendo sistema claro y cambio a oscuro comprobados; no se hizo una prueba independiente de todos los cambios del sistema |
| Actualizar y Reintentar | Recarga con red válida; error sin red; recuperación al restaurar Internet |
| Caché | Datos en memoria utilizables sin red; no persisten entre procesos; aviso de mantenimiento falso |
| Registros de cierre | Sin excepciones en el buffer de crash consultado durante la sesión |

## Límites de esta revisión

Se recorrieron las funciones principales, incluyendo los casos de viaje, errores y preferencias descritos. **No se han probado todas las combinaciones posibles ni todos los modelos de teléfono.** La prueba corresponde a un único emulador Android 15; conviene repetir permisos y ubicación en un teléfono real y en Android 8/12/14/16. No se midieron batería, consumo de datos o rendimiento de forma controlada. No se ejecutaron pruebas unitarias, porque el objetivo era probar el APK publicado sin cambiar ni compilar código.

Waze y Petal Maps no estaban instalados: su compatibilidad real queda pendiente. Google Maps recibió el destino correcto; no se completó un recorrido con instrucciones de voz. El envío usa un enlace geo que abre el lugar, no inicia por sí solo una navegación guiada. No se instalaron servicios adicionales ni se inició sesión en cuentas.

El catálogo se inspeccionó y se probó la personalización con Gasolina 98; no se comprobó una estación real para cada uno de los 12 carburantes. Sí se observaron precios adicionales en las fichas, incluido HVO. Los escenarios de pérdida de GPS deben complementarse con túneles y pérdidas de cobertura reales; los errores de adquisición del emulador no permiten estimar su frecuencia en carretera.

## Orden de mejora propuesto

1. Renovar ubicación durante el uso y al reanudar; conservar la última posición válida y avisar de fallos o referencia Madrid.
2. Mantener una respuesta de precios persistente, con fecha visible y funcionamiento sin red tras reiniciar.
3. Conservar las tarjetas al fallar una recarga y corregir los mensajes de error y éxito.
4. Unificar los selectores de carburante y el radio entre lista y mapa.
5. Resolver permisos permanentes, textos de favoritas fuera de zona y comportamiento de Atrás.
6. Repetir esta matriz en un teléfono real y verificar los clientes de navegación adicionales antes de afirmar compatibilidad completa.

## Entrega y estado final

Se guardan 35 capturas seleccionadas y los árboles de interfaz disponibles en `evidencias/`, la identificación del entorno en `entorno.json`, el contraste con datos oficiales y el registro de cierres (vacío en esta sesión). Se comprobó que todos los enlaces a evidencias de este informe tienen archivo.

Se restauraron Internet móvil y el interruptor de Ubicación, se retiraron los proveedores de ubicación de prueba y se dejó Gass 2.2.4 abierta en el emulador. Las preferencias y favoritas del emulador reflejan las pruebas; no pertenecen a un dispositivo físico del usuario. Los únicos archivos añadidos al proyecto son los de esta revisión.
