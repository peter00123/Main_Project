package com.qrshare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivityReceiveBinding

/**
 * Receiver's starting screen.
 * Prompts user to scan the sender's QR code to begin pairing.
 */
class ReceiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Receive Files"

        binding.btnScanQR.setOnClickListener {
            startActivity(Intent(this, QRScanActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
