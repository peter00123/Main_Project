package com.qrshare.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qrshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardSend.setOnClickListener {
            startActivity(Intent(this, SendActivity::class.java))
        }

        binding.cardReceive.setOnClickListener {
            startActivity(Intent(this, ReceiveActivity::class.java))
        }
    }
}
