package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.gambarerentaro.database.Score
import com.example.gambarerentaro.database.ScoreDatabase
import kotlinx.coroutines.launch

class ResultActivity : ComponentActivity() {
    private lateinit var correctMediaPlayer: MediaPlayer
    private lateinit var wrongMediaPlayer: MediaPlayer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTION", 0)
        // CATEGORY1～CATEGORY5 を取得
        val category1 = intent.getStringExtra("CATEGORY1") ?: ""
        val category2 = intent.getStringExtra("CATEGORY2") ?: ""
        val category3 = intent.getStringExtra("CATEGORY3") ?: ""
        val category4 = intent.getStringExtra("CATEGORY4") ?: ""
        val category5 = intent.getStringExtra("CATEGORY5") ?: ""
        // categories を構築
        val categories = "$category1, $category2, $category3, $category4, $category5"

        val totalScoreTextView = findViewById<TextView>(R.id.totalScoreTextView)
        val totalQuestionsTextView = findViewById<TextView>(R.id.totalQuestionsTextView)
        val resultImageView = findViewById<ImageView>(R.id.resultImageView)

        correctMediaPlayer = MediaPlayer.create(this, R.raw.quiz_correct)
        wrongMediaPlayer = MediaPlayer.create(this, R.raw.quiz_wrong)

        totalScoreTextView.text = "合計スコア: $totalScore"
        totalQuestionsTextView.text = "問題数: $totalQuestions"

        if (totalScore == totalQuestions) {
            resultImageView.setImageResource(R.drawable.perfect_image)
            correctMediaPlayer.start()
        } else {
            resultImageView.setImageResource(R.drawable.not_perfect_image)
            wrongMediaPlayer.start()
        }

        // クイズ結果の取得
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", totalScore)

        // データベースに保存
        val scoreDao = ScoreDatabase.getDatabase(this).scoreDao()
        val score = Score(
            timestamp = System.currentTimeMillis(),
            categories = categories,
            score = correctAnswers,
            totalQuestions = totalQuestions
        )
        lifecycleScope.launch {
            scoreDao.insert(score)
        }

        val menuButton = findViewById<Button>(R.id.menuButton)
        menuButton.setOnClickListener {
            // MenuActivityを起動する処理
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        correctMediaPlayer.release()
        wrongMediaPlayer.release()
    }
}



