# ⛽ Gasolineras España (con soporte especial GLP)

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
  - Por tipo de combustible: Gasolina 95 E5, Diésel (Gasóleo A), Gasolina 98, Diésel Premium, GLP, GNC, GNL, Biodiésel.
  - Por radio de búsqueda: 3 km, 5 km, 10 km, 20 km y 50 km.
  - Por marca o localidad mediante buscador de texto reactivo.
- **Detección de la Gasolinera Más Barata**:
  - Detección automática del precio mínimo en la zona con el distintivo `¡MÁS BARATA!`.
- **Navegación GPS Guiada**:
  - Botón de acción rápida **"Ir"** para iniciar navegación paso a paso en **Google Maps, Waze o Petal Maps**.
- **Vista de Mapa sin Cuenta**:
  - Mapa interactivo integrado basado en **OpenStreetMap** (100% libre, sin requerir cuenta ni tarjetas en Google Cloud).

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

## 🚀 Compilación e Instalación

### Requisitos
- Android Studio Ladybug / Koala o superior (o JDK 17 y Android SDK 34 por línea de comandos).
- Dispositivo Android con versión 8.0 (API 26) o superior.

### Compilar el APK desde terminal:
```bash
./gradlew assembleDebug
```
El archivo `.apk` se generará en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Licencia

Este proyecto está bajo la licencia [GNU General Public License v3.0](LICENSE).
