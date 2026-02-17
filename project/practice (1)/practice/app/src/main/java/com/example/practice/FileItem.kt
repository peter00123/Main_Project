package com.example.practice.com.example.practice



import android.net.Uri

data class FileItem(
    val uri: Uri,
    val name: String,
    var isSelected: Boolean = false
)
