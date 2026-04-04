package com.atezhare.ui.shareddata

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.atezhare.data.ReceivedFile
import com.atezhare.data.ReceivedFileRepository
import com.atezhare.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class SharedDataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReceivedFileRepository(application)
    private val _currentFilter = MutableLiveData("all")

    val fileList: LiveData<List<ReceivedFile>> = _currentFilter.switchMap { filter ->
        if (filter == "all") repository.allFiles
        else repository.getFilesByType(filter)
    }

    val unviewedCount: LiveData<Int> = repository.unviewedCount

    init {
        startActiveFilesMonitoring()
    }

    private fun startActiveFilesMonitoring() {
        viewModelScope.launch {
            while (isActive) {
                val currentFiles = repository.allFiles.value ?: emptyList()
                
                // Only count files that are NOT deleted
                val activeCount = currentFiles.count { !it.isDeleted }

                if (activeCount > 0) {
                    Log.d("SharedDataVM", "Monitoring $activeCount active files")
                    
                    for (file in currentFiles) {
                        if (file.isDeleted) continue

                        // 1. Check Local Countdown Expiry
                        if (file.mode == "COUNTDOWN" && file.expiresAt != null) {
                            if (System.currentTimeMillis() >= file.expiresAt) {
                                Log.d("SharedDataVM", "File ${file.fileId} expired locally")
                                repository.markDeleted(file.fileId)
                                continue
                            }
                        }

                        // 2. Poll Server for Deletion (Live stop or Server-side countdown)
                        try {
                            val response = RetrofitClient.apiService.getFileStatus(file.fileId)
                            if (response.isSuccessful && response.body()?.deleted == true) {
                                Log.d("SharedDataVM", "File ${file.fileId} deleted on server")
                                repository.markDeleted(file.fileId)
                            }
                        } catch (e: Exception) {
                            // Log and continue
                        }
                    }
                } else {
                    // Log.d("SharedDataVM", "No active files to monitor. Idling...")
                }

                delay(30_000) // Poll every 30 seconds
            }
        }
    }

    fun setFilter(filter: String) {
        _currentFilter.value = filter
    }

    fun openFile(context: Context, file: ReceivedFile) {
        viewModelScope.launch { repository.markViewed(file.id) }

        val localFile = File(file.localPath)
        if (!localFile.exists()) {
            Toast.makeText(context, "File not found on device", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context, "${context.packageName}.provider", localFile
            )
            
            var mimeType = file.mimeType
            if (mimeType == "application/octet-stream" || mimeType.isBlank()) {
                val extension = MimeTypeMap.getFileExtensionFromUrl(localFile.absolutePath)
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, "Open with")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(file: ReceivedFile) {
        viewModelScope.launch { repository.delete(file) }
    }

    fun deleteAll() {
        viewModelScope.launch { repository.deleteAll() }
    }
}
