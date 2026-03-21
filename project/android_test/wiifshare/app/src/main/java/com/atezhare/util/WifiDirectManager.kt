package com.atezhare.util

import android.content.*
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.os.Looper
import android.util.Log


class WifiDirectManager(private val context: Context) :
    WifiP2pManager.PeerListListener {

    private val manager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

    private val channel =
        manager.initialize(context, Looper.getMainLooper(), null)

    val peersList = mutableListOf<WifiP2pDevice>()

    interface ConnectionInfoListener {
        fun onConnectionReady(isGroupOwner: Boolean, hostAddress: String)
    }

    var connectionListener: ConnectionInfoListener? = null

    fun discoverPeers() {
        manager.discoverPeers(channel, null)
    }

    override fun onPeersAvailable(peerList: WifiP2pDeviceList) {
        peersList.clear()
        peersList.addAll(peerList.deviceList)
    }

    fun connectToDevice(deviceAddress: String) {
        val device = peersList.find { it.deviceAddress == deviceAddress } ?: return

        val config = WifiP2pConfig().apply {
            this.deviceAddress = device.deviceAddress
        }

        manager.connect(channel, config, null)
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    manager.requestPeers(channel, this@WifiDirectManager)
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(
                            WifiP2pManager.EXTRA_NETWORK_INFO
                        )

                    if (networkInfo?.isConnected == true) {
                        manager.requestConnectionInfo(channel) { info ->
                            if (info.groupFormed) {
                                connectionListener?.onConnectionReady(
                                    info.isGroupOwner,
                                    info.groupOwnerAddress.hostAddress
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(receiver)
    }
}