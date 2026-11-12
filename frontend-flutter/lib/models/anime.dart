class AnimeItem {
  final int id;
  final String title;
  final String? englishTitle;
  final String? nativeTitle;
  final String? imageUrl;
  final String? bannerUrl;
  final String? description;
  final int? score;
  final int? episodes;
  final String? status;
  final String? season;
  final int? seasonYear;
  final List<String> genres;
  final int? nextEpisode;
  final int? nextAiringAt;

  AnimeItem({
    required this.id,
    required this.title,
    this.englishTitle,
    this.nativeTitle,
    this.imageUrl,
    this.bannerUrl,
    this.description,
    this.score,
    this.episodes,
    this.status,
    this.season,
    this.seasonYear,
    this.genres = const [],
    this.nextEpisode,
    this.nextAiringAt,
  });

  factory AnimeItem.fromJson(Map<String, dynamic> json) {
    final titleObj = json['title'] as Map<String, dynamic>?;
    final coverObj = json['coverImage'] as Map<String, dynamic>?;
    final nextEpObj = json['nextAiringEpisode'] as Map<String, dynamic>?;

    return AnimeItem(
      id: json['id'] as int,
      title: titleObj?['romaji'] ?? titleObj?['english'] ?? titleObj?['native'] ?? 'Sin título',
      englishTitle: titleObj?['english'] as String?,
      nativeTitle: titleObj?['native'] as String?,
      imageUrl: coverObj?['large'] ?? coverObj?['extraLarge'] ?? coverObj?['medium'] as String?,
      bannerUrl: json['bannerImage'] as String?,
      description: json['description'] as String?,
      score: json['averageScore'] as int?,
      episodes: json['episodes'] as int?,
      status: json['status'] as String?,
      season: json['season'] as String?,
      seasonYear: json['seasonYear'] as int?,
      genres: (json['genres'] as List<dynamic>?)?.map((e) => e.toString()).toList() ?? [],
      nextEpisode: nextEpObj?['episode'] as int?,
      nextAiringAt: nextEpObj?['airingAt'] as int?,
    );
  }
}

class AiringScheduleItem {
  final int id;
  final int episode;
  final int airingAt;
  final int timeUntilAiring;
  final AnimeItem media;

  AiringScheduleItem({
    required this.id,
    required this.episode,
    required this.airingAt,
    required this.timeUntilAiring,
    required this.media,
  });

  factory AiringScheduleItem.fromJson(Map<String, dynamic> json) {
    return AiringScheduleItem(
      id: json['id'] as int,
      episode: json['episode'] as int,
      airingAt: json['airingAt'] as int,
      timeUntilAiring: json['timeUntilAiring'] as int? ?? 0,
      media: AnimeItem.fromJson(json['media'] as Map<String, dynamic>),
    );
  }
}
