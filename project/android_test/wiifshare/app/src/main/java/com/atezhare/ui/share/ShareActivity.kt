package com.atezhare.ui.share

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.atezhare.databinding.ActivityShareBinding
import com.atezhare.util.WifiDirectManager
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class ShareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShareBinding
    private lateinit var wifiDirectManager: WifiDirectManager
    private var fileUris: List<Uri> = emptyList()
    private var isProcessing = false

    companion object {
        private const val TAG = "ShareActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val uriStrings = intent.getStringArrayListExtra("file_uris") ?: arrayListOf()
        fileUris = uriStrings.map { Uri.parse(it) }

        wifiDirectManager = WifiDirectManager(this)
        wifiDirectManager.register()

        wifiDirectManager.onConnectionInfoAvailable = { info ->
            val hostAddress = info.groupOwnerAddress?.hostAddress
            if (info.groupFormed && hostAddress != null) {
                binding.tvStatus.text = "Connected! Sending files…"

                thread {
                    wifiDirectManager.sendFiles(
                        host = hostAddress,
                        port = WifiDirectManager.TRANSFER_PORT,
                        fileUris = fileUris,
                        onProgress = { progress ->
                            runOnUiThread {
                                binding.tvStatus.text = "Sending… $progress%"
                            }
                        }
                    )
                    runOnUiThread {
                        binding.tvStatus.text = "Transfer complete!"
                        Toast.makeText(this, "Files sent successfully!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processImage(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val scanner = BarcodeScanning.getClient()
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue

                        if (rawValue.startsWith("WIFISHARE:")) {
                            isProcessing = true
                            val deviceAddress = rawValue.removePrefix("WIFISHARE:")

                            runOnUiThread {
                                binding.tvStatus.text = "QR found! Connecting…"
                            }

                            wifiDirectManager.connectToDevice(deviceAddress)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiDirectManager.unregister()
    }
}
