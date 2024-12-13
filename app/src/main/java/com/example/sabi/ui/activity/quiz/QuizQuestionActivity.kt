package com.example.sabi.ui.activity.quiz

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sabi.R
import com.example.sabi.data.repository.QuizRepository
import com.example.sabi.data.response.QuizAnswer
import com.example.sabi.data.response.SubmitQuizBody
import com.example.sabi.data.response.UserAnswer
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.data.store.SessionPreferences
import kotlinx.coroutines.launch

class QuizQuestionActivity : AppCompatActivity() {

    private lateinit var sessionPreferences: SessionPreferences
    private lateinit var questionNumberTextView: TextView
    private lateinit var questionImageView: ImageView
    private lateinit var optionButtons: List<Button>
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    private var quizGroupId: Int = 0
    private var currentQuestionIndex = 0
    private var questions: List<UserAnswer> = emptyList()
    private var selectedButtonIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_question)

        sessionPreferences = SessionPreferences(this)

        quizGroupId = intent.getIntExtra("QUIZ_GROUP_ID", 0)

        questionNumberTextView = findViewById(R.id.tv_question_number)
        questionImageView = findViewById(R.id.iv_question_image)
        optionButtons = listOf(
            findViewById(R.id.btn_option_1),
            findViewById(R.id.btn_option_2),
            findViewById(R.id.btn_option_3),
            findViewById(R.id.btn_option_4)
        )
        previousButton = findViewById(R.id.btn_previous)
        nextButton = findViewById(R.id.btn_next)

        fetchQuestions()
        setupNavigation()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun fetchQuestions() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    val repository = QuizRepository(ApiConfig.getApiService())
                    val response = repository.getQuizQuestions(token, quizGroupId)
                    questions = response.data.userAnswerGroup.userAnswers
                    showQuestion()
                }
            }
        }
    }

    private fun showQuestion() {
        if (questions.isEmpty() || currentQuestionIndex < 0 || currentQuestionIndex >= questions.size) return

        val currentQuestion = questions[currentQuestionIndex].question
        questionNumberTextView.text = "Soal No. ${currentQuestionIndex + 1}"
        Glide.with(this).load(currentQuestion.question).into(questionImageView)

        for (i in optionButtons.indices) {
            val option = currentQuestion.options[i]
            optionButtons[i].text = option.option

            optionButtons[i].setBackgroundColor(getColor(R.color.medium_gray))
            optionButtons[i].setOnClickListener {

                questions[currentQuestionIndex].selectedOptionId = option.id
                selectedButtonIndex = i

                for (button in optionButtons) {
                    button.setBackgroundColor(getColor(R.color.medium_gray))
                }
                optionButtons[i].setBackgroundColor(getColor(R.color.lightgreen))
            }
        }

        nextButton.text = if (currentQuestionIndex == questions.size - 1) "Selesai" else "Selanjutnya"
    }

    private fun setupNavigation() {
        previousButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                selectedButtonIndex = null
                showQuestion()
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedButtonIndex = null
                showQuestion()
            } else {
                submitAnswers()
            }
        }
    }

    private fun submitAnswers() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    val quizAnswers = questions.map { userAnswer ->
                        QuizAnswer(
                            userAnswerId = userAnswer.id,
                            optionId = userAnswer.selectedOptionId ?: 0
                        )
                    }

                    if (quizAnswers.any { it.optionId == 0 }) {
                        println("Harap jawab semua pertanyaan sebelum mengirim.")
                        return@collect
                    }

                    val repository = QuizRepository(ApiConfig.getApiService())
                    try {
                        val response = repository.submitQuiz(
                            token,
                            SubmitQuizBody(
                                userAnswerGroupId = quizGroupId,
                                quizAnswers = quizAnswers
                            )
                        )

                        val intent = Intent(this@QuizQuestionActivity, QuizResultActivity::class.java).apply {
                            putExtra("QUIZ_RESULTS", ArrayList(response.data.results))
                        }
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Gagal mengirim jawaban: ${e.message}")
                    }
                }
            }
        }
    }

    private fun showExitConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_close_quiz_confirm)
        dialog.findViewById<Button>(R.id.btnBack).setOnClickListener {
            dialog.dismiss()
            finish()
        }
        dialog.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
