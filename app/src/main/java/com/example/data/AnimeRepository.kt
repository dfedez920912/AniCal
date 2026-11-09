package com.example.data

import android.content.Context
import android.util.Log
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
    private val watchedEpisodeDao: com.example.data.local.WatchedEpisodeDao = AppDatabase.getDatabase(context).watchedEpisodeDao()
) {

    private val translationService = TranslationService()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

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
     * Fetches airing schedule for the given date window [startDate..endDate].
     * If the remote call fails or is empty, falls back gracefully to the curated offline schedule.
     */
    suspend fun getSchedule(
        startDate: LocalDate = LocalDate.now(),
        daysCount: Int = 7
    ): Result<List<AnimeRelease>> = withContext(Dispatchers.IO) {
        val zoneId = ZoneId.systemDefault()
        val startEpoch = startDate.atStartOfDay(zoneId).toEpochSecond()
        val endEpoch = startDate.plusDays(daysCount.toLong()).atTime(23, 59, 59).atZone(zoneId).toEpochSecond()

        val favEntities = favoriteAnimeDao.getFavoriteIdsList().toSet()
        val watchedKeys = watchedEpisodeDao.getAllWatchedKeysList().toSet()

        try {
            val remoteReleases = fetchFromAniList(startEpoch, endEpoch, favEntities, watchedKeys)
            if (remoteReleases.isNotEmpty()) {
                val curated = OfflineAnimeData.getCuratedWeekSchedule(startDate)
                val combined = mergeReleases(remoteReleases, curated, favEntities, watchedKeys)
                return@withContext Result.success(combined)
            }
        } catch (e: Exception) {
            Log.w("AnimeRepository", "AniList API fetch failed, using curated offline schedule: ${e.message}")
        }

        // Curated fallback
        val curated = OfflineAnimeData.getCuratedWeekSchedule(startDate).map { release ->
            val key = "${release.id}_${release.episode}"
            release.copy(
                isFavorite = favEntities.contains(release.id),
                isWatched = watchedKeys.contains(key)
            )
        }
        Result.success(curated)
    }

    private fun mergeReleases(
        remote: List<AnimeRelease>,
        curated: List<AnimeRelease>,
        favIds: Set<Int>,
        watchedKeys: Set<String>
    ): List<AnimeRelease> {
        val resultMap = LinkedHashMap<Int, AnimeRelease>()

        for (item in remote) {
            val key = "${item.id}_${item.episode}"
            resultMap[item.id] = item.copy(
                isFavorite = favIds.contains(item.id),
                isWatched = watchedKeys.contains(key)
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
                    isWatched = watchedKeys.contains(key)
                )
            } else {
                resultMap[item.id] = item.copy(
                    isFavorite = favIds.contains(item.id),
                    isWatched = watchedKeys.contains(key)
                )
            }
        }

        return resultMap.values.sortedBy { it.airingAtSeconds }
    }

    private fun fetchFromAniList(
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
