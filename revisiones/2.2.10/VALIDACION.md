# Gass 2.2.10 — cambios y validación

Fecha: 23 de octubre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.10 / código 30.

## Cambios

- Eliminados el contador flotante de gasolineras y el acceso flotante a la leyenda del mapa. La leyenda permanece en el menú de tres puntos.
- Búsqueda de zona mediante botón circular de lupa con diana debajo de GLP (tercer carburante visible). La disposición usa una columna para mantener el botón bajo los filtros; área táctil de 48 dp y descripción «Buscar en esta zona» para lectores de pantalla.
- Se conserva la búsqueda por el centro actual del mapa; el botón está desactivado mientras no se conoce ese centro. No confunde la referencia manual «Zona del mapa» con la posición GPS.
- Se conserva la atribución obligatoria de OpenStreetMap y el resto de funciones de 2.2.9.
- Inventario de licencias identificado como 2.2.10, sin incorporar nuevas bibliotecas.

## Comprobaciones antes de publicar

El cambio de interfaz del commit `40679610e94ee30ba3bf3850d77dae81b591be83` supera la [compilación limpia en Linux](https://github.com/mosqueteroweb/gasolineras/actions/runs/39983635409): pruebas del publicador, pruebas unitarias Android, lint y compilación de release con verificación estricta de dependencias.

La candidata local supera assembleDebug, lintDebug y assembleRelease. Lint: cero errores y 62 advertencias existentes. Instalación de la variante firmada en el emulador conservando los datos; certificado compatible con la versión anterior.

Comprobaciones en el emulador:

- Contador y acceso flotante a la leyenda ausentes; lupa con diana debajo de GLP.
- Desplazamiento del mapa y pulsación de la lupa: cambian el centro de búsqueda y los marcadores; el panel de información identifica la referencia manual como «Zona del mapa».
- Apertura de la leyenda desde los tres puntos y cierre con Atrás.
- Texto al 150 %: el nuevo botón permanece debajo de los filtros, sin solapamiento.

Para publicar esta versión se exige además validar en Linux el commit exacto que contiene 2.2.10, y después validar, firmar y publicar mediante el workflow de su etiqueta. No se reutiliza la validación de otro commit ni se sobrescriben versiones anteriores.

No se han cambiado las funciones de ubicación ni batería en esta entrega. Se mantienen los límites y comprobaciones de [2.2.9](../2.2.9/VALIDACION.md).
