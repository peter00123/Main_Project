package com.atezhare.ui.receive

import com.atezhare.service.TransferService
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.ReceivedFileRepository
import com.atezhare.data.TransferProgress
import com.atezhare.model.*
import com.atezhare.network.ApiConstants
import com.atezhare.network.RetrofitClient
import com.atezhare.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class ReceiveViewModel(application: Application) : AndroidViewModel(application) {

    private var sessionId: String? = null
    private var senderId: String = "unknown"
    private var pollJob: Job? = null

    private val _qrData = MutableLiveData<String?>()
    val qrData: LiveData<String?> = _qrData

    private val _sessionStatus = MutableLiveData<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val sessionManager = SessionManager(application)
    private val repository = ReceivedFileRepository(application)

    fun requestReceiverQr() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getReceiverQr(
                    ReceiverQrRequest(receiverUserId = sessionManager.getUserId(), sessionId = null)
                )
                if (response.isSuccessful && response.body() != null) {
                    sessionId = response.body()!!.sessionId
                    _qrData.value = response.body()!!.qrData
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

    fun submitCode(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.submitCode(
                    SubmitCodeRequest(code = code, receiverUserId = sessionManager.getUserId())
                )
                if (response.isSuccessful && response.body() != null) {
                    sessionId = response.body()!!.sessionId
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

    private fun startPollingStatus() {
        pollJob?.cancel()
        val sid = sessionId ?: return
        pollJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val response = RetrofitClient.apiService.getSessionStatus(sid)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val status = SessionStatus.from(body.status)
                        _sessionStatus.value = status
                        if (!body.receiverId.isNullOrEmpty()) senderId = body.receiverId
                        
                        // Automatically start downloading if PAIRED or TRANSFERRING and we have fileIds
                        val fileIds = body.fileIds ?: emptyList()
                        if (fileIds.isNotEmpty() && (status == SessionStatus.PAIRED || status == SessionStatus.TRANSFERRING || status == SessionStatus.DONE)) {
                             // Offload download to Foreground Service
                             TransferService.startDownload(
                                 getApplication(),
                                 sid,
                                 senderId,
                                 ArrayList(fileIds)
                             )
                             if (status == SessionStatus.DONE) break
                        }

                        if (status == SessionStatus.ERROR) break
                    }
                } catch (_: Exception) {}
                delay(ApiConstants.POLL_INTERVAL_MS)
            }
        }
    }

    private val _progress = MutableLiveData<TransferProgress?>()
    val progress: LiveData<TransferProgress?> = _progress

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
