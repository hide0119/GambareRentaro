package com.example.gambarerentaro

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gambarerentaro.database.ScoreDatabase
import kotlinx.coroutines.launch

class ScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score) // activity_score.xml を設定

        val scoreDao = ScoreDatabase.getDatabase(this).scoreDao()
        lifecycleScope.launch {
            val scoreList = scoreDao.getLatestScores()
            val recyclerView = findViewById<RecyclerView>(R.id.score_recycler_view)
            recyclerView.adapter = ScoreAdapter(scoreList)
            recyclerView.layoutManager = LinearLayoutManager(this@ScoreActivity)
        }
    }
}