// =============================================================================
// FILE: SharePayload.kt
// Package: com.nearbyshare.data.models
// =============================================================================
// INDEX OF CONTENTS:
//   1. PayloadType enum — classifies the kind of data being shared
//   2. SharePayload data class — wraps the content the user wants to send
//   3. Companion factory methods for common payload creation patterns
//
// OBJECTIVE:
//   Represents the content that a user wishes to share with a nearby device.
//   A payload can be a file (photo, video, document, APK), a URL link, or
//   plain text. This model is created by ShareActivity when it receives an
//   implicit Intent.ACTION_SEND from another app, and is passed to the
//   NearbyShareService for transmission.
// =============================================================================

package com.nearbyshare.data.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Describes the kind of data encapsulated in a [SharePayload].
 * The sender and receiver use this to decide how to render a preview
 * and how to handle the incoming data once received.
 */
enum class PayloadType {
    FILE,       // Any binary file (image, video, document, APK, etc.)
    TEXT,       // Plain text snippet
    URL,        // Web link — displayed with a thumbnail if available
    MULTIPLE    // Multiple files bundled in one transfer session
}

/**
 * Wraps the data the user wants to transmit to a nearby peer.
 *
 * @property type         The kind of content (file, text, URL, multiple files).
 * @property uris         List of content URIs for file payloads.
 *                        For TEXT/URL payloads this list is empty.
 * @property text         Raw text or URL string for TEXT/URL payloads.
 * @property displayName  Human-readable name shown in transfer previews.
 *                        For files this is the filename; for text a truncated snippet.
 * @property mimeType     MIME type string (e.g. "image/jpeg", "application/pdf").
 *                        Used to show the correct file-type icon.
 * @property totalBytes   Total size of all payloads combined, in bytes.
 *                        Displayed as "3.2 MB" in the transfer preview card.
 */
@Parcelize
data class SharePayload(
    val type: PayloadType,
    val uris: List<Uri> = emptyList(),
    val text: String = "",
    val displayName: String = "",
    val mimeType: String = "*/*",
    val totalBytes: Long = 0L
) : Parcelable {

    companion object {

        /**
         * Factory: creates a payload for a single file URI.
         *
         * @param uri          Content URI pointing to the file.
         * @param displayName  Filename to show in the UI.
         * @param mimeType     MIME type of the file.
         * @param sizeBytes    File size in bytes.
         */
        fun fromFile(
            uri: Uri,
            displayName: String,
            mimeType: String = "*/*",
            sizeBytes: Long = 0L
        ) = SharePayload(
            type = PayloadType.FILE,
            uris = listOf(uri),
            displayName = displayName,
            mimeType = mimeType,
            totalBytes = sizeBytes
        )

        /**
         * Factory: creates a payload for multiple file URIs.
         *
         * @param uris      List of content URIs.
         * @param totalSize Combined byte size of all files.
         */
        fun fromMultipleFiles(uris: List<Uri>, totalSize: Long = 0L) = SharePayload(
            type = PayloadType.MULTIPLE,
            uris = uris,
            displayName = "${uris.size} files",
            totalBytes = totalSize
        )

        /**
         * Factory: creates a payload wrapping a plain-text string.
         *
         * @param text The text to share (e.g. copied clipboard content).
         */
        fun fromText(text: String) = SharePayload(
            type = PayloadType.TEXT,
            text = text,
            displayName = text.take(50) + if (text.length > 50) "…" else "",
            mimeType = "text/plain"
        )

        /**
         * Factory: creates a payload wrapping a URL.
         *
         * @param url The full URL string to share.
         */
        fun fromUrl(url: String) = SharePayload(
            type = PayloadType.URL,
            text = url,
            displayName = url,
            mimeType = "text/plain"
        )
    }

    /**
     * Formats [totalBytes] as a human-readable string (KB / MB / GB).
     * Returns an empty string for non-file payloads.
     */
    fun formattedSize(): String {
        if (totalBytes <= 0L) return ""
        return when {
            totalBytes < 1_024           -> "${totalBytes} B"
            totalBytes < 1_048_576       -> "${"%.1f".format(totalBytes / 1_024.0)} KB"
            totalBytes < 1_073_741_824   -> "${"%.1f".format(totalBytes / 1_048_576.0)} MB"
            else                         -> "${"%.2f".format(totalBytes / 1_073_741_824.0)} GB"
        }
    }
}
