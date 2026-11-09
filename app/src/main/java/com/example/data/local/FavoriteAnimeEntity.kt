package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AnimeRelease

@Entity(tableName = "favorite_anime")
data class FavoriteAnimeEntity(
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
    val hasReminder: Boolean = true,
    val reminderMinutesBefore: Int = 15,
    val addedAt: Long = System.currentTimeMillis()
) {
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
            episode = 1,
            totalEpisodes = null,
            airingAtSeconds = 0,
            genres = if (genres.isBlank()) emptyList() else genres.split(", ").filter { it.isNotBlank() },
            score = score,
            studio = studio,
            format = format,
            isFavorite = true,
            hasReminder = hasReminder,
            reminderMinutesBefore = reminderMinutesBefore
        )
    }

    companion object {
        fun fromAnimeRelease(anime: AnimeRelease, hasReminder: Boolean = true, minutesBefore: Int = 15): FavoriteAnimeEntity {
            return FavoriteAnimeEntity(
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
                hasReminder = hasReminder,
                reminderMinutesBefore = minutesBefore,
                addedAt = System.currentTimeMillis()
            )
        }
    }
}
