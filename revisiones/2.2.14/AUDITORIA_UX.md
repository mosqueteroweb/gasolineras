# Gass 2.2.14 — auditoría de UX

Fecha: 25 de octubre de 2026. Autor público: Mosqueteroweb. Versión evaluada: 2.2.13. Correcciones publicadas en 2.2.14 (código 34).

## Método

Recorrido completo en emulador (Android 16, 1080 × 2400) con datos reales del Ministerio y ubicación en Madrid: lista, ordenación, tarjetas, ficha de gasolinera, mapa, menú, ajustes y modo oscuro. Se revisan coherencia de textos, jerarquía visual, legibilidad y claridad de la información.

## Hallazgos corregidos

| # | Hallazgo | Corrección |
|---|---|---|
| 1 | La misma gasolina aparece como «95», «Gas 95», «G95» y «Gasolina 95 E5» según la pantalla. | Nombre corto único «Gasolina 95» (también «Gasolina 98», «Biodiésel», «Hidrógeno») en tarjetas, distintivos, ajustes y mapa. El botón principal conserva «95» por espacio, con su nombre completo para lectores de pantalla. |
| 2 | «Más barata» no indica qué carburante ordena la lista cuando hay varios seleccionados. | El botón muestra el carburante de comparación: «Más barata · Diésel». |
| 3 | Las marcas se cortaban a 10 letras («Low Cost R» en vez de «Low Cost Repost»). | Nombre completo; solo se abrevia con «…» si no cabe. |
| 4 | Direcciones y municipios en mayúsculas, tal como los publica el Ministerio. | Texto legible («Avenida Democracia, 15»), respetando carreteras (M-30, N-II), números y «S/N». |
| 5 | La ficha repetía «Madrid (MADRID)». | La provincia solo se añade cuando es distinta: «Getafe (Madrid)». |
| 6 | La ficha decía «desde la referencia» aunque la distancia se calculara con el GPS. | «desde tu ubicación», «desde Getafe» (municipio elegido) o «desde Madrid (referencia)». |
| 7 | En la ficha los carburantes salían en un orden distinto al de los botones. | Primero los carburantes elegidos, en el mismo orden; después el resto. |
| 8 | En modo oscuro las casillas de precio más barato y GLP mantenían fondos claros que deslumbraban; el distintivo «24H» seguía el tema del sistema y no el elegido en la app. | Colores específicos para modo oscuro y detección del tema de la app. |
| 9 | Distintivo «🏆 Diésel, Gas 95» poco explícito. | «🏆 Más barata: Diésel, Gasolina 95». |
| 10 | «10km» sin espacio en la barra y en el selector de radio. | «10 km». |
| 11 | En el menú, «Elegir municipio» y «Usar mi ubicación GPS» no tenían icono, a diferencia del resto. | Iconos de ciudad y de ubicación. |
| 12 | Las etiquetas accesibles alternaban «favoritos» y «favoritas». | «favoritas» en toda la app. |

## Pendiente para próximas versiones

- Las tarjetas son altas: caben unas dos por pantalla. Una variante compacta (horario y botón «Ir» en la fila de la distancia) permitiría ver más resultados.
- La cabecera muestra la fecha completa de la última ubicación; una hora relativa («hace 2 min») sería más rápida de leer.
- La hoja de ajustes es larga y usa emojis como iconos de sección; conviene agruparla y usar iconos del sistema.
