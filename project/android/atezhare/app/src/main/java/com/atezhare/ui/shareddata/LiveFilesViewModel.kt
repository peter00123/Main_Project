package com.atezhare.ui.shareddata

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.SentFile
import com.atezhare.data.SentFileRepository
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SentFileRepository(application)
    val activeSentFiles: LiveData<List<SentFile>> = repository.getActiveSentFiles()

    init {
        startExpiryMonitoring()
    }

    private fun startExpiryMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                // Get current active sent files that are countdown mode
                val activeFiles = repository.getActiveSentFilesSync()
                val now = System.currentTimeMillis()
                
                for (file in activeFiles) {
                    if (file.mode == "COUNTDOWN" && file.expiresAt != null) {
                        if (now >= file.expiresAt) {
                            Log.d("LiveFilesVM", "File ${file.fileId} expired")
                            deleteExpiredCountdown(file.fileId)
                        }
                    }
                }
                delay(10_000) // Check every 10 seconds
            }
        }
    }

    private suspend fun getActiveSentFilesSync(): List<SentFile> {
        // We'll add this method to repository/dao
        return repository.getActiveSentFilesSync()
    }

    fun stopFile(fileId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteFile(fileId)
                if (response.isSuccessful) {
                    repository.markInactive(fileId)
                }
            } catch (e: Exception) {
                Log.e("LiveFilesVM", "Stop failed", e)
            }
        }
    }

    fun deleteExpiredCountdown(fileId: String) {
        viewModelScope.launch {
            try {
                // Tell server to mark deleted — receiver's poll will catch this
                RetrofitClient.apiService.deleteFile(fileId)
            } catch (e: Exception) {
                Log.e("LiveFilesVM", "Server delete failed on countdown expiry", e)
            }
            // Always mark inactive locally
            repository.markInactive(fileId)
        }
    }
}
