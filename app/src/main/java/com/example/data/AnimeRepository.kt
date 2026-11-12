package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteAnimeDao
import com.example.data.local.FavoriteAnimeEntity
import com.example.model.AnimeRelease
import com.example.model.SynopsisLanguage
import com.example.notifications.AnimeNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class AnimeRepository(
    private val context: Context,
    private val favoriteAnimeDao: FavoriteAnimeDao = AppDatabase.getDatabase(context).favoriteAnimeDao(),
    private val watchedEpisodeDao: com.example.data.local.WatchedEpisodeDao = AppDatabase.getDatabase(context).watchedEpisodeDao(),
    private val watchlistDao: com.example.data.local.WatchlistDao = AppDatabase.getDatabase(context).watchlistDao(),
    private val cachedAnimeDao: com.example.data.local.CachedAnimeDao = AppDatabase.getDatabase(context).cachedAnimeDao(),
    private val userRatingDao: com.example.data.local.UserRatingDao = AppDatabase.getDatabase(context).userRatingDao()
) {

    private val translationService = TranslationService()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Reactive stream of user ratings (animeId -> rating 1..5).
     */
    val userRatingsFlow: Flow<Map<Int, Int>> = userRatingDao.getAllRatingsFlow()
        .map { list -> list.associate { it.animeId to it.rating } }

    suspend fun setUserRating(animeId: Int, rating: Int) = withContext(Dispatchers.IO) {
        if (rating <= 0) {
            userRatingDao.deleteRating(animeId)
        } else {
            userRatingDao.insertOrUpdateRating(
                com.example.data.local.UserRatingEntity(animeId = animeId, rating = rating.coerceIn(1, 5))
            )
        }
    }

    suspend fun getUserRating(animeId: Int): Int = withContext(Dispatchers.IO) {
        userRatingDao.getRatingDirect(animeId) ?: 0
    }

    /**
     * Reactive stream of favorite anime IDs persisted in Room.
     */
    val favoriteIdsFlow: Flow<Set<Int>> = favoriteAnimeDao.getFavoriteIdsFlow()
        .map { list -> list.toSet() }

    /**
     * Reactive stream of all tracked favorite anime entities in Room.
     */
    val allFavoritesFlow: Flow<List<FavoriteAnimeEntity>> = favoriteAnimeDao.getAllFavorites()

    /**
     * Reactive stream of watched episode keys ("animeId_episodeNumber").
     */
    val watchedEpisodeKeysFlow: Flow<Set<String>> = watchedEpisodeDao.getAllWatchedKeysFlow()
        .map { list -> list.toSet() }

    /**
     * Reactive stream of all Watchlist entries in Room.
     */
    val allWatchlistFlow: Flow<List<com.example.data.local.WatchlistEntity>> = watchlistDao.getAllWatchlist()

    /**
     * Reactive stream of anime IDs in the user's Watchlist.
     */
    val watchlistIdsFlow: Flow<Set<Int>> = watchlistDao.getWatchlistIdsFlow()
        .map { list -> list.toSet() }

    suspend fun getWatchlistIds(): Set<Int> = withContext(Dispatchers.IO) {
        watchlistDao.getWatchlistIdsList().toSet()
    }

    suspend fun toggleWatchlist(
        anime: AnimeRelease,
        status: com.example.data.local.WatchlistStatus = com.example.data.local.WatchlistStatus.PLAN_TO_WATCH
    ): Boolean = withContext(Dispatchers.IO) {
        val exists = watchlistDao.isInWatchlistDirect(anime.id)
        if (exists) {
            watchlistDao.deleteFromWatchlist(anime.id)
            false
        } else {
            watchlistDao.insertWatchlist(
                com.example.data.local.WatchlistEntity.fromAnimeRelease(
                    anime = anime,
                    status = status
                )
            )
            true
        }
    }

    suspend fun updateWatchlistStatus(animeId: Int, status: com.example.data.local.WatchlistStatus) = withContext(Dispatchers.IO) {
        watchlistDao.updateStatus(animeId, status.name)
    }

    suspend fun updateWatchlistEpisodes(animeId: Int, episodes: Int) = withContext(Dispatchers.IO) {
        watchlistDao.updateWatchedEpisodes(animeId, episodes)
    }

    suspend fun removeFromWatchlist(animeId: Int) = withContext(Dispatchers.IO) {
        watchlistDao.deleteFromWatchlist(animeId)
    }

    suspend fun isInWatchlist(animeId: Int): Boolean = withContext(Dispatchers.IO) {
        watchlistDao.isInWatchlistDirect(animeId)
    }

    suspend fun getWatchedEpisodeKeys(): Set<String> = withContext(Dispatchers.IO) {
        watchedEpisodeDao.getAllWatchedKeysList().toSet()
    }

    suspend fun toggleWatchedEpisode(animeId: Int, episodeNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val key = "${animeId}_${episodeNumber}"
        val isAlreadyWatched = watchedEpisodeDao.isWatched(key)
        if (isAlreadyWatched) {
            watchedEpisodeDao.unmarkWatched(key)
            false
        } else {
            watchedEpisodeDao.markWatched(
                com.example.data.local.WatchedEpisodeEntity(
                    episodeKey = key,
                    animeId = animeId,
                    episodeNumber = episodeNumber
                )
            )
            true
        }
    }

    suspend fun getFavoriteIds(): Set<Int> = withContext(Dispatchers.IO) {
        favoriteAnimeDao.getFavoriteIdsList().toSet()
    }

    suspend fun toggleFavorite(anime: AnimeRelease): Boolean = withContext(Dispatchers.IO) {
        val exists = favoriteAnimeDao.isFavoriteDirect(anime.id)
        val result = if (exists) {
            favoriteAnimeDao.deleteFavorite(anime.id)
            AnimeNotificationHelper.cancelAnimeReminder(context, anime.id)
            false
        } else {
            favoriteAnimeDao.insertFavorite(
                FavoriteAnimeEntity.fromAnimeRelease(
                    anime = anime,
                    hasReminder = true,
                    minutesBefore = anime.reminderMinutesBefore
                )
            )
            // Schedule reminder for upcoming episode
            AnimeNotificationHelper.scheduleAnimeReminder(context, anime, anime.reminderMinutesBefore)
            true
        }
        com.example.widget.AnimeCountdownWidget.sendUpdateBroadcast(context)
        result
    }

    suspend fun updateReminder(anime: AnimeRelease, enabled: Boolean, minutesBefore: Int) = withContext(Dispatchers.IO) {
        favoriteAnimeDao.updateReminderStatus(anime.id, enabled)
        favoriteAnimeDao.updateReminderMinutes(anime.id, minutesBefore)
        if (enabled) {
            AnimeNotificationHelper.scheduleAnimeReminder(context, anime, minutesBefore)
        } else {
            AnimeNotificationHelper.cancelAnimeReminder(context, anime.id)
        }
    }

    fun sendTestNotification(anime: AnimeRelease, minutesBefore: Int = 15) {
        AnimeNotificationHelper.showImmediateNotification(
            context = context,
            animeTitle = anime.displayTitle,
            episode = anime.episode,
            airingTime = anime.getFormattedTime(),
            minutesBefore = minutesBefore
        )
    }

    suspend fun isFavorite(animeId: Int): Boolean = withContext(Dispatchers.IO) {
        favoriteAnimeDao.isFavoriteDirect(animeId)
    }

    suspend fun translateSynopsis(text: String, targetLanguage: SynopsisLanguage): String {
        return translationService.translate(text, targetLanguage.code)
    }

    /**
     * Caches retrieved releases in Room database. If remote fetch fails (e.g. offline),
     * falls back gracefully to Room cached data and curated offline schedule.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSchedule(
        startDate: LocalDate = LocalDate.now(),
        daysCount: Int = 7
    ): Result<List<AnimeRelease>> = withContext(Dispatchers.IO) {
        val zoneId = ZoneId.systemDefault()
        val startEpoch = startDate.atStartOfDay(zoneId).toEpochSecond()
        val endEpoch = startDate.plusDays(daysCount.toLong()).atTime(23, 59, 59).atZone(zoneId).toEpochSecond()

        val favEntities = favoriteAnimeDao.getFavoriteIdsList().toSet()
        val watchedKeys = watchedEpisodeDao.getAllWatchedKeysList().toSet()
        val userRatingsMap = userRatingDao.getAllRatingsList().associate { it.animeId to it.rating }

        try {
            val remoteReleases = fetchFromBackend(startEpoch, endEpoch, favEntities, watchedKeys)
            if (remoteReleases.isNotEmpty()) {
                // Save to Room cache for offline access
                cachedAnimeDao.insertAll(remoteReleases.map { com.example.data.local.CachedAnimeEntity.fromAnimeRelease(it) })
                val curated = OfflineAnimeData.getCuratedWeekSchedule(startDate)
                // Cache curated items as well so full offline experience is preserved
                cachedAnimeDao.insertAll(curated.map { com.example.data.local.CachedAnimeEntity.fromAnimeRelease(it) })

                val combined = mergeReleases(remoteReleases, curated, favEntities, watchedKeys, userRatingsMap)
                return@withContext Result.success(combined)
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "Backend service fetch failed (offline or network error), attempting Room cache fallback: ${e.message}")
        }

        // Offline / Cache fallback
        val cachedEntities = cachedAnimeDao.getAllCachedAnimeList()
        val curated = OfflineAnimeData.getCuratedWeekSchedule(startDate)

        val cachedReleases = if (cachedEntities.isNotEmpty()) {
            cachedEntities.map { it.toAnimeRelease() }
        } else {
            emptyList()
        }

        val combinedOffline = mergeReleases(cachedReleases, curated, favEntities, watchedKeys, userRatingsMap)
        Result.success(combinedOffline)
    }

    private fun mergeReleases(
        remote: List<AnimeRelease>,
        curated: List<AnimeRelease>,
        favIds: Set<Int>,
        watchedKeys: Set<String>,
        userRatingsMap: Map<Int, Int> = emptyMap()
    ): List<AnimeRelease> {
        val resultMap = LinkedHashMap<Int, AnimeRelease>()

        for (item in remote) {
            val key = "${item.id}_${item.episode}"
            resultMap[item.id] = item.copy(
                isFavorite = favIds.contains(item.id),
                isWatched = watchedKeys.contains(key),
                userRating = userRatingsMap[item.id] ?: 0
            )
        }

        for (item in curated) {
            val key = "${item.id}_${item.episode}"
            val existing = resultMap[item.id]
            if (existing != null) {
                val synEs = if (item.synopsisEs.isNotBlank()) item.synopsisEs else existing.synopsisEs
                val synEn = if (existing.synopsisEn.isNotBlank()) existing.synopsisEn else item.synopsisEn
                resultMap[item.id] = existing.copy(
                    synopsis = synEs.ifBlank { existing.synopsis },
                    synopsisEs = synEs,
                    synopsisEn = synEn,
                    studio = existing.studio ?: item.studio,
                    bannerImageUrl = existing.bannerImageUrl ?: item.bannerImageUrl,
                    streamingPlatform = item.streamingPlatform,
                    isFavorite = favIds.contains(item.id),
                    isWatched = watchedKeys.contains(key),
                    userRating = userRatingsMap[item.id] ?: existing.userRating
                )
            } else {
                resultMap[item.id] = item.copy(
                    isFavorite = favIds.contains(item.id),
                    isWatched = watchedKeys.contains(key),
                    userRating = userRatingsMap[item.id] ?: 0
                )
            }
        }

        return resultMap.values.sortedBy { it.airingAtSeconds }
    }

    /**
     * Centralized query to AniVexa Rust Backend (/api/schedule) per api.md specification.
     * Centralizes rate limiting, caching, and GraphQL complexity in the Rust service.
     */
    private fun fetchFromBackend(
        startEpoch: Long,
        endEpoch: Long,
        favIds: Set<Int>,
        watchedKeys: Set<String>
    ): List<AnimeRelease> {
        // Primary: Centralized Rust Backend on localhost or emulator host 10.0.2.2:8080
        val backendUrls = listOf(
            "http://10.0.2.2:8080/api/schedule?from=$startEpoch&to=$endEpoch&page=1&per_page=50",
            "http://localhost:8080/api/schedule?from=$startEpoch&to=$endEpoch&page=1&per_page=50"
        )

        for (url in backendUrls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: continue
                    val json = JSONObject(bodyString)
                    val schedulesArray = json.optJSONObject("Page")?.optJSONArray("airingSchedules")
                        ?: json.optJSONObject("data")?.optJSONObject("Page")?.optJSONArray("airingSchedules")
                    if (schedulesArray != null) {
                        return parseSchedulesArray(schedulesArray, favIds, watchedKeys)
                    }
                }
            } catch (e: Exception) {
                Log.d("AnimeRepository", "Backend endpoint $url not reachable, trying next: ${e.message}")
            }
        }

        // Secondary fallback to AniList proxy if backend server is not active
        return fetchFromAniListDirect(startEpoch, endEpoch, favIds, watchedKeys)
    }

    private fun fetchFromAniListDirect(
        startEpoch: Long,
        endEpoch: Long,
        favIds: Set<Int>,
        watchedKeys: Set<String>
    ): List<AnimeRelease> {
        val query = """
            query (${'$'}start: Int, ${'$'}end: Int) {
              Page(page: 1, perPage: 50) {
                airingSchedules(airingAt_greater: ${'$'}start, airingAt_lesser: ${'$'}end, sort: TIME) {
                  id
                  airingAt
                  episode
                  media {
                    id
                    title {
                      romaji
                      english
                      native
                    }
                    coverImage {
                      extraLarge
                      large
                    }
                    bannerImage
                    description(asHtml: false)
                    genres
                    averageScore
                    episodes
                    format
                    studios(isMain: true) {
                      nodes {
                        name
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("query", query)
            put("variables", JSONObject().apply {
                put("start", startEpoch)
                put("end", endEpoch)
            })
        }

        val request = Request.Builder()
            .url("https://graphql.anilist.co")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }

        val bodyString = response.body?.string() ?: throw Exception("Empty response body")
        val json = JSONObject(bodyString)
        val dataObj = json.optJSONObject("data") ?: return emptyList()
        val pageObj = dataObj.optJSONObject("Page") ?: return emptyList()
        val schedulesArray = pageObj.optJSONArray("airingSchedules") ?: return emptyList()

        return parseSchedulesArray(schedulesArray, favIds, watchedKeys)
    }

    private fun parseSchedulesArray(
        schedulesArray: org.json.JSONArray,
        favIds: Set<Int>,
        watchedKeys: Set<String>
    ): List<AnimeRelease> {

        val list = mutableListOf<AnimeRelease>()
        for (i in 0 until schedulesArray.length()) {
            val item = schedulesArray.optJSONObject(i) ?: continue
            val airingId = item.optInt("id", i)
            val airingAt = item.optLong("airingAt", 0)
            val episode = item.optInt("episode", 1)

            val media = item.optJSONObject("media") ?: continue
            val mediaId = media.optInt("id", 0)
            if (mediaId == 0) continue

            val titleObj = media.optJSONObject("title")
            val romaji = titleObj?.optString("romaji")?.takeIf { it.isNotBlank() } ?: "Anime #$mediaId"
            val english = titleObj?.optString("english")?.takeIf { it.isNotBlank() }

            val coverObj = media.optJSONObject("coverImage")
            val coverUrl = coverObj?.optString("extraLarge")?.takeIf { it.isNotBlank() }
                ?: coverObj?.optString("large")
                ?: "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/default.jpg"

            val bannerUrl = media.optString("bannerImage").takeIf { it.isNotBlank() }

            var rawDescription = media.optString("description", "")
            rawDescription = cleanDescription(rawDescription)

            val genresArray = media.optJSONArray("genres")
            val genres = mutableListOf<String>()
            if (genresArray != null) {
                for (g in 0 until genresArray.length()) {
                    genres.add(genresArray.optString(g))
                }
            }

            val avgScoreInt = media.optInt("averageScore", -1)
            val score = if (avgScoreInt > 0) (avgScoreInt / 10.0) else null

            val episodesInt = media.optInt("episodes", -1)
            val totalEpisodes = if (episodesInt > 0) episodesInt else null

            val format = media.optString("format", "TV")

            var studioName: String? = null
            val studiosObj = media.optJSONObject("studios")
            val studiosNodes = studiosObj?.optJSONArray("nodes")
            if (studiosNodes != null && studiosNodes.length() > 0) {
                studioName = studiosNodes.optJSONObject(0)?.optString("name")
            }

            val episodeKey = "${mediaId}_${episode}"
            val isWatched = watchedKeys.contains(episodeKey)

            list.add(
                AnimeRelease(
                    id = mediaId,
                    airingId = airingId,
                    titleRomaji = romaji,
                    titleEnglish = english,
                    coverImageUrl = coverUrl,
                    bannerImageUrl = bannerUrl,
                    synopsis = rawDescription.ifBlank { "Sinopsis no disponible." },
                    synopsisEs = "",
                    synopsisEn = rawDescription,
                    activeLanguage = SynopsisLanguage.SPANISH,
                    episode = episode,
                    totalEpisodes = totalEpisodes,
                    airingAtSeconds = airingAt,
                    genres = genres,
                    score = score,
                    studio = studioName,
                    format = format,
                    streamingPlatform = "Crunchyroll",
                    isFavorite = favIds.contains(mediaId),
                    isWatched = isWatched,
                    hasReminder = favIds.contains(mediaId)
                )
            )
        }

        return list
    }

    private fun cleanDescription(raw: String): String {
        return raw.replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)<p.*?>"), "")
            .replace(Regex("(?i)</p>"), "\n\n")
            .replace(Regex("<.*?>"), "")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }
}
