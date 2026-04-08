package com.atezhare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * LibrarianEntry.kt
 *
 * Room entity representing one tracked file in the Librarian database.
 * The Librarian stores the fileName and the absolute time at which
 * the file should be deleted (deleteAt = receivedAt + timerMillis).
 *
 * Rule: only files where timer != 00.00.00 are stored here.
 * Files with timer 00.00.00 are LIVE mode — never added to this table.
 *
 * Used by: data/LibrarianDao, data/LibrarianRepository
 * Checked by: ui/shareddata/SharedDataFragment (onViewCreated)
 */
@Entity(tableName = "librarian")
data class LibrarianEntry(
    @PrimaryKey
    val fileName: String,        // The encoded filename e.g. "invoic482929_01.30.00.pdf"
    val localPath: String,       // Absolute path on device to delete the file
    val fileId: String,          // Matches ReceivedFile.fileId for DB cross-reference
    val deleteAt: Long,          // Unix timestamp (ms): receivedAt + timerDurationMillis
    val addedAt: Long = System.currentTimeMillis()
)
