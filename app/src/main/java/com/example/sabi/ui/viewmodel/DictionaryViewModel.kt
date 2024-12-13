package com.example.sabi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.sabi.data.repository.DictionaryRepository
import com.example.sabi.data.response.DictionaryListResponse
import com.example.sabi.data.response.ReadingsData
import kotlinx.coroutines.Dispatchers

class DictionaryViewModel(private val repository: DictionaryRepository) : ViewModel() {

    fun getDictionaries(token: String) = liveData(Dispatchers.IO) {
        try {
            val response: DictionaryListResponse = repository.getDictionaries(token)
            emit(response)
        } catch (e: Exception) {
            emit(
                DictionaryListResponse(
                    status = "error",
                    message = e.message ?: "Terjadi kesalahan",
                    data = ReadingsData(readings = emptyList())
                )
            )
        }
    }
}
