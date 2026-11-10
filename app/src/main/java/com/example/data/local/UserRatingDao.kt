package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRatingDao {

    @Query("SELECT * FROM user_ratings")
    fun getAllRatingsFlow(): Flow<List<UserRatingEntity>>

    @Query("SELECT * FROM user_ratings")
    suspend fun getAllRatingsList(): List<UserRatingEntity>

    @Query("SELECT rating FROM user_ratings WHERE animeId = :animeId LIMIT 1")
    fun getRatingFlow(animeId: Int): Flow<Int?>

    @Query("SELECT rating FROM user_ratings WHERE animeId = :animeId LIMIT 1")
    suspend fun getRatingDirect(animeId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRating(ratingEntity: UserRatingEntity)

    @Query("DELETE FROM user_ratings WHERE animeId = :animeId")
    suspend fun deleteRating(animeId: Int)
}
