package com.qrshare.network

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * TCP client that connects to SocketServer and sends files.
 * Protocol mirror of SocketServer — sends JSON metadata line then raw bytes per file,
 * followed by END_SESSION.
 */
class SocketClient(
    private val context: Context,
    private val hostIp: String,
    private val port: Int,
    private val listener: SocketClientListener
) {
    companion object {
        private const val TAG = "SocketClient"
        private const val END_SESSION = "END_SESSION"
        private const val BUFFER_SIZE = 65536
        private const val CONNECT_TIMEOUT_MS = 15000
    }

    private var clientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Send a list of files to the server. Runs on IO dispatcher.
     */
    fun sendFiles(files: List<TransferFile>) {
        clientJob = scope.launch {
            var socket: Socket? = null
            try {
                Log.d(TAG, "Connecting to $hostIp:$port")
                listener.onConnecting()

                socket = Socket()
                socket.connect(java.net.InetSocketAddress(hostIp, port), CONNECT_TIMEOUT_MS)
                Log.d(TAG, "Connected to server")
                listener.onConnected()

                val outputStream = socket.getOutputStream()
                val writer = outputStream.bufferedWriter(Charsets.UTF_8)

                for (file in files) {
                    sendSingleFile(context, file, outputStream, writer)
                }

                // Signal end of session
                writer.write("$END_SESSION\n")
                writer.flush()
                Log.d(TAG, "All files sent, session ended")
                listener.onAllFilesSent()

            } catch (e: Exception) {
                Log.e(TAG, "Send error", e)
                listener.onError("Send failed: ${e.message}")
            } finally {
                try { socket?.close() } catch (_: Exception) {}
            }
        }
    }

    private fun sendSingleFile(
        context: Context,
        file: TransferFile,
        outputStream: OutputStream,
        writer: java.io.BufferedWriter
    ) {
        val inputStream: InputStream = context.contentResolver.openInputStream(file.uri)
            ?: throw IllegalStateException("Cannot open file: ${file.name}")

        val metadata = FileMetadata(
            fileName = file.name,
            fileSize = file.size,
            mimeType = file.mimeType
        )

        Log.d(TAG, "Sending: ${file.name} (${file.size} bytes)")
        listener.onFileSendStarted(file.name, file.size)

        // Send metadata header line
        writer.write("${metadata.toJson()}\n")
        writer.flush()

        // Send file bytes
        val buffer = ByteArray(BUFFER_SIZE)
        var sentBytes = 0L
        val startTime = System.currentTimeMillis()

        try {
            inputStream.use { stream ->
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    sentBytes += bytesRead

                    val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                    val speed = if (elapsed > 0) (sentBytes / elapsed).toLong() else 0L

                    listener.onFileProgress(
                        TransferProgress(
                            fileName = file.name,
                            totalBytes = file.size,
                            transferredBytes = sentBytes,
                            speedBytesPerSec = speed,
                            state = TransferState.IN_PROGRESS
                        )
                    )
                }
                outputStream.flush()
            }

            Log.d(TAG, "File sent: ${file.name}")
            listener.onFileSent(file)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending file ${file.name}", e)
            throw e
        }
    }

    fun cancel() {
        clientJob?.cancel()
    }
}

/**
 * Callbacks for SocketClient events.
 */
interface SocketClientListener {
    fun onConnecting()
    fun onConnected()
    fun onFileSendStarted(fileName: String, fileSize: Long)
    fun onFileProgress(progress: TransferProgress)
    fun onFileSent(file: TransferFile)
    fun onAllFilesSent()
    fun onError(message: String)
}
