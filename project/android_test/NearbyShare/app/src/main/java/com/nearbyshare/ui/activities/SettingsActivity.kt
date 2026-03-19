// =============================================================================
// FILE: SettingsActivity.kt
// Package: com.nearbyshare.ui.activities
// =============================================================================
// INDEX OF CONTENTS:
//   1. Activity setup with back-arrow toolbar
//   2. Hosting SettingsFragment (PreferenceFragmentCompat)
//
// OBJECTIVE:
//   Simple host activity for the NearbyShare settings screen.
//   Provides device name configuration, visibility mode selection,
//   and data usage preferences — matching the settings available
//   in Google's Nearby Share implementation.
// =============================================================================

package com.nearbyshare.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nearbyshare.R
import com.nearbyshare.databinding.ActivitySettingsBinding
import com.nearbyshare.ui.fragments.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Toolbar with back navigation ──────────────────────────────────
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)   // Show the ← back arrow
            title = getString(R.string.settings_title)
        }

        // ── Load settings fragment ────────────────────────────────────────
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    // Navigate back when the toolbar back arrow is pressed
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
