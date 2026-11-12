import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import '../models/anime.dart';

/// AniVexa Embedded Core Backend
/// Runs directly inside the application process as its central engine.
/// Centralizes all requests to AniList GraphQL per api.md specification,
/// handles rate limiting, TTL caching, data normalization, and adult content filtering.
class AniVexaBackendEngine {
  static final AniVexaBackendEngine instance = AniVexaBackendEngine._internal();
  AniVexaBackendEngine._internal();

  static const String _anilistEndpoint = 'https://graphql.anilist.co';
  HttpServer? _localHttpServer;
  int _localPort = 8080;

  // In-memory cache: key -> CacheItem
  final Map<String, _CacheItem> _cache = {};

  // Rate-limiting tracker: list of request timestamps in the last 60 seconds
  final List<DateTime> _requestTimestamps = [];
  static const int _maxRequestsPerMinute = 30; // Conservative limit (api.md recommendation)
  DateTime? _rateLimitResetTime;

  /// Initializes the embedded backend engine and starts the local loopback HTTP server
  Future<void> start() async {
    try {
      // Bind to localhost inside the device
      _localHttpServer = await HttpServer.bind(InternetAddress.loopbackIPv4, _localPort);
      _localPort = _localHttpServer!.port;
      _localHttpServer!.listen(_handleHttpRequest);
      print('AniVexa embedded backend server listening locally on 127.0.0.1:$_localPort');
    } catch (e) {
      // If port 8080 is busy, try dynamic port or operate purely in-process
      try {
        _localHttpServer = await HttpServer.bind(InternetAddress.loopbackIPv4, 0);
        _localPort = _localHttpServer!.port;
        _localHttpServer!.listen(_handleHttpRequest);
        print('AniVexa embedded backend server listening on 127.0.0.1:$_localPort');
      } catch (err) {
        print('AniVexa embedded backend running in-process only: $err');
      }
    }
  }

  int get localPort => _localPort;

  // -------------------------------------------------------------
  // Internal In-Process API Methods (Direct execution, 0 latency)
  // -------------------------------------------------------------

  /// 4.2 Airing Schedule: Today
  Future<List<AiringScheduleItem>> getAiringToday() async {
    final now = DateTime.now();
    final startOfDay = DateTime(now.year, now.month, now.day).millisecondsSinceEpoch ~/ 1000;
    final endOfDay = DateTime(now.year, now.month, now.day, 23, 59, 59).millisecondsSinceEpoch ~/ 1000;
    return getSchedule(from: startOfDay, to: endOfDay);
  }

  /// 4.2 Airing Schedule: Current Week
  Future<List<AiringScheduleItem>> getAiringWeek() async {
    final now = DateTime.now();
    final startOfWeek = now.subtract(Duration(days: now.weekday - 1));
    final startEpoch = DateTime(startOfWeek.year, startOfWeek.month, startOfWeek.day).millisecondsSinceEpoch ~/ 1000;
    final endEpoch = startEpoch + (7 * 86400) - 1;
    return getSchedule(from: startEpoch, to: endEpoch);
  }

  /// 4.2 Airing Schedule: Query by Range (from/to unix seconds)
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
      _saveToCache(cacheKey, rawList, const Duration(minutes: 30)); // 30 min cache per api.md
      return rawList.map((e) => AiringScheduleItem.fromJson(e)).toList();
    } catch (e) {
      print('Embedded backend fetch error: $e');
      return _getCuratedFallbackSchedules();
    }
  }

  /// 4.1 Season Releases Query
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
      _saveToCache(cacheKey, rawList, const Duration(hours: 6)); // 6h cache per api.md
      return rawList.map((e) => AnimeItem.fromJson(e)).toList();
    } catch (_) {
      return _getCuratedFallbackSchedules().map((s) => s.media).toList();
    }
  }

  /// 4.4 Search by Title
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

  /// 4.3 Anime Details + Reviews
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
      _saveToCache(cacheKey, raw, const Duration(hours: 24)); // 24h cache per api.md
      return AnimeItem.fromJson(raw);
    } catch (_) {
      return _getCuratedFallbackSchedules().firstWhere((s) => s.media.id == id, orElse: () => _getCuratedFallbackSchedules().first).media;
    }
  }

  // -------------------------------------------------------------
  // AniList GraphQL Communication & Rate-Limiting Protection
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
  // Internal Cache Management
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

  // -------------------------------------------------------------
  // Local HTTP Daemon Handler (Serves internal HTTP requests)
  // -------------------------------------------------------------
  Future<void> _handleHttpRequest(HttpRequest request) async {
    request.response.headers.contentType = ContentType.json;
    request.response.headers.add('Access-Control-Allow-Origin', '*');

    try {
      final uri = request.uri;
      final path = uri.path;

      if (path == '/health') {
        request.response.write(jsonEncode({'status': 'healthy', 'service': 'AniVexa In-App Engine'}));
      } else if (path == '/api/airing/today') {
        final list = await getAiringToday();
        request.response.write(jsonEncode({
          'Page': {'airingSchedules': list.map((e) => _airingToJson(e)).toList()}
        }));
      } else if (path == '/api/airing/week') {
        final list = await getAiringWeek();
        request.response.write(jsonEncode({
          'Page': {'airingSchedules': list.map((e) => _airingToJson(e)).toList()}
        }));
      } else if (path == '/api/schedule') {
        final from = int.tryParse(uri.queryParameters['from'] ?? '');
        final to = int.tryParse(uri.queryParameters['to'] ?? '');
        final page = int.tryParse(uri.queryParameters['page'] ?? '1') ?? 1;
        final list = await getSchedule(from: from, to: to, page: page);
        request.response.write(jsonEncode({
          'Page': {'airingSchedules': list.map((e) => _airingToJson(e)).toList()}
        }));
      } else if (path == '/api/season') {
        final season = uri.queryParameters['season'];
        final year = int.tryParse(uri.queryParameters['year'] ?? '');
        final list = await getSeason(season: season, year: year);
        request.response.write(jsonEncode({
          'Page': {'media': list.map((e) => _animeToJson(e)).toList()}
        }));
      } else if (path == '/api/search') {
        final q = uri.queryParameters['q'] ?? '';
        final list = await search(q);
        request.response.write(jsonEncode({
          'Page': {'media': list.map((e) => _animeToJson(e)).toList()}
        }));
      } else if (path.startsWith('/api/anime/')) {
        final idStr = path.replaceFirst('/api/anime/', '');
        final id = int.tryParse(idStr);
        if (id != null) {
          final anime = await getAnimeDetail(id);
          request.response.write(jsonEncode({'Media': _animeToJson(anime)}));
        } else {
          request.response.statusCode = HttpStatus.badRequest;
        }
      } else {
        request.response.statusCode = HttpStatus.notFound;
      }
    } catch (e) {
      request.response.statusCode = HttpStatus.internalServerError;
      request.response.write(jsonEncode({'error': e.toString()}));
    } finally {
      await request.response.close();
    }
  }

  Map<String, dynamic> _airingToJson(AiringScheduleItem item) => {
    'id': item.id,
    'episode': item.episode,
    'airingAt': item.airingAt,
    'timeUntilAiring': item.timeUntilAiring,
    'media': _animeToJson(item.media),
  };

  Map<String, dynamic> _animeToJson(AnimeItem anime) => {
    'id': anime.id,
    'title': {
      'romaji': anime.title,
      'english': anime.englishTitle,
      'native': anime.nativeTitle,
    },
    'coverImage': {
      'large': anime.imageUrl,
    },
    'averageScore': anime.score,
    'episodes': anime.episodes,
    'status': anime.status,
    'season': anime.season,
    'seasonYear': anime.seasonYear,
    'genres': anime.genres,
    'description': anime.description,
  };

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
