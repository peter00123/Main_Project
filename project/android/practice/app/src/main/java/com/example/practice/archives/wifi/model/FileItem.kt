package com.example.practice.archives.wifi.model

import android.net.Uri

data class FileItem(
    val uri: Uri,
    val name: String,
    var isSelected: Boolean = false
)