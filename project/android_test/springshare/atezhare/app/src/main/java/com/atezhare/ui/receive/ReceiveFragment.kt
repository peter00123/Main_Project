// ui/receive/ReceiveFragment.kt
// Receiver flow screen. Two pairing methods:
//   A) QR Code: Displays a generated QR code for the sender to scan
//   B) 6-digit Code: User enters the code shared by the sender and presses Submit
// On pairing success: polls session status until DONE, then shows success message
// Depends on: ReceiveViewModel (backend calls), utils/QrUtils (QR bitmap generation),
//             model/Models (SessionStatus), network/ApiService

package com.atezhare.ui.receive

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.atezhare.databinding.FragmentReceiveBinding
import com.atezhare.model.SessionStatus
import com.atezhare.utils.QrUtils

class ReceiveFragment : Fragment() {

    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!

    // ViewModel handles all backend communication for receiver flow
    // See ReceiveViewModel
    private val viewModel: ReceiveViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCodeInputBoxes()
        setupClickListeners()
        observeViewModel()

        // Request receiver QR from backend → POST /pair/receiver-qr
        viewModel.requestReceiverQr()
    }

    // ==================== QR DISPLAY ====================

    private fun observeViewModel() {
        // Render QR code bitmap from qrData string returned by backend
        // QR generation — see utils/QrUtils.generateQrBitmap()
        viewModel.qrData.observe(viewLifecycleOwner) { qrData ->
            if (!qrData.isNullOrEmpty()) {
                val bitmap = QrUtils.generateQrBitmap(qrData, 600)
                if (bitmap != null) {
                    binding.ivQrCode.setImageBitmap(bitmap)
                    binding.ivQrCode.visibility = View.VISIBLE
                    binding.tvQrLabel.visibility = View.VISIBLE
                } else {
                    binding.tvQrLabel.text = "QR generation failed"
                }
            }
        }

        // Session status updates — show loading/success/error states
        viewModel.sessionStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                SessionStatus.PAIRED -> {
                    showTransferStartingDialog()
                }
                SessionStatus.DONE -> {
                    binding.progressBar.visibility = View.GONE
                    showSuccessDialog()
                }
                SessionStatus.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Transfer failed. Please try again.", Toast.LENGTH_LONG).show()
                }
                SessionStatus.TRANSFERRING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvStatus.text = "Receiving files..."
                    binding.tvStatus.visibility = View.VISIBLE
                }
                else -> {}
            }
        }

        // Loading indicator
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== 6-DIGIT CODE INPUT ====================

    /**
     * Sets up the 6 individual EditText boxes for code entry.
     * Auto-advances focus box-to-box as digits are typed.
     * Uses the layout IDs et_code_1 through et_code_6 from fragment_receive.xml
     */
    private fun setupCodeInputBoxes() {
        val boxes = listOf(
            binding.etCode1, binding.etCode2, binding.etCode3,
            binding.etCode4, binding.etCode5, binding.etCode6
        )

        boxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Auto-advance to next box
                        if (index < boxes.size - 1) {
                            boxes[index + 1].requestFocus()
                        } else {
                            // Last digit entered — auto-submit
                            submitCode(boxes)
                        }
                    }
                }
            })

            // Handle backspace to go to previous box
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() && index > 0) {
                    boxes[index - 1].requestFocus()
                    true
                } else false
            }
        }
    }

    private fun setupClickListeners() {
        // Submit button triggers code validation and backend call
        binding.btnSubmitCode.setOnClickListener {
            val boxes = listOf(
                binding.etCode1, binding.etCode2, binding.etCode3,
                binding.etCode4, binding.etCode5, binding.etCode6
            )
            submitCode(boxes)
        }
    }

    /**
     * Collects digits from all 6 boxes, validates, and calls ReceiveViewModel.submitCode()
     * → POST /pair/submit-code
     */
    private fun submitCode(boxes: List<EditText>) {
        val code = boxes.joinToString("") { it.text.toString() }

        if (!QrUtils.isValidCode(code)) {
            Toast.makeText(requireContext(), "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            return
        }

        // Submit to backend — see ReceiveViewModel.submitCode()
        viewModel.submitCode(code)
    }

    // ==================== DIALOGS ====================

    private fun showTransferStartingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sender Connected")
            .setMessage("The sender has confirmed. Files will be transferred shortly.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Transfer Complete")
            .setMessage("Files received successfully!")
            .setPositiveButton("OK") { _, _ ->
                // Navigate back home
                activity?.onBackPressed()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
