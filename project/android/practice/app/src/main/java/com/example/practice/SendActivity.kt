package com.example.practice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practice.com.example.practice.FileItem
import java.io.File

class SendActivity : ComponentActivity() {

    private val selectedFiles = mutableListOf<FileItem>()
    private lateinit var adapter: FileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val sendButton = findViewById<Button>(R.id.button2)

        val recyclerView = findViewById<RecyclerView>(R.id.fileList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(selectedFiles)
        recyclerView.adapter = adapter

        // ✅ Load files from app storage
        loadAppStoredFiles()

        // Go to QR scan
        sendButton.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
    }

    private fun loadAppStoredFiles() {
        selectedFiles.clear()

        val files: Array<File>? = filesDir.listFiles()

        files?.forEach { file ->
            val uri = Uri.fromFile(file)
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
