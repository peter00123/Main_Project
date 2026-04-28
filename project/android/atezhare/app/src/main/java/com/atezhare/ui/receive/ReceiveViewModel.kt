package com.atezhare.ui.receive

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.ReceivedFileRepository
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
                        if (status == SessionStatus.DONE) {
                            Log.d("ReceiveViewModel", "Session DONE, downloading files: ${body.fileIds}")
                            val fileIds = body.fileIds ?: emptyList()
                            if (fileIds.isNotEmpty()) downloadAndSaveFiles(sid, fileIds)
                            else Log.w("ReceiveViewModel", "DONE status but fileIds is empty")
                            break
                        }
                        if (status == SessionStatus.ERROR) break
                    }
                } catch (_: Exception) {}
                delay(ApiConstants.POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun downloadAndSaveFiles(sid: String, fileIds: List<String>) {
        _isLoading.value = true
        for (fileId in fileIds) {
            try {
                if (repository.getByFileId(fileId) != null) continue
                val response = RetrofitClient.apiService.downloadFile(fileId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val contentDisposition = response.headers()["Content-Disposition"] ?: ""
                    val fileName = Regex("""filename="?([^";\n]+)"?""")
                        .find(contentDisposition)?.groupValues?.get(1) ?: "file_$fileId"
                    val mimeType = response.headers()["Content-Type"] ?: "application/octet-stream"
                    
                    repository.saveDownloadedFile(
                        fileId = fileId,
                        fileName = fileName,
                        mimeType = mimeType,
                        inputStream = body.byteStream(),
                        sessionId = sid,
                        senderId = senderId
                    )
                    
                    // Register the file with the Librarian for timer-based expiry
                    // LibrarianRepository reads the timer from the encoded filename
                    // and stores a deleteAt timestamp in the librarian SQLite table
                    val librarian = com.atezhare.data.LibrarianRepository(getApplication())
                    librarian.registerFile(
                        fileName = fileName,
                        localPath = File(
                            getApplication<android.app.Application>().filesDir,
                            "received/${fileId}_${fileName}"
                        ).absolutePath,
                        fileId = fileId
                    )

                    // Start polling for deletion status after each successful save
                    startFileStatusPolling(fileId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Download failed: ${e.message}"
            }
        }
        _isLoading.value = false
    }

    private fun startFileStatusPolling(fileId: String) {
        viewModelScope.launch {
            while (isActive) {
                delay(5000) // faster for debugging

                try {
                    val response = RetrofitClient.apiService.getFileStatus(fileId)
                    val status = response.body()
                    
                    Log.d("ReceiverPoll", "file=$fileId deleted=${status?.deleted}")

                    if (response.isSuccessful && status?.deleted == true) {
                        repository.markDeleted(fileId)
                        Log.d("ReceiverPoll", "Marked deleted in Room: $fileId")
                        break
                    }
                } catch (e: Exception) {
                    Log.e("ReceiverPoll", "Polling failed for $fileId", e)
                }
            }
        }
    }

    override fun onCleared() { super.onCleared(); pollJob?.cancel() }
}
