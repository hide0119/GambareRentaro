package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var rgChoices: RadioGroup
    private lateinit var btnAnswer: Button
    private lateinit var btnQuit: Button
    private lateinit var tvScore: TextView

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private lateinit var soundPool: SoundPool
    private var correctSoundId: Int = 0
    private var incorrectSoundId: Int = 0

    // カテゴリ情報を保存する変数
    private var category1: String? = null
    private var category2: String? = null
    private var category3: String? = null
    private var category4: String? = null
    private var category5: String? = null

    private lateinit var db: AppDatabase
    private lateinit var questionDao: QuestionDao
    private lateinit var questions: List<Question>
    private lateinit var imageView: ImageView // ImageView をプロパティとして宣言

    private lateinit var generativeModel: GenerativeModel
    private val API_KEY = "AIzaSyDmnVMgcM4AF4PeMj57SA729ZnMtFKSHeI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 区分情報を受け取る
        category1 = intent.getStringExtra("CATEGORY1")
        category2 = intent.getStringExtra("CATEGORY2")
        category3 = intent.getStringExtra("CATEGORY3")
        category4 = intent.getStringExtra("CATEGORY4")
        category5 = intent.getStringExtra("CATEGORY5")
        val questionCount = intent.getIntExtra("QUESTION_COUNT", -1)

        // UI要素の初期化
        tvQuestion = findViewById(R.id.tv_question)
        rgChoices = findViewById(R.id.rg_choices)
        btnAnswer = findViewById(R.id.btn_answer)
        btnQuit = findViewById(R.id.btn_quit)
        tvScore = findViewById(R.id.tv_score)
        imageView = findViewById(R.id.iv_question_image)

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
                val json = loadQuestionsJson()
                parseAndInsertQuestions(json)
            }
            // 区分を指定して問題を取得
            questions = questionDao.getQuestionsByCategory(category1, category2, category3, category4, category5)
            questions = questions.shuffled() // リストをシャッフル

            // 全部ではない場合
            if (questionCount != -1) {
                questions = questions.take(questionCount)
            }
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

        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = API_KEY
        )
        val webView = findViewById<WebView>(R.id.webView)
        // キャッシュモードを変更
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.javaScriptEnabled = true // JavaScript を有効にする
        webView.webViewClient = WebViewClient() // リンクを WebView 内で開く

        btnQuit.setOnClickListener {
            // 残り問題を不正解として扱う
            val remainingQuestions = questions.size - (currentQuestionIndex + 1)

            // スコアを更新 (全問不正解で更新)
            correctAnswers += 0

            // 結果画面へ遷移
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("TOTAL_SCORE", correctAnswers)
            intent.putExtra("TOTAL_QUESTION", questions.size)
            // CATEGORY1～CATEGORY5 を Intent に追加
            intent.putExtra("CATEGORY1", category1)
            intent.putExtra("CATEGORY2", category2)
            intent.putExtra("CATEGORY3", category3)
            intent.putExtra("CATEGORY4", category4)
            intent.putExtra("CATEGORY5", category5)
            startActivity(intent)
            finish() // 現在のActivityを終了
        }
    }

    private fun showQuestion() {
        // 問題番号を表示
        val questionNumberText = "${currentQuestionIndex + 1}/${questions.size}"
        findViewById<TextView>(R.id.tv_question_number).text = questionNumberText

        // WebView を非表示にする
        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
        webViewContainer.visibility = View.GONE
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            tvQuestion.text = question.text
            // 画像を表示
            if (question.imageResourceId != null) {
                imageView.setImageResource(question.imageResourceId) // imageView をプロパティとして使用
                imageView.visibility = View.VISIBLE
            } else {
                imageView.visibility = View.GONE // imageView をプロパティとして使用
            }

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
        // スクロールを一番上に移動
        val scrollView = findViewById<ScrollView>(R.id.scrollView) // ScrollViewのIDを設定
        scrollView.fullScroll(ScrollView.FOCUS_UP)
    }

    private fun showResult() {
        val totalQuestions = questions.size
        val message = "全問終了！\n正解数は $correctAnswers / $totalQuestions です。"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // 結果画面へ
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("TOTAL_SCORE", correctAnswers)
        intent.putExtra("TOTAL_QUESTION", totalQuestions)
        // CATEGORY1～CATEGORY5 を Intentに追加
        intent.putExtra("CATEGORY1", intent.getStringExtra("CATEGORY1"))
        intent.putExtra("CATEGORY2", intent.getStringExtra("CATEGORY2"))
        intent.putExtra("CATEGORY3", intent.getStringExtra("CATEGORY3"))
        intent.putExtra("CATEGORY4", intent.getStringExtra("CATEGORY4"))
        intent.putExtra("CATEGORY5", intent.getStringExtra("CATEGORY5"))

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

//    fun toggleWebView(view: View) {
//        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
//        val webView = findViewById<WebView>(R.id.webView)
//
//        if (webViewContainer.visibility == View.GONE) {
//            webViewContainer.visibility = View.VISIBLE
//            // WebView に URL をロード
//            val searchQuery = questions[currentQuestionIndex].text // 現在の問題文を検索キーワードにする
//            val url = "https://www.google.com/search?q=" + Uri.encode(searchQuery)
//            webView.loadUrl(url)
//        } else {
//            webViewContainer.visibility = View.GONE
//        }
//    }
//    fun toggleWebView(view: View) {
//        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
//        val webView = findViewById<WebView>(R.id.webView)
//
//        if (webViewContainer.visibility == View.GONE) {
//            webViewContainer.visibility = View.VISIBLE
//
//            // AIにヒントを質問するプロンプトを生成
//            val prompt = "問題: ${questions[currentQuestionIndex].text} についてヒントをください。ただし、直接的な答えは含めないでください。ちょっと面白い感じで回答してください。"
//
//            // GenerativeModelを使用してAIに質問
//            lifecycleScope.launch {
//                try {
//                    val response = withContext(Dispatchers.IO) {
//                        generativeModel.generateContent(prompt)
//                    }
//
//                    // 回答をWebViewに表示
//                    withContext(Dispatchers.Main) {
//                        val answer = response.text ?: "回答を生成できませんでした。"
//                        webView.loadDataWithBaseURL(null, answer, "text/html", "UTF-8", null)
//                    }
//                } catch (e: Exception) {
//                    // エラー発生時の処理
//                    withContext(Dispatchers.Main) {
//                        webView.loadDataWithBaseURL(null, "エラーが発生しました: ${e.message}", "text/html", "UTF-8", null)
//                    }
//                }
//            }
//        } else {
//            webViewContainer.visibility = View.GONE
//        }
//    }
    fun toggleWebView(view: View) {
        val webViewContainer = findViewById<LinearLayout>(R.id.webViewContainer)
        val webView = findViewById<WebView>(R.id.webView)

        if (webViewContainer.visibility == View.GONE) {
            webViewContainer.visibility = View.VISIBLE

            // AI にヒントを質問するプロンプトを生成
            val prompt = "問題: ${questions[currentQuestionIndex].text} についてヒントをください。ただし、直接的な答えは含めないでください。フランクな感じで回答してください。"

            lifecycleScope.launch {
                try {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro",
                        apiKey = API_KEY
                    )
                    val response = withContext(Dispatchers.IO) {
                        generativeModel.generateContent(prompt)
                    }

                    // 回答を WebView に表示
                    withContext(Dispatchers.Main) {
                        val answer = response.text ?: "回答を生成できませんでした。"
                        webView.loadDataWithBaseURL(null, answer, "text/html", "UTF-8", null)
                    }
                } catch (e: Exception) {
                    // エラー発生時は Google 検索にフォールバック
                    withContext(Dispatchers.Main) {
                        val searchQuery = questions[currentQuestionIndex].text
                        val url = "https://www.google.com/search?q=" + Uri.encode(searchQuery)
                        webView.loadUrl(url)
                    }
                }
            }
        } else {
            webViewContainer.visibility = View.GONE
        }
    }
    private fun loadQuestionsJson(): String {
        val inputStream: InputStream = assets.open("questions.json")
        val size:Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer, Charsets.UTF_8)
    }

    @SuppressLint("DiscouragedApi")
    private suspend fun parseAndInsertQuestions(jsonString: String) {
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val imageResourceName = jsonObject.optString("imageResourceId", null)
            val imageResourceId = if (imageResourceName != null) {
                resources.getIdentifier(imageResourceName, "drawable", packageName)
            } else {
                null
            }
            val question = Question(
                text = jsonObject.getString("text"),
                answer = jsonObject.getString("answer"),
                option1 = jsonObject.getString("option1"),
                option2 = jsonObject.getString("option2"),
                option3 = jsonObject.getString("option3"),
                option4 = jsonObject.getString("option4"),
                category1 = jsonObject.getString("category1"),
                category2 = jsonObject.getString("category2"),
                category3 = jsonObject.getString("category3"),
                category4 = jsonObject.getString("category4"),
                category5 = jsonObject.getString("category5"),
                imageResourceId = imageResourceId
            )
            questionDao.insertAll(question)
        }
    }
}

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate =true) val id: Int = 0,
    @ColumnInfo(name = "question_text") val text: String,
    @ColumnInfo(name = "correct_answer") val answer: String,
    @ColumnInfo(name = "option1") val option1: String,
    @ColumnInfo(name = "option2") val option2: String,
    @ColumnInfo(name = "option3") val option3: String,
    @ColumnInfo(name = "option4") val option4: String,
    @ColumnInfo(name = "category1") val category1: String?, // 塾
    @ColumnInfo(name = "category2") val category2: String?, // 学年
    @ColumnInfo(name = "category3") val category3: String?, // テストの種類
    @ColumnInfo(name = "category4") val category4: String?, // 月
    @ColumnInfo(name = "category5") val category5: String?, // 科目
    @ColumnInfo(name = "image_resource_id") val imageResourceId: Int? // 画像リソースID
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

    @Query("SELECT * FROM questions WHERE category1 = :category1 AND category2 = :category2 AND category3 = :category3 AND category4 = :category4 AND category5 = :category5")
    fun getQuestionsByCategory(category1: String?, category2: String?, category3: String?, category4: String?, category5: String?): List<Question>
}

@Database(entities = [Question::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = context.getDatabasePath("quiz_database")
                if (dbFile.exists()) {
                    dbFile.delete()
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_database"
                )
                // 移行処理を追加
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // データベースを削除して再作成
                .build()
                INSTANCE = instance
                instance
            }
        }
        // 移行処理を定義
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 新しいカラムを追加
                database.execSQL("ALTER TABLE questions ADD COLUMN category1 TEXT")
                database.execSQL("ALTER TABLE questions ADD COLUMN category2 TEXT")
                database.execSQL("ALTER TABLE questions ADD COLUMN category3 TEXT")
                database.execSQL("ALTER TABLE questions ADD COLUMN category4 TEXT")
                database.execSQL("ALTER TABLE questions ADD COLUMN category5 TEXT")
            }
        }
    }
}