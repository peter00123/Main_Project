package com.qrshare.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket

class FileSender(private val context: Context) {

    interface SendCallback {
        fun onStateChanged(state: TransferState, message: String = "")
        fun onProgress(progress: TransferProgress)
        fun onComplete(fileName: String)
        fun onError(error: String)
    }

    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun sendFile(
        connectionInfo: ConnectionInfo,
        fileUri: Uri,
        fileName: String,
        fileSize: Long,
        mimeType: String,
        callback: SendCallback
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.Main) {
                    callback.onStateChanged(TransferState.HANDSHAKING, "Connecting to ${connectionInfo.deviceName}...")
                }

                socket = Socket(connectionInfo.ipAddress, connectionInfo.port)
                val outputStream = socket!!.getOutputStream()
                val inputStream = socket!!.getInputStream()
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Send session verification
                val sessionMsg = "SESSION:${connectionInfo.sessionId}\n"
                outputStream.write(sessionMsg.toByteArray())
                outputStream.flush()

                withContext(Dispatchers.Main) {
                    callback.onStateChanged(TransferState.VERIFYING_QR, "Verifying QR session...")
                }

                val response = reader.readLine()
                if (response != "SESSION_OK") {
                    withContext(Dispatchers.Main) {
                        callback.onError("Session verification failed")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    callback.onStateChanged(TransferState.CONNECTED, "Connected!")
                }

                // Send file metadata
                val metadata = FileMetadata(fileName, fileSize, mimeType)
                val metaMsg = "META:${metadata.toJson()}\n"
                outputStream.write(metaMsg.toByteArray())
                outputStream.flush()

                val metaResponse = reader.readLine()
                if (metaResponse != "READY") {
                    withContext(Dispatchers.Main) { callback.onError("Receiver not ready") }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    callback.onStateChanged(TransferState.TRANSFERRING, "Sending $fileName...")
                }

                // Send file data
                val contentResolver = context.contentResolver
                val fileInputStream = contentResolver.openInputStream(fileUri) ?: run {
                    withContext(Dispatchers.Main) { callback.onError("Cannot read file") }
                    return@launch
                }

                val buffer = ByteArray(8192)
                var totalSent = 0L
                var startTime = System.currentTimeMillis()

                fileInputStream.use { fis ->
                    var bytesRead: Int
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalSent += bytesRead

                        val elapsed = System.currentTimeMillis() - startTime
                        val speed = if (elapsed > 0) (totalSent * 1000) / elapsed else 0

                        withContext(Dispatchers.Main) {
                            callback.onProgress(
                                TransferProgress(fileName, totalSent, fileSize, speed)
                            )
                        }
                    }
                    outputStream.flush()
                }

                val doneResponse = reader.readLine()
                withContext(Dispatchers.Main) {
                    if (doneResponse == "DONE") {
                        callback.onStateChanged(TransferState.COMPLETED, "Transfer complete!")
                        callback.onComplete(fileName)
                    } else {
                        callback.onError("Transfer may have failed")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e.message ?: "Unknown error")
                    callback.onStateChanged(TransferState.FAILED, e.message ?: "Failed")
                }
            }
        }
    }

    fun cancel() {
        scope.cancel()
        try { socket?.close() } catch (_: Exception) {}
    }
}
