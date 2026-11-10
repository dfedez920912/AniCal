package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_ratings")
data class UserRatingEntity(
    @PrimaryKey
    val animeId: Int,
    val rating: Int, // 1 to 5 stars
    val ratedAt: Long = System.currentTimeMillis()
)
