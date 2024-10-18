package com.example.gambarerentaro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.media.SoundPool
import android.net.Uri
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.*
import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var rgChoices: RadioGroup
    private lateinit var btnAnswer: Button
    private lateinit var tvScore: TextView

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private lateinit var soundPool: SoundPool
    private var correctSoundId: Int = 0
    private var incorrectSoundId: Int = 0

    private lateinit var db: AppDatabase
    private lateinit var questionDao: QuestionDao
    private lateinit var questions: List<Question>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI要素の初期化
        tvQuestion = findViewById(R.id.tv_question)
        rgChoices = findViewById(R.id.rg_choices)
        btnAnswer = findViewById(R.id.btn_answer)
        tvScore = findViewById(R.id.tv_score)

        // 標準の効果音をロード
        soundPool = SoundPool.Builder()
            .setMaxStreams(2) // 同時に再生する音の最大数
            .build()
        correctSoundId = soundPool.load(this.resources.openRawResourceFd(R.raw.hit), 1) // 正解音
        incorrectSoundId = soundPool.load(this.resources.openRawResourceFd(R.raw.over), 1)  // 不正解音

        lifecycleScope.launch(Dispatchers.IO) {
            db = AppDatabase.getDatabase(applicationContext)
            questionDao = db.questionDao()

            // 初回起動時にDBに問題データを挿入
            if (questionDao.getAllQuestions().isEmpty()) {
                questionDao.insertAll(
                    Question(
                        text = "日本の首都はどこですか？",
                        answer = "東京",
                        option1 = "大阪",
                        option2 = "京都",
                        option3 = "東京",
                        option4 = "流山"
                    ),
                    Question(
                        text = "世界で一番高い山はどこですか？",
                        answer = "エベレスト",
                        option1 = "富士山",
                        option2 = "エベレスト",
                        option3 = "マッターホルン",
                        option4 = "筑波山"
                    ),
                    Question(
                        text = "九州に含まれる県はどれですか？",
                        answer = "鹿児島",
                        option1 = "山口",
                        option2 = "香川",
                        option3 = "大阪",
                        option4 = "鹿児島"
                    ),
                    Question(
                        text = "東北に含まれる県はどれですか？",
                        answer = "青森",
                        option1 = "青森",
                        option2 = "香川",
                        option3 = "大阪",
                        option4 = "鹿児島"
                    )
                )
            }
            questions = questionDao.getAllQuestions() // 問題データを取得

            // メインスレッドで UI を更新
            withContext(Dispatchers.Main) {
                // 最初の問題を表示
                showQuestion()
            }
        }
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

        val webView = findViewById<WebView>(R.id.webView)
        // キャッシュモードを変更
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.javaScriptEnabled = true // JavaScript を有効にする
        webView.webViewClient = WebViewClient() // リンクを WebView 内で開く
    }

    private fun showQuestion() {
        // WebView を非表示にする
        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
        webViewContainer.visibility = View.GONE
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            tvQuestion.text = question.text

            // setChoices() を呼び出して選択肢を設定
            setChoices(question)
        }
    }

    private fun setChoices(question: Question) {
        rgChoices.removeAllViews() // 既存の選択肢を削除
        val options = listOf(question.option1, question.option2, question.option3, question.option4).shuffled()
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
            soundPool.play(correctSoundId, 1f, 1f, 0, 0, 1f)
        } else {
            Toast.makeText(this, "残念！正解は「$correctAnswer」でした", Toast.LENGTH_SHORT).show()
            soundPool.play(incorrectSoundId, 1f, 1f, 0, 0, 1f)
        }
        tvScore.text = "スコア: $correctAnswers"
    }

    private fun showResult() {
        val totalQuestions = questions.size
        val message = "全問終了！\n正解数は $correctAnswers / $totalQuestions です。"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // 結果画面へ
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("TOTAL_SCORE", correctAnswers)
        intent.putExtra("TOTAL_QUESTION", totalQuestions)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    fun toggleWebView(view: View) {
        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
        val webView = findViewById<WebView>(R.id.webView)

        if (webViewContainer.visibility == View.GONE) {
            webViewContainer.visibility = View.VISIBLE
            // WebView に URL をロード
            val searchQuery = questions[currentQuestionIndex].text // 現在の問題文を検索キーワードにする
            val url = "https://www.google.com/search?q=" + Uri.encode(searchQuery)
            webView.loadUrl(url)
        } else {
            webViewContainer.visibility = View.GONE
        }
    }
}

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "question_text") val text: String,
    @ColumnInfo(name = "correct_answer") val answer: String,
    @ColumnInfo(name = "option1") val option1: String,
    @ColumnInfo(name = "option2") val option2: String,
    @ColumnInfo(name = "option3") val option3: String,
    @ColumnInfo(name = "option4") val option4: String
)

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): List<Question>

    @Insert
    fun insertAll(vararg questions: Question)

    @Delete
    fun delete(question: Question)

    @Update
    fun update(question: Question)
}

@Database(entities = [Question::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}