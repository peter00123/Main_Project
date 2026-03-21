package com.atezhare.ui.shareddata

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.atezhare.data.ReceivedFile
import com.atezhare.data.ReceivedFileRepository
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
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, file.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                context.startActivity(Intent.createChooser(
                    intent.apply { setDataAndType(uri, "*/*") }, "Open with"
                ))
            }
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
