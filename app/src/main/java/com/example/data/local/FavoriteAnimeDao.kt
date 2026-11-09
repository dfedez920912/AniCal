package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAnimeDao {

    @Query("SELECT * FROM favorite_anime ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteAnimeEntity>>

    @Query("SELECT animeId FROM favorite_anime")
    fun getFavoriteIdsFlow(): Flow<List<Int>>

    @Query("SELECT animeId FROM favorite_anime")
    suspend fun getFavoriteIdsList(): List<Int>

    @Query("SELECT * FROM favorite_anime WHERE animeId = :animeId LIMIT 1")
    suspend fun getFavoriteById(animeId: Int): FavoriteAnimeEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_anime WHERE animeId = :animeId)")
    fun isFavorite(animeId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_anime WHERE animeId = :animeId)")
    suspend fun isFavoriteDirect(animeId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteAnimeEntity)

    @Query("DELETE FROM favorite_anime WHERE animeId = :animeId")
    suspend fun deleteFavorite(animeId: Int)

    @Query("UPDATE favorite_anime SET hasReminder = :enabled WHERE animeId = :animeId")
    suspend fun updateReminderStatus(animeId: Int, enabled: Boolean)

    @Query("UPDATE favorite_anime SET reminderMinutesBefore = :minutes WHERE animeId = :animeId")
    suspend fun updateReminderMinutes(animeId: Int, minutes: Int)

    @Query("DELETE FROM favorite_anime")
    suspend fun clearAll()
}
