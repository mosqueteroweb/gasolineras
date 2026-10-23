# Gass 2.2.11 — cambios y comprobaciones

Fecha: 23 de octubre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.11 / código 31.

## Cambios

Restaurado el botón «💶 Más barata / 📍 Más cercana» junto al número de resultados, con área táctil mínima de 48 dp y colores adaptados al tema. Se retiran el texto «Orden: precio de…», el rótulo «Comparar precio de» y sus opciones de carburante.

Se conserva la lógica existente de cálculo de precios y distancias, los filtros de carburantes, la información del Ministerio al pie de la lista y las mejoras del mapa de 2.2.10. No se modifican GPS, permisos, dependencias ni funciones de segundo plano. El inventario de licencias se identifica como 2.2.11.

## Validación de la candidata local

Compilación release y lintDebug correctos, sin errores de lint. La candidata firmada se instala en el emulador conservando los datos.

Con 38 resultados en Madrid:

- «💶 Más barata»: encabeza Área 117, con diésel a 1,749 €/L.
- Al pulsar el botón, «📍 Más cercana»: encabeza Repsol de Alberto Aguilera, a 1,5 km.
- Segunda pulsación: vuelve «💶 Más barata» y Área 117 al primer lugar.
- Comprobada la ausencia de los textos «Comparar precio de» y «Orden: precio de» en los tres estados.

Estos precios corresponden a los datos consultados durante la prueba, no a una garantía de precios actuales.

## Publicación

El publicador debe comprobar el commit exacto de 2.2.11 en Linux con Gradle vacío y verificación estricta, antes de crear la etiqueta. El workflow de la etiqueta repite la validación, firma el APK y publica la release con su suma SHA-256. La entrega se comprueba descargando el archivo oficial y contrastando versión, certificado y digest de GitHub. No se sobrescriben las versiones anteriores.
