package com.atezhare.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.atezhare.R
import com.atezhare.data.LibrarianRepository
import com.atezhare.data.ReceivedFileRepository
import com.atezhare.data.TransferProgress
import com.atezhare.network.RetrofitClient
import com.atezhare.utils.FileUtils
import com.atezhare.utils.TransferProgressManager
import kotlinx.coroutines.*
import java.io.File

class TransferService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val CHANNEL_ID = "transfer_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_SENDER_ID = "EXTRA_SENDER_ID"
        const val EXTRA_FILE_IDS = "EXTRA_FILE_IDS"

        fun startDownload(context: Context, sessionId: String, senderId: String, fileIds: ArrayList<String>) {
            val intent = Intent(context, TransferService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_SENDER_ID, senderId)
                putStringArrayListExtra(EXTRA_FILE_IDS, fileIds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_DOWNLOAD) {
            val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: ""
            val senderId = intent.getStringExtra(EXTRA_SENDER_ID) ?: ""
            val fileIds = intent.getStringArrayListExtra(EXTRA_FILE_IDS) ?: arrayListOf()

            val notification = createNotification("Preparing download...")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            
            serviceScope.launch {
                downloadFiles(sessionId, senderId, fileIds)
                ServiceCompat.stopForeground(this@TransferService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun downloadFiles(sid: String, senderId: String, fileIds: List<String>) {
        val repository = ReceivedFileRepository(applicationContext)
        val librarian = LibrarianRepository(applicationContext)

        for (fileId in fileIds) {
            try {
                if (repository.getByFileId(fileId) != null) continue

                val response = RetrofitClient.apiService.downloadFile(fileId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val contentDisposition = response.headers()["Content-Disposition"] ?: ""
                    val fileName = Regex("""filename="?([^";\n]+)"?""")
                        .find(contentDisposition)?.groupValues?.get(1) ?: "file_$fileId"
                    val mimeType = response.headers()["Content-Type"] ?: "application/octet-stream"
                    val totalSize = body.contentLength()

                    val destFile = File(applicationContext.filesDir, "received/${fileId}_${fileName}")
                    destFile.parentFile?.mkdirs()

                    var bytesCopied = 0L
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    val startTime = System.currentTimeMillis()

                    destFile.outputStream().use { output ->
                        val inputStream = body.byteStream()
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesCopied += bytesRead

                            val percent = if (totalSize > 0) (bytesCopied * 100 / totalSize).toInt() else 0
                            val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                            val speed = if (elapsedSeconds > 0) {
                                FileUtils.formatFileSize((bytesCopied / elapsedSeconds).toLong()) + "/s"
                            } else "0 B/s"

                            val progress = TransferProgress(fileName, percent, speed, true)
                            TransferProgressManager.updateProgress(progress)
                            updateNotification("Downloading $fileName", percent)
                        }
                    }

                    repository.saveDownloadedFileRecord(
                        fileId = fileId,
                        fileName = fileName,
                        mimeType = mimeType,
                        fileSize = bytesCopied,
                        localPath = destFile.absolutePath,
                        sessionId = sid,
                        senderId = senderId
                    )
                    
                    librarian.registerFile(fileName, destFile.absolutePath, fileId)
                }
            } catch (e: Exception) {
                Log.e("TransferService", "Download failed for $fileId", e)
            }
        }
        TransferProgressManager.updateProgress(null)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "File Transfers",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String, progress: Int = 0): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Atezhare Transfer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_receive_file)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String, progress: Int) {
        val notification = createNotification(content, progress)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
