package com.example.sabi.data.repository

import com.example.sabi.data.response.*
import com.example.sabi.data.retrofit.ApiService

class QuizRepository(private val apiService: ApiService) {

    suspend fun getQuizGroups(token: String): QuizGroupsResponse {
        return apiService.getQuizGroups("Bearer $token")
    }

    suspend fun createQuizGroup(token: String, body: CreateQuizGroupBody): QuizGroupResponse {
        return apiService.createQuizGroup("Bearer $token", body)
    }

    suspend fun getQuizQuestions(token: String, groupId: Int): QuizQuestionsResponse {
        return apiService.getQuizQuestions("Bearer $token", groupId)
    }

    suspend fun submitQuiz(token: String, body: SubmitQuizBody): QuizResultsResponse {
        return apiService.submitQuiz("Bearer $token", body)
    }

}