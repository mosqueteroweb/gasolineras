# Licencias y atribuciones — 2.2.9 local

Autor público de Gass: Mosqueteroweb. La licencia propia continúa siendo GPLv3.

La pantalla «Autoría» carga los archivos de `app/src/main/assets/licenses/` sin depender de conexión. Incluye el inventario resuelto de ejecución, licencias de sus familias, los avisos incorporados en AAR/JAR (incluidos los `third_party_licenses` de Google) y el documento completo de componentes nativos de MapLibre de su etiqueta Android 13.7.1. Se conservan las condiciones de Google Play services separadas de las licencias abiertas; no se califican como Apache sus componentes propietarios.

Fuentes oficiales:

- [MapLibre Android y sus componentes, versión elegida](https://github.com/maplibre/maplibre-native/blob/android-v13.7.1/platform/android/LICENSE.md).
- [Artefactos y motores disponibles](https://maplibre.org/maplibre-native/android/examples/artifacts/).
- [OpenStreetMap: atribución y ODbL](https://www.openstreetmap.org/copyright).
- [Política del proveedor actual](https://operations.osmfoundation.org/policies/tiles/): atribución visible, identificación y caché; sin descarga masiva ni paquetes de mapas sin conexión.
- [Avisos de componentes de Google Play services](https://developers.google.com/android/guides/opensource).

Al cambiar dependencias debe regenerarse el inventario y revisarse su correspondencia con los avisos distribuidos. Las bibliotecas de pruebas y herramientas de compilación no se distribuyen como runtime de la aplicación. Los textos de MapLibre incluyen además sus componentes nativos y sus propios avisos conservados.

## Regeneración y comprobación

Con JDK 17, ejecutar desde la raíz:

```sh
./gradlew -I scripts/export-runtime-licenses.gradle :app:runtimeLicenseIndex --dependency-verification strict
python3 scripts/update-license-notices.py
```

Revisar las licencias de cualquier dependencia nueva y actualizar los textos de su versión; la exportación automática no sustituye esta revisión. El compilado comprueba que el inventario de versiones incluido en el APK coincide con el grafo de ejecución y falla si está desactualizado. El índice temporal con rutas locales queda en `app/build/reports`, excluido de Git. Los textos adicionales de MapLibre Java, Gestures y Okio se han conservado desde sus repositorios oficiales y se revisan al actualizar esas bibliotecas.
