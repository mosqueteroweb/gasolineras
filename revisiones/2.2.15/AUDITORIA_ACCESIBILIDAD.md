# Gass 2.2.15 — auditoría de usabilidad y accesibilidad

Fecha: 26 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 2.2.14. Correcciones publicadas en 2.2.15 (código 35).

## Método

- **Texto grande**: tamaño de letra del sistema al 200 % en emulador (Android 16) con datos reales.
- **Lector de pantalla**: árbol de accesibilidad (uiautomator) y pruebas de Compose sobre las etiquetas, roles y acciones que anuncia TalkBack.
- **Contraste**: relación de contraste WCAG 2.1 calculada para cada combinación de texto y fondo de la lista y la ficha, en modo claro y oscuro.
- **Áreas táctiles**: medidas de los controles en el árbol de accesibilidad (densidad 420 ppp).

## Hallazgos corregidos

| # | Hallazgo | Criterio | Corrección |
|---|---|---|---|
| 1 | Precio normal en azul sobre casilla gris: 3,6:1 en claro y 3,0:1 en oscuro. | Contraste mínimo 4,5:1 (WCAG 1.4.3) | Azul oscuro en modo claro y azul claro en oscuro (8,0:1). También en la ficha. |
| 2 | Precio más barato en verde sobre verde claro: 3,8:1. | WCAG 1.4.3 | Verde más oscuro (más de 6:1). |
| 3 | Al 200 % el precio se partía en «1,769 €/» y «L». | Texto ampliable sin pérdida (WCAG 1.4.4) | El precio ocupa siempre una línea y reduce su tamaño si no cabe. |
| 4 | Al 200 % el distintivo «24H» se mostraba en vertical, una letra por línea. | WCAG 1.4.4 | El distintivo no se parte; la marca se abrevia con «…» si falta espacio. |
| 5 | Al 200 % las iniciales de la marca se salían del círculo. | WCAG 1.4.4 | Iniciales decorativas con tamaño fijo dentro del círculo. |
| 6 | Dirección limitada a una línea, cortada pronto con texto grande. | WCAG 1.4.4 | Hasta dos líneas. |
| 7 | TalkBack leía «uno coma siete seis nueve euro barra L» y los emojis («trofeo», «círculo verde»). | Nombre accesible comprensible (WCAG 1.1.1, 4.1.2) | Cada casilla se lee como «Diésel: 1,769 euros por litro, la más barata de la zona». Igual en la ficha. |
| 8 | TalkBack leía las iniciales del avatar («ÁR», «SH»). | Contenido decorativo (WCAG 1.1.1) | Avatares ocultos al lector de pantalla. |
| 9 | El botón de radio se anunciaba dos veces («Cambiar radio… actual: 10 km», «10 km»). | WCAG 4.1.2 | Un solo anuncio: «Radio de búsqueda: 10 km. Cambiar radio». |
| 10 | El botón de ordenación leía el emoji y no explicaba la acción. | WCAG 4.1.2 | «Ordenadas por precio de Diésel. Cambiar a cercanía». |
| 11 | La tarjeta se podía pulsar, pero TalkBack no decía para qué. | WCAG 4.1.2 | Acción anunciada como «Ver detalles». |
| 12 | Direcciones con una palabra en minúscula seguían en mayúsculas («AVDA. … KM 4,700 km»). | Legibilidad | Se convierten si la mayoría del texto está en mayúsculas. |

## Comprobaciones sin hallazgos

- Botones de carburante: rol de casilla de verificación con estado marcado y nombre completo («Gasolina 95 E5»).
- Áreas táctiles de 48 dp en carburantes, favoritas, «Ir», ordenación y barra superior.
- Contrastes de texto blanco sobre azul (4,6:1) y rojo (5,6:1), direcciones (6,3:1 y 6,9:1), textos secundarios (4,5:1 o más).
- Contenido equivalente al mapa disponible en la lista, indicado en la descripción del mapa.

## Pendiente

- Los iconos de los botones de carburante no crecen con el texto (son decorativos y el nombre sí crece).
- Las casillas de precio de una misma tarjeta pueden tener alturas distintas con texto muy grande.
- Los marcadores del mapa no son accesibles individualmente; la lista ofrece la misma información.
