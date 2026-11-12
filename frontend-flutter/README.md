# AniVexa - Frontend Flutter & In-App Core Backend

Aplicación móvil multiplataforma desarrollada en **Flutter** para **AniVexa**, con animaciones fluidas, tema oscuro Cyberpunk, y un **núcleo backend embebido** (`lib/backend/anivexa_backend_engine.dart`) que corre directamente dentro del proceso de la aplicación.

## Arquitectura del Núcleo Embebido

El backend no requiere de un servidor externo en otra máquina para funcionar en el móvil:
- **Motor Interno (`lib/backend/anivexa_backend_engine.dart`)**:
  - Centraliza todas las peticiones a la API GraphQL de AniList siguiendo `api.md`.
  - Sistema de **caché inteligente en memoria**:
    - Temporadas: 6 horas.
    - Horarios y estrenos diarios/semanales: 30 minutos.
    - Ficha de detalles y reseñas: 24 horas.
    - Búsqueda: 1 hora.
  - **Control de Cuota (Rate Limit)**: Respeta los límites de AniList (30-90 req/min) y gestiona respuestas 429 con `Retry-After`.
  - **Filtro Seguro**: Excluye automáticamente contenido adulto (`isAdult: false`).
  - **Servidor Loopback Opcional**: Inicia un demonio HTTP interno en `127.0.0.1` dentro del mismo teléfono.

## Estructura de Archivos

- `lib/backend/anivexa_backend_engine.dart`: Motor central de red, caché y normalización.
- `lib/services/api_service.dart`: Fachada de servicios consumida por la interfaz.
- `lib/models/anime.dart`: Modelos fuertemente tipados.
- `lib/main.dart`: Interfaz moderna con animaciones y pestañas.
- `android/`: Configuración nativa de Android con permisos de red y el icono oficial adaptativo de AniVexa.

## Compilación y Ejecución

```bash
cd frontend-flutter
flutter pub get
flutter run
```
O compila el APK de release directamente:
```bash
flutter build apk --release
```
