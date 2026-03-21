package com.atezhare.data

import android.content.Context
import androidx.lifecycle.LiveData
import java.io.File
import java.io.FileOutputStream

class ReceivedFileRepository(context: Context) {

    private val dao = ReceivedFileDatabase.getInstance(context).receivedFileDao()
    private val receivedDir = File(context.filesDir, "received").also { it.mkdirs() }

    val allFiles: LiveData<List<ReceivedFile>> = dao.getAllFiles()
    val unviewedCount: LiveData<Int> = dao.getUnviewedCount()

    fun getFilesByType(mimePrefix: String): LiveData<List<ReceivedFile>> =
        dao.getFilesByType(mimePrefix)

    suspend fun saveDownloadedFile(
        fileId: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        sessionId: String,
        senderId: String
    ): ReceivedFile {
        val destFile = File(receivedDir, "${fileId}_${fileName}")
        FileOutputStream(destFile).use { it.write(bytes) }

        val record = ReceivedFile(
            fileId = fileId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = bytes.size.toLong(),
            localPath = destFile.absolutePath,
            sessionId = sessionId,
            senderId = senderId
        )
        dao.insert(record)
        return record
    }

    suspend fun markViewed(id: Long) = dao.markViewed(id)
    suspend fun delete(file: ReceivedFile) = dao.delete(file)
    suspend fun deleteAll() = dao.deleteAll()
    suspend fun getByFileId(fileId: String): ReceivedFile? = dao.getByFileId(fileId)
}
