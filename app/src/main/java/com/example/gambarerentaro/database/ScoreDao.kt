package com.example.gambarerentaro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScoreDao {
    @Insert
    suspend fun insert(score: Score)

    @Query("SELECT * FROM scores ORDER BY timestamp DESC LIMIT 10")
    suspend fun getLatestScores(): List<Score>
}