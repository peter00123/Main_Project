package com.atezhare.ui.testing

// TestingFragment.kt
// Testing page accessible from the bottom navigation bar.
// Shows:
//   - "testing" variable (empty, editable)
//   - Backend connection status (calls GET /auth/test)
//   - Logged-in user ID from SessionManager
// Depends on: TestingViewModel, utils/SessionManager

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.atezhare.databinding.FragmentTestingBinding
import com.atezhare.utils.SessionManager
import com.atezhare.network.WebURL


class TestingFragment : Fragment() {

    private var _binding: FragmentTestingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TestingViewModel by viewModels()
    val BASE_URL = WebURL.MAINURL



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val valueFromKt = WebURL.MAINURL
        binding.tvTestingValue.text = valueFromKt



        // Show user ID from session
        val sessionManager = SessionManager(requireContext())
        binding.tvUserId.text = sessionManager.getUserId()

        // Check backend connection on load
        viewModel.checkBackendConnection()

        // Retry button
        binding.btnCheckConnection.setOnClickListener {
            viewModel.checkBackendConnection()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Backend connection status
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            binding.tvConnectionStatus.text = status
            binding.tvConnectionStatus.setTextColor(
                if (status.contains("Connected"))
                    requireContext().getColor(android.R.color.holo_green_dark)
                else if (status.contains("Checking"))
                    requireContext().getColor(android.R.color.darker_gray)
                else
                    requireContext().getColor(android.R.color.holo_red_dark)
            )
        }

        // Testing variable value
        viewModel.testingValue.observe(viewLifecycleOwner) { value ->
            binding.tvTestingValue.text = if (value.isNullOrEmpty()) "(empty)" else value
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
