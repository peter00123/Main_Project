// =============================================================================
// FILE: SettingsFragment.kt
// Package: com.nearbyshare.ui.fragments
// =============================================================================
// INDEX OF CONTENTS:
//   1. PreferenceFragmentCompat setup
//   2. Device name preference with edit dialog
//   3. Visibility mode preference (Everyone / Contacts / Hidden)
//   4. Data usage preference (Wi-Fi only / All networks)
//   5. SharedPreferences integration
//
// OBJECTIVE:
//   Settings screen that mirrors Nearby Share's configuration options.
//   Uses AndroidX PreferenceFragment for native-feeling preference tiles
//   with automatic SharedPreferences persistence.
//   Key settings:
//     - Device name shown to others during discovery
//     - Who can see this device (Everyone / Contacts / Hidden)
//     - Data usage (whether to use mobile data for transfers)
// =============================================================================

package com.nearbyshare.ui.fragments

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.nearbyshare.R

class SettingsFragment : PreferenceFragmentCompat() {

    // SharedPreferences key constants — used by the service and UI to read settings
    companion object {
        const val PREF_DEVICE_NAME      = "pref_device_name"
        const val PREF_VISIBILITY_MODE  = "pref_visibility_mode"
        const val PREF_WIFI_ONLY        = "pref_wifi_only"
        const val PREF_SHOW_NOTIFICATION= "pref_show_notification"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load preferences from XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupDeviceNamePref()
        setupVisibilityPref()
        setupDataPref()
    }

    // ============================================================
    // Preference Setup
    // ============================================================

    /**
     * Device name preference — shows current device model name by default.
     * Summary dynamically reflects the current value.
     */
    private fun setupDeviceNamePref() {
        findPreference<EditTextPreference>(PREF_DEVICE_NAME)?.apply {
            // Default to the system's device model name
            if (text.isNullOrEmpty()) {
                text = android.os.Build.MODEL
            }
            // Show the current device name as the summary
            summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }

    /**
     * Visibility mode preference — a dropdown list with three options:
     * "Everyone", "Your contacts", "Hidden".
     * Summary reflects the currently selected option.
     */
    private fun setupVisibilityPref() {
        findPreference<ListPreference>(PREF_VISIBILITY_MODE)?.apply {
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }

    /**
     * Data usage preference — a toggle for Wi-Fi-only vs all networks.
     * Default: Wi-Fi only (to avoid unexpected mobile data usage).
     */
    private fun setupDataPref() {
        findPreference<SwitchPreferenceCompat>(PREF_WIFI_ONLY)?.apply {
            // Ensure default is Wi-Fi only for safety
            if (!sharedPreferences!!.contains(PREF_WIFI_ONLY)) {
                isChecked = true
            }
        }
    }
}
