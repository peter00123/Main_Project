package com.qrshare.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.qrshare.databinding.ActivityTransferBinding
import com.qrshare.network.*
import com.qrshare.sharing.DownloadDirectoryHelper
import com.qrshare.sharing.FilePickerHelper
import com.qrshare.ui.TransferItemAdapter

/**
 * Live transfer progress screen — used by both sender and receiver.
 * Binds to FileTransferService and reflects real-time progress.
 *
 * Extras:
 *   EXTRA_MODE        → MODE_SEND or MODE_RECEIVE
 *   EXTRA_SESSION_JSON → JSON string of SessionInfo
 *   EXTRA_FILES       → ArrayList<Uri> (send mode only)
 */
class TransferActivity : AppCompatActivity(), TransferServiceListener {

    companion object {
        const val EXTRA_MODE        = "mode"
        const val EXTRA_SESSION_JSON = "session_json"
        const val EXTRA_FILES       = "files"
        const val MODE_SEND         = "send"
        const val MODE_RECEIVE      = "receive"
    }

    private lateinit var binding: ActivityTransferBinding
    private val transferItems = mutableListOf<TransferItemAdapter.TransferItem>()
    private lateinit var transferAdapter: TransferItemAdapter

    private var transferService: FileTransferService? = null
    private var serviceBound = false
    private var mode = MODE_RECEIVE
    private var sessionInfo: SessionInfo? = null
    private var fileUrisForSend: List<android.net.Uri> = emptyList()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FileTransferService.TransferBinder
            transferService = binder.getService()
            transferService?.setListener(this@TransferActivity)
            serviceBound = true
            startTransfer()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_RECEIVE
        sessionInfo = SessionInfo.fromJson(intent.getStringExtra(EXTRA_SESSION_JSON) ?: "")
        fileUrisForSend = intent.getParcelableArrayListExtra<android.net.Uri>(EXTRA_FILES) ?: arrayListOf()

        setupUI()
        setupRecyclerView()
        bindToService()

        binding.btnCancel.setOnClickListener {
            transferService?.cancelTransfer()
            finish()
        }

        binding.btnDone.setOnClickListener { finish() }
    }

    private fun setupUI() {
        if (mode == MODE_SEND) {
            binding.tvTitle.text = "Sending Files"
            binding.tvSubtitle.text = "To: ${sessionInfo?.deviceName ?: "Device"}"
            binding.ivModeIcon.setImageResource(android.R.drawable.ic_menu_share)
        } else {
            binding.tvTitle.text = "Receiving Files"
            binding.tvSubtitle.text = "From: ${sessionInfo?.deviceName ?: "Device"}"
            binding.ivModeIcon.setImageResource(android.R.drawable.ic_menu_save)
        }
        binding.btnDone.visibility = View.GONE
        binding.tvOverallStatus.text = "Initializing..."
    }

    private fun setupRecyclerView() {
        transferAdapter = TransferItemAdapter(transferItems)
        binding.rvTransferItems.apply {
            layoutManager = LinearLayoutManager(this@TransferActivity)
            adapter = transferAdapter
        }
    }

    private fun bindToService() {
        val serviceIntent = Intent(this, FileTransferService::class.java)
        startForegroundService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startTransfer() {
        val info = sessionInfo ?: run {
            showError("Invalid session info")
            return
        }

        if (mode == MODE_RECEIVE) {
            val downloadDir = DownloadDirectoryHelper.getDownloadDirectory(this)
            transferService?.startReceiving(info.port, downloadDir)
        } else {
            // Resolve URIs to TransferFile objects and start sending
            val filesToSend = FilePickerHelper.resolveTransferFiles(this, fileUrisForSend)
            if (filesToSend.isEmpty()) {
                showError("No files to send")
                return
            }
            transferService?.startSending(info.hostIp, info.port, filesToSend)
        }
    }

    // ─── TransferServiceListener callbacks (may come from background thread) ───

    override fun onStatusUpdate(message: String) = runOnUiThread {
        binding.tvOverallStatus.text = message
    }

    override fun onFileStarted(fileName: String, fileSize: Long) = runOnUiThread {
        val item = TransferItemAdapter.TransferItem(
            fileName = fileName,
            fileSize = fileSize,
            progress = 0,
            state = TransferState.IN_PROGRESS,
            speed = ""
        )
        transferItems.add(item)
        transferAdapter.notifyItemInserted(transferItems.size - 1)
        binding.tvOverallStatus.text = if (mode == MODE_SEND) "Sending $fileName…" else "Receiving $fileName…"
    }

    override fun onProgress(progress: TransferProgress) = runOnUiThread {
        val idx = transferItems.indexOfFirst { it.fileName == progress.fileName }
        if (idx >= 0) {
            transferItems[idx] = transferItems[idx].copy(
                progress = progress.progressPercent,
                speed = FilePickerHelper.formatSpeed(progress.speedBytesPerSec),
                state = TransferState.IN_PROGRESS
            )
            transferAdapter.notifyItemChanged(idx)
        }
        // Overall progress bar
        binding.overallProgress.progress = progress.progressPercent
    }

    override fun onFileComplete(filePath: String, mimeType: String) = runOnUiThread {
        val name = filePath.substringAfterLast('/')
        val idx = transferItems.indexOfFirst { it.fileName == name }
        if (idx >= 0) {
            transferItems[idx] = transferItems[idx].copy(
                progress = 100,
                state = TransferState.COMPLETED,
                speed = "Done"
            )
            transferAdapter.notifyItemChanged(idx)
        }
    }

    override fun onAllComplete() = runOnUiThread {
        binding.tvOverallStatus.text = if (mode == MODE_SEND) "All files sent! ✓" else "All files received! ✓"
        binding.overallProgress.progress = 100
        binding.btnCancel.visibility = View.GONE
        binding.btnDone.visibility = View.VISIBLE
        binding.successBanner.visibility = View.VISIBLE
        if (mode == MODE_RECEIVE) {
            binding.tvSaveLocation.text = "Saved to Downloads/QRShare"
            binding.tvSaveLocation.visibility = View.VISIBLE
        }
    }

    override fun onError(message: String) = runOnUiThread {
        showError(message)
    }

    private fun showError(message: String) {
        binding.tvOverallStatus.text = "Error: $message"
        binding.btnCancel.text = "Close"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
