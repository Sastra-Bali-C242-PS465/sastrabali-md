package com.example.sabi.data.repository

import android.util.Log
import com.example.sabi.data.response.LoginResponse
import com.example.sabi.data.response.RegisterResponse
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.data.store.SessionPreferences
import retrofit2.HttpException

class AuthRepository(private val sessionPreferences: SessionPreferences) {

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return try {
            val requestBody = mapOf("fullname" to name, "email" to email, "password" to password)
            val response = ApiConfig.getApiService().register(requestBody)

            if (response.status == "success") {
                response
            } else {
                RegisterResponse(status = "fail", message = response.message, data = null)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = errorBody ?: "Kesalahan HTTP terjadi"
            RegisterResponse(status = "fail", message = errorMessage, data = null)
        } catch (e: Exception) {
            RegisterResponse(status = "fail", message = "Terdapat kesalahan: ${e.message}", data = null)
        }
    }

    suspend fun loginUser(email: String, password: String): LoginResponse {
        return try {
            val requestBody = mapOf("email" to email, "password" to password)
            val response = ApiConfig.getApiService().login(requestBody)

            if (response.status == "success") {
                response.data?.token?.let { sessionPreferences.saveToken(it) }
                response
            } else {
                Log.e("LoginActivity", "Login failed: ${response.message}")
                LoginResponse(status = "fail", message = response.message, data = null)
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = errorBody ?: "Kesalahan HTTP terjadi"
            Log.e("LoginActivity", "HTTP Exception during login: $errorMessage")
            LoginResponse(status = "fail", message = errorMessage, data = null)
        } catch (e: Exception) {
            Log.e("LoginActivity", "Exception during login: ${e.message}", e)
            LoginResponse(status = "fail", message = "Terdapat kesalahan: ${e.message}", data = null)
        }
    }
}
