package com.atezhare.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.versionText.text = "Version $versionName"

        binding.btnAboutApp.setOnClickListener {
            startActivity(Intent(this, AboutDescriptionActivity::class.java))
        }

        binding.btnAboutVersion.setOnClickListener {
            startActivity(Intent(this, AboutVersionActivity::class.java))
        }

        binding.btnTesting.setOnClickListener {
            val intent = Intent(this, TestingActivity::class.java)
            startActivity(intent)
        }
    }
}
