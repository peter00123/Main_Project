package com.qrshare.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qrshare.network.*
import com.qrshare.utils.FileUtils

class SendViewModel(application: Application) : AndroidViewModel(application) {

    private val _transferState = MutableLiveData(TransferState.IDLE)
    val transferState: LiveData<TransferState> = _transferState

    private val _statusMessage = MutableLiveData("")
    val statusMessage: LiveData<String> = _statusMessage

    private val _progress = MutableLiveData<TransferProgress?>()
    val progress: LiveData<TransferProgress?> = _progress

    private val _transferComplete = MutableLiveData(false)
    val transferComplete: LiveData<Boolean> = _transferComplete

    private var fileSender: FileSender? = null

    fun sendFile(connectionInfo: ConnectionInfo, fileUri: Uri) {
        val context = getApplication<Application>()
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSize(context, fileUri)
        val mimeType = FileUtils.getMimeType(context, fileUri)

        fileSender = FileSender(context)
        fileSender?.sendFile(connectionInfo, fileUri, fileName, fileSize, mimeType,
            object : FileSender.SendCallback {
                override fun onStateChanged(state: TransferState, message: String) {
                    _transferState.value = state
                    _statusMessage.value = message
                }
                override fun onProgress(progress: TransferProgress) {
                    _progress.value = progress
                }
                override fun onComplete(fileName: String) {
                    _transferComplete.value = true
                }
                override fun onError(error: String) {
                    _statusMessage.value = "Error: $error"
                }
            }
        )
    }

    override fun onCleared() {
        fileSender?.cancel()
        super.onCleared()
    }
}
