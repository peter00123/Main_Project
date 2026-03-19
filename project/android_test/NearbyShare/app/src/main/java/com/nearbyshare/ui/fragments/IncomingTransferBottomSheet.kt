// =============================================================================
// FILE: IncomingTransferBottomSheet.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. BottomSheetDialogFragment for incoming transfers
//   2. Sender device info display
//   3. Payload preview (file name, size, type icon)
//   4. Accept / Decline button logic
//   5. TransferViewModel observation
//
// OBJECTIVE:
//   Shown on the RECEIVER side when a remote device initiates a transfer.
//   Launched by ReceiveActivity (which wakes the screen if needed).
//   Displays the sender's device name, the file being offered, and
//   two action buttons: Accept (green) and Decline (outlined).
//   Mirrors the accept/decline dialog in Google Nearby Share.
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nearbyshare.data.models.TransferStatus
import com.nearbyshare.databinding.BottomsheetIncomingTransferBinding
import com.nearbyshare.ui.viewmodels.TransferViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IncomingTransferBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "IncomingTransferBottomSheet"
    }

    private var _binding: BottomsheetIncomingTransferBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransferViewModel by activityViewModels()

    // ============================================================
    // Fragment Lifecycle
    // ============================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetIncomingTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeSession()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ============================================================
    // Setup
    // ============================================================

    private fun setupClickListeners() {
        // Accept button: green filled button at the bottom of the sheet
        binding.btnIncomingAccept.setOnClickListener {
            viewModel.acceptTransfer()
            dismiss()
        }

        // Decline button: outlined/text button
        binding.btnIncomingDecline.setOnClickListener {
            viewModel.declineTransfer()
            // Close ReceiveActivity after declining
            requireActivity().finish()
        }
    }

    // ============================================================
    // Observation
    // ============================================================

    /**
     * Populates the UI with sender info and payload details
     * as soon as the session becomes available.
     * Also dismisses the sheet automatically if the sender
     * cancels before the user responds.
     */
    private fun observeSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.session?.let { session ->
                        // ── Sender info ───────────────────────────────────
                        binding.tvSenderName.text    = session.remoteDevice.name
                        binding.tvSenderInitials.text = session.remoteDevice.resolvedInitials()

                        // ── Payload preview ───────────────────────────────
                        binding.tvIncomingFileName.text = session.payload.displayName
                        binding.tvIncomingFileSize.text = session.payload.formattedSize()

                        // ── Auto-dismiss on sender cancel / session fail ──
                        if (session.status in listOf(
                                TransferStatus.CANCELLED,
                                TransferStatus.FAILED
                            )
                        ) {
                            dismiss()
                        }
                    }
                }
            }
        }
    }
}
