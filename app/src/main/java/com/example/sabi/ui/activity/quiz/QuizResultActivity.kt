package com.example.sabi.ui.activity.quiz

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sabi.R
import com.example.sabi.data.response.QuizResult

class QuizResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)

        val results = intent.getSerializableExtra("QUIZ_RESULTS") as? ArrayList<QuizResult> ?: return

        val tableResults: TableLayout = findViewById(R.id.table_results)
        val tvTotalScore: TextView = findViewById(R.id.tv_total_score)
        val btnBack: Button = findViewById(R.id.btn_back) // Tombol Kembali

        var totalScore = 0
        for ((index, result) in results.withIndex()) {
            val answerStatus = if (result.isCorrect) "Benar" else "Salah"

            val row = TableRow(this)
            val noColumn = TextView(this).apply {
                text = (index + 1).toString()
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
            }

            val answerColumn = TextView(this).apply {
                text = answerStatus
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
            }

            row.addView(noColumn)
            row.addView(answerColumn)
            tableResults.addView(row)

            if (result.isCorrect) {
                totalScore += 20
            }
        }

        tvTotalScore.text = "$totalScore"

        btnBack.setOnClickListener {
            finish()
        }
    }
}
