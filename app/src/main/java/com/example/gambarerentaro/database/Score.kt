package com.example.gambarerentaro.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "categories") val categories: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "total_questions") val totalQuestions: Int // totalQuestions カラムを追加
)