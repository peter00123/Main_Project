package com.atezhare.ui.shareddata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.atezhare.R
import com.atezhare.databinding.FragmentSharedDataBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class SharedDataFragment : Fragment() {

    private var _binding: FragmentSharedDataBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedDataViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharedDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTransferProgress()

        // LIBRARIAN CHECK: every time this screen opens, check for expired files
        // LibrarianRepository.checkAndDelete() finds files where deleteAt <= now,
        // deletes them from disk, marks them deleted in the received_files DB,
        // and removes them from the librarian table. No backend call needed.
        lifecycleScope.launch {
            try {
                val librarian = com.atezhare.data.LibrarianRepository(requireContext())
                val deleted = librarian.checkAndDelete()
                if (deleted > 0) {
                    android.util.Log.d("SharedData", "Librarian deleted $deleted expired file(s)")
                }
            } catch (e: Exception) {
                android.util.Log.e("SharedData", "Librarian check failed", e)
            }
        }

        setupTabs()
        observeViewModel()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(LiveFilesFragment())
                    1 -> replaceFragment(ReceivedFilesFragment())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Default to Received tab (index 1)
        binding.tabLayout.getTabAt(1)?.select()
        replaceFragment(ReceivedFilesFragment())
    }

    private fun observeTransferProgress() {
        com.atezhare.utils.TransferProgressManager.progress.observe(viewLifecycleOwner) { progress ->
            if (progress != null && progress.isDownloading) {
                binding.layoutProgress.cardProgress.visibility = View.VISIBLE
                binding.layoutProgress.tvProgressFilename.text = "Downloading: ${progress.fileName}"
                binding.layoutProgress.tvProgressPercent.text = "${progress.progress}%"
                binding.layoutProgress.progressIndicator.progress = progress.progress
                binding.layoutProgress.tvProgressSpeed.text = progress.speed
            } else {
                binding.layoutProgress.cardProgress.visibility = View.GONE
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.tab_container, fragment)
            .commit()
    }

    private fun observeViewModel() {
        viewModel.unviewedCount.observe(viewLifecycleOwner) { count ->
            binding.tvNewCount.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.tvNewCount.text = "$count new"
        }
    }

    override fun onStart() {
        super.onStart()
        // Prevent screenshots when this fragment is visible
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onStop() {
        super.onStop()
        // Allow screenshots again when leaving this screen
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
