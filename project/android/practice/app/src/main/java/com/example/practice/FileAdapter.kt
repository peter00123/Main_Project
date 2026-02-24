package com.example.practice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.practice.com.example.practice.FileItem

class FileAdapter(
    private val files: List<FileItem>,
    private val onItemClick: (FileItem) -> Unit   // ✅ NEW
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)

        init {
            // ✅ Click on row → preview
            view.setOnClickListener {
                onItemClick(files[adapterPosition])
            }

            // ❌ Prevent checkbox click from triggering preview
            checkBox.setOnClickListener { }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]

        holder.fileName.text = file.name
        holder.checkBox.isChecked = file.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            file.isSelected = isChecked
        }
    }

    override fun getItemCount() = files.size
}
