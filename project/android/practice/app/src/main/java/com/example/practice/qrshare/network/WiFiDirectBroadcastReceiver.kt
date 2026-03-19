package com.qrshare.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

/**
 * Listens for Wi-Fi Direct system broadcasts and relays them to the manager listener.
 * Register/unregister this in onResume/onPause of your Activity.
 */
class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val listener: WiFiDirectListener
) : BroadcastReceiver() {

    companion object {
        private const val TAG = "WiFiDirectReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                val isEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                Log.d(TAG, "Wi-Fi Direct enabled: $isEnabled")
                listener.onWifiDirectStateChanged(isEnabled)
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.d(TAG, "Peers changed")
                manager.requestPeers(channel) { peerList ->
                    val peers = peerList.deviceList.map { device ->
                        PeerDevice(
                            deviceName = device.deviceName,
                            deviceAddress = device.deviceAddress
                        )
                    }
                    listener.onPeersChanged(peers)
                }
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_STATE_CHANGE_ACTION -> {
                val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(
                    WifiP2pManager.EXTRA_NETWORK_INFO
                )
                Log.d(TAG, "Connection state: ${networkInfo?.isConnected}")
                if (networkInfo?.isConnected == true) {
                    manager.requestConnectionInfo(channel) { info ->
                        listener.onConnectionInfoAvailable(info)
                    }
                } else {
                    listener.onDisconnected()
                }
            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device = intent.getParcelableExtra<WifiP2pDevice>(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                )
                Log.d(TAG, "This device changed: ${device?.deviceName}")
                device?.let { listener.onThisDeviceChanged(it) }
            }
        }
    }
}

/**
 * Callback interface for Wi-Fi Direct events.
 */
interface WiFiDirectListener {
    fun onWifiDirectStateChanged(isEnabled: Boolean)
    fun onPeersChanged(peers: List<PeerDevice>)
    fun onConnectionInfoAvailable(info: android.net.wifi.p2p.WifiP2pInfo)
    fun onDisconnected()
    fun onThisDeviceChanged(device: WifiP2pDevice)
}
