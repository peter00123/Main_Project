package com.atezhare.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SentFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: SentFile)

    @Query("SELECT * FROM sent_files WHERE isActive = 1 ORDER BY sentAt DESC")
    fun getActiveSentFiles(): LiveData<List<SentFile>>

    @Query("SELECT * FROM sent_files WHERE fileId = :fileId LIMIT 1")
    suspend fun getByFileId(fileId: String): SentFile?

    @Query("UPDATE sent_files SET isActive = 0 WHERE fileId = :fileId")
    suspend fun markInactive(fileId: String)

    @Query("DELETE FROM sent_files")
    suspend fun deleteAll()
}
