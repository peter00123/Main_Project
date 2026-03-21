package com.atezhare.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ReceivedFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: ReceivedFile)

    @Query("SELECT * FROM received_files ORDER BY receivedAt DESC")
    fun getAllFiles(): LiveData<List<ReceivedFile>>

    @Query("SELECT * FROM received_files WHERE mimeType LIKE :mimePrefix || '%' ORDER BY receivedAt DESC")
    fun getFilesByType(mimePrefix: String): LiveData<List<ReceivedFile>>

    @Query("UPDATE received_files SET isViewed = 1 WHERE id = :id")
    suspend fun markViewed(id: Long)

    @Delete
    suspend fun delete(file: ReceivedFile)

    @Query("DELETE FROM received_files")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM received_files WHERE isViewed = 0")
    fun getUnviewedCount(): LiveData<Int>

    @Query("SELECT * FROM received_files WHERE fileId = :fileId LIMIT 1")
    suspend fun getByFileId(fileId: String): ReceivedFile?
}
