package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AnimeRelease

enum class WatchlistStatus(val displayName: String, val shortLabel: String) {
    PLAN_TO_WATCH("Por Ver", "Plan"),
    WATCHING("Viendo", "Viendo"),
    COMPLETED("Completado", "Visto"),
    DROPPED("En Pausa", "Pausa")
}

@Entity(tableName = "watchlist_anime")
data class WatchlistEntity(
    @PrimaryKey
    val animeId: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val coverImageUrl: String,
    val bannerImageUrl: String?,
    val synopsis: String,
    val genres: String,
    val studio: String?,
    val score: Double?,
    val format: String,
    val totalEpisodes: Int?,
    val watchedEpisodes: Int = 0,
    val status: String = WatchlistStatus.PLAN_TO_WATCH.name,
    val streamingPlatform: String = "Crunchyroll",
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (!titleEnglish.isNullOrBlank()) titleEnglish else titleRomaji

    val statusEnum: WatchlistStatus
        get() = try {
            WatchlistStatus.valueOf(status)
        } catch (e: Exception) {
            WatchlistStatus.PLAN_TO_WATCH
        }

    fun toAnimeRelease(): AnimeRelease {
        return AnimeRelease(
            id = animeId,
            airingId = animeId,
            titleRomaji = titleRomaji,
            titleEnglish = titleEnglish,
            coverImageUrl = coverImageUrl,
            bannerImageUrl = bannerImageUrl,
            synopsis = synopsis,
            synopsisEs = synopsis,
            synopsisEn = "",
            episode = watchedEpisodes.coerceAtLeast(1),
            totalEpisodes = totalEpisodes,
            airingAtSeconds = 0,
            genres = if (genres.isBlank()) emptyList() else genres.split(", ").filter { it.isNotBlank() },
            score = score,
            studio = studio,
            format = format,
            streamingPlatform = streamingPlatform,
            isFavorite = true
        )
    }

    companion object {
        fun fromAnimeRelease(
            anime: AnimeRelease,
            status: WatchlistStatus = WatchlistStatus.PLAN_TO_WATCH,
            watchedEpisodes: Int = 0,
            notes: String = ""
        ): WatchlistEntity {
            return WatchlistEntity(
                animeId = anime.id,
                titleRomaji = anime.titleRomaji,
                titleEnglish = anime.titleEnglish,
                coverImageUrl = anime.coverImageUrl,
                bannerImageUrl = anime.bannerImageUrl,
                synopsis = anime.getActiveSynopsis(),
                genres = anime.genres.joinToString(", "),
                studio = anime.studio,
                score = anime.score,
                format = anime.format,
                totalEpisodes = anime.totalEpisodes,
                watchedEpisodes = watchedEpisodes,
                status = status.name,
                streamingPlatform = anime.streamingPlatform,
                notes = notes,
                addedAt = System.currentTimeMillis()
            )
        }
    }
}
