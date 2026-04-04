// ui/directory/DirectoryAdapter.kt
// RecyclerView adapter for the file list in DirectoryFragment.
// Each item shows: file name, file size, and a checkbox (checked by default).
// Checkbox state changes are reported back via onCheckedChange callback.
// Depends on: model/LocalFile (data), utils/FileUtils (formatFileSize),
//             res/layout/item_file.xml (item layout)

package com.atezhare.ui.directory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atezhare.databinding.ItemFileBinding
import com.atezhare.model.LocalFile
import com.atezhare.utils.FileUtils
import com.bumptech.glide.Glide
import java.io.File

class DirectoryAdapter(
    private val onCheckedChange: (LocalFile, Boolean) -> Unit
) : ListAdapter<LocalFile, DirectoryAdapter.FileViewHolder>(FileDiffCallback()) {

    inner class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: LocalFile) {
            binding.tvFileName.text = file.name
            // Format file size — see utils/FileUtils.formatFileSize()
            binding.tvFileSize.text = FileUtils.formatFileSize(file.size)

            // Prevent checkbox listener firing during bind
            binding.cbFileSelected.setOnCheckedChangeListener(null)
            binding.cbFileSelected.isChecked = file.isChecked

            // Report check state change back to DirectoryViewModel via DirectoryFragment
            binding.cbFileSelected.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(file, isChecked)
            }

            // Set file type icon or image preview based on MIME type
            if (file.mimeType.startsWith("image/")) {
                Glide.with(binding.ivFileIcon.context)
                    .load(File(file.path))
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivFileIcon)
            } else {
                val iconRes = when {
                    file.mimeType.startsWith("video/") -> android.R.drawable.ic_media_play
                    file.mimeType.contains("pdf") -> android.R.drawable.ic_menu_agenda
                    else -> android.R.drawable.ic_menu_save
                }
                binding.ivFileIcon.setImageResource(iconRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FileDiffCallback : DiffUtil.ItemCallback<LocalFile>() {
        override fun areItemsTheSame(oldItem: LocalFile, newItem: LocalFile) =
            oldItem.path == newItem.path

        override fun areContentsTheSame(oldItem: LocalFile, newItem: LocalFile) =
            oldItem == newItem
    }
}
