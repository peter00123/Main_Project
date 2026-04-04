package com.atezhare.ui.shareddata

import android.graphics.Color
import android.os.CountDownTimer
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
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SharedDataAdapter(
    private val onItemClick: (ReceivedFile) -> Unit,
    private val onItemLongClick: (ReceivedFile) -> Unit
) : ListAdapter<ReceivedFile, SharedDataAdapter.ViewHolder>(DiffCallback()) {

    private val timers = mutableMapOf<String, CountDownTimer>()

    inner class ViewHolder(private val binding: ItemReceivedFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var boundFileId: String? = null

        fun bind(file: ReceivedFile) {
            boundFileId = file.fileId
            
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
                binding.tvCountdown.visibility = View.GONE
                binding.root.isClickable = false
                binding.root.isFocusable = false
                binding.root.setOnClickListener(null)
                binding.root.setOnLongClickListener(null)
                return
            }

            binding.tvFileName.text = file.fileName
            binding.tvFileName.setTextColor(Color.BLACK) 
            binding.ivFileTypeIcon.alpha = 1.0f
            binding.tvSender.text = "From: ${file.senderId}"
            binding.tvFileSize.text = FileUtils.formatFileSize(file.fileSize)
            binding.tvReceivedAt.text =
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(file.receivedAt))
            binding.tvNewBadge.visibility = if (file.isViewed) View.GONE else View.VISIBLE

            // Image preview or icon
            if (file.mimeType.startsWith("image/")) {
                Glide.with(binding.ivFileTypeIcon.context)
                    .load(File(file.localPath))
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivFileTypeIcon)
            } else {
                binding.ivFileTypeIcon.setImageResource(
                    when {
                        file.mimeType.startsWith("video/") -> android.R.drawable.ic_media_play
                        file.mimeType.contains("pdf")      -> android.R.drawable.ic_menu_agenda
                        else                               -> android.R.drawable.ic_menu_save
                    }
                )
            }

            // Timer display for Countdown Mode
            if (file.mode == "COUNTDOWN" && file.expiresAt != null) {
                binding.tvCountdown.visibility = View.VISIBLE
                startTimer(file)
            } else {
                binding.tvCountdown.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(file) }
            binding.root.setOnLongClickListener { onItemLongClick(file); true }
        }

        private fun startTimer(file: ReceivedFile) {
            timers[file.fileId]?.cancel()
            val millisLeft = (file.expiresAt ?: 0L) - System.currentTimeMillis()

            if (millisLeft > 0) {
                val timer = object : CountDownTimer(millisLeft, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvCountdown.text = formatTime(millisUntilFinished)
                    }

                    override fun onFinish() {
                        binding.tvCountdown.text = "Expired"
                        // The actual deletion is handled by SharedDataViewModel's monitoring loop
                    }
                }
                timer.start()
                timers[file.fileId] = timer
            } else {
                binding.tvCountdown.text = "Expired"
            }
        }

        private fun formatTime(millis: Long): String {
            val days = TimeUnit.MILLISECONDS.toDays(millis)
            val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
            return when {
                days > 0 -> "${days}d ${hours}h"
                hours > 0 -> "${hours}h ${minutes}m"
                else -> "${minutes}m ${seconds}s"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReceivedFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.boundFileId?.let { fileId ->
            timers[fileId]?.cancel()
            timers.remove(fileId)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReceivedFile>() {
        override fun areItemsTheSame(a: ReceivedFile, b: ReceivedFile) = a.id == b.id
        override fun areContentsTheSame(a: ReceivedFile, b: ReceivedFile) = a == b
    }
}
