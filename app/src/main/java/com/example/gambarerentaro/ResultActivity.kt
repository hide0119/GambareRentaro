package com.example.gambarerentaro

import android.os.Bundle
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val resultTextView = findViewById<TextView>(R.id.tv_result)
        val correctCountTextView = findViewById<TextView>(R.id.tv_correct_count)
        val incorrectCountTextView = findViewById<TextView>(R.id.tv_incorrect_count)

        // 結果を表示
        resultTextView.text = "あなたの結果は..."
        correctCountTextView.text = "正解数: $correctCount"
        incorrectCountTextView.text = "不正解数: $incorrectCount"

        // 再挑戦ボタンのクリックイベント
        val retryButton = findViewById<Button>(R.id.btn_retry)
        retryButton.setOnClickListener {
            // 再挑戦時の処理
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GambareRentaroTheme {
        Greeting("Android")
    }
}

