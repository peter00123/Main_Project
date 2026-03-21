// ui/directory/DirectoryFragment.kt
// File directory screen. Allows user to:
//   - Tap (+) FAB to open system file picker
//   - See list of selected files with checkboxes
//   - Tap Send button to proceed to SendActivity with selected files
// Depends on: DirectoryViewModel (file list management), utils/FileUtils (URI resolution),
//             DirectoryAdapter (RecyclerView), ui/send/SendActivity (next step)

package com.atezhare.ui.directory

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.atezhare.databinding.FragmentDirectoryBinding
import com.atezhare.model.LocalFile
import com.atezhare.ui.send.SendActivity
import com.atezhare.utils.FileUtils

class DirectoryFragment : Fragment() {

    private var _binding: FragmentDirectoryBinding? = null
    private val binding get() = _binding!!

    // ViewModel manages the file list — see DirectoryViewModel
    private val viewModel: DirectoryViewModel by viewModels()

    // RecyclerView adapter — see DirectoryAdapter
    private lateinit var adapter: DirectoryAdapter

    // File picker launcher — opens Android system file chooser
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            // Handle single or multiple file selection
            val uris = mutableListOf<Uri>()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    uris.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                uris.add(data.data!!)
            }

            // Convert URIs to LocalFile objects — see utils/FileUtils.uriToLocalFile()
            uris.forEach { uri ->
                val localFile = FileUtils.uriToLocalFile(requireContext(), uri)
                if (localFile != null) {
                    viewModel.addFile(localFile)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDirectoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // DirectoryAdapter handles checkbox toggle callbacks
        adapter = DirectoryAdapter { file, isChecked ->
            viewModel.updateFileChecked(file, isChecked)
        }
        binding.recyclerFiles.adapter = adapter
    }

    private fun setupClickListeners() {
        // FAB (+) button opens file picker
        binding.fabAddFile.setOnClickListener {
            openFilePicker()
        }

        // Send button — passes selected files to SendActivity
        binding.btnSend.setOnClickListener {
            val selectedFiles = viewModel.getCheckedFiles()
            if (selectedFiles.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Launch SendActivity with the selected files as Parcelable list
            // SendActivity is standalone (no toolbar/bottom nav) — see ui/send/SendActivity
            val intent = Intent(requireContext(), SendActivity::class.java).apply {
                putParcelableArrayListExtra(SendActivity.EXTRA_FILES, ArrayList(selectedFiles))
            }
            startActivity(intent)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Files"))
    }

    private fun observeViewModel() {
        viewModel.fileList.observe(viewLifecycleOwner) { files ->
            adapter.submitList(files.toList())
            binding.tvEmptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
