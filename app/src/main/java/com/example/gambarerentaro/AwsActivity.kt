package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.example.gambarerentaro.database.Score
import com.example.gambarerentaro.database.ScoreDatabase
import com.example.gambarerentaro.ui.theme.GambareRentaroTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File

class AwsActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aws)

        val userName = intent.getStringExtra("USER_NAME") ?: "" // Intent から名前を取得
        val userNameTextView = findViewById<TextView>(R.id.user_name_text_view)
        userNameTextView.text = userName // TextView に名前を表示

        val uploadButton = findViewById<Button>(R.id.upload_button)
        uploadButton.setOnClickListener {
            uploadScoresToS3()
        }
    }

    private fun uploadScoresToS3() {

        val scoreDao = ScoreDatabase.getDatabase(this).scoreDao()
        lifecycleScope.launch {
            val scoreList = scoreDao.getLatestScores()
            val jsonString = Gson().toJson(scoreList) // JSONに変換

            // AWS S3へのアップロード処理
            val credentialsProvider = CognitoCachingCredentialsProvider(
                this@AwsActivity,
                "ap-northeast-1:1fc78fb7-b122-4db8-be69-a859bbf42818", //アイデンティティプールのID
                Regions.AP_NORTHEAST_1 // リージョン
            )
            val s3Client = AmazonS3Client(credentialsProvider)
            val transferUtility = TransferUtility.builder()
                .context(this@AwsActivity)
                .s3Client(s3Client)
                .build()
            val tempFile = File.createTempFile("scores", ".json", cacheDir)
            tempFile.writeBytes(jsonString.toByteArray(Charsets.UTF_8))

            val uploadObserver = transferUtility.upload(
                "gambarerentaro", // バケット名
                "scores.json", // ファイル名
                tempFile,
                ObjectMetadata()
            )

            // アップロード完了時の処理などを記述
            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        // アップロード完了時の処理
                        runOnUiThread {
                            Toast.makeText(this@AwsActivity, "成績データをアップロードしました", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // ... other override methods ...
                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // 進捗状況が変化したときの処理
                    // 例: プログレスバーを更新する
                }

                override fun onError(id: Int, ex: Exception) {
                    Log.e("AwsActivity", "Upload error: ${ex.javaClass.simpleName} - ${ex.message}", ex)
                    ex.printStackTrace() // スタックトレースを出力
                    runOnUiThread {
                        Toast.makeText(this@AwsActivity, "アップロード中にエラーが発生しました", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun downloadScoresFromS3() {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            this,
            "ap-northeast-1:1fc78fb7-b122-4db8-be69-a859bbf42818", // アイデンティティプールのID
            Regions.AP_NORTHEAST_1 // リージョン
        )
        val s3Client = AmazonS3Client(credentialsProvider)
        val transferUtility = TransferUtility.builder()
            .context(this)
            .s3Client(s3Client)
            .build()

        val downloadObserver = transferUtility.download(
            "your-bucket-name", // バケット名
            "scores.json", // ファイル名
            File(cacheDir, "scores.json") // ダウンロード先のファイル
        )

        downloadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    // ダウンロード完了時の処理
                    val downloadedFile = File(cacheDir, "scores.json")
                    val jsonString = downloadedFile.readText()
                    val scoreList = Gson().fromJson(jsonString, Array<Score>::class.java).toList()

                    // 最下部の10件分のデータを取得
                    val latestScores = scoreList.takeLast(10)

                    // データをテキストに変換
                    val scoreText = latestScores.joinToString("\n") { "${it.score}" }
                    //val scoreText = latestScores.joinToString("\n") { "${it.name}: ${it.score}" }

                    // TextView に表示
                    runOnUiThread {
                        val textView = findViewById<TextView>(R.id.score_text_view) // activity_aws.xml に TextView を追加
                        textView.text = scoreText
                    }
                }
            }

            // ... other override methods ...
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                // 進捗状況が変化したときの処理
                // 例: プログレスバーを更新する
            }

            override fun onError(id: Int, ex: Exception) {
                // エラーが発生したときの処理
                // 例: エラーメッセージを表示する
            }

            // ... other override methods ...
        })
    }

    // ... (その他のコードは省略)
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