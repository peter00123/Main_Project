package com.atezhare.ui.send

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.atezhare.adapter.FileAdapter
import com.atezhare.databinding.ActivitySendBinding
import com.atezhare.model.FileItem
import com.atezhare.ui.share.ShareActivity
import com.atezhare.util.FileUtils

class SendActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySendBinding
    private val selectedFiles = mutableListOf<FileItem>()
    private lateinit var fileAdapter: FileAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris?.forEach { uri ->
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val name = FileUtils.getFileName(this, uri)
            val sizeBytes = FileUtils.getFileSize(this, uri)
            val sizeFormatted = FileUtils.formatFileSize(sizeBytes)

            selectedFiles.add(FileItem(uri, name, sizeFormatted, sizeBytes))
        }
        updateFileListVisibility()
        fileAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        fileAdapter = FileAdapter(selectedFiles) { position ->
            selectedFiles.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
            updateFileListVisibility()
        }

        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(this@SendActivity)
            adapter = fileAdapter
        }

        binding.fabAdd.setOnClickListener {
            filePickerLauncher.launch(arrayOf("*/*"))
        }

        binding.btnShare.setOnClickListener {
            if (selectedFiles.isNotEmpty()) {
                val uriStrings = ArrayList(selectedFiles.map { it.uri.toString() })
                val intent = Intent(this, ShareActivity::class.java).apply {
                    putStringArrayListExtra("file_uris", uriStrings)
                }
                startActivity(intent)
            }
        }
    }

    private fun updateFileListVisibility() {
        if (selectedFiles.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvFiles.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvFiles.visibility = View.VISIBLE
        }
    }
}
