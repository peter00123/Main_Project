package com.example.practice.archives.wifi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.practice.R

class TransferActivity : AppCompatActivity() {

    private lateinit var viewModel: TransferViewModel
    private var expiresAt: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        viewModel = ViewModelProvider(this)[TransferViewModel::class.java]

        val mode = intent.getStringExtra("MODE") ?: "RECEIVER"
        expiresAt = intent.getLongExtra("EXPIRES_AT", 0L)

        observeUi()
        startSessionTimer()

        viewModel.status.value =
            if (mode == "RECEIVER") "Waiting for sender..."
            else "Sending files..."
    }

    private fun observeUi() {


        print("hi")
    }





    private fun startSessionTimer() {
        handler.post(object : Runnable {
            override fun run() {
                val left = expiresAt - System.currentTimeMillis()

                if (left <= 0) {
                    finish()
                } else {
                    viewModel.timeLeft.value = left
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}