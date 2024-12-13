package com.example.sabi.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.sabi.data.repository.DictionaryRepository
import com.example.sabi.data.repository.QuizRepository
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.data.store.SessionPreferences
import com.example.sabi.ui.activity.dictionary.DetailDictionaryActivity
import com.example.sabi.ui.activity.quiz.QuizQuestionActivity
import com.example.sabi.ui.adapter.QuizAdapter
import com.example.sabi.ui.viewmodel.DictionaryViewModel
import com.example.sabi.ui.viewmodel.QuizViewModel
import com.example.sabi.utils.DictionaryViewModelFactory
import com.example.sabi.utils.QuizViewModelFactory
import kotlinx.coroutines.launch
import androidx.viewpager2.widget.ViewPager2
import com.example.sabi.data.response.QuizGroup
import com.example.sabi.ui.adapter.CarouselAdapter
import com.example.sabi.ui.adapter.HomeDictionaryAdapter
import com.example.sabi.ui.adapter.HomeQuizAdapter

class HomeFragment : Fragment() {

    private lateinit var dictionaryRecyclerView: RecyclerView
    private lateinit var quizRecyclerView: RecyclerView

    private val dictionaryViewModel: DictionaryViewModel by viewModels {
        DictionaryViewModelFactory(DictionaryRepository(ApiConfig.getApiService()))
    }

    private val quizViewModel: QuizViewModel by viewModels {
        QuizViewModelFactory(QuizRepository(ApiConfig.getApiService()))
    }

    private lateinit var dictionaryAdapter: HomeDictionaryAdapter
    private lateinit var quizAdapter: HomeQuizAdapter
    private lateinit var sessionPreferences: SessionPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)

        val carouselItems = listOf(
            CarouselAdapter.CarouselItem(R.drawable.viewpager_1),
            CarouselAdapter.CarouselItem(R.drawable.viewpager_2)
        )

        val adapter = CarouselAdapter(carouselItems)
        viewPager.adapter = adapter

        sessionPreferences = SessionPreferences(requireContext())

        dictionaryRecyclerView = view.findViewById(R.id.rv_dictionary)
        quizRecyclerView = view.findViewById(R.id.rv_quiz)

        dictionaryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        dictionaryAdapter = HomeDictionaryAdapter(emptyList()) { dictionary ->
            val intent = Intent(requireContext(), DetailDictionaryActivity::class.java)
            intent.putExtra("DICTIONARY_ID", dictionary.id)
            startActivity(intent)
        }
        dictionaryRecyclerView.adapter = dictionaryAdapter

        fetchDictionaries()

        quizRecyclerView.layoutManager = LinearLayoutManager(context)
        quizAdapter = HomeQuizAdapter(emptyList()) { selectedQuiz ->
            showQuizDialog(selectedQuiz)
        }
        quizRecyclerView.adapter = quizAdapter

        fetchQuizzes()

        return view
    }

    private fun fetchDictionaries() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    observeDictionaries(token)
                } else {
                    dictionaryRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun observeDictionaries(token: String) {
        dictionaryViewModel.getDictionaries(token).observe(viewLifecycleOwner) { response ->
            val data = response.data.readings?.take(5)
            if (data.isNullOrEmpty()) {
                dictionaryRecyclerView.visibility = View.GONE
            } else {
                dictionaryRecyclerView.visibility = View.VISIBLE
                dictionaryAdapter = HomeDictionaryAdapter(data) { dictionary ->
                    Log.d("DictionaryFragment", "Clicked Dictionary ID: ${dictionary.id}")
                    val intent = Intent(requireContext(), DetailDictionaryActivity::class.java)
                    intent.putExtra("DICTIONARY_ID", dictionary.id)  // Pass the dictionary ID to the detail activity
                    startActivity(intent)
                }
                dictionaryRecyclerView.adapter = dictionaryAdapter
            }
        }
    }

    private fun fetchQuizzes() {
        lifecycleScope.launch {
            sessionPreferences.token.collect { token ->
                if (!token.isNullOrEmpty()) {
                    observeQuizzes(token)
                } else {
                    quizRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun observeQuizzes(token: String) {
        quizViewModel.getQuizGroups(token)

        quizViewModel.quizGroupsResponse.observe(viewLifecycleOwner) { response ->
            val data = response?.data?.groups
            if (data.isNullOrEmpty()) {
                quizRecyclerView.visibility = View.GONE
            } else {
                quizRecyclerView.visibility = View.VISIBLE
                quizAdapter.updateData(data)
            }
        }

        quizViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                quizRecyclerView.visibility = View.GONE
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
            intent.putExtra("QUIZ_GROUP_ID", quiz.id) // Kirim ID grup soal
            startActivity(intent)
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}




