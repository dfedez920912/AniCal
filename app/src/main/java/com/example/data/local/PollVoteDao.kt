package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PollVoteDao {
    @Query("SELECT * FROM poll_votes")
    fun getAllVotes(): Flow<List<PollVoteEntity>>

    @Query("SELECT * FROM poll_votes WHERE pollId = :pollId LIMIT 1")
    suspend fun getVoteForPoll(pollId: String): PollVoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveVote(vote: PollVoteEntity)

    @Query("DELETE FROM poll_votes WHERE pollId = :pollId")
    suspend fun deleteVote(pollId: String)
}
