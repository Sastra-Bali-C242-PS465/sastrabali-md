package com.example.sabi.data.response

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val user: LoginUser?,
    val token: String
)

data class LoginUser(
    val name: String
)

