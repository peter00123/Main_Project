package com.example.practice.archives.wifi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.*
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class WifiDirectManager(private val context: Context) {

    private val manager: WifiP2pManager =
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

    private val channel: WifiP2pManager.Channel =
        manager.initialize(context as Activity, context.mainLooper, null)

    fun discover() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)
            ) {
                // Permissions missing
                return
            }
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("WIFI", "Discovery started")
                }

                override fun onFailure(reason: Int) {
                    Log.e("WIFI", "Discovery failed: $reason")
                }
            })
        } catch (e: Exception) {
            Log.e("WIFI", "Discovery error: ${e.message}")
        }
    }

    fun connect(device: WifiP2pDevice, onSuccess: () -> Unit = {}, onFailure: (Int) -> Unit = {}) {
        if (device.deviceAddress == null) {
            Log.e("WIFI", "Cannot connect: deviceAddress is null")
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }

        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WIFI", "Connection initiated to ${device.deviceName}")
                onSuccess()
            }

            override fun onFailure(reason: Int) {
                Log.e("WIFI", "Connection failed: $reason")
                onFailure(reason)
            }
        })
    }

    fun requestInfo(callback: (WifiP2pInfo) -> Unit) {
        try {
            manager.requestConnectionInfo(channel) { info ->
                Log.d("WIFI", "Connection info: groupFormed=${info.groupFormed}, isGroupOwner=${info.isGroupOwner}")
                callback(info)
            }
        } catch (e: Exception) {
            Log.e("WIFI", "Request info error: ${e.message}")
        }
    }
}