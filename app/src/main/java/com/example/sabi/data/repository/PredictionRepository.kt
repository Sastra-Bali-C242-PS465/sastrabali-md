package com.example.sabi.data.repository

import com.example.sabi.data.retrofit.ApiService
import com.example.sabi.data.response.PredictionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class PredictionRepository(private val apiService: ApiService) {
    suspend fun postPrediction(result: RequestBody, image: MultipartBody.Part): Response<PredictionResponse> {
        return apiService.postPrediction(result, image)
    }
}
