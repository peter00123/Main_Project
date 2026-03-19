package com.atezhare.ui.receive

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivityReceiveBinding
import com.atezhare.util.QrCodeHelper
import com.atezhare.util.WifiDirectManager
import kotlin.concurrent.thread

class ReceiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiveBinding
    private lateinit var wifiDirectManager: WifiDirectManager

    companion object {
        private const val TAG = "ReceiveActivity"
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        wifiDirectManager = WifiDirectManager(this)
        wifiDirectManager.register()

        val p2pManager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        val channel = p2pManager.initialize(this, Looper.getMainLooper(), null)

        p2pManager.requestDeviceInfo(channel) { device ->
            if (device != null) {
                val deviceAddress = device.deviceAddress
                val qrData = "WIFISHARE:$deviceAddress"

                val qrBitmap = QrCodeHelper.generateQrCode(qrData, 512)
                binding.ivQrCode.setImageBitmap(qrBitmap)

                Log.d(TAG, "QR generated for device: $deviceAddress")
            } else {
                Toast.makeText(this, "Could not get device info", Toast.LENGTH_SHORT).show()
            }
        }

        wifiDirectManager.onConnectionInfoAvailable = { info ->
            if (info.groupFormed && info.isGroupOwner) {
                binding.tvStatus.text = "Connected! Receiving files…"
                binding.progressBar.visibility = View.VISIBLE

                thread {
                    wifiDirectManager.receiveFiles(
                        onFileReceived = { fileName ->
                            runOnUiThread {
                                binding.tvStatus.text = "Received: $fileName"
                            }
                        },
                        onComplete = {
                            runOnUiThread {
                                binding.tvStatus.text = "All files received!"
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "Transfer complete!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                }
            }
        }

        wifiDirectManager.discoverPeers()
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.unregister()
        wifiDirectManager.disconnect()
    }
}
