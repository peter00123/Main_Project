package com.qrshare.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.qrshare.R
import com.qrshare.activities.TransferActivity

/**
 * Foreground service that keeps file transfers alive even when the app is backgrounded.
 * Bound service pattern — Activities bind to get direct callbacks.
 */
class FileTransferService : Service() {

    companion object {
        private const val TAG = "FileTransferService"
        private const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "qrshare_transfer_channel"

        const val ACTION_SEND = "com.qrshare.ACTION_SEND"
        const val ACTION_RECEIVE = "com.qrshare.ACTION_RECEIVE"
        const val ACTION_CANCEL = "com.qrshare.ACTION_CANCEL"

        const val EXTRA_HOST_IP = "host_ip"
        const val EXTRA_PORT = "port"
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_DOWNLOAD_PATH = "download_path"
    }

    inner class TransferBinder : Binder() {
        fun getService(): FileTransferService = this@FileTransferService
    }

    private val binder = TransferBinder()
    private var transferListener: TransferServiceListener? = null

    private var socketServer: SocketServer? = null
    private var socketClient: SocketClient? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "FileTransferService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Initializing transfer..."))
        return START_NOT_STICKY
    }

    fun setListener(listener: TransferServiceListener) {
        transferListener = listener
    }

    /**
     * Start a socket server to receive files.
     */
    fun startReceiving(port: Int, downloadDir: java.io.File) {
        updateNotification("Waiting for sender...")
        socketServer = SocketServer(
            port = port,
            downloadDir = downloadDir,
            listener = object : SocketServerListener {
                override fun onServerStarted(port: Int) {
                    transferListener?.onStatusUpdate("Ready to receive on port $port")
                }
                override fun onClientConnected(address: String) {
                    updateNotification("Receiving files...")
                    transferListener?.onStatusUpdate("Connected from $address")
                }
                override fun onFileReceiveStarted(fileName: String, fileSize: Long) {
                    transferListener?.onFileStarted(fileName, fileSize)
                }
                override fun onFileProgress(progress: TransferProgress) {
                    updateNotification("Receiving: ${progress.fileName} (${progress.progressPercent}%)")
                    transferListener?.onProgress(progress)
                }
                override fun onFileReceived(file: java.io.File, metadata: FileMetadata) {
                    transferListener?.onFileComplete(file.absolutePath, metadata.mimeType)
                }
                override fun onAllFilesReceived() {
                    updateNotification("Transfer complete!")
                    transferListener?.onAllComplete()
                    stopForeground(true)
                    stopSelf()
                }
                override fun onError(message: String) {
                    updateNotification("Error: $message")
                    transferListener?.onError(message)
                    stopForeground(true)
                    stopSelf()
                }
            }
        )
        socketServer!!.start()
    }

    /**
     * Start sending files to the given host.
     */
    fun startSending(hostIp: String, port: Int, files: List<TransferFile>) {
        updateNotification("Connecting to receiver...")
        socketClient = SocketClient(
            context = this,
            hostIp = hostIp,
            port = port,
            listener = object : SocketClientListener {
                override fun onConnecting() {
                    transferListener?.onStatusUpdate("Connecting...")
                }
                override fun onConnected() {
                    updateNotification("Sending files...")
                    transferListener?.onStatusUpdate("Connected!")
                }
                override fun onFileSendStarted(fileName: String, fileSize: Long) {
                    transferListener?.onFileStarted(fileName, fileSize)
                }
                override fun onFileProgress(progress: TransferProgress) {
                    updateNotification("Sending: ${progress.fileName} (${progress.progressPercent}%)")
                    transferListener?.onProgress(progress)
                }
                override fun onFileSent(file: TransferFile) {
                    transferListener?.onFileComplete(file.name, file.mimeType)
                }
                override fun onAllFilesSent() {
                    updateNotification("All files sent!")
                    transferListener?.onAllComplete()
                    stopForeground(true)
                    stopSelf()
                }
                override fun onError(message: String) {
                    updateNotification("Error: $message")
                    transferListener?.onError(message)
                    stopForeground(true)
                    stopSelf()
                }
            }
        )
        socketClient!!.sendFiles(files)
    }

    fun cancelTransfer() {
        socketClient?.cancel()
        socketServer?.stop()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketServer?.stop()
        socketClient?.cancel()
        Log.d(TAG, "FileTransferService destroyed")
    }

    // ---- Notification helpers ----

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Transfers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of QRShare file transfers"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("QRShare")
        .setContentText(text)
        .setSmallIcon(android.R.drawable.ic_menu_share)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, TransferActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }
}

/**
 * Listener for high-level transfer events — implemented by TransferActivity.
 */
interface TransferServiceListener {
    fun onStatusUpdate(message: String)
    fun onFileStarted(fileName: String, fileSize: Long)
    fun onProgress(progress: TransferProgress)
    fun onFileComplete(filePath: String, mimeType: String)
    fun onAllComplete()
    fun onError(message: String)
}
