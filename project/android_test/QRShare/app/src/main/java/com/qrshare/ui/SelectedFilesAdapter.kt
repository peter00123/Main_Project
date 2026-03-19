package com.qrshare.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrshare.R
import com.qrshare.network.TransferFile
import com.qrshare.sharing.FilePickerHelper

/**
 * Adapter for displaying selected files in SendActivity before sending.
 */
class SelectedFilesAdapter(
    private val files: MutableList<TransferFile>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveFile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.tvFileName.text = file.name
        holder.tvFileSize.text = FilePickerHelper.formatFileSize(file.size)
        holder.ivFileIcon.setImageResource(getMimeIcon(file.mimeType))
        holder.btnRemove.setOnClickListener { onRemove(holder.adapterPosition) }
    }

    override fun getItemCount() = files.size

    private fun getMimeIcon(mimeType: String): Int {
        return when {
            mimeType.startsWith("image/") -> android.R.drawable.ic_menu_gallery
            mimeType.startsWith("video/") -> android.R.drawable.ic_media_play
            mimeType.startsWith("audio/") -> android.R.drawable.ic_lock_silent_mode_off
            mimeType == "application/pdf" -> android.R.drawable.ic_menu_agenda
            else -> android.R.drawable.ic_menu_save
        }
    }
}
