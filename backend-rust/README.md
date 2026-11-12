# AniVexa Centralized Rust Backend

Backend centralizado de alto rendimiento desarrollado en **Rust** (con Axum y Tokio) para **AniVexa**. Centraliza todas las consultas a la GraphQL API de AniList según las especificaciones de `api.md`.

## Características

- **Centralización Total**: Todas las consultas a AniList (`/api/season`, `/api/schedule`, `/api/airing/today`, `/api/airing/week`, `/api/airing/month`, `/api/anime/:id`, `/api/search`) se realizan desde este backend.
- **Caché en Memoria**:
  - Estrenos de temporada: 6 horas
  - Horarios y estrenos diarios/semanales: 30 minutos
  - Detalles de anime y reseñas: 24 horas
  - Búsqueda: 1 hora
- **Protección de Rate Limit**: Detección de headers de AniList (`Retry-After`, error 429) y control de concurrencia.
- **Filtro Seguro**: Excluye automáticamente contenido para adultos (`isAdult: false`).
- **Soporte CORS**: Listo para clientes Web, Flutter y Android.

## Ejecución

### Requisitos
- Rust 1.70+ con Cargo

### Iniciar en Desarrollo
```bash
cd backend-rust
cargo run
```
El servidor arrancará en `http://0.0.0.0:8080`.

### Endpoints Disponibles

- `GET /health` -> Estado del servicio
- `GET /api/season?season=FALL&year=2026&page=1` -> Estrenos de temporada
- `GET /api/schedule?from=1731283200&to=1731887999&page=1` -> Calendario de emisión
- `GET /api/airing/today` -> Episodios que se estrenan hoy
- `GET /api/airing/week` -> Episodios de esta semana
- `GET /api/airing/month` -> Episodios de este mes
- `GET /api/anime/:id` -> Detalle completo del anime + Reseñas
- `GET /api/search?q=frieren&page=1` -> Búsqueda por título
