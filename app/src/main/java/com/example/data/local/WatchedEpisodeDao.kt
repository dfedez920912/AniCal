package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedEpisodeDao {

    @Query("SELECT episodeKey FROM watched_episodes")
    fun getAllWatchedKeysFlow(): Flow<List<String>>

    @Query("SELECT episodeKey FROM watched_episodes")
    suspend fun getAllWatchedKeysList(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM watched_episodes WHERE episodeKey = :key)")
    suspend fun isWatched(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markWatched(entity: WatchedEpisodeEntity)

    @Query("DELETE FROM watched_episodes WHERE episodeKey = :key")
    suspend fun unmarkWatched(key: String)

    @Query("DELETE FROM watched_episodes WHERE animeId = :animeId")
    suspend fun clearForAnime(animeId: Int)
}
