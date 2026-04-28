package com.atezhare.data

data class TransferProgress(
    val fileName: String,
    val progress: Int,
    val speed: String,
    val isDownloading: Boolean = false
)
