package com.atezhare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.atezhare.databinding.ItemFileBinding
import com.atezhare.model.FileItem

class FileAdapter(
    private val files: MutableList<FileItem>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(
        private val binding: ItemFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FileItem, position: Int) {
            binding.tvFileName.text = item.name
            binding.tvFileSize.text = item.size
            binding.btnRemove.setOnClickListener {
                onRemove(position)
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
        holder.bind(files[position], position)
    }

    override fun getItemCount(): Int = files.size
}
