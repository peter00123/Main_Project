package com.atezhare.ui.shareddata

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.atezhare.data.ReceivedFile
import com.atezhare.databinding.FragmentReceivedFilesBinding

class ReceivedFilesFragment : Fragment() {

    private var _binding: FragmentReceivedFilesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedDataViewModel by viewModels()
    private lateinit var adapter: SharedDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceivedFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterTabs()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SharedDataAdapter(
            onItemClick = { viewModel.openFile(requireContext(), it) },
            onItemLongClick = { showDeleteDialog(it) }
        )
        binding.recyclerSharedFiles.adapter = adapter
    }

    private fun setupFilterTabs() {
        binding.btnFilterAll.setOnClickListener    { setFilter("all") }
        binding.btnFilterImages.setOnClickListener { setFilter("image/") }
        binding.btnFilterDocs.setOnClickListener   { setFilter("application/") }
        binding.btnFilterVideos.setOnClickListener { setFilter("video/") }
        setFilter("all")
    }

    private fun setFilter(filter: String) {
        viewModel.setFilter(filter)
        val dim = 0.4f
        binding.btnFilterAll.alpha    = if (filter == "all")          1f else dim
        binding.btnFilterImages.alpha = if (filter == "image/")       1f else dim
        binding.btnFilterDocs.alpha   = if (filter == "application/") 1f else dim
        binding.btnFilterVideos.alpha = if (filter == "video/")       1f else dim
    }

    private fun observeViewModel() {
        viewModel.fileList.observe(viewLifecycleOwner) { files ->
            Log.d("ReceiverUI", "Files updated = ${files?.size ?: 0}")
            adapter.submitList(files)
            val empty = files.isNullOrEmpty()
            binding.recyclerSharedFiles.visibility = if (empty) View.GONE else View.VISIBLE
            binding.layoutEmptyState.visibility    = if (empty) View.VISIBLE else View.GONE
            binding.tvFileCount.text = "${files?.size ?: 0} file(s) received"
        }
    }

    private fun showDeleteDialog(file: ReceivedFile) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove File")
            .setMessage("Remove \"${file.fileName}\" from Shared Data?")
            .setPositiveButton("Remove") { _, _ -> viewModel.deleteFile(file) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
