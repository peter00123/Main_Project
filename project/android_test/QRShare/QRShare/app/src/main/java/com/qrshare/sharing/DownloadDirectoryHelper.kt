package com.qrshare.sharing

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Manages the directory where received files are saved.
 * Uses the public Downloads/QRShare folder so files are accessible after the transfer.
 */
object DownloadDirectoryHelper {

    private const val APP_FOLDER = "QRShare"

    /**
     * Get (and create if needed) the download directory for received files.
     */
    fun getDownloadDirectory(context: Context): File {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloads, APP_FOLDER)
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        return appDir
    }

    /**
     * Get a unique file path in the download directory, appending a counter if needed.
     */
    fun getUniqueFilePath(directory: File, fileName: String): File {
        var file = File(directory, fileName)
        if (!file.exists()) return file

        val nameWithoutExt = fileName.substringBeforeLast('.')
        val ext = if (fileName.contains('.')) ".${fileName.substringAfterLast('.')}" else ""
        var counter = 1
        while (file.exists()) {
            file = File(directory, "${nameWithoutExt}_$counter$ext")
            counter++
        }
        return file
    }

    /**
     * List all received files in the download directory.
     */
    fun listReceivedFiles(context: Context): List<File> {
        val dir = getDownloadDirectory(context)
        return dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
