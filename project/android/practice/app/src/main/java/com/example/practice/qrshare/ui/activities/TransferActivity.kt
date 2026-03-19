package com.qrshare.ui.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivityTransferBinding
import com.qrshare.network.ConnectionInfo
import com.qrshare.network.TransferState
import com.qrshare.ui.viewmodels.SendViewModel

class TransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferBinding
    private val viewModel: SendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val connectionJson = intent.getStringExtra("connection_json") ?: return finish()
        val fileUriStr = intent.getStringExtra("file_uri") ?: return finish()
        val fileName = intent.getStringExtra("file_name") ?: "File"

        binding.tvFileName.text = fileName

        val connectionInfo = ConnectionInfo.fromJson(connectionJson)
        val fileUri = Uri.parse(fileUriStr)

        observeViewModel()
        viewModel.sendFile(connectionInfo, fileUri)
    }

    private fun observeViewModel() {
        viewModel.transferState.observe(this) { state ->
            when (state) {
                TransferState.COMPLETED -> {
                    binding.layoutProgress.visibility = View.GONE
                    binding.layoutComplete.visibility = View.VISIBLE
                }
                TransferState.FAILED -> {
                    binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
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
            }
        }

        viewModel.transferComplete.observe(this) { done ->
            if (done) {
                binding.btnDone.visibility = View.VISIBLE
                binding.btnDone.setOnClickListener { finish() }
            }
        }
    }
}
