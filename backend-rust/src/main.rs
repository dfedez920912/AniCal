use axum::{
    extract::{Path, Query, State},
    http::{header, HeaderMap, StatusCode},
    response::{IntoResponse, Json},
    routing::get,
    Router,
};
use chrono::{Datelike, Duration, Local, NaiveTime, TimeZone, Utc};
use serde::{Deserialize, Serialize};
use serde_json::{json, Value};
use std::{
    collections::HashMap,
    net::SocketAddr,
    sync::{Arc, RwLock},
    time::{Duration as StdDuration, Instant},
};
use tower_http::cors::{Any, CorsLayer};

const ANILIST_GRAPHQL_URL: &str = "https://graphql.anilist.co";

#[derive(Clone)]
struct AppState {
    client: reqwest::Client,
    cache: Arc<RwLock<HashMap<String, CacheEntry>>>,
}

struct CacheEntry {
    data: Value,
    inserted_at: Instant,
    ttl: StdDuration,
}

#[derive(Deserialize)]
struct SeasonParams {
    season: Option<String>,
    year: Option<i32>,
    page: Option<i32>,
    per_page: Option<i32>,
}

#[derive(Deserialize)]
struct ScheduleParams {
    from: Option<i64>,
    to: Option<i64>,
    page: Option<i32>,
    per_page: Option<i32>,
}

#[derive(Deserialize)]
struct SearchParams {
    q: String,
    page: Option<i32>,
}

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt::init();

    let state = AppState {
        client: reqwest::Client::builder()
            .timeout(StdDuration::from_secs(15))
            .build()
            .unwrap(),
        cache: Arc::new(RwLock::new(HashMap::new())),
    };

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let app = Router::new()
        .route("/health", get(health_check))
        .route("/api/season", get(get_season))
        .route("/api/schedule", get(get_schedule))
        .route("/api/airing/today", get(get_airing_today))
        .route("/api/airing/week", get(get_airing_week))
        .route("/api/airing/month", get(get_airing_month))
        .route("/api/anime/:id", get(get_anime_detail))
        .route("/api/search", get(search_anime))
        .layer(cors)
        .with_state(state);

    let port = std::env::var("PORT")
        .unwrap_or_else(|_| "8080".to_string())
        .parse::<u16>()
        .unwrap_or(8080);

    let addr = SocketAddr::from(([0, 0, 0, 0], port));
    println!("AniVexa centralized backend listening on http://{}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

async fn health_check() -> impl IntoResponse {
    Json(json!({
        "status": "healthy",
        "service": "AniVexa Rust Backend",
        "version": "1.0.0"
    }))
}

// 4.1 Season releases query (api.md)
async fn get_season(
    State(state): State<AppState>,
    Query(params): Query<SeasonParams>,
) -> Result<Json<Value>, (StatusCode, String)> {
    let now = Utc::now();
    let year = params.year.unwrap_or(now.year());
    let season = params.season.unwrap_or_else(|| match now.month() {
        1..=3 => "WINTER".to_string(),
        4..=6 => "SPRING".to_string(),
        7..=9 => "SUMMER".to_string(),
        _ => "FALL".to_string(),
    });
    let page = params.page.unwrap_or(1);
    let per_page = params.per_page.unwrap_or(25).min(50);

    let cache_key = format!("season:{}:{}:{}:{}", season, year, page, per_page);
    if let Some(cached) = check_cache(&state, &cache_key) {
        return Ok(Json(cached));
    }

    let query = r#"
        query ($season: MediaSeason, $seasonYear: Int, $page: Int, $perPage: Int) {
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
    "#;

    let variables = json!({
        "season": season,
        "seasonYear": year,
        "page": page,
        "perPage": per_page
    });

    let data = fetch_anilist_graphql(&state.client, query, variables).await?;
    // Season cache TTL: 6 hours (api.md recommendation)
    save_cache(&state, cache_key, data.clone(), StdDuration::from_secs(6 * 3600));

    Ok(Json(data))
}

// 4.2 Airing Schedule query (api.md)
async fn get_schedule(
    State(state): State<AppState>,
    Query(params): Query<ScheduleParams>,
) -> Result<Json<Value>, (StatusCode, String)> {
    let from = params.from.unwrap_or_else(|| Utc::now().timestamp());
    let to = params.to.unwrap_or(from + 86400 * 7);
    let page = params.page.unwrap_or(1);
    let per_page = params.per_page.unwrap_or(50).min(50);

    let cache_key = format!("schedule:{}:{}:{}:{}", from, to, page, per_page);
    if let Some(cached) = check_cache(&state, &cache_key) {
        return Ok(Json(cached));
    }

    let query = r#"
        query ($page: Int, $perPage: Int, $airingAt_greater: Int, $airingAt_lesser: Int) {
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
    "#;

    let variables = json!({
        "page": page,
        "perPage": per_page,
        "airingAt_greater": from,
        "airingAt_lesser": to
    });

    let data = fetch_anilist_graphql(&state.client, query, variables).await?;
    // Schedule cache TTL: 30 minutes (api.md recommendation)
    save_cache(&state, cache_key, data.clone(), StdDuration::from_secs(1800));

    Ok(Json(data))
}

// Airing today shortcut
async fn get_airing_today(State(state): State<AppState>) -> Result<Json<Value>, (StatusCode, String)> {
    let now = Local::now();
    let start_of_day = now.date_naive().and_time(NaiveTime::MIN);
    let end_of_day = now.date_naive().and_time(NaiveTime::MAX);

    let from = Local.from_local_datetime(&start_of_day).unwrap().timestamp();
    let to = Local.from_local_datetime(&end_of_day).unwrap().timestamp();

    get_schedule(
        State(state),
        Query(ScheduleParams {
            from: Some(from),
            to: Some(to),
            page: Some(1),
            per_page: Some(50),
        }),
    )
    .await
}

// Airing week shortcut
async fn get_airing_week(State(state): State<AppState>) -> Result<Json<Value>, (StatusCode, String)> {
    let now = Local::now();
    let days_from_monday = now.weekday().num_days_from_monday();
    let monday = (now - Duration::days(days_from_monday as i64))
        .date_naive()
        .and_time(NaiveTime::MIN);
    let sunday = (now + Duration::days((6 - days_from_monday) as i64))
        .date_naive()
        .and_time(NaiveTime::MAX);

    let from = Local.from_local_datetime(&monday).unwrap().timestamp();
    let to = Local.from_local_datetime(&sunday).unwrap().timestamp();

    get_schedule(
        State(state),
        Query(ScheduleParams {
            from: Some(from),
            to: Some(to),
            page: Some(1),
            per_page: Some(50),
        }),
    )
    .await
}

// Airing month shortcut
async fn get_airing_month(State(state): State<AppState>) -> Result<Json<Value>, (StatusCode, String)> {
    let now = Local::now();
    let first_day = now.date_naive().with_day(1).unwrap().and_time(NaiveTime::MIN);
    let next_month = if now.month() == 12 { 1 } else { now.month() + 1 };
    let next_month_year = if now.month() == 12 { now.year() + 1 } else { now.year() };
    let first_day_next_month = chrono::NaiveDate::from_ymd_opt(next_month_year, next_month, 1)
        .unwrap()
        .and_time(NaiveTime::MIN);
    let last_day = first_day_next_month - Duration::seconds(1);

    let from = Local.from_local_datetime(&first_day).unwrap().timestamp();
    let to = Local.from_local_datetime(&last_day).unwrap().timestamp();

    get_schedule(
        State(state),
        Query(ScheduleParams {
            from: Some(from),
            to: Some(to),
            page: Some(1),
            per_page: Some(50),
        }),
    )
    .await
}

// 4.3 Anime detail + reviews query (api.md)
async fn get_anime_detail(
    State(state): State<AppState>,
    Path(id): Path<i32>,
) -> Result<Json<Value>, (StatusCode, String)> {
    let cache_key = format!("anime:{}", id);
    if let Some(cached) = check_cache(&state, &cache_key) {
        return Ok(Json(cached));
    }

    let query = r#"
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
    "#;

    let variables = json!({ "id": id });
    let data = fetch_anilist_graphql(&state.client, query, variables).await?;
    // Anime detail cache TTL: 24 hours (api.md recommendation)
    save_cache(&state, cache_key, data.clone(), StdDuration::from_secs(24 * 3600));

    Ok(Json(data))
}

// 4.4 Search query (api.md)
async fn search_anime(
    State(state): State<AppState>,
    Query(params): Query<SearchParams>,
) -> Result<Json<Value>, (StatusCode, String)> {
    let page = params.page.unwrap_or(1);
    let cache_key = format!("search:{}:{}", params.q.trim().to_lowercase(), page);
    if let Some(cached) = check_cache(&state, &cache_key) {
        return Ok(Json(cached));
    }

    let query = r#"
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
    "#;

    let variables = json!({
        "search": params.q,
        "page": page
    });

    let data = fetch_anilist_graphql(&state.client, query, variables).await?;
    // Search cache TTL: 1 hour
    save_cache(&state, cache_key, data.clone(), StdDuration::from_secs(3600));

    Ok(Json(data))
}

async fn fetch_anilist_graphql(
    client: &reqwest::Client,
    query: &str,
    variables: Value,
) -> Result<Value, (StatusCode, String)> {
    let body = json!({
        "query": query,
        "variables": variables
    });

    let response = client
        .post(ANILIST_GRAPHQL_URL)
        .header(header::CONTENT_TYPE, "application/json")
        .header(header::ACCEPT, "application/json")
        .json(&body)
        .send()
        .await
        .map_err(|e| (StatusCode::BAD_GATEWAY, format!("Request to AniList failed: {}", e)))?;

    let status = response.status();
    if status == StatusCode::TOO_MANY_REQUESTS {
        let retry_after = response
            .headers()
            .get("retry-after")
            .and_then(|h| h.to_str().ok())
            .unwrap_or("60");
        return Err((
            StatusCode::TOO_MANY_REQUESTS,
            format!("Rate limit exceeded. Retry after {} seconds.", retry_after),
        ));
    }

    let result: Value = response
        .json()
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, format!("Failed to parse AniList response: {}", e)))?;

    if let Some(errors) = result.get("errors") {
        return Err((
            StatusCode::BAD_REQUEST,
            format!("AniList GraphQL Error: {}", errors),
        ));
    }

    Ok(result.get("data").cloned().unwrap_or(result))
}

fn check_cache(state: &AppState, key: &str) -> Option<Value> {
    let cache = state.cache.read().ok()?;
    if let Some(entry) = cache.get(key) {
        if entry.inserted_at.elapsed() < entry.ttl {
            return Some(entry.data.clone());
        }
    }
    None
}

fn save_cache(state: &AppState, key: String, data: Value, ttl: StdDuration) {
    if let Ok(mut cache) = state.cache.write() {
        cache.insert(
            key,
            CacheEntry {
                data,
                inserted_at: Instant::now(),
                ttl,
            },
        );
    }
}
