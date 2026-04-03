package com.atezhare.ui.shareddata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.atezhare.data.SentFile
import com.atezhare.data.SentFileRepository
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.launch

class LiveFilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SentFileRepository(application)
    val activeSentFiles: LiveData<List<SentFile>> = repository.getActiveSentFiles()

    private val _stopSuccess = MutableLiveData<String>()
    val stopSuccess: LiveData<String> = _stopSuccess

    fun stopFile(fileId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteFile(fileId)
                if (response.isSuccessful && response.body()?.success == true) {
                    repository.markInactive(fileId)
                    _stopSuccess.postValue(fileId)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun deleteExpiredCountdown(fileId: String) {
        viewModelScope.launch {
            repository.markInactive(fileId)
        }
    }
}
