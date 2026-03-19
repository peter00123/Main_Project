package com.qrshare.network

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class FileReceiver(private val context: Context) {

    interface ReceiveCallback {
        fun onStateChanged(state: TransferState, message: String = "")
        fun onProgress(progress: TransferProgress)
        fun onComplete(fileName: String, savedPath: String)
        fun onError(error: String)
    }

    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var expectedSessionId: String = ""

    fun startServer(port: Int, sessionId: String, callback: ReceiveCallback) {
        expectedSessionId = sessionId
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                withContext(Dispatchers.Main) {
                    callback.onStateChanged(TransferState.WAITING_FOR_CONNECTION, "Waiting for sender...")
                }

                val clientSocket: Socket = serverSocket!!.accept()
                handleClient(clientSocket, callback)

            } catch (e: Exception) {
                if (e.message?.contains("closed") != true) {
                    withContext(Dispatchers.Main) {
                        callback.onError(e.message ?: "Server error")
                        callback.onStateChanged(TransferState.FAILED, e.message ?: "Failed")
                    }
                }
            }
        }
    }

    private suspend fun handleClient(socket: Socket, callback: ReceiveCallback) {
        try {
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Verify session
            withContext(Dispatchers.Main) {
                callback.onStateChanged(TransferState.VERIFYING_QR, "Verifying QR session...")
            }

            val sessionLine = reader.readLine()
            if (!sessionLine.startsWith("SESSION:")) {
                outputStream.write("SESSION_FAIL\n".toByteArray())
                outputStream.flush()
                withContext(Dispatchers.Main) { callback.onError("Invalid handshake") }
                return
            }

            val receivedSession = sessionLine.substringAfter("SESSION:")
            if (receivedSession != expectedSessionId) {
                outputStream.write("SESSION_FAIL\n".toByteArray())
                outputStream.flush()
                withContext(Dispatchers.Main) { callback.onError("Session mismatch") }
                return
            }

            outputStream.write("SESSION_OK\n".toByteArray())
            outputStream.flush()

            withContext(Dispatchers.Main) {
                callback.onStateChanged(TransferState.CONNECTED, "Connected!")
            }

            // Receive metadata
            val metaLine = reader.readLine()
            if (!metaLine.startsWith("META:")) {
                withContext(Dispatchers.Main) { callback.onError("No metadata received") }
                return
            }

            val metadata = FileMetadata.fromJson(metaLine.substringAfter("META:"))
            outputStream.write("READY\n".toByteArray())
            outputStream.flush()

            withContext(Dispatchers.Main) {
                callback.onStateChanged(TransferState.TRANSFERRING, "Receiving ${metadata.fileName}...")
            }

            // Receive file data
            val savedPath = saveFileToDownloads(metadata, inputStream, metadata.fileSize, callback)

            outputStream.write("DONE\n".toByteArray())
            outputStream.flush()

            withContext(Dispatchers.Main) {
                callback.onStateChanged(TransferState.COMPLETED, "Transfer complete!")
                callback.onComplete(metadata.fileName, savedPath)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback.onError(e.message ?: "Receive error")
                callback.onStateChanged(TransferState.FAILED, e.message ?: "Failed")
            }
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private suspend fun saveFileToDownloads(
        metadata: FileMetadata,
        inputStream: InputStream,
        fileSize: Long,
        callback: ReceiveCallback
    ): String {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, metadata.fileName)
            put(MediaStore.Downloads.MIME_TYPE, metadata.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRShare")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IOException("Failed to create file entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            val buffer = ByteArray(8192)
            var totalReceived = 0L
            val startTime = System.currentTimeMillis()

            while (totalReceived < fileSize) {
                val toRead = minOf(buffer.size.toLong(), fileSize - totalReceived).toInt()
                val bytesRead = inputStream.read(buffer, 0, toRead)
                if (bytesRead == -1) break

                outputStream.write(buffer, 0, bytesRead)
                totalReceived += bytesRead

                val elapsed = System.currentTimeMillis() - startTime
                val speed = if (elapsed > 0) (totalReceived * 1000) / elapsed else 0

                withContext(Dispatchers.Main) {
                    callback.onProgress(
                        TransferProgress(metadata.fileName, totalReceived, fileSize, speed)
                    )
                }
            }
        } ?: throw IOException("Failed to open output stream")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return "Downloads/QRShare/${metadata.fileName}"
    }

    fun stop() {
        scope.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
    }
}
