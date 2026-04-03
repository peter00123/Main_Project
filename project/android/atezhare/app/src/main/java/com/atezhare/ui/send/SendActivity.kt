// ui/send/SendActivity.kt
// Standalone activity for the sender flow. No toolbar or bottom nav bar.
// Layout:
//   - CameraX preview (center) for scanning receiver's QR code
//   - 6-digit share code displayed at the bottom (generated from backend session)
//   - "Or share this code" label
// Flow:
//   1. Activity starts → SendViewModel creates a session via POST /session/create
//   2. A 6-digit code is shown at bottom for the receiver to type
//   3. CameraX scans QR from receiver's screen → calls POST /pair/scan-qr
//   4. On match: show confirm dialog → POST /session/confirm → upload files
// Depends on: SendViewModel (business logic), utils/QrUtils (validation),
//             network/ApiService (session/scan/upload), model/LocalFile

package com.atezhare.ui.send

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.atezhare.databinding.ActivitySendBinding
import com.atezhare.model.LocalFile
import com.atezhare.model.SessionStatus
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SendActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILES = "extra_files"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_EXPIRES_AT = "extra_expires_at"
    }

    private lateinit var binding: ActivitySendBinding

    // ViewModel handles session creation, QR scan submit, confirm, upload
    // See SendViewModel for all network calls
    private val viewModel: SendViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService
    private var qrScanned = false // Prevent duplicate scan events

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else {
            Toast.makeText(this, "Camera permission required for QR scanning", Toast.LENGTH_LONG).show()
            binding.cameraPreview.visibility = View.GONE
            binding.tvNoCameraPermission.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve selected files passed from DirectoryFragment
        // See model/LocalFile (Parcelable)
        val files = intent.getParcelableArrayListExtra<LocalFile>(EXTRA_FILES) ?: arrayListOf()
        viewModel.setSelectedFiles(files)

        val mode = intent.getStringExtra(EXTRA_MODE) ?: "LIVE"
        val expiresAt = intent.getLongExtra(EXTRA_EXPIRES_AT, 0L)
        viewModel.setMode(mode, expiresAt)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupClickListeners()
        observeViewModel()

        // Create session on backend — triggers POST /session/create in SendViewModel
        viewModel.createSession()

        // Check camera permission for QR scanning
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupClickListeners() {
        // Back / close button
        binding.btnClose.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        // Show generated 6-digit share code at the bottom
        viewModel.shareCode.observe(this) { code ->
            binding.tvShareCode.text = code ?: "------"
        }

        // Session status polling — show receiver joined, then confirm dialog
        viewModel.sessionStatus.observe(this) { status ->
            when (status) {
                SessionStatus.PAIRED -> {
                    // Receiver has joined — show confirm dialog
                    showConfirmDialog()
                }
                SessionStatus.DONE -> {
                    Toast.makeText(this, "Files sent successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                SessionStatus.ERROR -> {
                    Toast.makeText(this, "Transfer failed. Please try again.", Toast.LENGTH_LONG).show()
                }
                else -> { /* WAITING or TRANSFERRING — no UI change */ }
            }
        }

        // Loading state
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Error messages
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // QR scan result processed — backend matched sender to session
        viewModel.qrScanSuccess.observe(this) { success ->
            if (success) showConfirmDialog()
        }
    }

    // ==================== CAMERA / QR SCANNING ====================

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            // ImageAnalysis for ZXing QR decoding on each frame
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageForQr(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Processes each camera frame to detect a QR code using ZXing.
     * On success, calls SendViewModel.onQrScanned() with the decoded string.
     */
    private fun processImageForQr(imageProxy: ImageProxy) {
        if (qrScanned) {
            imageProxy.close()
            return
        }

        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val source = PlanarYUVLuminanceSource(
            bytes,
            imageProxy.width, imageProxy.height,
            0, 0,
            imageProxy.width, imageProxy.height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = MultiFormatReader().decode(binaryBitmap)
            qrScanned = true
            runOnUiThread {
                // Pass scanned QR content to ViewModel → POST /pair/scan-qr
                viewModel.onQrScanned(result.text)
                Toast.makeText(this, "QR Scanned!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NotFoundException) {
            // No QR found in this frame — normal, keep scanning
        } finally {
            imageProxy.close()
        }
    }

    // ==================== CONFIRM DIALOG ====================

    /**
     * Shows a confirmation dialog when a receiver has paired.
     * On confirm: calls SendViewModel.confirmAndUpload() → POST /session/confirm + POST /files/upload
     */
    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Receiver Connected")
            .setMessage("A receiver has paired. Do you want to send the selected files?")
            .setPositiveButton("Send") { _, _ ->
                // Confirm and upload — see SendViewModel.confirmAndUpload()
                viewModel.confirmAndUpload()
            }
            .setNegativeButton("Cancel") { _, _ ->
                qrScanned = false // Allow re-scanning
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
