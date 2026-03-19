package com.example.practice

import android.Manifest
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class ScanActivity : AppCompatActivity() {

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        // QR Scanner Setup
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan QR Code")
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    ])
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {

            val deviceAddress = result.contents

            val config = WifiP2pConfig().apply {
                this.deviceAddress = deviceAddress
            }

            manager.connect(channel, config, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    Toast.makeText(
                        this@ScanActivity,
                        "Connecting...",
                        Toast.LENGTH_SHORT
                    ).show()

                    // IMPORTANT: wait for connection info
                    manager.requestConnectionInfo(channel) { info ->

                        if (info.groupFormed) {

                            val hostAddress =
                                info.groupOwnerAddress.hostAddress

                            Toast.makeText(
                                this@ScanActivity,
                                "Connected!",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@ScanActivity,
                                    TransferActivity::class.java
                                )
                                    .putExtra("MODE", "SENDER")
                                    .putExtra("HOST", hostAddress)
                            )

                            finish()
                        }
                    }
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(
                        this@ScanActivity,
                        "Connection failed: $reason",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}