package com.example.practice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practice.com.example.practice.FileItem
import org.json.JSONObject
import java.io.File

class SendActivity : ComponentActivity() {

    private val selectedFiles = mutableListOf<FileItem>()
    private lateinit var adapter: FileAdapter

    companion object {
        private const val QR_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val sendButton = findViewById<Button>(R.id.button2)
        val recyclerView = findViewById<RecyclerView>(R.id.fileList)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(selectedFiles) { fileItem ->
            val intent = Intent(this, FilePreviewActivity::class.java)
            intent.putExtra("file_uri", fileItem.uri.toString())
            intent.putExtra("file_name", fileItem.name)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // Load files from app storage
        loadAppStoredFiles()

        // Open QR Scanner
        sendButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivityForResult(intent, QR_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == QR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val qrData = data?.getStringExtra("QR_DATA") ?: return
            startTransferFromQr(qrData)
        }
    }

    private fun startTransferFromQr(qrData: String) {
        try {
            val json = JSONObject(qrData)
            val expiresAt = json.getLong("expiresAt")

            // Open common transfer screen (SENDER MODE)
            val intent = Intent(this, TransferActivity::class.java).apply {
                putExtra("MODE", "SENDER")
                putExtra("EXPIRES_AT", expiresAt)
            }

            startActivity(intent)

            // 🔥 Later: start actual file sending here

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAppStoredFiles() {
        selectedFiles.clear()

        val files: Array<File>? = filesDir.listFiles()

        files?.forEach { file ->
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            selectedFiles.add(
                FileItem(
                    uri = uri,
                    name = file.name
                )
            )
        }

        adapter.notifyDataSetChanged()
    }
}