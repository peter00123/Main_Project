package com.atezhare.util

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WifiDirectManager(private val context: Context) {

    companion object {
        private const val TAG = "WifiDirectManager"
        const val TRANSFER_PORT = 8988
        private const val BUFFER_SIZE = 8192
    }

    private val manager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel: WifiP2pManager.Channel =
        manager.initialize(context, Looper.getMainLooper(), null)

    var onConnectionInfoAvailable: ((WifiP2pInfo) -> Unit)? = null
    var onPeersAvailable: ((List<WifiP2pDevice>) -> Unit)? = null

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel) { peers ->
                        onPeersAvailable?.invoke(peers.deviceList.toList())
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    manager.requestConnectionInfo(channel) { info ->
                        onConnectionInfoAvailable?.invoke(info)
                    }
                }
            }
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w(TAG, "Receiver already unregistered", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer discovery started")
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "Peer discovery failed: $reason")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceAddress: String) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection initiated to $deviceAddress")
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "Connection failed: $reason")
            }
        })
    }

    fun sendFiles(
        host: String,
        port: Int,
        fileUris: List<Uri>,
        onProgress: ((Int) -> Unit)? = null
    ) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 5000)
            val outputStream = DataOutputStream(socket.getOutputStream())

            outputStream.writeInt(fileUris.size)

            for ((index, uri) in fileUris.withIndex()) {
                val fileName = FileUtils.getFileName(context, uri)
                val fileSize = FileUtils.getFileSize(context, uri)

                outputStream.writeUTF(fileName)
                outputStream.writeLong(fileSize)

                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.let { stream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    var totalSent = 0L

                    while (stream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalSent += bytesRead
                    }
                    stream.close()
                }

                val progress = ((index + 1) * 100) / fileUris.size
                onProgress?.invoke(progress)
            }

            outputStream.flush()
            socket.close()
            Log.d(TAG, "All files sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending files", e)
        }
    }

    fun receiveFiles(
        onFileReceived: ((String) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        try {
            val serverSocket = ServerSocket(TRANSFER_PORT)
            serverSocket.soTimeout = 60000 
            val client = serverSocket.accept()

            val inputStream = DataInputStream(client.getInputStream())

            val fileCount = inputStream.readInt()

            for (i in 0 until fileCount) {
                val fileName = inputStream.readUTF()
                val fileSize = inputStream.readLong()

                val downloadsDir = context.getExternalFilesDir("received")
                downloadsDir?.mkdirs()
                val outFile = File(downloadsDir, fileName)
                val fileOutputStream = FileOutputStream(outFile)

                val buffer = ByteArray(BUFFER_SIZE)
                var remaining = fileSize
                while (remaining > 0) {
                    val toRead = minOf(BUFFER_SIZE.toLong(), remaining).toInt()
                    val bytesRead = inputStream.read(buffer, 0, toRead)
                    if (bytesRead == -1) break
                    fileOutputStream.write(buffer, 0, bytesRead)
                    remaining -= bytesRead
                }

                fileOutputStream.close()
                onFileReceived?.invoke(fileName)
                Log.d(TAG, "Received file: $fileName")
            }

            client.close()
            serverSocket.close()
            onComplete?.invoke()
            Log.d(TAG, "All files received successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving files", e)
        }
    }

    fun disconnect() {
        manager.removeGroup(channel, null)
    }
}
