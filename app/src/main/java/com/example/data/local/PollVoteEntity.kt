package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poll_votes")
data class PollVoteEntity(
    @PrimaryKey
    val pollId: String,
    val optionId: String,
    val votedAt: Long = System.currentTimeMillis()
)
