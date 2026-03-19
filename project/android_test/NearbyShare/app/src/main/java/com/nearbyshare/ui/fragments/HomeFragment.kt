// =============================================================================
// FILE: HomeFragment.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. Fragment setup with ViewBinding
//   2. RecyclerView initialisation with DevicesAdapter
//   3. Visibility toggle switch wiring
//   4. Scan button and loading indicator
//   5. ViewModel state collection (devices list, scanning state, visibility)
//   6. Device tap → TransferBottomSheet launch
//
// OBJECTIVE:
//   The main "discovery" screen — the first thing the user sees when they
//   open the app. Mirrors the Nearby Share home screen with:
//     • A large device avatar at the top representing THIS device
//     • A visibility toggle ("Everyone / Contacts / Hidden")
//     • A pulsing scan animation while searching
//     • A grid of discovered nearby device cards
//   When the user taps a discovered device, it opens the
//   DevicePickerBottomSheet (or TransferBottomSheet directly if a
//   payload is already queued).
// =============================================================================

package com.nearbyshare.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.nearbyshare.R
import com.nearbyshare.databinding.FragmentHomeBinding
import com.nearbyshare.ui.adapters.DevicesAdapter
import com.nearbyshare.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    // ── ViewBinding ───────────────────────────────────────────────────────────
    // _binding is nullable to guard against access after onDestroyView()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ── ViewModel shared with the parent Activity ─────────────────────────────
    private val viewModel: MainViewModel by activityViewModels()

    // ── RecyclerView adapter for the discovered-devices grid ──────────────────
    private lateinit var devicesAdapter: DevicesAdapter

    // ============================================================
    // Fragment Lifecycle
    // ============================================================

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

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null out binding to prevent memory leaks after fragment view is destroyed
        _binding = null
    }

    // ============================================================
    // Setup
    // ============================================================

    /**
     * Configures the RecyclerView with a 2-column grid layout and
     * attaches the DevicesAdapter. Device taps are forwarded to the
     * ViewModel which triggers the transfer flow.
     */
    private fun setupRecyclerView() {
        devicesAdapter = DevicesAdapter { device ->
            // User tapped a device card
            viewModel.selectDevice(device)
            // Show bottom sheet for sending
            TransferBottomSheet.newInstance()
                .show(childFragmentManager, TransferBottomSheet.TAG)
        }

        binding.rvDevices.apply {
            // 2-column grid matches Nearby Share's device card layout
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = devicesAdapter
            // Disable change animations so progress bar updates are smooth
            itemAnimator = null
        }
    }

    /**
     * Wires all button click and switch change listeners.
     */
    private fun setupClickListeners() {
        // "Scan" / Refresh FAB
        binding.fabScan.setOnClickListener {
            viewModel.startScan()
        }

        // Visibility toggle switch
        binding.switchVisibility.setOnCheckedChangeListener { _, _ ->
            viewModel.toggleVisibility()
        }

        // Settings gear icon
        binding.ivSettings.setOnClickListener {
            // Navigate to settings — handled by NavController in parent Activity
            // or open SettingsActivity directly
            startActivity(
                android.content.Intent(requireContext(),
                    com.nearbyshare.ui.activities.SettingsActivity::class.java)
            )
        }
    }

    // ============================================================
    // ViewModel Observation
    // ============================================================

    /**
     * Collects UI state updates from the ViewModel using the
     * repeatOnLifecycle pattern, which automatically stops collection
     * when the Fragment is stopped and resumes when it starts again.
     * This prevents unnecessary work and avoids crashes from
     * accessing a destroyed view.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // ── Device list ──────────────────────────────────────
                    devicesAdapter.submitList(state.nearbyDevices)

                    // Show the empty-state illustration when no devices found
                    binding.layoutEmptyState.visibility =
                        if (state.nearbyDevices.isEmpty() && !state.isScanning)
                            View.VISIBLE else View.GONE

                    // ── Scanning indicator ───────────────────────────────
                    // Show a pulsing ring + progress bar while scan is active
                    binding.layoutScanning.visibility =
                        if (state.isScanning) View.VISIBLE else View.GONE

                    binding.progressScanning.visibility =
                        if (state.isScanning) View.VISIBLE else View.GONE

                    // ── Visibility toggle ────────────────────────────────
                    // Update the switch without triggering the listener
                    binding.switchVisibility.setOnCheckedChangeListener(null)
                    binding.switchVisibility.isChecked = state.isDeviceVisible
                    binding.switchVisibility.setOnCheckedChangeListener { _, _ ->
                        viewModel.toggleVisibility()
                    }

                    // Update visibility label text
                    binding.tvVisibilityStatus.text =
                        if (state.isDeviceVisible)
                            getString(R.string.visibility_everyone)
                        else
                            getString(R.string.visibility_hidden)
                }
            }
        }
    }
}
