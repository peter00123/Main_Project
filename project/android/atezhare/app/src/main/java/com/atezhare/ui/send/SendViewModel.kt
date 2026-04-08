// ui/send/SendViewModel.kt
// Handles all backend communication for the sender flow:
//   1. createSession()     → POST /session/create   — gets sessionId + 6-digit code
//   2. pollSessionStatus() → GET  /session/status   — polls until receiver joins
//   3. onQrScanned()       → POST /pair/scan-qr     — after camera decodes receiver's QR
//   4. confirmAndUpload()  → POST /session/confirm  + POST /files/upload
// Depends on: network/RetrofitClient (API calls), network/ApiConstants (POLL_INTERVAL_MS),
//             model/Models (request/response), utils/FileUtils (multipart prep),
//             utils/SessionManager (userId), model/SessionStatus

package com.atezhare.ui.send

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.SentFileRepository
import com.atezhare.model.*
import com.atezhare.network.ApiConstants
import com.atezhare.network.RetrofitClient
import com.atezhare.utils.FileUtils
import com.atezhare.utils.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody

class SendViewModel(application: Application) : AndroidViewModel(application) {

    // Session data
    private var sessionId: String? = null
    private var selectedFiles: List<LocalFile> = emptyList()
    private var pollJob: Job? = null
    private var sendMode: String = "LIVE"
    private var expiresAt: Long = 0L
    private var receiverId: String? = null

    // LiveData observed by SendActivity
    private val _shareCode = MutableLiveData<String?>()
    val shareCode: LiveData<String?> = _shareCode

    private val _sessionStatus = MutableLiveData<SessionStatus>()
    val sessionStatus: LiveData<SessionStatus> = _sessionStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _qrScanSuccess = MutableLiveData<Boolean>()
    val qrScanSuccess: LiveData<Boolean> = _qrScanSuccess

    // SessionManager used to get userId for API requests
    private val sessionManager = SessionManager(application)
    private val repository = SentFileRepository(application)

    /** Called by SendActivity to pass selected files before session creation */
    fun setSelectedFiles(files: List<LocalFile>) {
        selectedFiles = files
    }

    fun setMode(mode: String, expiresAt: Long) {
        this.sendMode = mode
        this.expiresAt = expiresAt
    }

    // ==================== STEP 1: Create Session ====================

    /**
     * Creates a new transfer session on the backend.
     * Endpoint: POST /session/create — see network/ApiService.createSession()
     * On success: stores sessionId, updates _shareCode, starts polling
     */
    fun createSession() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.createSession(
                    CreateSessionRequest(
                        userId = sessionManager.getUserId(),
                        fileCount = selectedFiles.size
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.sessionId
                    _shareCode.value = body.code
                    startPollingStatus()
                } else {
                    _errorMessage.value = "Failed to create session: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== STEP 2: Poll Session Status ====================

    /**
     * Polls GET /session/status/{sessionId} every POLL_INTERVAL_MS milliseconds.
     * Stops when status becomes PAIRED, DONE, or ERROR.
     * See network/ApiConstants.POLL_INTERVAL_MS and ApiService.getSessionStatus()
     */
    private fun startPollingStatus() {
        val sid = sessionId ?: return
        pollJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val response = RetrofitClient.apiService.getSessionStatus(sid)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val status = SessionStatus.from(body.status)
                        _sessionStatus.value = status
                        if (status == SessionStatus.PAIRED) {
                            receiverId = body.receiverId
                        }
                        if (status == SessionStatus.PAIRED ||
                            status == SessionStatus.DONE ||
                            status == SessionStatus.ERROR) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Polling error — log but keep trying
                }
                delay(ApiConstants.POLL_INTERVAL_MS)
            }
        }
    }

    // ==================== STEP 3a: QR Scan ====================

    /**
     * Called by SendActivity when CameraX decodes a QR from the receiver's screen.
     * Endpoint: POST /pair/scan-qr — see network/ApiService.scanQr()
     */
    fun onQrScanned(qrContent: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.scanQr(
                    ScanQrRequest(
                        senderId = sessionManager.getUserId(),
                        qrContent = qrContent
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionId = body.sessionId
                    _qrScanSuccess.value = true
                    pollJob?.cancel()
                } else {
                    _errorMessage.value = "QR scan failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "QR scan error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== STEP 4: Confirm + Upload ====================

    /**
     * Confirms the transfer to the backend, then uploads all selected files.
     * Endpoint 1: POST /session/confirm — see network/ApiService.confirmSend()
     * Endpoint 2: POST /files/upload  — see network/ApiService.uploadFiles()
     * File prep: see utils/FileUtils.localFilesToMultipart()
     */
    fun confirmAndUpload() {
        val sid = sessionId ?: run {
            _errorMessage.value = "No active session"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Step 4a: Confirm send
                val confirmResp = RetrofitClient.apiService.confirmSend(
                    ConfirmSendRequest(
                        sessionId = sid,
                        senderId = sessionManager.getUserId()
                    )
                )
                if (!confirmResp.isSuccessful) {
                    _errorMessage.value = "Confirm failed: ${confirmResp.code()}"
                    return@launch
                }

                // Step 4b: Upload files — prepare MultipartBody.Part list
                // Rename each file with encoded format BEFORE building multipart
                // FileNameEncoder embeds the timer into the filename
                // FileUtils.renameForSending() creates a renamed copy in cache
                val renamedFiles = selectedFiles.map { file ->
                    FileUtils.renameForSending(
                        getApplication(),
                        file,
                        if (sendMode == "COUNTDOWN") expiresAt else 0L
                    )
                }
                val parts = FileUtils.localFilesToMultipart(renamedFiles)
                if (parts.isEmpty()) {
                    _errorMessage.value = "No files to upload"
                    return@launch
                }

                val sessionIdBody = sid.toRequestBody()
                val uploadResp = RetrofitClient.apiService.uploadFiles(sessionIdBody, parts)

                if (uploadResp.isSuccessful && uploadResp.body() != null) {
                    val uploadBody = uploadResp.body()!!
                    val uploadedFileIds = uploadBody.fileIds

                    // Register mode on server (LIVE only)
                    if (sendMode == "LIVE") {
                        uploadedFileIds.forEach { fileId ->
                            RetrofitClient.apiService.setFileExpiry(
                                SetExpiryRequest(fileId, mode = "LIVE", expiresAt = null)
                            )
                        }
                    }

                    // Save to local sent_files DB
                    val rId = receiverId ?: "unknown"
                    renamedFiles.forEachIndexed { index, localFile ->
                        if (index < uploadedFileIds.size) {
                            repository.saveSentFile(
                                fileId = uploadedFileIds[index],
                                fileName = localFile.name,   // now the encoded name
                                mimeType = localFile.mimeType,
                                fileSize = localFile.size,
                                localPath = localFile.path,
                                sessionId = sid,
                                receiverId = rId,
                                mode = sendMode,
                                expiresAt = if (sendMode == "COUNTDOWN") expiresAt else null
                            )
                        }
                    }

                    _sessionStatus.value = SessionStatus.DONE
                } else {
                    _errorMessage.value = "Upload failed: ${uploadResp.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Upload error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
