// ui/auth/LoginActivity.kt
// Login screen with userId and password fields.
// Credentials hardcoded as admin/1234 for current phase.
// On success: saves session via SessionManager and navigates to MainActivity.
// Depends on: ui/auth/LoginViewModel, utils/SessionManager, network/ApiService (login call)

package com.atezhare.ui.auth

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

    // ViewModel handles login logic and API call — see LoginViewModel
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
            val userId = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Delegate to ViewModel — see LoginViewModel.login()
            viewModel.login(userId, password)
        }
    }

    private fun observeViewModel() {
        // Loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        // Login result — see LoginViewModel.loginResult LiveData
        viewModel.loginResult.observe(this) { result ->
            if (result.success) {
                // Save session — see utils/SessionManager.saveSession()
                sessionManager.saveSession(
                    userId = result.userId ?: binding.etUserId.text.toString(),
                    token = result.token
                )
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, result.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Error handling
        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
