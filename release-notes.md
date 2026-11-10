## Novedades de la versión

### 🚀 Nuevas Funcionalidades
- **Caché sin conexión con Room**: Implementada la persistencia local en Room para consultar detalles de animes previamente cargados y la lista de seguimiento (Watchlist) sin necesidad de una conexión activa a internet.
- **Sistema de calificación de 5 estrellas**: Añadido un componente interactivo de 5 estrellas en el diálogo de detalles de cada anime para puntuar los títulos y guardar las calificaciones permanentemente en la base de datos Room.

### 🎨 Mejoras de Interfaz (UI/UX)
- **Filtros avanzados colapsables**: Se optimizó el espacio vertical de la pantalla principal haciendo que los selectores de géneros, plataformas de streaming, filtros de episodios y ordenamiento sean desplegables ("Filtros avanzados y géneros"), maximizando el área visible para los animes.
- **Doble variante para el Widget**: El widget de la pantalla de inicio ahora incluye una versión compacta (muestra la cantidad de estrenos del día) y una versión extendida (con detalles del próximo estreno), eliminando por completo los errores de visualización en blanco.
- **Localización de zonas horarias**: Se reemplazaron las etiquetas ambiguas `"LOCA"` y `"JST"` por los indicadores claros en español `"Local"` y `"Japón"`, mejorando la consistencia idiomática en todo el dispositivo.

### 🛠️ Correcciones de Errores (Bug Fixes)
- **Estabilidad al iniciar (Crash fix)**: Se corrigió el error de cierre inesperado al abrir la aplicación (`ClassNotFoundException`) ajustando las rutas de los componentes en el manifiesto (`AndroidManifest.xml`) de acuerdo con el paquete principal.

## Descargas

El APK firmado y su checksum SHA-256 están adjuntos a este release.
