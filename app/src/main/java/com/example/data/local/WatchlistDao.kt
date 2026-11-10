package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist_anime ORDER BY addedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist_anime WHERE status = :status ORDER BY addedAt DESC")
    fun getWatchlistByStatus(status: String): Flow<List<WatchlistEntity>>

    @Query("SELECT animeId FROM watchlist_anime")
    fun getWatchlistIdsFlow(): Flow<List<Int>>

    @Query("SELECT animeId FROM watchlist_anime")
    suspend fun getWatchlistIdsList(): List<Int>

    @Query("SELECT * FROM watchlist_anime WHERE animeId = :animeId LIMIT 1")
    suspend fun getWatchlistItemById(animeId: Int): WatchlistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_anime WHERE animeId = :animeId)")
    fun isInWatchlistFlow(animeId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_anime WHERE animeId = :animeId)")
    suspend fun isInWatchlistDirect(animeId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Update
    suspend fun updateWatchlist(item: WatchlistEntity)

    @Query("UPDATE watchlist_anime SET status = :status WHERE animeId = :animeId")
    suspend fun updateStatus(animeId: Int, status: String)

    @Query("UPDATE watchlist_anime SET watchedEpisodes = :episodes WHERE animeId = :animeId")
    suspend fun updateWatchedEpisodes(animeId: Int, episodes: Int)

    @Query("DELETE FROM watchlist_anime WHERE animeId = :animeId")
    suspend fun deleteFromWatchlist(animeId: Int)

    @Query("DELETE FROM watchlist_anime")
    suspend fun clearWatchlist()
}
