package com.qrshare.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qrshare.R
import com.qrshare.network.TransferState
import com.qrshare.sharing.FilePickerHelper

/**
 * Adapter for displaying per-file transfer progress in TransferActivity.
 */
class TransferItemAdapter(
    private val items: List<TransferItem>
) : RecyclerView.Adapter<TransferItemAdapter.TransferViewHolder>() {

    data class TransferItem(
        val fileName: String,
        val fileSize: Long,
        val progress: Int,
        val state: TransferState,
        val speed: String
    )

    inner class TransferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvTransferFileName)
        val tvMeta: TextView = itemView.findViewById(R.id.tvTransferMeta)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarFile)
        val tvStatus: TextView = itemView.findViewById(R.id.tvTransferStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transfer_file, parent, false)
        return TransferViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.fileName
        holder.tvMeta.text = FilePickerHelper.formatFileSize(item.fileSize)
        holder.progressBar.progress = item.progress

        when (item.state) {
            TransferState.COMPLETED -> {
                holder.tvStatus.text = "✓ Done"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.success_green))
                holder.progressBar.progress = 100
            }
            TransferState.FAILED -> {
                holder.tvStatus.text = "✗ Failed"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.error_red))
            }
            TransferState.IN_PROGRESS -> {
                holder.tvStatus.text = "${item.progress}%  ${item.speed}"
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.primary_blue))
            }
            else -> {
                holder.tvStatus.text = "Waiting..."
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
            }
        }
    }

    override fun getItemCount() = items.size
}
