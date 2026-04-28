package com.atezhare.ui.profile

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.FragmentTestingBinding
import com.atezhare.network.WebURL
import com.atezhare.ui.testing.TestingViewModel
import com.atezhare.utils.SessionManager

class TestingActivity : AppCompatActivity() {

    private lateinit var binding: FragmentTestingBinding
    private val viewModel: TestingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentTestingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTestingValue.text = WebURL.MAINURL

        // Show user ID from session
        val sessionManager = SessionManager(this)
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
        viewModel.connectionStatus.observe(this) { status ->
            binding.tvConnectionStatus.text = status
            binding.tvConnectionStatus.setTextColor(
                if (status.contains("Connected"))
                    getColor(android.R.color.holo_green_dark)
                else if (status.contains("Checking"))
                    getColor(android.R.color.darker_gray)
                else
                    getColor(android.R.color.holo_red_dark)
            )
        }

        // Testing variable value
        viewModel.testingValue.observe(this) { value ->
            binding.tvTestingValue.text = if (value.isNullOrEmpty()) "(empty)" else value
        }
    }
}
