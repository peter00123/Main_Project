package com.example.practice.qrshare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.practice.databinding.ActivityQrScanBinding
import com.example.practice.qrshare.network.SessionInfo
import com.example.practice.qrshare.utils.QRCodeHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera-based QR code scanner (receiver side).
 * Uses CameraX + ML Kit Barcode Scanning.
 * On successful scan, extracts SessionInfo and navigates to TransferActivity.
 */
class QRScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var scanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Scan QR Code"

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (scanned) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE) {
                        val rawValue = barcode.rawValue ?: continue
                        val sessionInfo = QRCodeHelper.decodeSessionInfo(rawValue)
                        if (sessionInfo != null && !scanned) {
                            scanned = true
                            runOnUiThread { onQRScanned(sessionInfo) }
                        }
                    }
                }
            }
            .addOnFailureListener { /* Ignore scan failures */ }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun onQRScanned(sessionInfo: SessionInfo) {
        Toast.makeText(this, "Connected to ${sessionInfo.deviceName}!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, TransferActivity::class.java).apply {
            putExtra(TransferActivity.EXTRA_MODE, TransferActivity.MODE_RECEIVE)
            putExtra(TransferActivity.EXTRA_SESSION_JSON, sessionInfo.toJson())
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
