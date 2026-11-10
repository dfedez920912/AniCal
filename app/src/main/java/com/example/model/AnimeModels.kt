package com.example.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class SynopsisLanguage(val code: String, val displayName: String, val shortLabel: String) {
    SPANISH("es", "Español", "ES"),
    ENGLISH("en", "Inglés (Original)", "EN")
}

enum class EpisodeFilter(val label: String, val shortLabel: String) {
    ALL("Todos los episodios", "Todos"),
    ONGOING_EPISODES("Episodios del día", "Episodios hoy"),
    PREMIERES("Nuevos estrenos", "Estrenos")
}

data class AnimeRelease(
    val id: Int,
    val airingId: Int,
    val titleRomaji: String,
    val titleEnglish: String?,
    val coverImageUrl: String,
    val bannerImageUrl: String?,
    val synopsis: String,
    val synopsisEs: String = "",
    val synopsisEn: String = "",
    val activeLanguage: SynopsisLanguage = SynopsisLanguage.SPANISH,
    val isTranslating: Boolean = false,
    val episode: Int,
    val totalEpisodes: Int?,
    val airingAtSeconds: Long,
    val genres: List<String>,
    val score: Double?,
    val studio: String?,
    val format: String = "TV",
    val streamingPlatform: String = "Crunchyroll",
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val hasReminder: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val userRating: Int = 0
) {
    val displayTitle: String
        get() = if (!titleEnglish.isNullOrBlank()) titleEnglish else titleRomaji

    // Classification: New premiere vs ongoing episode of the day
    val isSeriesPremiere: Boolean
        get() = episode == 1 || format.equals("Película", ignoreCase = true) || format.equals("Movie", ignoreCase = true)

    val isOngoingEpisode: Boolean
        get() = !isSeriesPremiere

    val releaseTypeBadge: String
        get() = if (isSeriesPremiere) "NUEVO ESTRENO" else "EPISODIO DEL DÍA"

    fun getActiveSynopsis(): String {
        return when (activeLanguage) {
            SynopsisLanguage.SPANISH -> {
                if (synopsisEs.isNotBlank()) synopsisEs else synopsis.ifBlank { synopsisEn }
            }
            SynopsisLanguage.ENGLISH -> {
                if (synopsisEn.isNotBlank()) synopsisEn else synopsis.ifBlank { synopsisEs }
            }
        }
    }

    fun getAiringLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return Instant.ofEpochSecond(airingAtSeconds)
            .atZone(zoneId)
            .toLocalDate()
    }

    fun getFormattedTime(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val zonedDateTime = Instant.ofEpochSecond(airingAtSeconds).atZone(zoneId)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return zonedDateTime.format(formatter)
    }

    fun getCountdownStatus(currentTimeSeconds: Long = System.currentTimeMillis() / 1000): CountdownStatus {
        val diff = airingAtSeconds - currentTimeSeconds
        return when {
            diff < -7200 -> CountdownStatus.Aired("Emitido hoy")
            diff <= 0 -> CountdownStatus.AiringNow("¡Estrenando ahora!")
            diff < 3600 -> {
                val mins = (diff / 60).coerceAtLeast(1)
                CountdownStatus.Upcoming("En $mins min")
            }
            diff < 86400 -> {
                val hours = diff / 3600
                val mins = (diff % 3600) / 60
                CountdownStatus.Upcoming("En ${hours}h ${mins}m")
            }
            else -> {
                val days = diff / 86400
                CountdownStatus.Upcoming("En $days días")
            }
        }
    }
}

sealed class CountdownStatus(val text: String) {
    class Aired(text: String) : CountdownStatus(text)
    class AiringNow(text: String) : CountdownStatus(text)
    class Upcoming(text: String) : CountdownStatus(text)
}

data class DayScheduleTab(
    val date: LocalDate,
    val label: String,
    val dayOfMonth: Int,
    val monthName: String,
    val fullDayName: String,
    val isToday: Boolean,
    val isTomorrow: Boolean,
    val episodeCount: Int = 0,
    val premieresCount: Int = 0,
    val ongoingCount: Int = 0
)
