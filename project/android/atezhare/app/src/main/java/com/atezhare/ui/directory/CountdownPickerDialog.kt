package com.atezhare.ui.directory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.atezhare.databinding.DialogCountdownPickerBinding

class CountdownPickerDialog : DialogFragment() {

    private var _binding: DialogCountdownPickerBinding? = null
    private val binding get() = _binding!!

    interface CountdownPickerListener {
        fun onCountdownConfirmed(expiresAtMillis: Long)
    }

    var listener: CountdownPickerListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCountdownPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pickerDays.apply {
            minValue = 0
            maxValue = 30
        }
        binding.pickerHours.apply {
            minValue = 0
            maxValue = 23
        }
        binding.pickerMinutes.apply {
            minValue = 0
            maxValue = 59
        }

        binding.btnSendCountdown.setOnClickListener {
            val days = binding.pickerDays.value
            val hours = binding.pickerHours.value
            val minutes = binding.pickerMinutes.value

            val expiresAtMillis = System.currentTimeMillis() +
                    (days * 86400000L) +
                    (hours * 3600000L) +
                    (minutes * 60000L)

            listener?.onCountdownConfirmed(expiresAtMillis)
            dismiss()
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
