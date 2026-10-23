# Publicación de Gass 2.2.8

**Autor: Mosqueteroweb. Fecha: 23/10/2026.**

La 2.2.7 ya está publicada y verificada. Publicación de la 2.2.8 autorizada y en preparación. Incluye los cambios previos de 2.2.7. Este registro se completará al verificar las releases y sus APK firmados en GitHub. La auditoría completa y la atribución privada local permanecen excluidas del repositorio.

## Comprobación de metadatos en un entorno limpio

La primera ejecución remota se detuvo antes de firmar porque la línea base local no incluía dos archivos `.module` anunciados por los POM de JUnit BOM 5.9.2 y 5.9.3. Se añadieron sus SHA-256 desde Maven Central, contrastándolos con las sumas del editor. La verificación estricta sigue activa. No se publicó un APK 2.2.8 en ese intento. Se repitieron correctamente las comprobaciones locales antes de volver a ejecutar la publicación.
