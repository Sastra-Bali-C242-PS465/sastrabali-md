package com.example.sabi.data.retrofit

import com.example.sabi.data.response.CreateQuizGroupBody
import com.example.sabi.data.response.DictionaryListResponse
import com.example.sabi.data.response.LoginResponse
import com.example.sabi.data.response.PredictionResponse
import com.example.sabi.data.response.QuizGroupResponse
import com.example.sabi.data.response.QuizGroupsResponse
import com.example.sabi.data.response.QuizQuestionsResponse
import com.example.sabi.data.response.QuizResultsResponse
import com.example.sabi.data.response.RegisterResponse
import com.example.sabi.data.response.SubmitQuizBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import retrofit2.Response

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): LoginResponse

    @GET("readings")
    suspend fun getDictionaries(@Header("Authorization") token: String): DictionaryListResponse

    @GET("groups")
    suspend fun getQuizGroups(
        @Header("Authorization") token: String
    ): QuizGroupsResponse

    @POST("user-answer-groups")
    suspend fun createQuizGroup(
        @Header("Authorization") token: String,
        @Body body: CreateQuizGroupBody
    ): QuizGroupResponse

    @GET("user-answer-groups/{id}")
    suspend fun getQuizQuestions(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): QuizQuestionsResponse

    @PATCH("user-answer-groups")
    suspend fun submitQuiz(
        @Header("Authorization") token: String,
        @Body body: SubmitQuizBody
    ): QuizResultsResponse

    @Multipart
    @POST("predictions")
    suspend fun postPrediction(
        @Part("result") result: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<PredictionResponse>

}

