package com.atezhare.ui.shareddata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.atezhare.R
import com.atezhare.databinding.FragmentSharedDataBinding
import com.google.android.material.tabs.TabLayout

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
