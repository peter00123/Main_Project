package com.atezhare.data

import android.content.Context
import androidx.lifecycle.LiveData

class SentFileRepository(context: Context) {
    private val dao = ReceivedFileDatabase.getInstance(context).sentFileDao()

    fun getActiveSentFiles(): LiveData<List<SentFile>> = dao.getActiveSentFiles()

    suspend fun getActiveSentFilesSync(): List<SentFile> = dao.getActiveSentFilesSync()

    suspend fun saveSentFile(
        fileId: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        localPath: String,               // ADDED
        sessionId: String,
        receiverId: String,
        mode: String,
        expiresAt: Long?
    ) {
        val sentFile = SentFile(
            fileId = fileId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            localPath = localPath,       // ADDED
            sessionId = sessionId,
            receiverId = receiverId,
            sentAt = System.currentTimeMillis(),
            mode = mode,
            expiresAt = expiresAt,
            isActive = true
        )
        dao.insert(sentFile)
    }

    suspend fun markInactive(fileId: String) {
        dao.markInactive(fileId)
    }

    suspend fun getByFileId(fileId: String): SentFile? {
        return dao.getByFileId(fileId)
    }
}
