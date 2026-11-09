package com.example.model

enum class PollType(val title: String, val description: String) {
    BEST_EPISODE("Mejor Episodio de la Semana", "Vota por el episodio con mejor animación, trama e impacto de esta semana"),
    MOST_ANTICIPATED("Serie más Esperada de la Temporada", "Vota por el anime que más ansías ver o continuar esta temporada")
}

data class PollOption(
    val id: String,
    val animeId: Int,
    val title: String,
    val subtitle: String,
    val coverImageUrl: String,
    val votes: Int = 0
)

data class WeeklyPoll(
    val id: String,
    val type: PollType,
    val title: String,
    val weekLabel: String,
    val description: String,
    val options: List<PollOption>,
    val totalVotes: Int = 0,
    val userVotedOptionId: String? = null
)
