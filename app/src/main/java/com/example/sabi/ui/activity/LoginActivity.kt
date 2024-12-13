package com.example.sabi.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sabi.data.repository.AuthRepository
import com.example.sabi.data.store.SessionPreferences
import com.example.sabi.databinding.ActivityLoginBinding
import com.example.sabi.ui.viewmodel.AuthState
import com.example.sabi.ui.viewmodel.AuthViewModel
import com.example.sabi.utils.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(SessionPreferences(this)))
    }
    private lateinit var sessionPreferences: SessionPreferences
    private var currentEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionPreferences = SessionPreferences(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || password.length < 8) {
                Toast.makeText(this, "Email dan password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentEmail = email
            viewModel.login(email, password)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            Toast.makeText(this@LoginActivity, "Loading...", Toast.LENGTH_SHORT).show()
                        }
                        is AuthState.Success -> {
                            state.loginResult?.data?.token?.let { token ->
                                lifecycleScope.launch {
                                    sessionPreferences.saveToken(token)
                                    sessionPreferences.saveEmail(currentEmail)

                                }
                            }
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is AuthState.Error -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}
