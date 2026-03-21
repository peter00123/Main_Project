// ui/directory/DirectoryViewModel.kt
// Manages the list of selected files in the directory screen.
// Exposes fileList LiveData consumed by DirectoryFragment.
// Depends on: model/LocalFile (data class), utils/FileUtils (file resolution)

package com.atezhare.ui.directory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.atezhare.model.LocalFile

class DirectoryViewModel : ViewModel() {

    private val _fileList = MutableLiveData<MutableList<LocalFile>>(mutableListOf())
    val fileList: LiveData<MutableList<LocalFile>> = _fileList

    /** Add a new file to the directory list. Called by DirectoryFragment after file picker result. */
    fun addFile(file: LocalFile) {
        val current = _fileList.value ?: mutableListOf()
        // Avoid duplicates by path
        if (current.none { it.path == file.path }) {
            current.add(file)
            _fileList.value = current
        }
    }

    /** Toggle the checked state of a file. Called by DirectoryAdapter checkbox listener. */
    fun updateFileChecked(file: LocalFile, isChecked: Boolean) {
        val current = _fileList.value ?: return
        val index = current.indexOfFirst { it.path == file.path }
        if (index >= 0) {
            current[index] = current[index].copy(isChecked = isChecked)
            _fileList.value = current
        }
    }

    /** Returns only checked files — used by DirectoryFragment to pass to SendActivity */
    fun getCheckedFiles(): List<LocalFile> =
        _fileList.value?.filter { it.isChecked } ?: emptyList()

    /** Remove a file from the list */
    fun removeFile(file: LocalFile) {
        val current = _fileList.value ?: return
        current.removeAll { it.path == file.path }
        _fileList.value = current
    }
}
