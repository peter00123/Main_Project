package com.atezhare.ui.shareddata

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.SentFile
import com.atezhare.data.SentFileRepository
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.launch

class LiveFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SentFileRepository(application)
    val activeSentFiles: LiveData<List<SentFile>> = repository.getActiveSentFiles()

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
                // Tell server to mark deleted — receiver's 30s poll will catch this
                RetrofitClient.apiService.deleteFile(fileId)
            } catch (e: Exception) {
                Log.e("LiveFilesVM", "Server delete failed on countdown expiry", e)
            }
            // Always mark inactive locally regardless of server response
            repository.markInactive(fileId)
        }
    }
}
