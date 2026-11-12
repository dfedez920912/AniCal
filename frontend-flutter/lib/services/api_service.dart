import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/anime.dart';

class ApiService {
  static String baseUrl = 'http://10.0.2.2:8080'; // 10.0.2.2 points to localhost from Android emulator

  static Future<List<AiringScheduleItem>> getScheduleToday() async {
    final response = await http.get(Uri.parse('$baseUrl/api/airing/today'));
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final schedules = json['Page']?['airingSchedules'] as List<dynamic>? ?? [];
      return schedules.map((e) => AiringScheduleItem.fromJson(e)).toList();
    }
    throw Exception('Error al cargar la programación de hoy');
  }

  static Future<List<AiringScheduleItem>> getScheduleWeek() async {
    final response = await http.get(Uri.parse('$baseUrl/api/airing/week'));
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final schedules = json['Page']?['airingSchedules'] as List<dynamic>? ?? [];
      return schedules.map((e) => AiringScheduleItem.fromJson(e)).toList();
    }
    throw Exception('Error al cargar la programación semanal');
  }

  static Future<List<AnimeItem>> getSeason({String? season, int? year}) async {
    final query = <String, String>{};
    if (season != null) query['season'] = season;
    if (year != null) query['year'] = year.toString();

    final uri = Uri.parse('$baseUrl/api/season').replace(queryParameters: query);
    final response = await http.get(uri);
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final media = json['Page']?['media'] as List<dynamic>? ?? [];
      return media.map((e) => AnimeItem.fromJson(e)).toList();
    }
    throw Exception('Error al cargar la lista de temporada');
  }

  static Future<List<AnimeItem>> search(String query) async {
    final uri = Uri.parse('$baseUrl/api/search').replace(queryParameters: {'q': query});
    final response = await http.get(uri);
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final media = json['Page']?['media'] as List<dynamic>? ?? [];
      return media.map((e) => AnimeItem.fromJson(e)).toList();
    }
    throw Exception('Error al buscar anime');
  }

  static Future<AnimeItem> getAnimeDetail(int id) async {
    final response = await http.get(Uri.parse('$baseUrl/api/anime/$id'));
    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final media = json['Media'] as Map<String, dynamic>;
      return AnimeItem.fromJson(media);
    }
    throw Exception('Error al cargar el detalle del anime');
  }
}
