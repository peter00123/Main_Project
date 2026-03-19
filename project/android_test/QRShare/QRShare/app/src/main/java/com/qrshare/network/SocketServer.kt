package com.qrshare.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * TCP server that listens for incoming file data from a sender.
 * Each file is preceded by a JSON metadata line terminated with '\n'.
 * Protocol:
 *   1. Client connects
 *   2. Client sends: <JSON FileMetadata>\n
 *   3. Client sends: <raw file bytes of size FileMetadata.fileSize>
 *   4. Steps 2-3 repeat for each additional file
 *   5. Client sends: END_SESSION\n  to signal completion
 */
class SocketServer(
    private val port: Int,
    private val downloadDir: File,
    private val listener: SocketServerListener
) {
    companion object {
        private const val TAG = "SocketServer"
        private const val END_SESSION = "END_SESSION"
        private const val BUFFER_SIZE = 65536 // 64 KB
    }

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Start listening on the given port in a background coroutine.
     */
    fun start() {
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                Log.d(TAG, "Server listening on port $port")
                listener.onServerStarted(port)

                val clientSocket: Socket = serverSocket!!.accept()
                Log.d(TAG, "Client connected: ${clientSocket.inetAddress.hostAddress}")
                listener.onClientConnected(clientSocket.inetAddress.hostAddress ?: "unknown")

                handleClient(clientSocket)

            } catch (e: Exception) {
                if (serverSocket?.isClosed == false) {
                    Log.e(TAG, "Server error", e)
                    listener.onError("Server error: ${e.message}")
                }
            } finally {
                stop()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val inputStream = socket.getInputStream()
            val reader = inputStream.bufferedReader(Charsets.UTF_8)

            while (true) {
                val headerLine = reader.readLine() ?: break
                if (headerLine.trim() == END_SESSION) {
                    Log.d(TAG, "Session ended by sender")
                    listener.onAllFilesReceived()
                    break
                }

                val metadata = FileMetadata.fromJson(headerLine)
                if (metadata == null) {
                    Log.e(TAG, "Invalid metadata: $headerLine")
                    listener.onError("Invalid file metadata received")
                    break
                }

                Log.d(TAG, "Receiving file: ${metadata.fileName} (${metadata.fileSize} bytes)")
                receiveFile(inputStream, metadata)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client", e)
            listener.onError("Transfer error: ${e.message}")
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun receiveFile(inputStream: InputStream, metadata: FileMetadata) {
        val destFile = File(downloadDir, metadata.fileName)
        var receivedBytes = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        val startTime = System.currentTimeMillis()

        listener.onFileReceiveStarted(metadata.fileName, metadata.fileSize)

        try {
            FileOutputStream(destFile).use { fos ->
                while (receivedBytes < metadata.fileSize) {
                    val toRead = minOf(BUFFER_SIZE.toLong(), metadata.fileSize - receivedBytes).toInt()
                    val bytesRead = inputStream.read(buffer, 0, toRead)
                    if (bytesRead == -1) break

                    fos.write(buffer, 0, bytesRead)
                    receivedBytes += bytesRead

                    val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
                    val speed = if (elapsed > 0) (receivedBytes / elapsed).toLong() else 0L

                    listener.onFileProgress(
                        TransferProgress(
                            fileName = metadata.fileName,
                            totalBytes = metadata.fileSize,
                            transferredBytes = receivedBytes,
                            speedBytesPerSec = speed,
                            state = TransferState.IN_PROGRESS
                        )
                    )
                }
            }

            if (receivedBytes == metadata.fileSize) {
                Log.d(TAG, "File received successfully: ${metadata.fileName}")
                listener.onFileReceived(destFile, metadata)
            } else {
                Log.e(TAG, "File incomplete: received $receivedBytes of ${metadata.fileSize}")
                destFile.delete()
                listener.onError("Incomplete file: ${metadata.fileName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing file", e)
            destFile.delete()
            listener.onError("Write error: ${e.message}")
        }
    }

    fun stop() {
        try {
            serverSocket?.close()
            serverJob?.cancel()
            Log.d(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }
    }
}

/**
 * Callbacks for SocketServer events.
 */
interface SocketServerListener {
    fun onServerStarted(port: Int)
    fun onClientConnected(address: String)
    fun onFileReceiveStarted(fileName: String, fileSize: Long)
    fun onFileProgress(progress: TransferProgress)
    fun onFileReceived(file: File, metadata: FileMetadata)
    fun onAllFilesReceived()
    fun onError(message: String)
}
