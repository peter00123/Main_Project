package com.atezhare.ui.share

import android.net.Uri
import android.os.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.R
import com.atezhare.ui.send.FileSender
import com.atezhare.util.WifiDirectManager



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ShareActivity : AppCompatActivity() {

    private lateinit var wifiDirectManager: WifiDirectManager
    private lateinit var progressBar: ProgressBar

    private lateinit var fileUri: Uri
    private var fileSize: Long = 1

    private val CAMERA_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        progressBar = findViewById(R.id.progressBar)

        wifiDirectManager = WifiDirectManager(this)
        wifiDirectManager.registerReceiver()
        wifiDirectManager.discoverPeers()

        checkCameraPermission()

        wifiDirectManager.connectionListener =
            object : WifiDirectManager.ConnectionInfoListener {
                override fun onConnectionReady(
                    isGroupOwner: Boolean,
                    hostAddress: String
                ) {
                    if (!isGroupOwner) {
                        FileSender().sendFile(
                            hostAddress,
                            fileUri,
                            this@ShareActivity,
                            fileSize
                        ) { progress ->
                            runOnUiThread {
                                progressBar.progress = progress
                            }
                        }
                    }
                }
            }
    }

    // 🔥 CHECK PERMISSION
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startQRScanner()
        }
    }

    // 🔥 HANDLE PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startQRScanner()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 START CAMERA
    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan Receiver QR")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    // 🔥 HANDLE SCAN RESULT
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result: IntentResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result.contents != null) {

            val scanned = result.contents
            val deviceAddress = scanned.replace("WIFISHARE:", "")

            Handler(Looper.getMainLooper()).postDelayed({
                wifiDirectManager.connectToDevice(deviceAddress)
            }, 3000)

        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.unregisterReceiver()
    }
}