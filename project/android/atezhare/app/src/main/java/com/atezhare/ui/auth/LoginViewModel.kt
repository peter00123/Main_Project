package com.atezhare.ui.auth

// LoginViewModel.kt
// Handles login via Supabase Auth (email + password).
// Replaces the old hardcoded admin/1234 logic.
// Flow:
//   1. supabase.auth.signInWith(Email) — validates credentials
//   2. Returns userId + access token to LoginActivity
//   3. Token stored via SessionManager → sent on all API requests
// Depends on: utils/SupabaseClient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atezhare.model.LoginResponse
import com.atezhare.utils.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Signs in the user using Supabase Auth.
     * The access token returned is stored in SessionManager and sent as
     * Authorization: Bearer <token> on all Retrofit requests via RetrofitClient.
     * Called by LoginActivity.setupClickListeners()
     */
    fun login(userId: String, password: String) {
        // userId here is the email address entered in the login form
        viewModelScope.launch {
            _isLoading.value = true
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email    = userId
                    this.password = password
                }

                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val user    = SupabaseClient.client.auth.currentUserOrNull()

                if (user != null && session != null) {
                    // ADD THIS LINE TEMPORARILY
                    android.util.Log.d("ATEZHARE", "Token: ${session.accessToken}")

                    _loginResult.value = LoginResponse(
                        success = true,
                        token   = session.accessToken,
                        userId  = user.id,
                        message = "Login successful"
                    )
                } else {
                    _loginResult.value = LoginResponse(
                        success = false,
                        token   = null,
                        userId  = null,
                        message = "Login failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("Invalid login") == true ->
                        "Incorrect email or password"
                    e.message?.contains("Email not confirmed") == true ->
                        "Please confirm your email first"
                    else -> e.message ?: "Login failed"
                }
                _loginResult.value = LoginResponse(
                    success = false,
                    token   = null,
                    userId  = null,
                    message = message
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
