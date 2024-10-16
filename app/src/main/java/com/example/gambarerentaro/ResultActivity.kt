package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gambarerentaro.ui.theme.GambareRentaroTheme
import android.widget.TextView

class ResultActivity : ComponentActivity() {
    private var correctCount = 0 // クラス変数として宣言
    private var incorrectCount = 0 // クラス変数として宣言

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTION", 0)

        val totalScoreTextView = findViewById<TextView>(R.id.totalScoreTextView)
        val totalQuestionsTextView = findViewById<TextView>(R.id.totalQuestionsTextView)
        val resultImageView = findViewById<ImageView>(R.id.resultImageView)

        totalScoreTextView.text = "合計スコア: $totalScore"
        totalQuestionsTextView.text = "問題数: $totalQuestions"

        if (totalScore == totalQuestions) {
            resultImageView.setImageResource(R.drawable.perfect_image)
        } else {
            resultImageView.setImageResource(R.drawable.not_perfect_image)
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
}



