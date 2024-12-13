package com.example.sabi.data.response

data class DictionaryListResponse(
    val status: String,
    val message: String,
    val data: ReadingsData
)

data class ReadingsData(
    val readings: List<DictionaryItem>
)

data class DictionaryItem(
    val id: Int,
    val title: String,
    val thumbnailUrl: String,
    val content: String
)
