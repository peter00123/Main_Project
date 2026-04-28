package com.atezhare.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.atezhare.data.TransferProgress

object TransferProgressManager {
    private val _progress = MutableLiveData<TransferProgress?>()
    val progress: LiveData<TransferProgress?> = _progress

    fun updateProgress(update: TransferProgress?) {
        _progress.postValue(update)
    }
}
