package com.atezhare.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivityAboutDescriptionBinding

class AboutDescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.aboutText.text = """
            AtezShare is a fast peer-to-peer offline sharing platform
            designed for secure and lightweight file transfer between devices.
            It supports pairing-based transfer, image preview, progress tracking,
            deep-link pairing, and a clean minimal UI for high-speed local sharing.
        """.trimIndent()
    }
}
