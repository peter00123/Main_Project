package com.example.practice

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practice.com.example.practice.FileItem

class MainActivity5 : ComponentActivity() {

    private val selectedFiles = mutableListOf<FileItem>()
    private lateinit var adapter: FileAdapter

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            selectedFiles.clear()

            uris.forEach { uri ->
                val name = uri.lastPathSegment ?: "Unknown file"
                selectedFiles.add(FileItem(uri, name))
            }

            adapter.notifyDataSetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)

        val recyclerView = findViewById<RecyclerView>(R.id.fileList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(selectedFiles)
        recyclerView.adapter = adapter

        // Open file picker

    }
}
