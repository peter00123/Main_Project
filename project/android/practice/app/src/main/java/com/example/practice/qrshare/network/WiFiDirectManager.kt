package com.qrshare.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import kotlin.coroutines.resume

/**
 * Manages all Wi-Fi Direct (P2P) operations.
 * Handles group creation, peer connection, and IP resolution.
 */
class WiFiDirectManager(private val context: Context) {

    companion object {
        private const val TAG = "WiFiDirectManager"
        const val DEFAULT_TRANSFER_PORT = 8765
    }

    private val wifiP2pManager: WifiP2pManager by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private var channel: WifiP2pManager.Channel? = null
    private var broadcastReceiver: WiFiDirectBroadcastReceiver? = null
    private var _listener: WiFiDirectListener? = null

    /**
     * Initialize the Wi-Fi Direct channel. Call once in Activity.
     */
    fun initialize(listener: WiFiDirectListener): WifiP2pManager.Channel {
        _listener = listener
        channel = wifiP2pManager.initialize(context, context.mainLooper) {
            Log.w(TAG, "Wi-Fi Direct channel disconnected")
        }
        return channel!!
    }

    fun getManager(): WifiP2pManager = wifiP2pManager
    fun getChannel(): WifiP2pManager.Channel = channel!!

    /**
     * Create a Wi-Fi Direct group (this device becomes the Group Owner / hotspot).
     */
    @SuppressLint("MissingPermission")
    fun createGroup(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ch = channel ?: return onFailure("Channel not initialized")
        wifiP2pManager.createGroup(ch, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Wi-Fi Direct group created")
                onSuccess()
            }
            override fun onFailure(reason: Int) {
                val msg = "Group creation failed: ${reasonToString(reason)}"
                Log.e(TAG, msg)
                onFailure(msg)
            }
        })
    }

    /**
     * Remove the current Wi-Fi Direct group.
     */
    @SuppressLint("MissingPermission")
    fun removeGroup(onDone: () -> Unit = {}) {
        val ch = channel ?: return onDone()
        wifiP2pManager.removeGroup(ch, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { Log.d(TAG, "Group removed"); onDone() }
            override fun onFailure(reason: Int) { Log.w(TAG, "Remove group failed: $reason"); onDone() }
        })
    }

    /**
     * Connect to a peer device by its MAC address.
     */
    @SuppressLint("MissingPermission")
    fun connectToPeer(
        deviceAddress: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val ch = channel ?: return onFailure("Channel not initialized")
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceAddress
        }
        wifiP2pManager.connect(ch, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connect initiated to $deviceAddress")
                onSuccess()
            }
            override fun onFailure(reason: Int) {
                val msg = "Connect failed: ${reasonToString(reason)}"
                Log.e(TAG, msg)
                onFailure(msg)
            }
        })
    }

    /**
     * Disconnect from the current peer / group.
     */
    fun disconnect() {
        removeGroup()
    }

    /**
     * Request connection info (group owner IP, etc).
     */
    @SuppressLint("MissingPermission")
    fun requestConnectionInfo(callback: (WifiP2pInfo) -> Unit) {
        val ch = channel ?: return
        wifiP2pManager.requestConnectionInfo(ch, callback)
    }

    /**
     * Get this device's IP address on the Wi-Fi Direct interface.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (!intf.name.startsWith("p2p") && !intf.name.startsWith("wlan")) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP", e)
        }
        return null
    }

    /**
     * Build the SessionInfo that gets encoded into a QR code.
     * The sender calls this after becoming Group Owner.
     */
    fun buildSessionInfo(deviceName: String): SessionInfo {
        val ip = getLocalIpAddress() ?: "0.0.0.0"
        return SessionInfo(
            hostIp = ip,
            port = DEFAULT_TRANSFER_PORT,
            sessionId = java.util.UUID.randomUUID().toString().take(8).uppercase(),
            deviceName = deviceName
        )
    }

    private fun reasonToString(reason: Int): String = when (reason) {
        WifiP2pManager.ERROR -> "Internal error"
        WifiP2pManager.P2P_UNSUPPORTED -> "P2P unsupported"
        WifiP2pManager.BUSY -> "Framework busy"
        else -> "Unknown ($reason)"
    }
}
