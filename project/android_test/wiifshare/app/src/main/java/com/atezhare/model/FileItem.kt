package com.atezhare.model

import android.net.Uri

data class FileItem(
    val uri: Uri,
    val name: String,
    val size: String,
    val sizeBytes: Long
)
