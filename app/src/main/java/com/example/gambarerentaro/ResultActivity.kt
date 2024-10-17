package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import android.widget.TextView

class ResultActivity : ComponentActivity() {
    private lateinit var correctMediaPlayer: MediaPlayer
    private lateinit var wrongMediaPlayer: MediaPlayer

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTION", 0)

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

        val menuButton = findViewById<Button>(R.id.menuButton)
        menuButton.setOnClickListener {
            // MenuActivityを起動する処理
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        val retryButton = findViewById<Button>(R.id.retryButton)
        retryButton.setOnClickListener {
            // MainActivityを起動する処理
            val intent = Intent(this, MainActivity::class.java)
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



