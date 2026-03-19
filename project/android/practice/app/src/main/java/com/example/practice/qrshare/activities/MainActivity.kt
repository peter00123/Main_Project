package com.example.practice.qrshare.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.practice.databinding.ActivityMainBinding

/**
 * Entry point of the app. Shows Send and Receive buttons,
 * requests permissions on first launch.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQUEST_PERMISSIONS = 100
    }

    private val requiredPermissions: Array<String>
        get() {
            val perms = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                perms.add(Manifest.permission.READ_MEDIA_IMAGES)
                perms.add(Manifest.permission.READ_MEDIA_VIDEO)
                perms.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return perms.toTypedArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        checkAndRequestPermissions()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            if (allPermissionsGranted()) {
                startActivity(Intent(this, SendActivity::class.java))
            } else {
                checkAndRequestPermissions()
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReceive.setOnClickListener {
            if (allPermissionsGranted()) {
                startActivity(Intent(this, ReceiveActivity::class.java))
            } else {
                checkAndRequestPermissions()
                Toast.makeText(this, "Please grant all permissions first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val denied = grantResults.filter { it != PackageManager.PERMISSION_GRANTED }
            if (denied.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Some permissions were denied. App may not work correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
