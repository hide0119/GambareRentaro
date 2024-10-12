package com.example.gambarerentaro

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var rgChoices: RadioGroup
    private lateinit var btnAnswer: Button
    private lateinit var tvScore: TextView

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private val questions = listOf(
        Question("日本の首都はどこですか？", "東京", listOf("大阪", "京都", "東京", "流山")),
        Question("世界で一番高い山はどこですか？", "エベレスト", listOf("富士山", "エベレスト", "マッターホルン", "筑波山")),
        Question("九州に含まれる県はどれですか？", "鹿児島", listOf("山口", "香川", "大阪", "鹿児島")),
        Question("東北に含まれる県はどれですか？", "青森", listOf("青森", "香川", "大阪", "鹿児島")),
        // ... (他の問題を追加)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI要素の初期化
        tvQuestion = findViewById(R.id.tv_question)
        rgChoices = findViewById(R.id.rg_choices)
        btnAnswer = findViewById(R.id.btn_answer)
        tvScore = findViewById(R.id.tv_score)

        // 最初の問題を表示
        showQuestion()

        // 回答ボタンのクリック処理
        btnAnswer.setOnClickListener {
            val selectedRadioButtonId = rgChoices.checkedRadioButtonId
            if (selectedRadioButtonId != -1) {
                val selectedAnswer = findViewById<RadioButton>(selectedRadioButtonId).text.toString()
                checkAnswer(selectedAnswer)
                rgChoices.clearCheck() // 選択状態をクリア
                currentQuestionIndex++

                // 全問終了の場合
                if (currentQuestionIndex == questions.size) {
                    showResult()
                } else {
                    showQuestion()
                }
            }
        }
    }

    private fun showQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            tvQuestion.text = question.text
            setChoices(question)
        }
    }

    private fun setChoices(question: Question) {
        rgChoices.removeAllViews() // 既存の選択肢を削除
        val options = question.options.shuffled() // 選択肢の順序をシャッフル
        for (option in options) {
            val radioButton = RadioButton(this)
            radioButton.text = option
            rgChoices.addView(radioButton)
        }
    }

    private fun checkAnswer(userAnswer: String) {
        val correctAnswer = questions[currentQuestionIndex].answer
        if (userAnswer == correctAnswer) {
            correctAnswers++
            Toast.makeText(this, "正解です！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "残念！正解は「$correctAnswer」でした", Toast.LENGTH_SHORT).show()
        }
        tvScore.text = "スコア: $correctAnswers"
    }

    private fun showResult() {
        val totalQuestions = questions.size
        val message = "全問終了！\n正解数は $correctAnswers / $totalQuestions です。"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // リセット処理 (必要に応じて実装)
    }

    data class Question(val text: String, val answer: String, val options: List<String>)
}