package com.example.sabi.ui.fragment.quiz

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sabi.R
import com.example.sabi.data.repository.QuizRepository
import com.example.sabi.data.response.QuizGroup
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.data.store.SessionPreferences
import com.example.sabi.ui.activity.quiz.QuizQuestionActivity
import com.example.sabi.ui.adapter.QuizAdapter
import com.example.sabi.ui.viewmodel.QuizViewModel
import com.example.sabi.utils.QuizViewModelFactory
import kotlinx.coroutines.launch

class QuizFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var noDataTextView: View
    private val quizViewModel: QuizViewModel by viewModels {
        QuizViewModelFactory(QuizRepository(ApiConfig.getApiService()))
    }

    private lateinit var quizAdapter: QuizAdapter
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz, container, false)

        sessionPreferences = SessionPreferences(requireContext())

        recyclerView = view.findViewById(R.id.rv_quiz)
        noDataTextView = view.findViewById(R.id.tv_no_data)

        recyclerView.layoutManager = LinearLayoutManager(context)
        quizAdapter = QuizAdapter(emptyList()) { selectedQuiz ->
            showQuizDialog(selectedQuiz)
        }
        recyclerView.adapter = quizAdapter

        fetchQuizzes()

        return view
    }

    private fun fetchQuizzes() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    observeQuizzes(token)
                } else {
                    noDataTextView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun observeQuizzes(token: String) {
        quizViewModel.getQuizGroups(token)

        quizViewModel.quizGroupsResponse.observe(viewLifecycleOwner) { response ->
            val data = response?.data?.groups
            if (data.isNullOrEmpty()) {
                noDataTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                noDataTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                quizAdapter.updateData(data)
            }
        }

        quizViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                noDataTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                noDataTextView.findViewById<TextView>(R.id.tv_no_data).text = error
            }
        }
    }

    private fun showQuizDialog(quiz: QuizGroup) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_quiz_starter, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.tvQuizTitle)
        val startButton = dialogView.findViewById<TextView>(R.id.btnStart)
        val closeButton = dialogView.findViewById<View>(R.id.btnClose)

        titleTextView.text = quiz.title

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        startButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), QuizQuestionActivity::class.java)
            intent.putExtra("QUIZ_GROUP_ID", quiz.id)
            startActivity(intent)
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}
