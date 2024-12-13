package com.example.sabi.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sabi.data.repository.QuizRepository
import com.example.sabi.data.response.*
import kotlinx.coroutines.launch

class QuizViewModel(private val quizRepository: QuizRepository) : ViewModel() {

    private val _quizGroupResponse = MutableLiveData<QuizGroupResponse>()
    val quizGroupResponse: LiveData<QuizGroupResponse> get() = _quizGroupResponse

    private val _quizQuestionsResponse = MutableLiveData<QuizQuestionsResponse>()
    val quizQuestionsResponse: LiveData<QuizQuestionsResponse> get() = _quizQuestionsResponse

    private val _quizResultsResponse = MutableLiveData<QuizResultsResponse>()
    val quizResultsResponse: LiveData<QuizResultsResponse> get() = _quizResultsResponse

    private val _quizGroupsResponse = MutableLiveData<QuizGroupsResponse>() // LiveData baru untuk grup kuis
    val quizGroupsResponse: LiveData<QuizGroupsResponse> get() = _quizGroupsResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun getQuizGroups(token: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = quizRepository.getQuizGroups(token)
                _quizGroupsResponse.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createQuizGroup(token: String, body: CreateQuizGroupBody) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = quizRepository.createQuizGroup(token, body)
                _quizGroupResponse.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getQuizQuestions(token: String, groupId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = quizRepository.getQuizQuestions(token, groupId)
                _quizQuestionsResponse.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun submitQuiz(token: String, body: SubmitQuizBody) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = quizRepository.submitQuiz(token, body)
                _quizResultsResponse.value = response
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
