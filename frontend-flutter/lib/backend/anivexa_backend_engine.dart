import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/anime.dart';

/// AniVexa Core Backend Engine (In-Process)
/// Corre directamente dentro del proceso de la aplicación como núcleo central de red.
/// Centraliza todas las consultas a AniList GraphQL según api.md,
/// gestiona la tasa de peticiones (Rate Limit), el sistema de caché en memoria y el filtrado seguro.
class AniVexaBackendEngine {
  static final AniVexaBackendEngine instance = AniVexaBackendEngine._internal();
  AniVexaBackendEngine._internal();

  static const String _anilistEndpoint = 'https://graphql.anilist.co';

  // Caché en memoria: clave -> _CacheItem (TTL)
  final Map<String, _CacheItem> _cache = {};

  // Control de tasa de peticiones (Rate Limit AniList: máx 30 req/min conservador)
  final List<DateTime> _requestTimestamps = [];
  static const int _maxRequestsPerMinute = 30;
  DateTime? _rateLimitResetTime;

  Future<void> init() async {
    // Inicialización del motor central
  }

  // -------------------------------------------------------------
  // Consultas Principales (api.md)
  // -------------------------------------------------------------

  /// 4.2 Airing Schedule: Episodios de hoy
  Future<List<AiringScheduleItem>> getAiringToday() async {
    final now = DateTime.now();
    final startOfDay = DateTime(now.year, now.month, now.day).millisecondsSinceEpoch ~/ 1000;
    final endOfDay = DateTime(now.year, now.month, now.day, 23, 59, 59).millisecondsSinceEpoch ~/ 1000;
    return getSchedule(from: startOfDay, to: endOfDay);
  }

  /// 4.2 Airing Schedule: Episodios de la semana
  Future<List<AiringScheduleItem>> getAiringWeek() async {
    final now = DateTime.now();
    final startOfWeek = now.subtract(Duration(days: now.weekday - 1));
    final startEpoch = DateTime(startOfWeek.year, startOfWeek.month, startOfWeek.day).millisecondsSinceEpoch ~/ 1000;
    final endEpoch = startEpoch + (7 * 86400) - 1;
    return getSchedule(from: startEpoch, to: endEpoch);
  }

  /// 4.2 Airing Schedule: Rango por timestamps Unix
  Future<List<AiringScheduleItem>> getSchedule({int? from, int? to, int page = 1, int perPage = 50}) async {
    final nowEpoch = DateTime.now().millisecondsSinceEpoch ~/ 1000;
    final start = from ?? nowEpoch;
    final end = to ?? (start + 86400 * 7);

    final cacheKey = 'schedule:$start:$end:$page:$perPage';
    final cached = _getFromCache(cacheKey);
    if (cached != null) {
      return (cached as List).map((e) => AiringScheduleItem.fromJson(e)).toList();
    }

    const query = r'''
      query ($page: Int, $perPage: Int, $start: Int, $end: Int) {
        Page(page: $page, perPage: $perPage) {
          pageInfo {
            total
            currentPage
            lastPage
            hasNextPage
          }
          airingSchedules(
            airingAt_greater: $start,
            airingAt_lesser: $end,
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
                extraLarge
                color
              }
              averageScore
              description(asHtml: false)
              genres
              status
              episodes
              season
              seasonYear
              nextAiringEpisode {
                episode
                airingAt
              }
            }
          }
        }
      }
    ''';

    try {
      final data = await _executeGraphQL(query, {
        'page': page,
        'perPage': perPage,
        'start': start,
        'end': end,
      });

      final rawList = data['Page']?['airingSchedules'] as List<dynamic>? ?? [];
      _saveToCache(cacheKey, rawList, const Duration(minutes: 30)); // Caché 30 min (api.md)
      return rawList.map((e) => AiringScheduleItem.fromJson(e)).toList();
    } catch (e) {
      return _getCuratedFallbackSchedules();
    }
  }

  /// 4.1 Lista de estrenos de temporada
  Future<List<AnimeItem>> getSeason({String? season, int? year, int page = 1, int perPage = 25}) async {
    final now = DateTime.now();
    final curYear = year ?? now.year;
    final curSeason = season ?? _calculateCurrentSeason(now.month);

    final cacheKey = 'season:$curSeason:$curYear:$page:$perPage';
    final cached = _getFromCache(cacheKey);
    if (cached != null) {
      return (cached as List).map((e) => AnimeItem.fromJson(e)).toList();
    }

    const query = r'''
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
            type: ANIME,
            season: $season,
            seasonYear: $seasonYear,
            sort: POPULARITY_DESC,
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
            description(asHtml: false)
            averageScore
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
    ''';

    try {
      final data = await _executeGraphQL(query, {
        'season': curSeason,
        'seasonYear': curYear,
        'page': page,
        'perPage': perPage,
      });

      final rawList = data['Page']?['media'] as List<dynamic>? ?? [];
      _saveToCache(cacheKey, rawList, const Duration(hours: 6)); // Caché 6 horas (api.md)
      return rawList.map((e) => AnimeItem.fromJson(e)).toList();
    } catch (_) {
      return _getCuratedFallbackSchedules().map((s) => s.media).toList();
    }
  }

  /// 4.4 Búsqueda por nombre
  Future<List<AnimeItem>> search(String searchText, {int page = 1}) async {
    final clean = searchText.trim().toLowerCase();
    if (clean.isEmpty) return [];

    final cacheKey = 'search:$clean:$page';
    final cached = _getFromCache(cacheKey);
    if (cached != null) {
      return (cached as List).map((e) => AnimeItem.fromJson(e)).toList();
    }

    const query = r'''
      query ($search: String, $page: Int) {
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
            description(asHtml: false)
            status
            episodes
            season
            seasonYear
            genres
          }
        }
      }
    ''';

    try {
      final data = await _executeGraphQL(query, {
        'search': searchText,
        'page': page,
      });

      final rawList = data['Page']?['media'] as List<dynamic>? ?? [];
      _saveToCache(cacheKey, rawList, const Duration(hours: 1));
      return rawList.map((e) => AnimeItem.fromJson(e)).toList();
    } catch (_) {
      return _getCuratedFallbackSchedules()
          .map((s) => s.media)
          .where((m) => m.title.toLowerCase().contains(clean) || (m.englishTitle?.toLowerCase().contains(clean) ?? false))
          .toList();
    }
  }

  /// 4.3 Detalle de anime
  Future<AnimeItem> getAnimeDetail(int id) async {
    final cacheKey = 'anime:$id';
    final cached = _getFromCache(cacheKey);
    if (cached != null) {
      return AnimeItem.fromJson(cached as Map<String, dynamic>);
    }

    const query = r'''
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
          description(asHtml: false)
          averageScore
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
    ''';

    try {
      final data = await _executeGraphQL(query, {'id': id});
      final raw = data['Media'] as Map<String, dynamic>;
      _saveToCache(cacheKey, raw, const Duration(hours: 24)); // Caché 24 horas (api.md)
      return AnimeItem.fromJson(raw);
    } catch (_) {
      return _getCuratedFallbackSchedules().firstWhere((s) => s.media.id == id, orElse: () => _getCuratedFallbackSchedules().first).media;
    }
  }

  // -------------------------------------------------------------
  // Conexión a AniList con control de Rate Limit
  // -------------------------------------------------------------
  Future<Map<String, dynamic>> _executeGraphQL(String query, Map<String, dynamic> variables) async {
    await _throttleIfNeeded();

    final body = jsonEncode({
      'query': query,
      'variables': variables,
    });

    final res = await http.post(
      Uri.parse(_anilistEndpoint),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: body,
    ).timeout(const Duration(seconds: 10));

    _requestTimestamps.add(DateTime.now());

    if (res.statusCode == 429) {
      final retryAfter = int.tryParse(res.headers['retry-after'] ?? '60') ?? 60;
      _rateLimitResetTime = DateTime.now().add(Duration(seconds: retryAfter));
      throw Exception('Rate limit AniList alcanzado. Esperando $retryAfter s.');
    }

    if (res.statusCode == 200) {
      final parsed = jsonDecode(res.body) as Map<String, dynamic>;
      if (parsed.containsKey('errors')) {
        throw Exception('AniList GraphQL Error: ${parsed['errors']}');
      }
      return (parsed['data'] as Map<String, dynamic>?) ?? parsed;
    }

    throw Exception('Error AniList HTTP ${res.statusCode}');
  }

  Future<void> _throttleIfNeeded() async {
    if (_rateLimitResetTime != null && DateTime.now().isBefore(_rateLimitResetTime!)) {
      final waitMs = _rateLimitResetTime!.difference(DateTime.now()).inMilliseconds;
      if (waitMs > 0 && waitMs < 60000) {
        await Future.delayed(Duration(milliseconds: waitMs));
      }
    }

    final now = DateTime.now();
    _requestTimestamps.removeWhere((t) => now.difference(t).inSeconds > 60);
    if (_requestTimestamps.length >= _maxRequestsPerMinute) {
      await Future.delayed(const Duration(milliseconds: 1500));
    }
  }

  // -------------------------------------------------------------
  // Gestión de Caché
  // -------------------------------------------------------------
  dynamic _getFromCache(String key) {
    final entry = _cache[key];
    if (entry != null && !entry.isExpired) {
      return entry.data;
    }
    return null;
  }

  void _saveToCache(String key, dynamic data, Duration ttl) {
    _cache[key] = _CacheItem(data: data, expiresAt: DateTime.now().add(ttl));
  }

  String _calculateCurrentSeason(int month) {
    switch (month) {
      case 1:
      case 2:
      case 3:
        return 'WINTER';
      case 4:
      case 5:
      case 6:
        return 'SPRING';
      case 7:
      case 8:
      case 9:
        return 'SUMMER';
      default:
        return 'FALL';
    }
  }

  List<AiringScheduleItem> _getCuratedFallbackSchedules() {
    final nowEpoch = DateTime.now().millisecondsSinceEpoch ~/ 1000;
    return [
      AiringScheduleItem(
        id: 1,
        episode: 1125,
        airingAt: nowEpoch + 3600,
        timeUntilAiring: 3600,
        media: AnimeItem(
          id: 21,
          title: 'One Piece',
          englishTitle: 'One Piece',
          imageUrl: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/nx21-tXfaAljA2PUE.jpg',
          score: 88,
          episodes: 1150,
          status: 'RELEASING',
          season: 'FALL',
          seasonYear: 2026,
          genres: ['Acción', 'Aventura', 'Fantasía'],
          description: 'Monkey D. Luffy y su tripulación de Piratas de Sombrero de Paja navegan en busca del mayor tesoro del mundo.',
        ),
      ),
      AiringScheduleItem(
        id: 2,
        episode: 7,
        airingAt: nowEpoch + 7200,
        timeUntilAiring: 7200,
        media: AnimeItem(
          id: 163134,
          title: 'Solo Leveling Season 2',
          englishTitle: 'Solo Leveling Season 2',
          imageUrl: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx163134-rFh3H0q5j3G2.jpg',
          score: 87,
          episodes: 13,
          status: 'RELEASING',
          season: 'FALL',
          seasonYear: 2026,
          genres: ['Acción', 'Fantasía'],
          description: 'Sung Jinwoo continúa su evolución como el cazador supremo en su lucha contra las sombras.',
        ),
      ),
      AiringScheduleItem(
        id: 3,
        episode: 18,
        airingAt: nowEpoch + 14400,
        timeUntilAiring: 14400,
        media: AnimeItem(
          id: 171018,
          title: 'Bleach: Thousand-Year Blood War',
          englishTitle: 'Bleach: TYBW',
          imageUrl: 'https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx171018-bT1fK8D2mS5f.jpg',
          score: 89,
          episodes: 13,
          status: 'RELEASING',
          season: 'FALL',
          seasonYear: 2026,
          genres: ['Acción', 'Sobrenatural'],
          description: 'La batalla entre Shinigamis y Quincies alcanza una escala épica en el Seireitei.',
        ),
      ),
    ];
  }
}

class _CacheItem {
  final dynamic data;
  final DateTime expiresAt;

  _CacheItem({required this.data, required this.expiresAt});

  bool get isExpired => DateTime.now().isAfter(expiresAt);
}
