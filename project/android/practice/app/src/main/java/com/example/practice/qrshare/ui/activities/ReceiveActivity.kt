package com.qrshare.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivityReceiveBinding
import com.qrshare.network.TransferState
import com.qrshare.ui.viewmodels.ReceiveViewModel

class ReceiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiveBinding
    private val viewModel: ReceiveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        observeViewModel()
        viewModel.startReceiving()
    }

    private fun observeViewModel() {
        viewModel.qrBitmap.observe(this) { bitmap ->
            binding.ivQrCode.setImageBitmap(bitmap)
        }

        viewModel.connectionInfo.observe(this) { info ->
            binding.tvDeviceInfo.text = "${info.deviceName}\n${info.ipAddress}:${info.port}"
        }

        viewModel.transferState.observe(this) { state ->
            when (state) {
                TransferState.WAITING_FOR_CONNECTION -> {
                    binding.layoutQr.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                    binding.layoutComplete.visibility = View.GONE
                }
                TransferState.HANDSHAKING, TransferState.VERIFYING_QR, TransferState.CONNECTED -> {
                    binding.layoutQr.visibility = View.GONE
                    binding.layoutProgress.visibility = View.VISIBLE
                }
                TransferState.TRANSFERRING -> {
                    binding.layoutQr.visibility = View.GONE
                    binding.layoutProgress.visibility = View.VISIBLE
                }
                TransferState.COMPLETED -> {
                    binding.layoutProgress.visibility = View.GONE
                    binding.layoutComplete.visibility = View.VISIBLE
                }
                TransferState.FAILED -> {
                    binding.layoutQr.visibility = View.VISIBLE
                    binding.layoutProgress.visibility = View.GONE
                }
                else -> {}
            }
        }

        viewModel.statusMessage.observe(this) { msg ->
            binding.tvStatus.text = msg
        }

        viewModel.progress.observe(this) { progress ->
            progress?.let {
                binding.progressBar.progress = it.progressPercent
                binding.tvProgressPercent.text = "${it.progressPercent}%"
                binding.tvTransferSpeed.text = it.speedMbps
                binding.tvFileName.text = it.fileName
            }
        }

        viewModel.receivedFile.observe(this) { pair ->
            pair?.let { (name, path) ->
                binding.tvCompleteName.text = name
                binding.tvCompletePath.text = "Saved to: $path"
            }
        }
    }
}
