# ⛽ Gass (con soporte especial GLP)

[![Descargar APK](https://img.shields.io/badge/📲_Descargar_APK-gasolineras.apk_v2.2.8-brightgreen?style=for-the-badge)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)

> **[📥 Descarga directa de gasolineras.apk (versión publicada 2.2.8)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)** · [Ver Notas de la Release](https://github.com/mosqueteroweb/gasolineras/releases/latest)

Aplicación Android nativa desarrollada con **Kotlin + Jetpack Compose (Material 3)** para consultar gasolineras cercanas y los últimos precios publicados de carburantes en España.

Especialmente diseñada para familias y conductores que desean ahorrar combustible, con **atención y resalte especial para gasolineras con GLP (Autogas)**.

---

## Novedades de la versión 2.2.8

**Autor: Mosqueteroweb.** La 2.2.8 / 28 incorpora las mejoras de seguridad, accesibilidad y uso documentadas en [sus cambios](revisiones/2.2.8/CAMBIOS.md), además de la 2.2.7. Las versiones 2.2.7 y 2.2.8 están publicadas y verificadas. La descarga superior corresponde a la 2.2.8.

Precios con unidad correcta, combustible de comparación explícito, búsqueda por municipio y zona del mapa, controles accesibles, ajustes con guardado visible y opciones de privacidad. La lista completa se desplaza con texto grande; las fechas del Ministerio aparecen después de las gasolineras. Consulta la [validación local](revisiones/2.2.8/VALIDACION.md) y el [plan de mantenimiento del mapa](revisiones/2.2.8/MANTENIMIENTO_MAPA.md).

## Novedades de la versión 2.2.7

**Autor: Mosqueteroweb.**

Los botones de ajustes tienen una altura mínima común calculada para dos líneas según el tamaño de letra. Radio, ordenación y tema usan etiquetas centradas en dos líneas para que las opciones cortas y largas mantengan el mismo tamaño. La altura se adapta si se amplía la letra del teléfono.

En la vista de lista, la fecha de precios del Ministerio, la fecha de guardado y los avisos de actualización aparecen en un pie debajo de las gasolineras; el estado de ubicación sigue arriba.

## Novedades de la versión 2.2.6

**Autor: Mosqueteroweb.**

En el mapa, la fecha de la última ubicación y la información de precios del Ministerio se consultan desde el icono **ⓘ** inferior, junto al botón de ubicación. El panel se cierra con la **X**, pulsando de nuevo el icono, con **Atrás** o automáticamente a los **5 segundos**. Los cambios de ubicación y precios se reflejan también mientras está abierto. La lista mantiene su información de estado visible.

Consulta el [historial](CHANGELOG.md), las [notas de publicación](RELEASE_NOTES.md) y la [validación](revisiones/2.2.6/VALIDACION.md).

## Novedades de la versión 2.2.5

**Autor: Mosqueteroweb.**

La versión **2.2.5 (código 25)** incorpora las correcciones comprobadas primero en la candidata interna 2.2.5-local (código 24). Consulta el [registro completo de cambios](CHANGELOG.md), la [relación de archivos modificados](CAMBIOS.md) y las [notas de la release](RELEASE_NOTES.md).

- La ubicación se renueva al volver a la app y durante su uso en primer plano (solicitudes cada 30 segundos, mínimo 100 metros entre actualizaciones). No se solicita ubicación en segundo plano.
- Si el GPS falla, se conserva la última posición válida; Madrid se identifica expresamente como referencia cuando todavía no hay una posición real.
- La última respuesta válida de precios se guarda en el dispositivo y se puede consultar tras reiniciar sin conexión. Se muestran la fecha oficial y la fecha de guardado; una actualización fallida conserva los datos anteriores.
- Mapa y lista comparten carburantes y radio. Se han corregido los textos de favoritas, el acceso a ajustes de permisos y el botón Atrás de la leyenda/búsqueda.
- El control de mantenimiento se llama «Actualizar precios del Ministerio»: no anuncia éxito anticipadamente ni afirma limpiar la caché del mapa.

Para compilar y comprobar localmente (JDK 17 y SDK Android configurados):

```sh
./gradlew testDebugUnitTest lintDebug assembleRelease
./gradlew connectedDebugAndroidTest
```

La segunda comprobación requiere un emulador o teléfono conectado. El APK generado está en `app/build/outputs/apk/release/gasolineras.apk`. La firma de release requiere las claves locales ya descritas por la configuración del proyecto. Identifica siempre el APK por su versión y huella; un APK anterior en la raíz no se actualiza por el mero hecho de cambiar el código.

Consulta [la revisión de la 2.2.4](revisiones/2.2.4-2026-10-22/INFORME.md), [la validación de la candidata local](revisiones/2.2.5-local/VALIDACION.md) y [el registro de publicación de 2.2.5](revisiones/2.2.5/PUBLICACION.md).


## 🌟 Características Principales

- **Datos Oficiales y Gratuitos**: Conexión directa en tiempo real con la API del **Ministerio para la Transición Ecológica (MITECO)**. Sin cuentas ni API keys de pago.
- **Especial Atención a GLP (Autogas)**:
  - Distintivo visual verde `🟢 GLP` en cada estación que dispone de GLP con su precio directo.
  - Filtro rápido de 1 toque: **"Solo con GLP"**.
  - Información detallada y precios de Autogas destacados.
- **⚙️ Menú de Ajustes y Preferencias de Usuario (v2.1.0)**:
  - Selector de carburantes visibles en pantalla (catálogo completo MITECO: Diésel, Gasolina 95/98, GLP, GNC, GNL, Biodiésel, Gasóleo B, AdBlue, HVO y Hidrógeno) con límite ergonómico de hasta 3 en pantalla.
  - Configuración de carburantes activos por defecto al iniciar la app.
  - Elección de vista inicial (Lista o Mapa), radio de búsqueda inicial (3, 10, 25, 100 km) y criterio de ordenación preferente (Más barata o Más cercana).
  - Selector de tema visual: Claro, Oscuro o Automático (según el sistema).
  - Actualización manual de precios conservando la última respuesta válida para consulta sin conexión.
- **🔄 Animación de Actualización**:
  - Efecto de giro continuo en el icono de recarga mientras se obtienen los últimos datos oficiales del MITECO.
- **Geolocalización GPS y Cálculo de Distancias**:
  - Detección precisa de la posición del dispositivo con `FusedLocationProviderClient`.
  - Cálculo de distancia en tiempo real con la fórmula de Haversine.
  - Conservación de la última posición válida si el GPS falla; Madrid se usa como referencia inicial identificada si aún no hay una posición real.
- **Filtros Flexibles**:
  - Por tipo de combustible: Selección dinámica de hasta 3 carburantes simultáneos en pantalla.
  - Por radio de búsqueda: 3 km, 10 km, 25 km y 100 km (10 km por defecto, accesible al instante desde el icono de radio en la barra superior).
  - Por marca o localidad mediante buscador de texto reactivo.
- **Detección de la Gasolinera Más Barata**:
  - Detección automática del precio mínimo en la zona para cada uno de los carburantes seleccionados con el distintivo `🏆 MÁS BARATA`.
  - En caso de empate en el precio más bajo, la lista muestra primero la estación más cercana.
- **Navegación GPS Guiada**:
  - Botón **"Ir"** para abrir el destino en una aplicación compatible; se verificó la entrega de coordenadas a **Google Maps**, donde se puede iniciar la ruta. La compatibilidad con Waze y Petal Maps queda pendiente de validación real.
- **Vista de Mapa o Lista a 1 Toque**:
  - Toggle directo en la barra superior (TopAppBar) entre vista de mapa y vista de lista, sin barra inferior para aprovechar el 100% de la pantalla del dispositivo.
  - Mapa interactivo integrado basado en **OpenStreetMap** (100% libre, sin requerir cuenta ni tarjetas en Google Cloud), con leyendas de precios grandes y toque para ficha completa.
  - **Sincronización de Distancia y Círculo Perimétrico (v2.2.0)**: Muestra en el mapa las mismas estaciones que en la lista según el radio activo, con un círculo translúcido delimitador y auto-encuadre/zoom calibrado.
  - **Leyenda Interactiva de Iconos (v2.2.0)**: Botón cápsula flotante con explicación detallada de cada distintivo (Más barata 🏆, Más cercana ⚡, Con GLP 🟢, Estándar ⛽, Tu ubicación 📍 y Radio activo ⭕).

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin 2.0+
- **Interfaz**: Jetpack Compose con Material 3 y Material Icons Extended
- **Red**: Retrofit 2 + OkHttp + Gson
- **Ubicación**: Google Play Services Location (`FusedLocationProviderClient`)
- **Mapas**: OpenStreetMap / Osmdroid (mapas libres sin API key) + Intent nativo de navegación
- **Asincronía y Reactividad**: Kotlin Coroutines & StateFlow
- **Arquitectura**: Clean Architecture + MVVM

---

## 📲 Instalación en Android

1. Descarga el archivo **`gasolineras.apk`** directamente desde el enlace oficial:
   👉 **[Descargar gasolineras.apk (v2.2.5)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)**
2. Ábrelo en tu dispositivo móvil Android (versión 8.0 o superior).
3. Si el sistema te lo solicita, autoriza la instalación de aplicaciones desde tu navegador o explorador de archivos.
4. ¡Listo para consultar los precios y encontrar las gasolineras más baratas!

---

## 📄 Licencia

Este proyecto está bajo la licencia [GNU General Public License v3.0](LICENSE).
