package com.qrshare.sharing

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.qrshare.network.TransferFile

/**
 * Utility for resolving content URIs to TransferFile metadata.
 * Handles both content:// and file:// URIs.
 */
object FilePickerHelper {

    /**
     * Convert a list of content URIs into TransferFile objects with full metadata.
     */
    fun resolveTransferFiles(context: Context, uris: List<Uri>): List<TransferFile> {
        return uris.mapNotNull { uri -> resolveUri(context, uri) }
    }

    fun resolveUri(context: Context, uri: Uri): TransferFile? {
        return try {
            var fileName = "unknown_file"
            var fileSize = 0L

            // Query content resolver for display name and size
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null, null, null
            )?.use { cursor: Cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: "unknown"
                    if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Fallback size via openAssetFileDescriptor
            if (fileSize == 0L) {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                    fileSize = it.length
                }
            }

            val mimeType = context.contentResolver.getType(uri)
                ?: guessMimeType(fileName)
                ?: "application/octet-stream"

            TransferFile(
                uri = uri,
                name = sanitizeFileName(fileName),
                size = fileSize,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun guessMimeType(fileName: String): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return if (ext.isNotEmpty()) MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) else null
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[/\\\\:*?\"<>|]"), "_")
    }

    /**
     * Format bytes to a human-readable string.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }

    /**
     * Format transfer speed to a human-readable string.
     */
    fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> "%.0f KB/s".format(bytesPerSec / 1024.0)
            else -> "%.1f MB/s".format(bytesPerSec / (1024.0 * 1024))
        }
    }

    /**
     * Estimate remaining time given current speed and remaining bytes.
     */
    fun formatETA(remainingBytes: Long, speedBytesPerSec: Long): String {
        if (speedBytesPerSec <= 0) return "calculating..."
        val seconds = remainingBytes / speedBytesPerSec
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
}
