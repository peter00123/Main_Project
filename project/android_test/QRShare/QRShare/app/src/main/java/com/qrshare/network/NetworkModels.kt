package com.qrshare.network

import android.net.Uri

/**
 * Represents a peer device available for connection.
 */
data class PeerDevice(
    val deviceName: String,
    val deviceAddress: String,
    val isGroupOwner: Boolean = false
)

/**
 * Holds all session info encoded/decoded in the QR code.
 * This is what two devices exchange to establish a Wi-Fi Direct + socket connection.
 */
data class SessionInfo(
    val hostIp: String,
    val port: Int,
    val sessionId: String,
    val deviceName: String
) {
    companion object {
        fun fromJson(json: String): SessionInfo? {
            return try {
                val gson = com.google.gson.Gson()
                gson.fromJson(json, SessionInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toJson(): String {
        val gson = com.google.gson.Gson()
        return gson.toJson(this)
    }
}

/**
 * Represents a file queued or being transferred.
 */
data class TransferFile(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String
)

/**
 * Tracks progress of an ongoing file transfer.
 */
data class TransferProgress(
    val fileName: String,
    val totalBytes: Long,
    val transferredBytes: Long,
    val speedBytesPerSec: Long = 0L,
    val state: TransferState = TransferState.PENDING
) {
    val progressPercent: Int
        get() = if (totalBytes > 0) ((transferredBytes * 100) / totalBytes).toInt() else 0
}

enum class TransferState {
    PENDING,
    CONNECTING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Metadata packet sent before actual file bytes over the socket.
 */
data class FileMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val checksum: String = ""
) {
    fun toJson(): String = com.google.gson.Gson().toJson(this)

    companion object {
        fun fromJson(json: String): FileMetadata? = try {
            com.google.gson.Gson().fromJson(json, FileMetadata::class.java)
        } catch (e: Exception) { null }
    }
}

/**
 * Result wrapper for network operations.
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}
