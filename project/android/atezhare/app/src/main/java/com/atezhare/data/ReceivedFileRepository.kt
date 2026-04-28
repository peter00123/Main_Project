package com.atezhare.data

import android.content.Context
import androidx.lifecycle.LiveData
import java.io.File
import java.io.FileOutputStream

class ReceivedFileRepository(context: Context) {

    private val dao = ReceivedFileDatabase.getInstance(context).receivedFileDao()
    private val receivedDir = File(context.filesDir, "received").also { 
        it.mkdirs() 
        // Create .nomedia file to hide from gallery
        File(it, ".nomedia").createNewFile()
    }

    val allFiles: LiveData<List<ReceivedFile>> = dao.getAllFiles()
    val unviewedCount: LiveData<Int> = dao.getUnviewedCount()

    fun getFilesByType(mimePrefix: String): LiveData<List<ReceivedFile>> =
        dao.getFilesByType(mimePrefix)

    suspend fun saveDownloadedFile(
        fileId: String,
        fileName: String,
        mimeType: String,
        inputStream: java.io.InputStream,
        sessionId: String,
        senderId: String,
        mode: String = "LIVE",
        expiresAt: Long? = null
    ): ReceivedFile {
        val destFile = File(receivedDir, "${fileId}_${fileName}")
        var fileSize = 0L
        
        destFile.outputStream().use { output ->
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                fileSize += bytesRead
            }
        }

        val record = ReceivedFile(
            fileId = fileId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            localPath = destFile.absolutePath,
            sessionId = sessionId,
            senderId = senderId,
            mode = mode,
            expiresAt = expiresAt
        )
        dao.insert(record)
        return record
    }

    suspend fun markViewed(id: Long) = dao.markViewed(id)
    suspend fun delete(file: ReceivedFile) = dao.delete(file)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun getByFileId(fileId: String): ReceivedFile? = dao.getByFileId(fileId)

    suspend fun markDeleted(fileId: String) {
        val record = dao.getByFileId(fileId)

        record?.let {
            val file = File(it.localPath)
            if (file.exists()) {
                file.delete()
            }
        }
        
        dao.markDeleted(fileId)
    }
}
