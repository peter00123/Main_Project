package com.atezhare.ui.shareddata

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atezhare.R
import com.atezhare.data.SentFile
import com.atezhare.databinding.ItemSentFileBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LiveFilesAdapter(
    private val onStopClick: (String) -> Unit,
    private val onCountdownExpired: (String) -> Unit
) : ListAdapter<SentFile, LiveFilesAdapter.FileViewHolder>(DiffCallback) {

    private val timers = mutableMapOf<String, CountDownTimer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSentFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: FileViewHolder) {
        super.onViewRecycled(holder)
        holder.boundFileId?.let { fileId ->
            timers[fileId]?.cancel()
            timers.remove(fileId)
        }
    }

    inner class FileViewHolder(private val binding: ItemSentFileBinding) : RecyclerView.ViewHolder(binding.root) {

        var boundFileId: String? = null

        fun bind(file: SentFile) {
            boundFileId = file.fileId
            binding.tvFileName.text = file.fileName
            binding.tvReceiver.text = "To: ${file.receiverId}"
            
            val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            binding.tvSentAt.text = "Sent at ${sdf.format(Date(file.sentAt))}"

            binding.tvModeBadge.text = file.mode
            if (file.mode == "COUNTDOWN") {
                binding.tvModeBadge.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                startTimer(file)
            } else {
                binding.tvModeBadge.setChipBackgroundColorResource(R.color.primary)
                binding.tvCountdown.text = "Live — active"
                binding.tvCountdown.setTextColor(ContextCompat.getColor(binding.root.context, R.color.primary))
            }

            binding.btnStop.setOnClickListener { 
                onStopClick(file.fileId) 
            }
        }

        private fun startTimer(file: SentFile) {
            timers[file.fileId]?.cancel()
            val millisLeft = (file.expiresAt ?: 0L) - System.currentTimeMillis()

            if (millisLeft > 0) {
                val timer = object : CountDownTimer(millisLeft, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvCountdown.text = formatTime(millisUntilFinished) + " left"
                        binding.tvCountdown.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.holo_orange_dark))
                    }

                    override fun onFinish() {
                        binding.tvCountdown.text = "Expired"
                        onCountdownExpired(file.fileId)
                    }
                }
                timer.start()
                timers[file.fileId] = timer
            } else {
                binding.tvCountdown.text = "Expired"
                onCountdownExpired(file.fileId)
            }
        }

        private fun formatTime(millis: Long): String {
            val days = TimeUnit.MILLISECONDS.toDays(millis)
            val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

            return when {
                days > 0 -> "${days}d ${hours}h ${minutes}m"
                hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                else -> "${minutes}m ${seconds}s"
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SentFile>() {
        override fun areItemsTheSame(oldItem: SentFile, newItem: SentFile) = oldItem.fileId == newItem.fileId
        override fun areContentsTheSame(oldItem: SentFile, newItem: SentFile) = oldItem == newItem
    }
}
