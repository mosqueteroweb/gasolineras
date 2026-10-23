# Instrucciones del proyecto

## Publicación

- Por instrucción del usuario, cada subida de código de la aplicación a GitHub debe incluir una nueva versión y su etiqueta para generar el APK y la release. No dar la entrega por terminada con solo el código en `main`. Esta autorización se mantiene para futuras peticiones de subir código, salvo que el usuario indique expresamente otra cosa.
- Seguir `PUBLICACION.md`. Para publicar una versión nueva, usar `python3 scripts/publish_release.py` desde `main` con los cambios confirmados.
- No crear ni subir etiquetas manualmente ni enviar `main` y su etiqueta a la vez. El publicador debe esperar la validación limpia de Linux del SHA exacto antes de crear una etiqueta nueva.
- No sobrescribir etiquetas ni releases publicadas. No desactivar la verificación de dependencias ni aceptar automáticamente sumas desconocidas en CI. Contrastar cualquier suma nueva con la fuente oficial.
- Si la validación falla, corregir y volver a subir `main`; no sortear el bloqueo de firma/publicación.
- Antes de dar por terminada una release, comprobar los trabajos de validación, firma y publicación; descargar el APK y contrastar versión, certificado y SHA-256 con su archivo de suma y el digest de GitHub.
- Autoría pública: Mosqueteroweb. La documentación privada y la atribución interna a herramientas o asistentes se conservan únicamente en `documentacion-local/`, excluida de Git.
- Usar la autenticación normal de Git. No extraer ni reutilizar tokens de la URL remota para otras herramientas.
