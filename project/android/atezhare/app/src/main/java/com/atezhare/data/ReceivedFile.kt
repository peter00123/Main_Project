package com.atezhare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "received_files")
data class ReceivedFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val localPath: String,
    val sessionId: String,
    val senderId: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false,
    val mode: String = "LIVE",          // "LIVE" or "COUNTDOWN"
    val isDeleted: Boolean = false,      // true when sender stops or countdown ends
    val expiresAt: Long? = null          // for COUNTDOWN — set when file is received
)
