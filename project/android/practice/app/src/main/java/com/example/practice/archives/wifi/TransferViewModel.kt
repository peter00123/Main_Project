package com.example.practice.archives.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class TransferViewModel : ViewModel() {

    val status = MutableLiveData<String>()
    val timeLeft = MutableLiveData<Long>()
    val progress = MutableLiveData<Pair<String, Int>>() // filename, %

    fun updateProgress(file: String, percent: Int) {
        progress.postValue(file to percent)
    }
}