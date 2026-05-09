package com.atezhare.ui.directory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.atezhare.databinding.DialogSendModeBinding

class SendModeDialog : DialogFragment() {

    private var _binding: DialogSendModeBinding? = null
    private val binding get() = _binding!!

    interface SendModeListener {
//        fun onLiveSelected()
        fun onCountdownSelected(expiresAtMillis: Long)
    }

    var listener: SendModeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSendModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.cardLive.setOnClickListener {
//            listener?.onLiveSelected()
//            dismiss()
//        }

        binding.cardCountdown.setOnClickListener {
            val countdownPicker = CountdownPickerDialog()
            countdownPicker.listener = object : CountdownPickerDialog.CountdownPickerListener {
                override fun onCountdownConfirmed(expiresAtMillis: Long) {
                    listener?.onCountdownSelected(expiresAtMillis)
                    dismiss()
                }
            }
            countdownPicker.show(parentFragmentManager, "countdown_picker")
        }

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
