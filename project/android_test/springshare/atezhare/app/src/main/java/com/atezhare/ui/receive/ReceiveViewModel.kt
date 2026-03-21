// ui/receive/ReceiveViewModel.kt
// Handles all backend communication for the receiver flow:
//   1. requestReceiverQr() → POST /pair/receiver-qr  — get QR data + sessionId
//   2. submitCode()        → POST /pair/submit-code  — submit 6-digit code
//   3. pollSessionStatus() → GET  /session/status    — wait for DONE/ERROR
// Exposes: qrData, sessionStatus, isLoading, errorMessage LiveData
// Depends on: network/RetrofitClient (API calls), network/ApiConstants (poll interval),
//             model/Models, utils/SessionManager (userId), model/SessionStatus

package com.atezhare.ui.receive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.model.*
import com.atezhare.network.ApiConstants
import com.atezhare.network.RetrofitClient
import com.atezhare.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReceiveViewModel(application: Application) : AndroidViewModel(application) {

    private var sessionId: String? = null
    private var pollJob: Job? = null

    // LiveData for ReceiveFragment
    private val _qrData = MutableLiveData<String?>()
    val qrData: LiveData<String?> = _qrData

    private val _sessionStatus = MutableLiveData<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // SessionManager used for userId — see utils/SessionManager
    private val sessionManager = SessionManager(application)

    // ==================== STEP 1: Get Receiver QR ====================

    /**
     * Requests a pairing QR code from the backend.
     * Endpoint: POST /pair/receiver-qr — see network/ApiService.getReceiverQr()
     * On success: stores sessionId, posts qrData for rendering in ReceiveFragment
     */
    fun requestReceiverQr() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getReceiverQr(
                    ReceiverQrRequest(
                        receiverUserId = sessionManager.getUserId(),
                        sessionId = null
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.sessionId
                    _qrData.value = body.qrData
                    // Start polling for sender to connect
                    startPollingStatus()
                } else {
                    _errorMessage.value = "Failed to get QR: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== STEP 2a: Submit 6-digit Code ====================

    /**
     * Submits the 6-digit code entered by the receiver.
     * Endpoint: POST /pair/submit-code — see network/ApiService.submitCode()
     * This notifies the sender (backend pushes status), which triggers sender confirm popup.
     */
    fun submitCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.submitCode(
                    SubmitCodeRequest(
                        code = code,
                        receiverUserId = sessionManager.getUserId()
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.sessionId
                    // Start polling for confirmation/transfer
                    startPollingStatus()
                } else {
                    _errorMessage.value = "Invalid code or session not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== STEP 3: Poll Session Status ====================

    /**
     * Polls GET /session/status/{sessionId} every POLL_INTERVAL_MS milliseconds.
     * Stops when status is DONE or ERROR.
     * See network/ApiConstants.POLL_INTERVAL_MS, ApiService.getSessionStatus()
     */
    private fun startPollingStatus() {
        pollJob?.cancel()
        val sid = sessionId ?: return

        pollJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val response = RetrofitClient.apiService.getSessionStatus(sid)
                    if (response.isSuccessful && response.body() != null) {
                        val status = SessionStatus.from(response.body()!!.status)
                        _sessionStatus.value = status
                        if (status == SessionStatus.DONE || status == SessionStatus.ERROR) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Polling error — keep retrying
                }
                delay(ApiConstants.POLL_INTERVAL_MS)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
