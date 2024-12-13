package com.example.sabi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sabi.data.response.LoginResponse
import com.example.sabi.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val loginResult: LoginResponse? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authRepository.registerUser(name, email, password)
                _authState.value = if (response.status == "success") {
                    AuthState.Success("Register Berhasil")
                } else {
                    AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Register Gagal: ${e.message}")
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = authRepository.loginUser(email, password)
                _authState.value = if (response.status == "success") {
                    AuthState.Success(
                        message = "Login Berhasil",
                        loginResult = response
                    )
                } else {
                    AuthState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Login Gagal: ${e.message}")
            }
        }
    }

}
