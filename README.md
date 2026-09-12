# ⛽ Gass (con soporte especial GLP)

[![Descargar APK](https://img.shields.io/badge/📲_Descargar_APK-gasolineras.apk_v2.0.1-brightgreen?style=for-the-badge)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)

> **[📥 Descarga directa de gasolineras.apk (versión 2.0.1)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)** · [Ver Notas de la Release](https://github.com/mosqueteroweb/gasolineras/releases/latest)

Aplicación Android nativa desarrollada con **Kotlin + Jetpack Compose (Material 3)** para consultar gasolineras cercanas y precios de carburantes en tiempo real en España.

Especialmente diseñada para familias y conductores que desean ahorrar combustible, con **atención y resalte especial para gasolineras con GLP (Autogas)**.

---

## 🌟 Características Principales

- **Datos Oficiales y Gratuitos**: Conexión directa en tiempo real con la API del **Ministerio para la Transición Ecológica (MITECO)**. Sin cuentas ni API keys de pago.
- **Especial Atención a GLP (Autogas)**:
  - Distintivo visual verde `🟢 GLP` en cada estación que dispone de GLP con su precio directo.
  - Filtro rápido de 1 toque: **"Solo con GLP"**.
  - Información detallada y precios de Autogas destacados.
- **Geolocalización GPS y Cálculo de Distancias**:
  - Detección precisa de la posición del dispositivo con `FusedLocationProviderClient`.
  - Cálculo de distancia en tiempo real con la fórmula de Haversine.
  - Fallback de referencia en Madrid para funcionamiento continuo incluso sin GPS activo.
- **Filtros Flexibles**:
  - Por tipo de combustible: Diésel (Gasóleo A), Gasolina 95 E5 y GLP destacados por defecto (con soporte para Gasolina 98, Diésel Premium, GNC, GNL y Biodiésel).
  - Por radio de búsqueda: 3 km, 10 km, 25 km y 100 km (10 km por defecto, accesible al instante desde el icono de radio en la barra superior).
  - Por marca o localidad mediante buscador de texto reactivo.
- **Detección de la Gasolinera Más Barata**:
  - Detección automática del precio mínimo en la zona para cada uno de los carburantes seleccionados con el distintivo `🏆 MÁS BARATA`.
  - En caso de empate en el precio más bajo, la lista muestra primero la estación más cercana.
- **Navegación GPS Guiada**:
  - Botón de acción rápida **"Ir"** para iniciar navegación paso a paso en **Google Maps, Waze o Petal Maps**.
- **Vista de Mapa o Lista a 1 Toque**:
  - Toggle directo en la barra superior (TopAppBar) entre vista de mapa y vista de lista, sin barra inferior para aprovechar el 100% de la pantalla del dispositivo.
  - Mapa interactivo integrado basado en **OpenStreetMap** (100% libre, sin requerir cuenta ni tarjetas en Google Cloud), con leyendas de precios grandes y toque para ficha completa.

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
   👉 **[Descargar gasolineras.apk (v2.0.1)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)**
2. Ábrelo en tu dispositivo móvil Android (versión 8.0 o superior).
3. Si el sistema te lo solicita, autoriza la instalación de aplicaciones desde tu navegador o explorador de archivos.
4. ¡Listo para consultar los precios y encontrar las gasolineras más baratas!

---

## 📄 Licencia

Este proyecto está bajo la licencia [GNU General Public License v3.0](LICENSE).
