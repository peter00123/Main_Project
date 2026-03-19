package com.qrshare.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivitySendBinding
import com.qrshare.utils.FileUtils
import com.qrshare.utils.PermissionHelper

class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var selectedFileSize: Long = 0

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = FileUtils.getFileName(this, uri)
                selectedFileSize = FileUtils.getFileSize(this, uri)
                showFileSelected()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) pickFile()
        else Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSelectFile.setOnClickListener {
            if (PermissionHelper.hasStoragePermission(this)) pickFile()
            else permissionLauncher.launch(PermissionHelper.getStoragePermissions())
        }

        binding.btnScanQr.setOnClickListener {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Select a file first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, QRScannerActivity::class.java).apply {
                putExtra("file_uri", selectedFileUri.toString())
                putExtra("file_name", selectedFileName)
                putExtra("file_size", selectedFileSize)
            }
            startActivity(intent)
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun showFileSelected() {
        binding.tvFileName.text = selectedFileName
        binding.tvFileSize.text = FileUtils.formatFileSize(selectedFileSize)
        binding.layoutFileInfo.visibility = View.VISIBLE
        binding.btnScanQr.isEnabled = true
        binding.btnScanQr.alpha = 1f
    }
}
