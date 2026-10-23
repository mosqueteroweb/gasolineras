> Actualización local 2.2.8: las mejoras aplicadas y sus límites están en [cambios](revisiones/2.2.8/CAMBIOS.md) y [validación](revisiones/2.2.8/VALIDACION.md). El informe siguiente es histórico; no describe el estado actual de la app.

> Contexto histórico: las correcciones posteriores están documentadas en [CAMBIOS.md](CAMBIOS.md).

> Documento histórico sobre la versión 2.2.1. Sus afirmaciones no describen necesariamente las versiones posteriores. La revisión funcional de 2.2.4 y la validación de 2.2.5-local están en `revisiones/`; en particular, el soporte offline persistente se implementa en 2.2.5-local.

# ⛽ Auditoría Integral de Software: Gass (Gasolineras España & GLP)

- **Proyecto**: `gasolineras` (Gass)
- **Ruta local**: `/Users/pedro/carpetaHermes/gasolineras`
- **Fecha de auditoría**: Octubre 2026
- **Tipo de aplicación**: Android Nativa (Kotlin + Jetpack Compose + OpenStreetMap/OSMDroid)
- **Versión evaluada**: `2.2.1` (versión code `20`)
- **Calificación Global**: **8.0 / 10**

---

## 1. Resumen Ejecutivo y Ficha Técnica

Gass es una aplicación Android nativa moderna, reactiva y muy bien estructurada que consume en tiempo real la API oficial del Ministerio para la Transición Ecológica (MITECO) para consultar estaciones de servicio y precios en España, con soporte especializado para GLP (Autogas).

El proyecto destaca por su excelente arquitectura limpia (separación por capas Data/Domain/UI con MVVM y Jetpack Compose), inclusión de pruebas unitarias y ausencia de servicios de telemetría invasivos. No obstante, presenta vulnerabilidades críticas en la gestión del almacén de claves (keystore expuesto en el repositorio con contraseñas en texto plano) y oportunidades de optimización en la minificación del APK.

---

## 2. Seguridad y Privacidad

- **Calificación**: **5.5 / 10 (Riesgo Crítico en firma)**

### Hallazgos
1. **Keystore de Firma y Contraseñas Expuestas en Git (🔴 CRÍTICO)**:
   - Archivo: `app/build.gradle.kts` (líneas 27-35):
     ```kotlin
     signingConfigs {
         create("release") {
             storeFile = file("keystore/release.keystore")
             storePassword = "gasolineras2026"
             keyAlias = "gasolineras"
             keyPassword = "gasolineras2026"
         }
     }
     ```
   - Archivo: `app/keystore/release.keystore` está trackeado en el repositorio público de Git.
   - *Impacto*: Cualquier tercero con acceso al repositorio puede suplantar la identidad de la aplicación, firmar actualizaciones maliciosas que Android aceptará como legítimas sobre las instalaciones existentes y comprometer la confianza del usuario.
   - *Remediación*: Extraer contraseñas a variables de entorno (`System.getenv("KEYSTORE_PASSWORD")`) o `local.properties` (ignorado en Git), y almacenar el archivo `.keystore` codificado en base64 en los Secrets de GitHub (`RELEASE_KEYSTORE_BASE64`).
2. **Permisos y Manifiesto (Muy Bueno)**:
   - `AndroidManifest.xml` restringe el tráfico en texto claro (`android:usesCleartextTraffic="false"`).
   - Bloquea respaldos no deseados (`android:allowBackup="false"`).
   - Solicita exclusivamente los permisos necesarios para geolocalización (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) e Internet.
3. **Privacidad de Usuario**:
   - No utiliza trackers publicitarios, SDKs de terceros de analítica (Firebase, Facebook) ni requiere cuentas de usuario. La geolocalización se procesa exclusivamente en el dispositivo.

---

## 3. Arquitectura y Calidad de Código

- **Calificación**: **9.2 / 10**

### Hallazgos
1. **Arquitectura Limpia y Modular (Excelente)**:
   - `data/`: ApiClient (Retrofit), FuelApiService, FuelStationDto, LocationClient, PreferencesManager y FavoritesManager.
   - `domain/`: Modelos puros desacoplados (`GasStation`, `FuelType`, `SortOption`) y utilidades de cálculo (`DistanceCalculator`).
   - `ui/`: Compose idiomático, componentes modulares (`StationCard`, `FuelTypeSelector`, `RadiusFilterBar`, `MapLegendCard`), estado unidireccional con `StateFlow` en `HomeViewModel`.
2. **Adopción de Kotlin 2.0 y Compose Compiler Plugin**:
   - Usa `org.jetbrains.kotlin.plugin.compose` moderno integrado en Gradle.
3. **Manejo de Errores en Red**:
   - Captura excepciones HTTP/SocketTimeout y permite funcionamiento resiliente offline con caché en preferencias.

---

## 4. Dependencias y Ciclo de Vida

- **Calificación**: **8.5 / 10**

### Hallazgos
1. **Componentes Actualizados**:
   - `compileSdk = 34`, `targetSdk = 34`, `minSdk = 26` (Android 8.0 Oreo, cubriendo >95% del parque de dispositivos activos).
   - Compose BOM `2024.08.00`, Retrofit `2.11.0`, OkHttp `4.12.0`, Kotlin Coroutines `1.8.1`.
2. **Stack Open Source**:
   - Utiliza `osmdroid-android:6.1.18` para renderizar mapas OpenStreetMap, eliminando la necesidad de claves de Google Maps API o facturación por consumo de mapas.

---

## 5. Rendimiento y Optimización de Binarios

- **Calificación**: **6.8 / 10**

### Hallazgos
1. **Minificación y Ofuscación Desactivadas**:
   - En `app/build.gradle.kts`:
     ```kotlin
     release {
         isMinifyEnabled = false
     }
     ```
   - El APK generado (`gasolineras.apk`) tiene un peso de **17.7 MB**.
   - Con R8/ProGuard activado (`isMinifyEnabled = true`, `isShrinkResources = true`), el tamaño del APK podría reducirse a **~8-10 MB** (más del 45% de ahorro) y eliminaría métodos no utilizados de librerías como Retrofit y OSMDroid.
2. **Pipeline de GitHub Actions compila `assembleDebug` para Release**:
   - El workflow `.github/workflows/build-apk.yml` ejecuta `./gradlew assembleDebug` en lugar de `assembleRelease`. Aunque la clave de firma coincida, el binario debug contiene flags de depuración innecesarios en producción.

---

## 6. Mantenibilidad, Testing y CI/CD

- **Calificación**: **8.5 / 10**

### Hallazgos
1. **Pruebas Unitarias Integradas**:
   - Dispone de tests unitarios en `app/src/test/java/` (`DistanceCalculatorTest`, `FuelSortingTest`, `FuelStationDtoTest`) que validan la lógica de negocio y parseo de datos.
2. **Automatización de Releases**:
   - Dispone de pipeline automatizado con GitHub Actions que genera la release de GitHub automáticamente ante cada push de etiqueta `v*`.

---

## 7. Plan de Acción y Recomendaciones

### 🔴 Crítico / Prioridad Alta
- [x] **Desvincular Keystore y Claves del Repositorio**:
  - Eliminar `app/keystore/release.keystore` del seguimiento de Git (`git rm --cached`).
  - Mover `storePassword` y `keyPassword` a variables de entorno o `local.properties`.
  - Configurar GitHub Actions Secrets para firmar el APK en el CI/CD sin comprometer la clave.

### 🟡 Medio / Importante
- [x] **Habilitar R8 y Reducción de Recursos**:
  - En `build.gradle.kts` activar `isMinifyEnabled = true` y `isShrinkResources = true` para pasar de 17.7 MB a menos de 10 MB (alcanzado: 1.8 MB).
- [x] **Corregir Workflow de Release**:
  - Cambiar `./gradlew assembleDebug` por `./gradlew assembleRelease` en `.github/workflows/release.yml`.

### 🟢 Bajo / Mejora Continua
- [x] Subir `compileSdk` y `targetSdk` a 35 (Android 15) para cumplir anticipadamente con los requerimientos anuales de Google Play.
