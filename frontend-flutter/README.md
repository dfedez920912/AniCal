# AniVexa - Frontend Flutter

Frontend móvil multiplataforma desarrollado en **Flutter** para **AniVexa**, con animaciones fluidas, tema oscuro Cyberpunk, y conexión directa al backend centralizado en Rust.

## Estructura del Proyecto

- `lib/main.dart`: Interfaz principal con pestañas animadas (`AnimatedSwitcher`), vista de calendario diario/semanal y lista de seguimiento.
- `lib/models/anime.dart`: Modelos de datos (`AnimeItem`, `AiringScheduleItem`).
- `lib/services/api_service.dart`: Cliente HTTP que consume los endpoints centralizados del backend en Rust (`/api/airing/today`, `/api/airing/week`, `/api/season`, `/api/anime/:id`, `/api/search`).

## Instrucciones para Ejecutar en Flutter

1. Asegúrate de tener Flutter SDK instalado en tu máquina (`flutter --version`).
2. Asegúrate de que el backend en Rust esté corriendo (`cargo run` en `backend-rust/`).
3. Instala las dependencias y ejecuta la app:
```bash
cd frontend-flutter
flutter pub get
flutter run
```
