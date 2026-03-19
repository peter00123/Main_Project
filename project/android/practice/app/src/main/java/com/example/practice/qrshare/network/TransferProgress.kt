package com.qrshare.network

data class TransferProgress(
    val fileName: String,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long
) {
    val progressPercent: Int
        get() = if (totalBytes > 0) ((bytesTransferred * 100) / totalBytes).toInt() else 0

    val speedMbps: String
        get() {
            val mbps = speedBytesPerSec / (1024.0 * 1024.0)
            return String.format("%.1f MB/s", mbps)
        }

    val formattedSize: String
        get() {
            val mb = totalBytes / (1024.0 * 1024.0)
            return if (mb >= 1) String.format("%.1f MB", mb)
            else String.format("%.0f KB", totalBytes / 1024.0)
        }
}
