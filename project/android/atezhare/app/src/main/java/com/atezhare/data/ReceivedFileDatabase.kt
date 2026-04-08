package com.atezhare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ReceivedFile::class, SentFile::class, LibrarianEntry::class],
    version = 4,
    exportSchema = false
)
abstract class ReceivedFileDatabase : RoomDatabase() {

    abstract fun receivedFileDao(): ReceivedFileDao
    abstract fun sentFileDao(): SentFileDao
    abstract fun librarianDao(): LibrarianDao

    companion object {
        @Volatile
        private var INSTANCE: ReceivedFileDatabase? = null

        fun getInstance(context: Context): ReceivedFileDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ReceivedFileDatabase::class.java,
                    "atezhare_received_files.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
