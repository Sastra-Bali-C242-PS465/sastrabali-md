package com.example.sabi.data.response

data class RegisterResponse(
    val status: String,
    val message: String,
    val data: UserData?
)

data class UserData(
    val user: User?
)

data class User(
    val fullname: String,
    val email: String?
)

