# ⛽ Gass — gasolineras y precios en España (con especial atención al GLP)

[![Descargar APK](https://img.shields.io/badge/📲_Descargar_APK-gasolineras.apk-brightgreen?style=for-the-badge)](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk)

Aplicación Android nativa para encontrar las gasolineras cercanas y sus últimos precios oficiales. Usa los datos públicos del **Ministerio para la Transición Ecológica (MITECO)**: sin cuentas, sin publicidad, sin analítica y sin claves de pago.

**Autor: Mosqueteroweb.** Licencia [GPLv3](LICENSE).

## Qué hace

- **Precios oficiales** de todos los carburantes que publica el Ministerio: diésel, gasolina 95/98, GLP, GNC, GNL, biodiésel, AdBlue, HVO, hidrógeno…
- **GLP destacado**: distintivo verde 🟢 con su precio en cada estación con Autogas, aunque no se haya elegido GLP entre los carburantes. Al seleccionar GLP se muestran solo las estaciones que lo venden.
- **Lista o mapa** con un toque. El mapa usa OpenStreetMap con MapLibre Native, sin claves de Google.
- **Más barata o más cercana**: ordenación alternable y distintivo 🏆 para el precio mínimo de la zona.
- **Filtros**: hasta 3 carburantes a la vez, radio de 3, 10, 25 o 100 km, búsqueda por marca, calle o municipio, y favoritas.
- **Búsqueda por zona**: explora el mapa y busca alrededor del punto elegido.
- **Funciona sin conexión** con la última consulta válida, indicando la fecha oficial y la de guardado.
- **Cómo llegar**: abre la gasolinera en la app de mapas o navegación del móvil.
- **Privacidad**: la ubicación solo se usa en primer plano y en el propio dispositivo. Los datos de la app no se incluyen en copias de seguridad ni en transferencias a otro móvil.

## Instalación

1. Descarga [`gasolineras.apk`](https://github.com/mosqueteroweb/gasolineras/releases/latest/download/gasolineras.apk) desde la última release.
2. Ábrelo en un móvil o tablet con Android 8.0 o superior y autoriza la instalación si el sistema lo pide.

Cada release incluye `gasolineras.apk.sha256` para comprobar el archivo. Todas las versiones se firman con el mismo certificado, así que se actualizan sobre la anterior sin perder favoritas ni ajustes.

## Compilar

Requiere JDK 17 y el SDK de Android con la plataforma 36.

```sh
./gradlew testDebugUnitTest lintDebug assembleRelease
./gradlew connectedDebugAndroidTest   # con un emulador o móvil conectado
```

El APK queda en `app/build/outputs/apk/release/gasolineras.apk`. La firma de release necesita las claves locales (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` en `local.properties` o como variables de entorno); sin ellas se genera un APK sin firmar.

Las dependencias se verifican con sumas SHA-256 (`gradle/verification-metadata.xml`). Las licencias de las bibliotecas incluidas se muestran en la app, en el menú «Autoría».

## Tecnología

Kotlin 2.2 · Jetpack Compose (Material 3) · MVVM con StateFlow y corrutinas · Retrofit, OkHttp y Gson · Google Play Services Location · MapLibre Native 13.7 con OpenStreetMap.

## Documentación

- [CHANGELOG.md](CHANGELOG.md): historial de versiones.
- [PUBLICACION.md](PUBLICACION.md): cómo se valida, firma y publica cada versión.
- [revisiones/](revisiones/): validación detallada de cada versión y documentos históricos.
