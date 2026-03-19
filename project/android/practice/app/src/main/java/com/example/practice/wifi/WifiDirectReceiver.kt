package com.example.practice.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager

class WifiDirectReceiver(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val onConnected: (WifiP2pInfo) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION) {
            manager.requestConnectionInfo(channel) { info ->
                if (info.groupFormed) {
                    onConnected(info)
                }
            }
        }
    }
}