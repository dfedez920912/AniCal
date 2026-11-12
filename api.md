Aquí tienes el documento completo en formato Markdown, listo para que tu agente de IA lo use y modifique el backend de la aplicación.

```markdown
# Integración AniList GraphQL API - Guía para Backend Android App

## 1. Información General

**Endpoint único:**
```
https://graphql.anilist.co
```

**Método:** Siempre `POST`  
**Content-Type:** `application/json`  
**Autenticación:** No se requiere para datos públicos (temporadas, airing, detalles, reseñas, etc.)

**Formato del body:**
```json
{
  "query": "query string aquí",
  "variables": {
    // variables opcionales
  }
}
```

---

## 2. Rate Limits (Importante)

- Límite normal: **90 requests por minuto**
- En modo degradado (común): **30 requests por minuto**
- Headers de respuesta útiles:
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `Retry-After` (cuando recibes 429)

**Recomendaciones:**
- Implementar caché en el backend (mínimo 1-6 horas para listas de temporada).
- Usar `perPage` razonable (máx 50, ideal 20-25).
- No hacer requests innecesarios desde la app (centralizar en backend).

---

## 3. Campos que necesitamos (Imagen, Sinopsis, Score, Reseñas)

### Campos principales de Media

| Dato              | Campo GraphQL                          | Notas |
|-------------------|----------------------------------------|-------|
| ID                | `id`                                   | ID de AniList |
| Título            | `title { romaji english native }`      | Preferir `romaji` o `english` |
| Imagen            | `coverImage { large medium extraLarge color }` | Usar `large` o `extraLarge` |
| Banner            | `bannerImage`                          | Opcional |
| Sinopsis          | `description`                          | Puede venir en Markdown. Usar `description(asHtml: true)` si se prefiere HTML |
| Puntuación        | `averageScore`                         | 0-100 (recomendado) |
| Puntuación media  | `meanScore`                            | Alternativa |
| Episodios         | `episodes`                             | Total |
| Estado            | `status`                               | RELEASING, FINISHED, NOT_YET_RELEASED, etc. |
| Temporada         | `season` + `seasonYear`                | WINTER, SPRING, SUMMER, FALL |
| Próximo episodio  | `nextAiringEpisode { episode airingAt timeUntilAiring }` | Muy útil |
| Géneros           | `genres`                               | Array de strings |
| Reseñas           | `reviews(page: 1, perPage: 5, sort: RATING_DESC) { nodes { ... } }` | Limitar a 3-5 |

---

## 4. Queries Principales para la App

### 4.1 Lista de anime de una temporada (Estrenos de temporada)

```graphql
query ($season: MediaSeason, $seasonYear: Int, $page: Int = 1, $perPage: Int = 20) {
  Page(page: $page, perPage: $perPage) {
    pageInfo {
      total
      currentPage
      lastPage
      hasNextPage
      perPage
    }
    media(
      type: ANIME
      season: $season
      seasonYear: $seasonYear
      sort: POPULARITY_DESC
      isAdult: false
    ) {
      id
      title {
        romaji
        english
        native
      }
      coverImage {
        large
        medium
        extraLarge
        color
      }
      description
      averageScore
      meanScore
      episodes
      status
      season
      seasonYear
      genres
      nextAiringEpisode {
        episode
        airingAt
        timeUntilAiring
      }
    }
  }
}
```

**Variables de ejemplo:**
```json
{
  "season": "FALL",
  "seasonYear": 2026,
  "page": 1,
  "perPage": 20
}
```

**Valores válidos de `MediaSeason`:**
- `WINTER` (Enero-Marzo)
- `SPRING` (Abril-Junio)
- `SUMMER` (Julio-Septiembre)
- `FALL` (Octubre-Diciembre)

---

### 4.2 Episodios que se estrenan (Airing Schedule) - Filtrar por día/semana/mes

Esta es la query más importante para el calendario.

```graphql
query ($page: Int = 1, $perPage: Int = 50, $airingAt_greater: Int, $airingAt_lesser: Int) {
  Page(page: $page, perPage: $perPage) {
    pageInfo {
      total
      currentPage
      lastPage
      hasNextPage
    }
    airingSchedules(
      airingAt_greater: $airingAt_greater
      airingAt_lesser: $airingAt_lesser
      sort: TIME
    ) {
      id
      episode
      airingAt
      timeUntilAiring
      media {
        id
        title {
          romaji
          english
          native
        }
        coverImage {
          large
          medium
          color
        }
        averageScore
        description
        genres
        status
        episodes
        nextAiringEpisode {
          episode
          airingAt
        }
      }
    }
  }
}
```

**Cómo calcular los timestamps (Unix seconds):**

- **Hoy / Día específico:**  
  `airingAt_greater` = inicio del día (00:00)  
  `airingAt_lesser` = fin del día (23:59:59)

- **Esta semana:**  
  Desde lunes 00:00 hasta domingo 23:59:59

- **Este mes:**  
  Desde día 1 00:00 hasta último día del mes 23:59:59

**Ejemplo de variables (próximos 7 días):**
```json
{
  "page": 1,
  "perPage": 50,
  "airingAt_greater": 1731283200,
  "airingAt_lesser": 1731887999
}
```

> Nota: AniList solo garantiza precisión en episodios **futuros**.

---

### 4.3 Detalle de un anime + Reseñas

```graphql
query ($id: Int) {
  Media(id: $id, type: ANIME) {
    id
    idMal
    title {
      romaji
      english
      native
    }
    coverImage {
      large
      extraLarge
      color
    }
    bannerImage
    description
    averageScore
    meanScore
    episodes
    duration
    status
    season
    seasonYear
    genres
    synonyms
    startDate {
      year
      month
      day
    }
    endDate {
      year
      month
      day
    }
    nextAiringEpisode {
      episode
      airingAt
      timeUntilAiring
    }
    studios {
      nodes {
        name
        isAnimationStudio
      }
    }
    reviews(page: 1, perPage: 5, sort: RATING_DESC) {
      nodes {
        id
        summary
        body
        score
        rating
        ratingAmount
        user {
          name
          avatar {
            medium
          }
        }
        createdAt
      }
    }
  }
}
```

---

### 4.4 Búsqueda por nombre

```graphql
query ($search: String, $page: Int = 1) {
  Page(page: $page, perPage: 20) {
    pageInfo {
      hasNextPage
      total
    }
    media(search: $search, type: ANIME, isAdult: false, sort: SEARCH_MATCH) {
      id
      title {
        romaji
        english
        native
      }
      coverImage {
        large
        medium
      }
      averageScore
      description
      status
      episodes
      season
      seasonYear
    }
  }
}
```

---

## 5. Estructura recomendada del Backend

### Endpoints sugeridos para tu API interna

```
GET /api/season?season=FALL&year=2026&page=1
GET /api/schedule?from=1731283200&to=1731887999&page=1   // o por fecha legible
GET /api/anime/{id}
GET /api/search?q=frieren&page=1
GET /api/airing/today
GET /api/airing/week
GET /api/airing/month
```

### Buenas prácticas

1. **Caché agresivo** en el backend:
   - Lista de temporada → 6-12 horas
   - Schedule del día → 30-60 minutos
   - Detalle de anime → 24 horas
   - Reseñas → 12 horas

2. **Normalizar la respuesta** antes de enviarla a la app Android (no enviar el JSON crudo de AniList).

3. **Manejo de errores:**
   - 429 → Esperar `Retry-After` y reintentar
   - 404 → Anime no encontrado
   - Errores de GraphQL vienen en `errors[]`

4. **Filtrar contenido adulto** siempre con `isAdult: false` a menos que el usuario lo active explícitamente.

5. **Paginación:** Siempre usar `Page` + `pageInfo`.

---

## 6. Ejemplo de Request HTTP (cURL)

```bash
curl -X POST https://graphql.anilist.co \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "query": "query ($season: MediaSeason, $seasonYear: Int) { Page(page: 1, perPage: 10) { media(type: ANIME, season: $season, seasonYear: $seasonYear, sort: POPULARITY_DESC, isAdult: false) { id title { romaji english } coverImage { large } averageScore description } } }",
    "variables": {
      "season": "FALL",
      "seasonYear": 2026
    }
  }'
```

---

## 7. Notas importantes para el agente de IA

- Todas las fechas de airing están en **Unix timestamp (segundos)**.
- `description` puede contener Markdown. Considerar limpiarlo o convertir a HTML.
- `averageScore` es de 0 a 100 (no de 0 a 10).
- No es necesario OAuth para ninguna de las funcionalidades de esta app (solo lectura pública).
- Preferir `sort: POPULARITY_DESC` o `SCORE_DESC` en listas de temporada.
- Para el calendario, ordenar siempre por `TIME` (ascendente).
- El campo `nextAiringEpisode` es muy útil para mostrar "Próximo episodio en X tiempo".

---

## 8. Documentación oficial

- Docs principales: https://docs.anilist.co/
- Reference: https://docs.anilist.co/reference/
- Playground (Apollo Studio): https://studio.apollographql.com/sandbox/explorer?endpoint=https://graphql.anilist.co

---

**Fin del documento.**  
Usar este archivo como fuente de verdad para modificar el backend de la aplicación Android.
```

Puedes copiar todo el bloque anterior y guardarlo como `anilist-integration.md`. Tu agente de IA tendrá toda la información necesaria para adaptar el backend.