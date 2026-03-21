package com.atezhare.ui.auth

// SignUpViewModel.kt
// Handles sign up logic using Supabase Auth.
// Flow:
//   1. supabase.auth.signUpWith(Email) — creates the user in auth.users
//   2. The SQL trigger handle_new_user() auto-inserts into profiles table
//   3. Returns userId + access token to SignUpActivity
// Depends on: utils/SupabaseClient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atezhare.utils.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SignUpViewModel : ViewModel() {

    data class SignUpResult(
        val success: Boolean,
        val userId: String? = null,
        val token: String? = null,
        val message: String? = null
    )

    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult: LiveData<SignUpResult> = _signUpResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Creates a new user in Supabase Auth.
     * The username is passed as user metadata and picked up by the
     * handle_new_user() SQL trigger which inserts it into the profiles table.
     * Called by SignUpActivity.setupClickListeners()
     */
    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Step 1: Create user in Supabase Auth
                // Username stored in metadata → picked up by SQL trigger
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email    = email
                    this.password = password
                    data = buildJsonObject {
                        put("username", username)
                    }
                }

                // Step 2: Get the session created after signup
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val user    = SupabaseClient.client.auth.currentUserOrNull()

                if (user != null) {
                    _signUpResult.value = SignUpResult(
                        success = true,
                        userId  = user.id,
                        token   = session?.accessToken,
                        message = "Account created successfully"
                    )
                } else {
                    _signUpResult.value = SignUpResult(
                        success = false,
                        message = "Sign up failed. Please try again."
                    )
                }
            } catch (e: Exception) {
                // Common errors:
                // "User already registered" → email already exists
                // "Password should be at least 6 characters"
                val message = when {
                    e.message?.contains("already registered") == true ->
                        "This email is already registered"
                    e.message?.contains("password") == true ->
                        "Password must be at least 6 characters"
                    else -> e.message ?: "Sign up failed"
                }
                _signUpResult.value = SignUpResult(success = false, message = message)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
