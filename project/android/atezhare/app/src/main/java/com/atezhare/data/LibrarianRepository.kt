package com.atezhare.data

import android.content.Context
import android.util.Log
import com.atezhare.utils.FileNameEncoder
import java.io.File

/**
 * LibrarianRepository.kt
 *
 * The Librarian — manages the lifecycle of time-limited received files.
 *
 * Responsibilities:
 *   1. registerFile()    — called after a file is downloaded. Parses the timer
 *                          from the encoded filename and stores in SQLite if timer != 00.00.00
 *   2. checkAndDelete()  — called every time SharedDataFragment opens. Finds all
 *                          expired entries, deletes the file from disk, marks the
 *                          ReceivedFile DB record as deleted, removes from librarian table.
 *
 * Used by:
 *   - ui/receive/ReceiveViewModel  → calls registerFile() after download
 *   - ui/shareddata/SharedDataFragment → calls checkAndDelete() on onViewCreated()
 */
class LibrarianRepository(context: Context) {

    private val dao: LibrarianDao = ReceivedFileDatabase.getInstance(context).librarianDao()
    private val receivedFileDao: ReceivedFileDao = ReceivedFileDatabase.getInstance(context).receivedFileDao()

    /**
     * Registers a newly received file with the Librarian.
     *
     * Steps:
     *   1. Check if fileName is in Atezhare encoded format
     *   2. Decode the timer duration from the filename
     *   3. If timer == 00.00.00 → do nothing (LIVE file, no expiry)
     *   4. If timer > 0 → compute deleteAt = System.currentTimeMillis() + timerMillis
     *   5. Insert LibrarianEntry into SQLite
     *
     * @param fileName   The encoded filename e.g. "invoic482929_01.30.00.pdf"
     * @param localPath  Absolute path to the saved file on this device
     * @param fileId     The fileId from the received_files table
     */
    suspend fun registerFile(fileName: String, localPath: String, fileId: String) {
        if (!FileNameEncoder.isEncodedFileName(fileName)) {
            Log.d("Librarian", "Not an encoded file — skipping: $fileName")
            return
        }

        val timerMillis = FileNameEncoder.decodeTimerMillis(fileName)

        if (timerMillis == 0L) {
            Log.d("Librarian", "Timer is 00.00.00 — LIVE mode, not tracking: $fileName")
            return
        }

        val deleteAt = System.currentTimeMillis() + timerMillis
        val entry = LibrarianEntry(
            fileName = fileName,
            localPath = localPath,
            fileId = fileId,
            deleteAt = deleteAt
        )

        dao.insert(entry)
        Log.d("Librarian", "Registered: $fileName → deleteAt=$deleteAt (in ${timerMillis/1000}s)")
    }

    /**
     * Checks for expired files and deletes them.
     *
     * Called from SharedDataFragment.onViewCreated() every time the screen is opened.
     *
     * Steps for each expired entry:
     *   1. Delete the physical file from disk (localPath)
     *   2. Mark the ReceivedFile DB record as isDeleted = 1
     *   3. Remove the entry from the librarian table
     *
     * @return count of files deleted
     */
    suspend fun checkAndDelete(): Int {
        val now = System.currentTimeMillis()
        val expired = dao.getExpiredEntries(currentTime = now)

        if (expired.isEmpty()) {
            Log.d("Librarian", "No expired files found at time $now")
            return 0
        }

        var deleted = 0
        for (entry in expired) {
            try {
                // 1. Delete physical file from disk
                val file = File(entry.localPath)
                if (file.exists()) {
                    file.delete()
                    Log.d("Librarian", "Deleted file from disk: ${entry.localPath}")
                } else {
                    Log.d("Librarian", "File already gone from disk: ${entry.localPath}")
                }

                // 2. Mark as deleted in received_files Room table
                receivedFileDao.markDeleted(entry.fileId)
                Log.d("Librarian", "Marked deleted in DB: ${entry.fileId}")

                // 3. Remove from librarian table
                dao.deleteByFileName(entry.fileName)
                Log.d("Librarian", "Removed from librarian: ${entry.fileName}")

                deleted++
            } catch (e: Exception) {
                Log.e("Librarian", "Error deleting ${entry.fileName}", e)
            }
        }

        Log.d("Librarian", "checkAndDelete complete: $deleted file(s) deleted")
        return deleted
    }
}
