// ui/home/HomeFragment.kt
// Landing page shown after login. Contains two primary action buttons:
//   - Send: navigates to DirectoryFragment (file selection before sending)
//   - Receive: navigates to ReceiveFragment (shows QR + code input)
// Depends on: navigation/nav_graph.xml (action IDs for navigation)

package com.atezhare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.atezhare.R
import com.atezhare.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBanner()
        setupClickListeners()
    }

    private fun setupBanner() {
        val bannerImages = listOf(
            R.drawable.banner_one,
            R.drawable.banner_two,
            R.drawable.banner_three
        )
        binding.viewPagerBanner.adapter = BannerAdapter(bannerImages)
    }

    private fun setupClickListeners() {
        // Send button → DirectoryFragment for file selection
        // Navigation defined in res/navigation/nav_graph.xml
        binding.btnSend.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_directory)
        }

        // Receive button → ReceiveFragment for QR display & code entry
        binding.btnReceive.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_receive)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
