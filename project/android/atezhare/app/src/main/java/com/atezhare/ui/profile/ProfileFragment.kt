package com.atezhare.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.atezhare.R
import com.atezhare.databinding.FragmentProfileBinding
import com.atezhare.ui.auth.LoginActivity
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

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId() ?: "User"
        
        val displayName = if (userId.contains("@")) {
            userId.substringBefore("@")
        } else {
            userId.takeLast(6)
        }
        
        binding.tvUserName.text = displayName
        binding.tvUserEmail.text = userId
        binding.tvProfileLetter.text = displayName.firstOrNull()?.toString()?.uppercase() ?: "U"

        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val version = packageInfo.versionName
        binding.tvVersionProfile.text = "Version $version"
        
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.btnAbout.isEnabled = true
        binding.btnAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        binding.btnTesting.visibility = View.GONE

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        binding.btnTesting.setOnClickListener {
            findNavController().navigate(R.id.testingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
