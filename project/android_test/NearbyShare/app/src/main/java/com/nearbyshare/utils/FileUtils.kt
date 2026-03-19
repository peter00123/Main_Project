// =============================================================================
// FILE: FileUtils.kt
// Package: com.nearbyshare.utils
// =============================================================================
// INDEX OF CONTENTS:
//   1. getMimeType() — resolves MIME type from URI or file extension
//   2. getFileName() — extracts display name from a content URI
//   3. getFileSize() — retrieves file byte size from ContentResolver
//   4. createReceiveFile() — creates a target file in the Downloads directory
//   5. formatFileSize() — converts bytes to human-readable string
//   6. getFileTypeIcon() — returns a drawable res ID for a given MIME type
//
// OBJECTIVE:
//   Shared file-system utilities used by ShareActivity (to inspect outgoing
//   files), NearbyShareService (to write received files to disk), and the
//   UI adapters (to choose type-appropriate icons).
//   All functions handle API-level differences (scoped storage on API 29+,
//   MediaStore vs ContentResolver vs File API) in one place.
// =============================================================================

package com.nearbyshare.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.nearbyshare.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    // ============================================================
    // MIME Type Resolution
    // ============================================================

    /**
     * Resolves the MIME type of a content URI.
     * First tries ContentResolver (most accurate for content:// URIs),
     * then falls back to the file extension.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to inspect.
     * @return MIME type string (e.g. "image/jpeg"), or *" as fallback.
     */
    fun getMimeType(context: Context, uri: Uri): String {
        // ContentResolver gives accurate type for MediaStore and FileProvider URIs
        val fromResolver = context.contentResolver.getType(uri)
        if (!fromResolver.isNullOrEmpty()) return fromResolver

        // Fall back to extension-based lookup
        val path = uri.lastPathSegment ?: return "*/*"
        val ext  = path.substringAfterLast(".", "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
               ?: "*/*"
    }

    // ============================================================
    // File Name
    // ============================================================

    /**
     * Retrieves the display name of a file from its content URI.
     * Queries OpenableColumns.DISPLAY_NAME via ContentResolver.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to query.
     * @return The filename string, or a generic fallback.
     */
    fun getFileName(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                val col = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) cursor.getString(col) else "file"
            } ?: uri.lastPathSegment ?: "file"
        } catch (e: Exception) {
            uri.lastPathSegment ?: "file"
        }
    }

    // ============================================================
    // File Size
    // ============================================================

    /**
     * Returns the byte size of a file pointed to by a content URI.
     *
     * @param context  Any context for ContentResolver access.
     * @param uri      The content URI to query.
     * @return File size in bytes, or 0 if not determinable.
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.SIZE), null, null, null
            )?.use { cursor ->
                val col = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) cursor.getLong(col) else 0L
            } ?: 0L
        } catch (e: Exception) { 0L }
    }

    // ============================================================
    // Receive File Creation
    // ============================================================

    /**
     * Creates a File object in the appropriate Downloads directory for
     * storing a received file. On API 29+ uses the scoped
     * Environment.DIRECTORY_DOWNLOADS; on older devices uses the
     * public Downloads folder.
     *
     * @param context      Any context (for getExternalFilesDir fallback).
     * @param fileName     Desired filename (sanitised for safety).
     * @return A File object pointing to the target path (not yet written).
     */
    fun createReceiveFile(context: Context, fileName: String): File {
        // Sanitise the filename to remove path traversal characters
        val safe = fileName.replace(Regex("[/\\\\:*?\"<>|]"), "_")

        val dir: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+: use scoped external files dir (no permission needed)
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "NearbyShare"
            )
        } else {
            // API < 29: use public Downloads (requires WRITE_EXTERNAL_STORAGE)
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "NearbyShare"
            )
        }

        dir.mkdirs() // Create the directory if it doesn't exist

        // If a file with that name already exists, append a timestamp
        return if (File(dir, safe).exists()) {
            val ts = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
            val nameWithoutExt = safe.substringBeforeLast(".")
            val ext = safe.substringAfterLast(".", "")
            File(dir, "${nameWithoutExt}_${ts}${if (ext.isNotEmpty()) ".$ext" else ""}")
        } else {
            File(dir, safe)
        }
    }

    // ============================================================
    // Formatting
    // ============================================================

    /**
     * Converts a raw byte count into a human-readable file size string.
     *
     * Examples:
     *   512          → "512 B"
     *   1_536        → "1.5 KB"
     *   3_145_728    → "3.0 MB"
     *   1_073_741_824 → "1.00 GB"
     *
     * @param bytes File size in bytes.
     * @return Formatted string with appropriate unit.
     */
    fun formatFileSize(bytes: Long): String = when {
        bytes < 1_024L           -> "$bytes B"
        bytes < 1_048_576L       -> "${"%.1f".format(bytes / 1_024.0)} KB"
        bytes < 1_073_741_824L   -> "${"%.1f".format(bytes / 1_048_576.0)} MB"
        else                     -> "${"%.2f".format(bytes / 1_073_741_824.0)} GB"
    }

    // ============================================================
    // File Type Icon
    // ============================================================

    /**
     * Maps a MIME type string to an appropriate drawable resource ID.
     * Used by the UI to show a contextual icon next to file names.
     *
     * @param mimeType MIME type string (e.g. "image/jpeg", "application/pdf").
     * @return A drawable resource ID from R.drawable.
     */
    fun getFileTypeIcon(mimeType: String): Int = when {
        mimeType.startsWith("image/")       -> R.drawable.ic_file_image
        mimeType.startsWith("video/")       -> R.drawable.ic_file_video
        mimeType.startsWith("audio/")       -> R.drawable.ic_file_audio
        mimeType == "application/pdf"       -> R.drawable.ic_file_pdf
        mimeType.contains("zip") ||
        mimeType.contains("compressed")     -> R.drawable.ic_file_archive
        mimeType.startsWith("text/")        -> R.drawable.ic_file_text
        mimeType == "application/vnd.android.package-archive"
                                            -> R.drawable.ic_file_apk
        else                                -> R.drawable.ic_file_generic
    }
}
