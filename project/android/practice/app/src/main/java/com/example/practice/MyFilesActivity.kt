package com.example.practice

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream

class MyFilesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var uploadBtn: Button
    private lateinit var files: List<File>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_files)

        listView = findViewById(R.id.fileListView)
        uploadBtn = findViewById(R.id.uploadBtn)

        loadFiles()

        uploadBtn.setOnClickListener {
            openFilePicker()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_my_files

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    finish()
                    true
                }
                R.id.nav_my_files -> true
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    // 📂 Load files from app storage
    private fun loadFiles() {
        files = filesDir.listFiles()?.toList() ?: emptyList()
        val names = files.map { it.name }

        listView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            names
        )
    }

    // 📤 File picker
    private fun openFilePicker() {
        filePickerLauncher.launch("*/*")
    }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                saveFileToAppStorage(it)
                loadFiles()
            }
        }

    // 💾 Copy selected file into app storage
    private fun saveFileToAppStorage(uri: Uri) {
        val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "file"
        val destFile = File(filesDir, fileName)

        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
