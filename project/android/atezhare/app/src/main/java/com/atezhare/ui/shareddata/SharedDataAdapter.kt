package com.atezhare.ui.shareddata

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atezhare.data.ReceivedFile
import com.atezhare.databinding.ItemReceivedFileBinding
import com.atezhare.utils.FileUtils
import java.text.SimpleDateFormat
import java.util.*

class SharedDataAdapter(
    private val onItemClick: (ReceivedFile) -> Unit,
    private val onItemLongClick: (ReceivedFile) -> Unit
) : ListAdapter<ReceivedFile, SharedDataAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemReceivedFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: ReceivedFile) {
            // Handle deleted files first
            if (file.isDeleted) {
                Log.d("Adapter", "Showing deleted file = ${file.fileId}")
                binding.tvFileName.text = "[Deleted by sender]"
                binding.tvFileName.setTextColor(
                    ContextCompat.getColor(binding.root.context, com.atezhare.R.color.text_secondary)
                )
                binding.ivFileTypeIcon.alpha = 0.35f
                binding.tvSender.text = ""
                binding.tvFileSize.text = ""
                binding.tvReceivedAt.text = ""
                binding.tvNewBadge.visibility = View.GONE
                binding.root.isClickable = false
                binding.root.isFocusable = false
                binding.root.setOnClickListener(null)
                binding.root.setOnLongClickListener(null)
                return  // skip all other binding
            }

            binding.tvFileName.text = file.fileName
            binding.tvFileName.setTextColor(Color.BLACK)
            binding.ivFileTypeIcon.alpha = 1.0f
            binding.tvSender.text = "From: ${file.senderId}"
            binding.tvFileSize.text = FileUtils.formatFileSize(file.fileSize)
            binding.tvReceivedAt.text =
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(file.receivedAt))
            binding.tvNewBadge.visibility = if (file.isViewed) View.GONE else View.VISIBLE

            binding.ivFileTypeIcon.setImageResource(
                when {
                    file.mimeType.startsWith("image/") -> android.R.drawable.ic_menu_gallery
                    file.mimeType.startsWith("video/") -> android.R.drawable.ic_media_play
                    file.mimeType.contains("pdf")      -> android.R.drawable.ic_menu_agenda
                    else                               -> android.R.drawable.ic_menu_save
                }
            )

            binding.root.setOnClickListener { onItemClick(file) }
            binding.root.setOnLongClickListener { onItemLongClick(file); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReceivedFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<ReceivedFile>() {
        override fun areItemsTheSame(a: ReceivedFile, b: ReceivedFile) = a.id == b.id
        override fun areContentsTheSame(a: ReceivedFile, b: ReceivedFile) = a == b
    }
}
