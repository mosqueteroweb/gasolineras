# Gass 2.2.18 — validación

Fecha: 3 de noviembre de 2026. Autor público: Mosqueteroweb. Versión Android: 2.2.18 / código 38.

Origen: auditoría de UX en Galaxy Z Fold8, hallazgos G2 y G3.

## Cambios

- **G2, lista y mapa juntos en pantallas anchas.** Con 600 dp o más (pantalla interior de un plegable, tablets, horizontal), `HomeScreen` muestra la lista (42 %) y el mapa (58 %) uno al lado del otro. El botón de cambiar de vista se oculta, la fila de carburantes solo aparece en la lista y «Leyenda del mapa» sigue en el menú. En la pantalla exterior y en móviles todo sigue igual. `StationListContent` acepta un `modifier`.
- **G3, sin volver a localizar al plegar.** Plegar o desplegar recrea la actividad. Si la pausa dura menos de 60 s y ya hay ubicación y gasolineras, `onForeground` no vuelve a pedir una ubicación nueva ni a recargar la lista, y desaparece el aviso «Obteniendo tu ubicación…». El seguimiento pasivo se reanuda igual. Tras una pausa larga se comporta como antes.

## Comprobación

- Pruebas unitarias correctas y lint sin errores.
- Emulador arm64 (Android 16) con 2184×1968 (interior) y 1080×2520 (exterior):
  - Interior: lista y mapa visibles a la vez; tocar un marcador abre la ficha con «Cómo llegar con GPS».
  - Exterior: lista como siempre; «atrás» desde el mapa vuelve a la lista.
  - Al plegar y desplegar se mantiene «38 encontradas» sin barra de progreso ni nueva localización.
  - 14 ciclos de plegado seguidos con una compilación release: sin ANR. Una compilación debug sí dio un ANR (espera de foco al recrear con el mapa); la 2.2.17 oficial y la 2.2.18 en release, no.
