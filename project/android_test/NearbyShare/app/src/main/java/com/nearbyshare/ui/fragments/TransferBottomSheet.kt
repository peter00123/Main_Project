// =============================================================================
// FILE: TransferBottomSheet.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. BottomSheetDialogFragment setup
//   2. Selected device display
//   3. Send confirmation button
//   4. Progress state observation
//
// OBJECTIVE:
//   Shown from HomeFragment when the user taps a discovered device
//   WITHOUT a pre-queued payload (i.e., opened the app directly rather
//   than via the share sheet). Lets the user confirm they want to send
//   to the selected device, then opens the file picker to choose content.
//   Once the payload is selected, it calls MainViewModel.sendPayload()
//   and collapses itself so TransferFragment can take over.
// =============================================================================

package com.nearbyshare.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nearbyshare.data.models.SharePayload
import com.nearbyshare.databinding.BottomsheetTransferBinding
import com.nearbyshare.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TransferBottomSheet"

        fun newInstance() = TransferBottomSheet()
    }

    private var _binding: BottomsheetTransferBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    // ── File picker launcher ──────────────────────────────────────────────────
    // Opens the system document picker and returns selected URIs
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            // Collect all selected URIs (single or multiple)
            val uris = mutableListOf<Uri>()
            data.clipData?.let { clip ->
                // Multiple files selected via EXTRA_ALLOW_MULTIPLE
                for (i in 0 until clip.itemCount) {
                    uris.add(clip.getItemAt(i).uri)
                }
            } ?: data.data?.let { uri ->
                // Single file selected
                uris.add(uri)
            }

            if (uris.isNotEmpty()) {
                // Build payload and kick off the transfer
                val payload = if (uris.size == 1) {
                    val uri = uris.first()
                    val name = resolveFileName(uri) ?: "File"
                    val size = resolveFileSize(uri)
                    SharePayload.fromFile(uri, name, sizeBytes = size)
                } else {
                    val totalSize = uris.sumOf { resolveFileSize(it) }
                    SharePayload.fromMultipleFiles(uris, totalSize)
                }
                viewModel.sendPayload(payload)
                dismiss()
            }
        }
    }

    // ============================================================
    // Fragment Lifecycle
    // ============================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeSelectedDevice()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ============================================================
    // Setup
    // ============================================================

    private fun setupClickListeners() {
        // "Choose files" button opens the system file picker
        binding.btnChooseFiles.setOnClickListener {
            openFilePicker()
        }

        // Cancel button dismisses the sheet
        binding.btnTransferCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Launches the system document picker allowing multi-select.
     * The ACTION_OPEN_DOCUMENT intent is preferred over ACTION_GET_CONTENT
     * because it grants persistent URI permissions.
     */
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"                              // Accept any file type
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple selection
        }
        filePickerLauncher.launch(intent)
    }

    // ============================================================
    // Observation
    // ============================================================

    /**
     * Shows the selected device's name at the top of the sheet
     * so the user knows who they're sending to.
     */
    private fun observeSelectedDevice() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.selectedDevice?.let { device ->
                        binding.tvSendToDevice.text = "Send to ${device.name}"
                        binding.tvDeviceInitialsTransfer.text = device.resolvedInitials()
                    }
                }
            }
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun resolveFileName(uri: Uri): String? = try {
        requireContext().contentResolver.query(
            uri, null, null, null, null
        )?.use { cursor ->
            val col = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && col >= 0) cursor.getString(col) else null
        }
    } catch (e: Exception) { null }

    private fun resolveFileSize(uri: Uri): Long = try {
        requireContext().contentResolver.query(
            uri, null, null, null, null
        )?.use { cursor ->
            val col = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst() && col >= 0) cursor.getLong(col) else 0L
        } ?: 0L
    } catch (e: Exception) { 0L }
}
