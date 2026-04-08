package com.atezhare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sent_files")
data class SentFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val localPath: String,               // ADDED: path to local cached file
    val sessionId: String,
    val receiverId: String,
    val sentAt: Long,
    val mode: String, // "LIVE" or "COUNTDOWN"
    val expiresAt: Long? = null,
    val isActive: Boolean = true
)
