package com.atezhare.ui.shareddata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.atezhare.databinding.FragmentLiveFilesBinding

class LiveFilesFragment : Fragment() {

    private var _binding: FragmentLiveFilesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LiveFilesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LiveFilesAdapter(
            onStopClick = { fileId -> viewModel.stopFile(fileId) },
            onCountdownExpired = { fileId -> viewModel.deleteExpiredCountdown(fileId) }
        )

        binding.recyclerSentFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSentFiles.adapter = adapter

        viewModel.activeSentFiles.observe(viewLifecycleOwner) { files ->
            adapter.submitList(files)
            binding.layoutEmptyState.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
