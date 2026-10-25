# Gass 2.2.13 — auditoría de seguridad

Fecha: 25 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 2.2.12 (código 32). Correcciones publicadas en 2.2.13 (código 33).

## Alcance

Código Kotlin, manifiesto fusionado del APK de release, permisos, componentes exportados, red, almacenamiento local, enlaces e intents externos, registros en tiempo de ejecución, cadena de compilación y publicación (Gradle, verificación de dependencias y GitHub Actions) y secretos del repositorio.

## Resultado

No se han encontrado vulnerabilidades graves. Cuatro mejoras aplicadas en 2.2.13:

| # | Hallazgo | Riesgo | Corrección |
|---|---|---|---|
| 1 | La resolución de plugins podía recurrir al Gradle Plugin Portal, que sirve POM de marcador distintos de los de Maven Central. La verificación estricta los rechazaba (fallo real en la publicación de 2.2.12) y obligaba a confiar en una segunda fuente. | Medio (cadena de suministro y fiabilidad de publicación) | Retirado `gradlePluginPortal()` de `settings.gradle.kts`: todos los plugins se resuelven solo desde Google Maven y Maven Central. Comprobado con Gradle vacío, `--refresh-dependencies` y verificación estricta. |
| 2 | MapLibre declara `ACCESS_WIFI_STATE`; ninguna clase de la biblioteca ni de la app usa `WifiManager`. | Bajo (permiso innecesario) | Eliminado del manifiesto fusionado con `tools:node="remove"`. Mapa comprobado en emulador. |
| 3 | El enlace «© OpenStreetMap contributors» del mapa y los tres enlaces de «Autoría» lanzaban la actividad sin capturar `ActivityNotFoundException`: la app se cerraba en un dispositivo sin navegador. | Bajo (cierre inesperado) | Nueva función `NavigationHelper.openLink`, que avisa con un mensaje si no hay navegador. |
| 4 | Código muerto: endpoint `getStationsByProvince` sin uso. | Informativo | Eliminado. |

## Comprobaciones sin hallazgos

- **Permisos**: Internet, ubicación precisa y aproximada (solo en primer plano) y estado de red (MapLibre). Sin ubicación en segundo plano.
- **Componentes exportados**: solo `MainActivity` (lanzador) y `ProfileInstallReceiver` de AndroidX, protegido por el permiso de sistema `DUMP`. El receptor de cambios de ubicación se registra como `RECEIVER_NOT_EXPORTED`.
- **Red**: solo HTTPS (`usesCleartextTraffic="false"`); confianza en las CA del sistema (comportamiento por defecto desde Android 7). Respuesta del Ministerio limitada a 32 MB. Registro HTTP solo en compilaciones de depuración. Petición de teselas con User-Agent identificativo, como exige la política de OpenStreetMap.
- **Datos externos**: coordenadas, precios e identificadores validados; textos recortados (pruebas `AuditSecurityTest`).
- **Almacenamiento**: preferencias y caché en el almacenamiento privado de la app. La última ubicación caduca en 24 horas y se puede desactivar. Exclusión de copias en la nube y transferencias entre dispositivos (desde 2.2.12).
- **Registros**: el código de la app no escribe en logcat. En una sesión real del APK de release no aparecen URL, coordenadas ni datos personales.
- **Intents**: la navegación usa `geo:` con la etiqueta codificada y una alternativa web; ajustes del sistema mediante acciones estándar.
- **Firma y publicación**: claves fuera del repositorio (secretos de Actions y `local.properties`); acciones de GitHub fijadas por SHA; permisos mínimos por trabajo; dependencias con SHA-256 contrastado.
- **Secretos**: ningún token ni contraseña en el código ni en la configuración. El remoto de Git ya no incluye credenciales.

## Riesgos aceptados

- La primera clave de firma (sustituida) permanece en el historial público de Git. Las versiones actuales usan otra clave.
- `verify-signatures` sigue desactivado en la verificación de dependencias: las sumas SHA-256 contrastadas cubren la integridad; las firmas PGP dependen de servidores de claves poco fiables en CI.
- Sin fijación de certificados (*pinning*) para el Ministerio: una rotación de su certificado dejaría la app sin datos hasta publicar otra versión.
