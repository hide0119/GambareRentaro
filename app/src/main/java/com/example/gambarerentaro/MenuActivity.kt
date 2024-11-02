package com.example.gambarerentaro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        // スピナーに選択肢を設定
        val category1Options = arrayOf("塾","学校","その他") // 区分の選択肢
        val category1Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category1Options)
        category1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category1Spinner.adapter = category1Adapter

        val category2Options = arrayOf("4年") // 区分の選択肢
        val category2Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category2Options)
        category2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category2Spinner.adapter = category2Adapter

        val category3Options = arrayOf("基礎テスト") // 区分の選択肢
        val category3Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category3Options)
        category3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category3Spinner.adapter = category3Adapter

        val category4Options = arrayOf("11月") // 区分の選択肢
        val category4Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category4Options)
        category4Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category4Spinner.adapter = category4Adapter

        val category5Options = arrayOf("国語", "算数", "社会", "理科", "英語") // 区分の選択肢
        val category5Adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, category5Options)
        category5Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        category5Spinner.adapter = category5Adapter

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