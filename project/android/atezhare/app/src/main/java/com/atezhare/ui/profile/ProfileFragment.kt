// ui/profile/ProfileFragment.kt
// Profile screen accessible from the bottom navigation bar.
// Currently shows the logged-in userId and a placeholder for future profile features.
// Depends on: utils/SessionManager (getUserId)

package com.atezhare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atezhare.databinding.FragmentProfileBinding
import com.atezhare.utils.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the logged-in userId — retrieved from utils/SessionManager
        val sessionManager = SessionManager(requireContext())
        binding.tvUserId.text = "User ID: ${sessionManager.getUserId()}"
        binding.tvAppVersion.text = "Atezhare v1.3"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
