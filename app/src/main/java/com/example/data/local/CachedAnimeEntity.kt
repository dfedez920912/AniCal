package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AnimeRelease
import com.example.model.SynopsisLanguage

@Entity(tableName = "cached_anime")
data class CachedAnimeEntity(
    @PrimaryKey
    val id: Int,
    val airingId: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val coverImageUrl: String,
    val bannerImageUrl: String?,
    val synopsis: String,
    val synopsisEs: String = "",
    val synopsisEn: String = "",
    val episode: Int,
    val totalEpisodes: Int?,
    val airingAtSeconds: Long,
    val genres: String,
    val score: Double?,
    val studio: String?,
    val format: String = "TV",
    val streamingPlatform: String = "Crunchyroll",
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toAnimeRelease(): AnimeRelease {
        return AnimeRelease(
            id = id,
            airingId = airingId,
            titleRomaji = titleRomaji,
            titleEnglish = titleEnglish,
            coverImageUrl = coverImageUrl,
            bannerImageUrl = bannerImageUrl,
            synopsis = synopsis,
            synopsisEs = synopsisEs,
            synopsisEn = synopsisEn,
            activeLanguage = if (synopsisEs.isNotBlank()) SynopsisLanguage.SPANISH else SynopsisLanguage.ENGLISH,
            episode = episode,
            totalEpisodes = totalEpisodes,
            airingAtSeconds = airingAtSeconds,
            genres = if (genres.isBlank()) emptyList() else genres.split(", ").filter { it.isNotBlank() },
            score = score,
            studio = studio,
            format = format,
            streamingPlatform = streamingPlatform
        )
    }

    companion object {
        fun fromAnimeRelease(anime: AnimeRelease): CachedAnimeEntity {
            return CachedAnimeEntity(
                id = anime.id,
                airingId = anime.airingId,
                titleRomaji = anime.titleRomaji,
                titleEnglish = anime.titleEnglish,
                coverImageUrl = anime.coverImageUrl,
                bannerImageUrl = anime.bannerImageUrl,
                synopsis = anime.synopsis,
                synopsisEs = anime.synopsisEs,
                synopsisEn = anime.synopsisEn,
                episode = anime.episode,
                totalEpisodes = anime.totalEpisodes,
                airingAtSeconds = anime.airingAtSeconds,
                genres = anime.genres.joinToString(", "),
                score = anime.score,
                studio = anime.studio,
                format = anime.format,
                streamingPlatform = anime.streamingPlatform,
                cachedAt = System.currentTimeMillis()
            )
        }
    }
}
