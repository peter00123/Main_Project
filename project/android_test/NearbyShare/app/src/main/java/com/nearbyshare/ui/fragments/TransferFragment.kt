// =============================================================================
// FILE: TransferFragment.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. Fragment setup and ViewBinding
//   2. Progress bar and status label observation
//   3. Speed and ETA display
//   4. Accept / Decline / Cancel / Done button wiring
//   5. Success / failure animation triggers
//   6. Back navigation on completion
//
// OBJECTIVE:
//   Full-screen transfer progress view shown during an active send or receive.
//   Mirrors the Nearby Share transfer screen which shows:
//     • Device name + avatar of the remote peer
//     • File name and size
//     • Animated circular progress ring
//     • Current speed and time remaining
//     • Accept/Decline buttons (receiver side only)
//     • Cancel button (both sides, while in progress)
//     • "Done" button and success animation on completion
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
import androidx.navigation.fragment.findNavController
import com.nearbyshare.R
import com.nearbyshare.databinding.FragmentTransferBinding
import com.nearbyshare.ui.viewmodels.TransferViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferFragment : Fragment() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    // TransferViewModel is scoped to the Activity so both sender/receiver
    // screens share the same session state.
    private val viewModel: TransferViewModel by activityViewModels()

    // ============================================================
    // Fragment Lifecycle
    // ============================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ============================================================
    // Click Listeners
    // ============================================================

    private fun setupClickListeners() {
        // Accept an incoming transfer (receiver side)
        binding.btnAccept.setOnClickListener {
            viewModel.acceptTransfer()
        }

        // Decline an incoming transfer (receiver side)
        binding.btnDecline.setOnClickListener {
            viewModel.declineTransfer()
        }

        // Cancel an in-progress transfer (both sides)
        binding.btnCancel.setOnClickListener {
            viewModel.cancelTransfer()
        }

        // "Done" — navigate back to home after terminal state
        binding.btnDone.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // ============================================================
    // ViewModel Observation
    // ============================================================

    /**
     * Collects [TransferUiState] and updates every view in the layout:
     *   - Device name / avatar
     *   - File name + size
     *   - Circular progress ring
     *   - Speed + ETA labels
     *   - Status text
     *   - Visible/hidden button set
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    // ── Device info ─────────────────────────────────────
                    state.session?.remoteDevice?.let { device ->
                        binding.tvDeviceName.text = device.name
                        binding.tvDeviceInitials.text = device.resolvedInitials()
                    }

                    // ── Payload info ────────────────────────────────────
                    state.session?.payload?.let { payload ->
                        binding.tvFileName.text    = payload.displayName
                        binding.tvFileSize.text    = state.formattedSize
                    }

                    // ── Progress ring ───────────────────────────────────
                    binding.progressRing.progress = state.progressPercent
                    binding.tvProgressPercent.text = "${state.progressPercent}%"

                    // ── Speed + ETA ─────────────────────────────────────
                    binding.tvSpeed.text      = state.formattedSpeed
                    binding.tvEta.text        = state.estimatedTime
                    binding.tvSpeed.visibility =
                        if (state.formattedSpeed.isNotEmpty()) View.VISIBLE else View.GONE
                    binding.tvEta.visibility  =
                        if (state.estimatedTime.isNotEmpty()) View.VISIBLE else View.GONE

                    // ── Status label ────────────────────────────────────
                    binding.tvStatus.text = state.statusLabel

                    // ── Button visibility ───────────────────────────────
                    // Accept / Decline shown only to the receiver
                    binding.btnAccept.visibility  =
                        if (state.showAcceptDecline) View.VISIBLE else View.GONE
                    binding.btnDecline.visibility =
                        if (state.showAcceptDecline) View.VISIBLE else View.GONE

                    // Cancel shown while transfer is live
                    binding.btnCancel.visibility  =
                        if (state.showCancel) View.VISIBLE else View.GONE

                    // Done shown after terminal state
                    binding.btnDone.visibility    =
                        if (state.showDone) View.VISIBLE else View.GONE

                    // ── Success / failure icon ──────────────────────────
                    // Swap the progress ring for a completion icon when done
                    val isCompleted = state.session?.isTerminal == true
                    binding.progressRing.visibility =
                        if (isCompleted) View.GONE else View.VISIBLE
                    binding.ivResultIcon.visibility =
                        if (isCompleted) View.VISIBLE else View.GONE

                    if (isCompleted) {
                        val iconRes = when {
                            state.statusLabel.contains("complete") ->
                                R.drawable.ic_check_circle
                            else ->
                                R.drawable.ic_error_circle
                        }
                        binding.ivResultIcon.setImageResource(iconRes)
                    }
                }
            }
        }
    }
}
