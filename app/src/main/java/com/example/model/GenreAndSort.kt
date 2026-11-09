package com.example.model

enum class AnimeSortOrder(val label: String, val shortLabel: String) {
    AIRING_TIME("Hora de emisión", "Hora"),
    SCORE("Mayor puntuación", "⭐ Nota"),
    TITLE_AZ("Título (A-Z)", "A-Z"),
    EPISODE("Número de episodio", "Episodio")
}

object GenreHelper {
    // Map normalized canonical genre name to equivalent representations
    val GENRE_SYNONYMS = mapOf(
        "Acción" to listOf("Action", "Acción"),
        "Action" to listOf("Action", "Acción"),
        "Drama" to listOf("Drama"),
        "Comedia" to listOf("Comedy", "Comedia"),
        "Comedy" to listOf("Comedy", "Comedia"),
        "Fantasía" to listOf("Fantasy", "Fantasía"),
        "Fantasy" to listOf("Fantasy", "Fantasía"),
        "Aventura" to listOf("Adventure", "Aventura"),
        "Adventure" to listOf("Adventure", "Aventura"),
        "Sobrenatural" to listOf("Supernatural", "Sobrenatural"),
        "Supernatural" to listOf("Supernatural", "Sobrenatural"),
        "Sci-Fi" to listOf("Sci-Fi", "Ciencia Ficción"),
        "Misterio" to listOf("Mystery", "Misterio"),
        "Mystery" to listOf("Mystery", "Misterio"),
        "Deportes" to listOf("Sports", "Deportes"),
        "Sports" to listOf("Sports", "Deportes"),
        "Romance" to listOf("Romance"),
        "Psicológico" to listOf("Psychological", "Psicológico"),
        "Shounen" to listOf("Shounen", "Shonen")
    )

    val FEATURED_GENRES = listOf(
        "Acción",
        "Drama",
        "Comedia",
        "Fantasía",
        "Aventura",
        "Sobrenatural",
        "Sci-Fi",
        "Misterio",
        "Deportes",
        "Romance"
    )

    fun matchesAnyGenre(animeGenres: List<String>, filterGenres: Set<String>): Boolean {
        if (filterGenres.isEmpty()) return true
        return filterGenres.any { targetGenre ->
            val synonyms = GENRE_SYNONYMS[targetGenre] ?: listOf(targetGenre)
            animeGenres.any { g ->
                synonyms.any { syn -> syn.equals(g.trim(), ignoreCase = true) }
            }
        }
    }

    fun normalizeGenre(genre: String): String {
        return when (genre.trim().lowercase()) {
            "action" -> "Acción"
            "comedy" -> "Comedia"
            "fantasy" -> "Fantasía"
            "adventure" -> "Aventura"
            "supernatural" -> "Sobrenatural"
            "mystery" -> "Misterio"
            "sports" -> "Deportes"
            "psychological" -> "Psicológico"
            else -> genre.trim().replaceFirstChar { it.uppercase() }
        }
    }
}
