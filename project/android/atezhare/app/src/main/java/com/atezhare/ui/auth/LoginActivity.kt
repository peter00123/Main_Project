package com.atezhare.ui.auth

// LoginActivity.kt
// Login screen. Email + password fields.
// Shows "Don't have an account? Sign up" link at the bottom.
// On success → saves session and navigates to MainActivity
// Depends on: LoginViewModel, utils/SessionManager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivityLoginBinding
import com.atezhare.ui.home.MainActivity
import com.atezhare.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email    = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Calls LoginViewModel.login() → Supabase Auth
            viewModel.login(email, password)
        }

        // Navigate to SignUpActivity
        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
        }

        viewModel.loginResult.observe(this) { result ->
            if (result.success) {
                sessionManager.saveSession(result.userId ?: "", result.token)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, result.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
