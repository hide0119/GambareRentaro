package com.example.gambarerentaro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gambarerentaro.database.Score
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScoreAdapter(private val scoreList: List<Score>) :
    RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
        val categoriesTextView: TextView = itemView.findViewById(R.id.categories_text_view)
        val scoreTextView: TextView = itemView.findViewById(R.id.score_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.score_item, parent, false)
        return ScoreViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scoreList[position]

        // timestampをフォーマット
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val timestampString = dateFormat.format(Date(score.timestamp))

        holder.timestampTextView.text = timestampString
        holder.categoriesTextView.text = score.categories
        holder.scoreTextView.text = "スコア: ${score.score} / ${score.totalQuestions}"
    }

    override fun getItemCount(): Int {
        return scoreList.size
    }
}