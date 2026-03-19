package com.qrshare.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.qrshare.R
import com.qrshare.databinding.ActivitySendBinding
import com.qrshare.network.SessionInfo
import com.qrshare.network.TransferFile
import com.qrshare.network.WiFiDirectManager
import com.qrshare.sharing.FilePickerHelper
import com.qrshare.sharing.QRCodeHelper
import com.qrshare.ui.SelectedFilesAdapter

/**
 * Sender flow:
 *  1. User picks files
 *  2. This device creates a Wi-Fi Direct group (becomes host)
 *  3. SessionInfo (IP + port + sessionId) is encoded into a QR code
 *  4. Receiver scans QR → navigates to QRScanActivity → connects → TransferActivity handles sending
 */
class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding
    private val wifiDirectManager by lazy { WiFiDirectManager(this) }

    private val selectedFiles = mutableListOf<TransferFile>()
    private lateinit var filesAdapter: SelectedFilesAdapter

    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val resolved = FilePickerHelper.resolveTransferFiles(this, uris)
            selectedFiles.clear()
            selectedFiles.addAll(resolved)
            filesAdapter.notifyDataSetChanged()
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Send Files"

        setupRecyclerView()
        setupClickListeners()
        updateUI()
    }

    private fun setupRecyclerView() {
        filesAdapter = SelectedFilesAdapter(selectedFiles) { position ->
            selectedFiles.removeAt(position)
            filesAdapter.notifyItemRemoved(position)
            updateUI()
        }
        binding.rvSelectedFiles.apply {
            layoutManager = LinearLayoutManager(this@SendActivity)
            adapter = filesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnPickFiles.setOnClickListener {
            pickFilesLauncher.launch("*/*")
        }

        binding.btnGenerateQR.setOnClickListener {
            if (selectedFiles.isEmpty()) {
                Toast.makeText(this, "Please select files first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateQRAndShowPairing()
        }
    }

    private fun generateQRAndShowPairing() {
        binding.btnGenerateQR.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Creating connection..."

        // Build session info with a local hotspot IP
        // In real deployment, device would create a Wi-Fi Direct group first
        // For socket-based transfer over same network, we use local IP
        val deviceName = android.os.Build.MODEL
        val sessionInfo = SessionInfo(
            hostIp = getLocalIpAddress(),
            port = WiFiDirectManager.DEFAULT_TRANSFER_PORT,
            sessionId = java.util.UUID.randomUUID().toString().take(8).uppercase(),
            deviceName = deviceName
        )

        val qrBitmap = QRCodeHelper.generateQRBitmap(sessionInfo)
        if (qrBitmap == null) {
            binding.progressBar.visibility = View.GONE
            binding.btnGenerateQR.isEnabled = true
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.GONE

        // Navigate to pairing screen showing QR code
        val intent = Intent(this, QRPairDisplayActivity::class.java).apply {
            putExtra(QRPairDisplayActivity.EXTRA_SESSION_JSON, sessionInfo.toJson())
            putParcelableArrayListExtra(
                QRPairDisplayActivity.EXTRA_FILES,
                ArrayList(selectedFiles.map { it.uri })
            )
        }
        startActivity(intent)
        binding.btnGenerateQR.isEnabled = true
    }

    private fun getLocalIpAddress(): String {
        return try {
            val interfaces = java.util.Collections.list(java.net.NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = java.util.Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
            "0.0.0.0"
        } catch (e: Exception) {
            "0.0.0.0"
        }
    }

    private fun updateUI() {
        val hasFiles = selectedFiles.isNotEmpty()
        binding.btnGenerateQR.isEnabled = hasFiles
        binding.emptyState.visibility = if (hasFiles) View.GONE else View.VISIBLE
        binding.rvSelectedFiles.visibility = if (hasFiles) View.VISIBLE else View.GONE

        if (hasFiles) {
            val totalSize = selectedFiles.sumOf { it.size }
            binding.tvFilesSummary.text = "${selectedFiles.size} file(s) · ${FilePickerHelper.formatFileSize(totalSize)}"
            binding.tvFilesSummary.visibility = View.VISIBLE
        } else {
            binding.tvFilesSummary.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
