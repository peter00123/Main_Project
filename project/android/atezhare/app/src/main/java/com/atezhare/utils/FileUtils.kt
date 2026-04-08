// utils/FileUtils.kt
// Utility functions for resolving file URIs to LocalFile objects,
// and preparing MultipartBody.Part lists for Retrofit upload.
// Used by: ui/directory/DirectoryViewModel (resolve picked files),
//          ui/send/SendViewModel (prepare upload payload)

package com.atezhare.utils

import android.content.Context
import com.atezhare.utils.FileNameEncoder
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.atezhare.model.LocalFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Converts a content URI to a LocalFile model.
     * Copies the file to app cache so it can be read by OkHttp.
     * Used by DirectoryViewModel when user picks a file.
     */
    fun uriToLocalFile(context: Context, uri: Uri): LocalFile? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE) ?: -1
            cursor?.moveToFirst()
            val name = cursor?.getString(nameIndex) ?: "unknown_file"
            val size = cursor?.getLong(sizeIndex) ?: 0L
            cursor?.close()

            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

            // Copy to cache so OkHttp can read it as a File
            val cacheFile = File(context.cacheDir, name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }

            LocalFile(
                name = name,
                path = cacheFile.absolutePath,
                size = size,
                mimeType = mimeType,
                isChecked = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts a list of LocalFile models into Retrofit MultipartBody.Part list.
     * Used by SendViewModel.uploadFiles() before calling ApiService.uploadFiles()
     */
    fun localFilesToMultipart(files: List<LocalFile>): List<MultipartBody.Part> {
        return files
            .filter { it.isChecked }
            .mapNotNull { localFile ->
                try {
                    val file = File(localFile.path)
                    if (!file.exists()) return@mapNotNull null
                    val mediaType = localFile.mimeType.toMediaTypeOrNull()
                    val requestBody = file.asRequestBody(mediaType)
                    MultipartBody.Part.createFormData("files", file.name, requestBody)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
    }

    /** Returns a human-readable file size string */
    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }

    /**
     * Renames a LocalFile using the Atezhare encoded format before upload.
     *
     * Called by SendViewModel.confirmAndUpload() before building MultipartBody parts.
     *
     * Steps:
     *   1. Generates encoded filename using FileNameEncoder.encode()
     *   2. Copies the cached file to a new cached file with the encoded name
     *   3. Returns a new LocalFile pointing to the renamed cached copy
     *
     * @param context      For cacheDir access
     * @param localFile    The original LocalFile picked by user
     * @param expiresAtMillis  0L for LIVE (timer=00.00.00), future ms for COUNTDOWN
     * @return A new LocalFile with the encoded filename and path
     */
    fun renameForSending(context: Context, localFile: LocalFile, expiresAtMillis: Long): LocalFile {
        val encodedName = FileNameEncoder.encode(localFile.name, expiresAtMillis)
        val originalFile = File(localFile.path)
        val renamedFile = File(context.cacheDir, encodedName)

        // Copy the cached file to the new encoded name
        originalFile.copyTo(renamedFile, overwrite = true)

        return localFile.copy(
            name = encodedName,
            path = renamedFile.absolutePath
        )
    }
}
