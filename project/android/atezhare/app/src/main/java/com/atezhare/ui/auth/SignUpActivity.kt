package com.atezhare.ui.auth

// SignUpActivity.kt
// Sign up screen. Collects username, email, password.
// On submit → calls SignUpViewModel.signUp()
// On success → navigates to MainActivity
// Has a "Already have account? Login" link at the bottom
// Depends on: SignUpViewModel, utils/SessionManager, ui/home/MainActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivitySignUpBinding
import com.atezhare.ui.home.MainActivity
import com.atezhare.utils.SessionManager

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()

            // Basic validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (username.length < 3) {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call ViewModel — see SignUpViewModel.signUp()
            viewModel.signUp(username, email, password)
        }

        // Navigate to LoginActivity
        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSignUp.isEnabled = !loading
        }

        viewModel.signUpResult.observe(this) { result ->
            if (result.success) {
                // Save session — see utils/SessionManager.saveSession()
                sessionManager.saveSession(result.userId ?: "", result.token)
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            } else {
                Toast.makeText(this, result.message ?: "Sign up failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
