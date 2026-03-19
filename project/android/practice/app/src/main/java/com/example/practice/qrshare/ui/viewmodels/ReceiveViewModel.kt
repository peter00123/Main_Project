package com.qrshare.ui.viewmodels

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qrshare.network.*
import com.qrshare.qr.QRCodeGenerator

class ReceiveViewModel(application: Application) : AndroidViewModel(application) {

    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap> = _qrBitmap

    private val _connectionInfo = MutableLiveData<ConnectionInfo>()
    val connectionInfo: LiveData<ConnectionInfo> = _connectionInfo

    private val _transferState = MutableLiveData(TransferState.IDLE)
    val transferState: LiveData<TransferState> = _transferState

    private val _statusMessage = MutableLiveData("")
    val statusMessage: LiveData<String> = _statusMessage

    private val _progress = MutableLiveData<TransferProgress?>()
    val progress: LiveData<TransferProgress?> = _progress

    private val _receivedFile = MutableLiveData<Pair<String, String>?>()
    val receivedFile: LiveData<Pair<String, String>?> = _receivedFile

    private var fileReceiver: FileReceiver? = null

    fun startReceiving() {
        val context = getApplication<Application>()
        val ip = NetworkUtils.getDeviceIpAddress(context)
        val port = NetworkUtils.findAvailablePort()
        val sessionId = NetworkUtils.generateSessionId()
        val deviceName = NetworkUtils.getDeviceName()

        val info = ConnectionInfo(ip, port, deviceName, sessionId)
        _connectionInfo.value = info
        _qrBitmap.value = QRCodeGenerator.generate(info.toJson(), 600)

        fileReceiver = FileReceiver(context)
        fileReceiver?.startServer(port, sessionId, object : FileReceiver.ReceiveCallback {
            override fun onStateChanged(state: TransferState, message: String) {
                _transferState.value = state
                _statusMessage.value = message
            }
            override fun onProgress(progress: TransferProgress) {
                _progress.value = progress
            }
            override fun onComplete(fileName: String, savedPath: String) {
                _receivedFile.value = Pair(fileName, savedPath)
            }
            override fun onError(error: String) {
                _statusMessage.value = "Error: $error"
            }
        })
    }

    override fun onCleared() {
        fileReceiver?.stop()
        super.onCleared()
    }
}
