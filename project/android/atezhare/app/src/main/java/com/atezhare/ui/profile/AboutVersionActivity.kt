package com.atezhare.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivityAboutVersionBinding

class AboutVersionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutVersionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutVersionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.versionDetails.text = """
            - Added deep link pairing
            - Added inbuilt image viewer
            - Added transfer progress system
            - Removed chat system
            - Improved navigation UI
            - Added screenshot protection
        """.trimIndent()
    }
}
