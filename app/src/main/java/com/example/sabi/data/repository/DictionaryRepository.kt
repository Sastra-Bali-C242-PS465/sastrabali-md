package com.example.sabi.data.repository

import com.example.sabi.data.response.DictionaryListResponse
import com.example.sabi.data.retrofit.ApiService

class DictionaryRepository(private val apiService: ApiService) {

    suspend fun getDictionaries(token: String): DictionaryListResponse {
        return apiService.getDictionaries("Bearer $token")
    }
}
