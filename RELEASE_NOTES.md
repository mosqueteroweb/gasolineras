# Gass 2.2.13 — seguridad

**Autor: Mosqueteroweb. Versión Android: 2.2.13 / 33.** Sin cambios visibles en el uso de la app.

- **Un permiso menos**: se retira el acceso al estado del Wi-Fi, que declaraba la biblioteca del mapa pero no se usaba.
- **Sin cierres por enlaces**: los enlaces a OpenStreetMap, al código fuente y a la fuente de precios muestran un aviso si el móvil no tiene navegador, en vez de cerrar la app.
- **Publicación más fiable**: las herramientas de compilación se descargan solo de Google y Maven Central, con sus sumas verificadas.
- Código sin uso eliminado.

**Auditoría:** permisos, componentes, red, almacenamiento, registros, enlaces, firma y publicación revisados; sin vulnerabilidades graves. Informe completo en [AUDITORIA_SEGURIDAD.md](https://github.com/mosqueteroweb/gasolineras/blob/v2.2.13/revisiones/2.2.13/AUDITORIA_SEGURIDAD.md).

**Comprobación local:** 36 pruebas unitarias y 21 en emulador correctas, lint sin errores, compilación desde cero con verificación estricta. Se instala encima de la 2.2.12 conservando datos.
