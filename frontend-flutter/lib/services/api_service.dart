import '../backend/anivexa_backend_engine.dart';
import '../models/anime.dart';

/// ApiService delegates directly to the in-app AniVexaBackendEngine.
/// The backend runs inside the same process as the core engine of the app,
/// managing all caching, rate-limiting, and network calls to AniList centrally.
class ApiService {
  static final AniVexaBackendEngine _engine = AniVexaBackendEngine.instance;

  static Future<void> initialize() async {
    await _engine.init();
  }

  static Future<List<AiringScheduleItem>> getScheduleToday() async {
    return _engine.getAiringToday();
  }

  static Future<List<AiringScheduleItem>> getScheduleWeek() async {
    return _engine.getAiringWeek();
  }

  static Future<List<AnimeItem>> getSeason({String? season, int? year}) async {
    return _engine.getSeason(season: season, year: year);
  }

  static Future<List<AnimeItem>> search(String query) async {
    return _engine.search(query);
  }

  static Future<AnimeItem> getAnimeDetail(int id) async {
    return _engine.getAnimeDetail(id);
  }
}
