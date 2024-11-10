package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gambarerentaro.ui.theme.GambareRentaroTheme
import org.json.JSONObject
import java.io.InputStream

class MenuActivity : ComponentActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // 名前の設定
        val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val editTextName = findViewById<EditText>(R.id.edit_text_name)
        val savedName = sharedPreferences.getString("user_name", "")
        editTextName.setText(savedName)
        editTextName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString()
                val editor = sharedPreferences.edit()
                editor.putString("user_name", name)
                editor.apply()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val category1Spinner = findViewById<Spinner>(R.id.category1_spinner)
        val category2Spinner = findViewById<Spinner>(R.id.category2_spinner)
        val category3Spinner = findViewById<Spinner>(R.id.category3_spinner)
        val category4Spinner = findViewById<Spinner>(R.id.category4_spinner)
        val category5Spinner = findViewById<Spinner>(R.id.category5_spinner)

        // JSON ファイルから選択肢を読み込む
        val jsonString = loadCategoriesJson()
        parseCategoriesJson(jsonString, category1Spinner, category2Spinner, category3Spinner, category4Spinner, category5Spinner)

        // ひとり対戦ボタンのクリックリスナーを設定
        val startButton: Button = findViewById(R.id.start_button)
        startButton.setOnClickListener {
            // 選択された区分を取得
            val category1 = category1Spinner.selectedItem.toString()
            val category2 = category2Spinner.selectedItem.toString()
            val category3 = category3Spinner.selectedItem.toString()
            val category4 = category4Spinner.selectedItem.toString()
            val category5 = category5Spinner.selectedItem.toString()
            val questionCount = when (findViewById<RadioGroup>(R.id.rg_question_count).checkedRadioButtonId) {
                R.id.rb_10_questions -> 10
                R.id.rb_all_questions -> -1 // 全部を表す値 (例: -1)
                else -> -1 // デフォルトは全部
            }
            // スタートボタンがクリックされた時の処理を記述
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("CATEGORY1", category1)
            intent.putExtra("CATEGORY2", category2)
            intent.putExtra("CATEGORY3", category3)
            intent.putExtra("CATEGORY4", category4)
            intent.putExtra("CATEGORY5", category5)
            intent.putExtra("QUESTION_COUNT", questionCount)
            startActivity(intent)
        }
        // 通信対戦ボタンのクリックリスナーを設定
        val multiplayerButton: Button =findViewById(R.id.multiplayer_button)
        multiplayerButton.setOnClickListener {
            val editTextName = findViewById<EditText>(R.id.edit_text_name)
            val nickname = editTextName.text.toString()

            val intent = Intent(this, MultiplayerActivity::class.java)
            intent.putExtra("NICKNAME", nickname) // 名前を Intent に格納
            startActivity(intent)
        }

        // 成績ボタンのクリックリスナーを設定
        val scoreButton = findViewById<Button>(R.id.score_button)
        scoreButton.setOnClickListener {
            // ScoreActivity を起動
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)
        }
        // MenuActivity.kt
        val awsButton = findViewById<Button>(R.id.aws_button)
        awsButton.setOnClickListener {
            val editTextName= findViewById<EditText>(R.id.edit_text_name)
            val name = editTextName.text.toString()

            val intent = Intent(this, AwsActivity::class.java)
            intent.putExtra("USER_NAME", name) // 名前を Intent に格納
            startActivity(intent)
        }
    }
    // JSON ファイルを読み込む関数
    private fun loadCategoriesJson(): String {
        val inputStream: InputStream = assets.open("categories.json")
        val size: Int = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        return String(buffer, Charsets.UTF_8)
    }

    // JSON データをパースし、Spinner の選択肢を設定する関数
    private fun parseCategoriesJson(jsonString: String, category1Spinner: Spinner, category2Spinner: Spinner, category3Spinner: Spinner, category4Spinner: Spinner, category5Spinner: Spinner) {
        try {
            val jsonObject = JSONObject(jsonString)

            val category1Options = (0 until jsonObject.getJSONArray("category1").length())
                .map { jsonObject.getJSONArray("category1").getString(it) }
            val category2Options = (0 until jsonObject.getJSONArray("category2").length())
                .map { jsonObject.getJSONArray("category2").getString(it) }
            val category3Options = (0 until jsonObject.getJSONArray("category3").length())
                .map { jsonObject.getJSONArray("category3").getString(it) }
            val category4Options = (0 until jsonObject.getJSONArray("category4").length())
                .map { jsonObject.getJSONArray("category4").getString(it) }
            val category5Options = (0 until jsonObject.getJSONArray("category5").length())
                .map { jsonObject.getJSONArray("category5").getString(it) }

            setSpinnerAdapter(category1Spinner, category1Options)
            setSpinnerAdapter(category2Spinner, category2Options)
            setSpinnerAdapter(category3Spinner, category3Options)
            setSpinnerAdapter(category4Spinner, category4Options)
            setSpinnerAdapter(category5Spinner, category5Options)

        } catch (e: Exception) {
            Log.e("MenuActivity", "JSON parse error: ${e.message}", e)
        }
    }

    // Spinner にアダプターを設定する関数
    private fun setSpinnerAdapter(spinner: Spinner, options: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    GambareRentaroTheme {
        Greeting2("Android")
    }
}