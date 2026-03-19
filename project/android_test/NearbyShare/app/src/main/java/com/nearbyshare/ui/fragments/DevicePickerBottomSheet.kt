// =============================================================================
// FILE: DevicePickerBottomSheet.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. BottomSheetDialogFragment setup with fixed peek height
//   2. Payload display (file name, size, icon)
//   3. RecyclerView with discovered devices
//   4. Device tap → send payload via ViewModel
//   5. Companion newInstance factory with Parcelable argument
//
// OBJECTIVE:
//   The bottom sheet shown when NearbyShare is invoked via the system
//   share sheet (ShareActivity). It displays the content being shared
//   at the top, then lists all discovered nearby devices below.
//   Tapping a device calls MainViewModel.sendPayload() which initiates
//   the transfer and fires a navigation event to open TransferFragment.
// =============================================================================

package com.nearbyshare.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nearbyshare.data.models.SharePayload
import com.nearbyshare.databinding.BottomsheetDevicePickerBinding
import com.nearbyshare.ui.adapters.DevicesAdapter
import com.nearbyshare.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DevicePickerBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "DevicePickerBottomSheet"
        private const val ARG_PAYLOAD = "arg_payload"

        /**
         * Factory method that bundles the SharePayload as a Fragment argument.
         * Using a factory instead of a public constructor is the Android-recommended
         * pattern — it ensures arguments survive process death/recreation.
         */
        fun newInstance(payload: SharePayload): DevicePickerBottomSheet {
            return DevicePickerBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PAYLOAD, payload)
                }
            }
        }
    }

    private var _binding: BottomsheetDevicePickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var devicesAdapter: DevicesAdapter

    // The payload passed from ShareActivity via newInstance()
    private val payload: SharePayload? by lazy {
        @Suppress("DEPRECATION")
        arguments?.getParcelable(ARG_PAYLOAD)
    }

    // ============================================================
    // Fragment Lifecycle
    // ============================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetDevicePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPayloadPreview()
        setupRecyclerView()
        observeDevices()

        // Start scanning for devices as soon as the sheet appears
        viewModel.startScan()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ============================================================
    // Setup
    // ============================================================

    /**
     * Populates the payload preview card at the top of the sheet.
     * Shows the file name, size, and a type-appropriate icon.
     */
    private fun setupPayloadPreview() {
        payload?.let { p ->
            binding.tvShareFileName.text = p.displayName
            binding.tvShareFileSize.text = p.formattedSize()
        }
    }

    /**
     * Configures the device list RecyclerView.
     * Uses a vertical LinearLayoutManager (list style, not grid)
     * because the bottom sheet has limited vertical space.
     */
    private fun setupRecyclerView() {
        devicesAdapter = DevicesAdapter { device ->
            // User tapped a device — store selection and trigger send
            viewModel.selectDevice(device)
            payload?.let { viewModel.sendPayload(it) }
            dismiss() // Close the picker sheet; transfer screen will appear
        }

        binding.rvPickerDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = devicesAdapter
        }
    }

    // ============================================================
    // Observation
    // ============================================================

    private fun observeDevices() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    devicesAdapter.submitList(state.nearbyDevices)

                    // Show a scanning spinner while the list is empty
                    binding.progressPicker.visibility =
                        if (state.isScanning && state.nearbyDevices.isEmpty())
                            View.VISIBLE else View.GONE

                    // Show "Looking for devices…" while scanning
                    binding.tvLookingFor.visibility =
                        if (state.nearbyDevices.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
