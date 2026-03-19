package com.qrshare.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivityQrPairDisplayBinding
import com.qrshare.network.SessionInfo
import com.qrshare.network.TransferFile
import com.qrshare.sharing.FilePickerHelper
import com.qrshare.sharing.QRCodeHelper

/**
 * Displays the QR code that the receiver must scan.
 * Once tapped "Ready", navigates to TransferActivity (send mode).
 */
class QRPairDisplayActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SESSION_JSON = "session_json"
        const val EXTRA_FILES = "files"
    }

    private lateinit var binding: ActivityQrPairDisplayBinding
    private var sessionInfo: SessionInfo? = null
    private var filesToSend: List<TransferFile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrPairDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Scan to Connect"

        val sessionJson = intent.getStringExtra(EXTRA_SESSION_JSON)
        val fileUris = intent.getParcelableArrayListExtra<Uri>(EXTRA_FILES) ?: arrayListOf()

        sessionInfo = SessionInfo.fromJson(sessionJson ?: "")
        filesToSend = FilePickerHelper.resolveTransferFiles(this, fileUris)

        if (sessionInfo == null) {
            Toast.makeText(this, "Invalid session data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayQRCode()
        setupClickListeners()
    }

    private fun displayQRCode() {
        val info = sessionInfo ?: return
        binding.tvSessionId.text = "Session ID: ${info.sessionId}"
        binding.tvDeviceName.text = "From: ${info.deviceName}"
        val totalSize = filesToSend.sumOf { it.size }
        binding.tvFileCount.text =
            "${filesToSend.size} file(s) · ${FilePickerHelper.formatFileSize(totalSize)}"
        binding.tvInstruction.text = "Ask the receiver to scan this QR code"

        val qrBitmap = QRCodeHelper.generateQRBitmap(info)
        if (qrBitmap != null) {
            binding.ivQrCode.setImageBitmap(qrBitmap)
            binding.progressBar.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnWaitForScan.setOnClickListener {
            val info = sessionInfo ?: return@setOnClickListener
            val intent = Intent(this, TransferActivity::class.java).apply {
                putExtra(TransferActivity.EXTRA_MODE, TransferActivity.MODE_SEND)
                putExtra(TransferActivity.EXTRA_SESSION_JSON, info.toJson())
                putParcelableArrayListExtra(
                    TransferActivity.EXTRA_FILES,
                    ArrayList(filesToSend.map { it.uri })
                )
            }
            startActivity(intent)
        }
        binding.btnCancel.setOnClickListener { finish() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
