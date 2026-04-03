package com.atezhare.ui.testing

// TestingViewModel.kt
// Handles backend connection check via GET /auth/test
// Exposes: connectionStatus, testingValue LiveData
// Depends on: network/RetrofitClient, network/ApiService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.launch

class TestingViewModel(application: Application) : AndroidViewModel(application) {

    // Empty variable named "testing" — can be set to any value for testing purposes
    private val _testingValue = MutableLiveData<String>("")
    val testingValue: LiveData<String> = _testingValue

    private val _connectionStatus = MutableLiveData<String>("Checking...")
    val connectionStatus: LiveData<String> = _connectionStatus

    /**
     * Calls GET /auth/test on the Spring Boot backend.
     * Updates connectionStatus with result.
     * Called by TestingFragment on load and on retry button press.
     */
    fun checkBackendConnection() {
        _connectionStatus.value = "Checking..."
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.testConnection()
                if (response.isSuccessful) {
                    _connectionStatus.value = "Connected ✓  (${response.body()})"
                } else {
                    _connectionStatus.value = "Failed — HTTP ${response.code()}"
                }
            } catch (e: Exception) {
                _connectionStatus.value = "Not Connected ✗  (${e.message})"
            }
        }
    }

    /** Set the testing variable value */
    fun setTestingValue(value: String) {
        _testingValue.value = value
    }
}
