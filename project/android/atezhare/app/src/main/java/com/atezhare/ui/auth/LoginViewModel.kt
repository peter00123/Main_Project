// ui/auth/LoginViewModel.kt
// Handles login business logic. Credentials hardcoded as admin/1234.
// Also attempts API login via network/ApiService.login() for token retrieval.
// Exposes: loginResult (LoginResponse), isLoading, errorMessage LiveData

package com.atezhare.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atezhare.model.LoginRequest
import com.atezhare.model.LoginResponse
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResponse>()
    val loginResult: LiveData<LoginResponse> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Hardcoded credentials — replace with API-only auth when backend is ready
    private val HARDCODED_USER = "admin"
    private val HARDCODED_PASS = "1234"

    /**
     * Validates credentials locally first, then optionally calls backend.
     * Called by LoginActivity on button press.
     * Uses network/ApiService.login() via RetrofitClient
     */
    fun login(userId: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        // Local validation first (hardcoded credentials)
        if (userId != HARDCODED_USER || password != HARDCODED_PASS) {
            _isLoading.value = false
            _loginResult.value = LoginResponse(
                success = false,
                token = null,
                userId = null,
                message = "Invalid credentials"
            )
            return
        }

        // Attempt API login to get a token
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(userId = userId, password = password)
                )
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.value = response.body()!!
                } else {
                    // Backend unavailable but creds are valid locally — allow offline login
                    _loginResult.value = LoginResponse(
                        success = true,
                        token = null,
                        userId = userId,
                        message = "Logged in (offline)"
                    )
                }
            } catch (e: Exception) {
                // Network error — still allow login with hardcoded creds
                _loginResult.value = LoginResponse(
                    success = true,
                    token = null,
                    userId = userId,
                    message = "Logged in (offline)"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}
