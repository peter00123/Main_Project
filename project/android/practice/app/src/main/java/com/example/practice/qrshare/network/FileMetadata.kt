package com.qrshare.network

data class FileMetadata(
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
) {
    fun toJson(): String = com.google.gson.Gson().toJson(this)

    companion object {
        fun fromJson(json: String): FileMetadata =
            com.google.gson.Gson().fromJson(json, FileMetadata::class.java)
    }
}
