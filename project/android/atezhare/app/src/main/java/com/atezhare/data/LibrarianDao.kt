package com.atezhare.data

import androidx.room.*

/**
 * LibrarianDao.kt
 *
 * Room DAO for the Librarian table.
 * Provides insert, delete, and expiry-check queries.
 *
 * Used by: data/LibrarianRepository
 */
@Dao
interface LibrarianDao {

    /** Insert or replace a tracked file entry */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LibrarianEntry)

    /** Remove a specific entry by fileName */
    @Query("DELETE FROM librarian WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String)

    /** Remove a specific entry by fileId */
    @Query("DELETE FROM librarian WHERE fileId = :fileId")
    suspend fun deleteByFileId(fileId: String)

    /**
     * Returns all entries where deleteAt <= currentTime.
     * These are the files that need to be deleted right now.
     * Called from SharedDataFragment.onViewCreated()
     */
    @Query("SELECT * FROM librarian WHERE deleteAt <= :currentTime")
    suspend fun getExpiredEntries(currentTime: Long): List<LibrarianEntry>

    /** Returns all entries — for debugging */
    @Query("SELECT * FROM librarian ORDER BY deleteAt ASC")
    suspend fun getAllEntries(): List<LibrarianEntry>

    /** Check if a fileName is already tracked */
    @Query("SELECT * FROM librarian WHERE fileName = :fileName LIMIT 1")
    suspend fun getByFileName(fileName: String): LibrarianEntry?
}
