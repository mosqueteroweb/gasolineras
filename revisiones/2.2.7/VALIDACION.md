# Validación de Gass 2.2.7

Autor: Mosqueteroweb. Fecha: 23 de octubre de 2026.

## Corrección

Todos los botones de ajustes comparten una altura calculada para dos líneas de texto y su relleno, con un mínimo de 64 dp. El cálculo usa el tamaño de letra configurado en Android. Las opciones de radio, tema y ordenación tienen saltos de línea explícitos y texto centrado, evitando que «3 km» y «Claro» queden más bajos que sus vecinos.

Se mantienen los anchos iguales dentro de cada grupo. El catálogo de carburantes conserva sus anchos según la longitud de cada nombre. El botón de guardar también utiliza la altura común adaptable.

## Comprobaciones

- Compilación del APK optimizado de release completada correctamente.
- Lint sin errores.
- Versión Android 2.2.7 / código 27.

Los cambios son de presentación. La selección y el guardado de preferencias conservan sus controladores existentes.

## Lista: información del Ministerio

La última ubicación se mantiene arriba. La fecha oficial de precios, la fecha de guardado y los avisos con reintento se sitúan en un pie fijo debajo de la zona desplazable de gasolineras, sin necesidad de llegar al último resultado. El área de lista respeta el espacio del pie y de la barra de navegación. El mapa mantiene su icono de información y temporizador.

- 23 pruebas unitarias y 9 instrumentadas superadas en Android 15 tras los dos cambios.
- Comprobación visual con letra al 130 %: la información de precios está debajo de las tarjetas y se muestra una única vez.
- Los cuatro radios y los tres temas tienen la misma altura: 168 píxeles en la captura al 130 %. Evidencia: `evidencias/dimensiones-130.json`.
- La revisión al 200 % detecta límites del diseño general de ajustes y de la vista horizontal, documentados como mejoras pendientes en la auditoría local; no se presenta esta entrega como una certificación de accesibilidad.
