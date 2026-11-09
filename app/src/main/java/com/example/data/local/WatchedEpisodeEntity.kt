package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_episodes")
data class WatchedEpisodeEntity(
    @PrimaryKey
    val episodeKey: String, // e.g. "animeId_episodeNumber"
    val animeId: Int,
    val episodeNumber: Int,
    val watchedAt: Long = System.currentTimeMillis()
)
