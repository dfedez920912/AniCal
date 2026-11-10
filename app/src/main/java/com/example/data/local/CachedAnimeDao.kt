package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedAnimeDao {

    @Query("SELECT * FROM cached_anime ORDER BY airingAtSeconds ASC")
    fun getAllCachedAnimeFlow(): Flow<List<CachedAnimeEntity>>

    @Query("SELECT * FROM cached_anime ORDER BY airingAtSeconds ASC")
    suspend fun getAllCachedAnimeList(): List<CachedAnimeEntity>

    @Query("SELECT * FROM cached_anime WHERE id = :id LIMIT 1")
    suspend fun getCachedAnimeById(id: Int): CachedAnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CachedAnimeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CachedAnimeEntity)

    @Query("DELETE FROM cached_anime")
    suspend fun clearAllCache()
}
