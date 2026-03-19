// =============================================================================
// FILE: ReceiveActivity.kt
// Package: com.nearbyshare.ui.activities
// =============================================================================
// INDEX OF CONTENTS:
//   1. Activity setup with showOnLockScreen / turnScreenOn flags
//   2. Incoming session presentation as bottom sheet
//   3. Accept / Decline action forwarding to TransferViewModel
//
// OBJECTIVE:
//   Launched by NearbyShareService when an incoming transfer request
//   arrives from a remote peer. Even if the device screen is off,
//   this Activity wakes the display (android:turnScreenOn="true") and
//   shows an accept/decline dialog above the lock screen
//   (android:showOnLockScreen="true"), mirroring Nearby Share behaviour.
// =============================================================================

package com.nearbyshare.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.nearbyshare.databinding.ActivityReceiveBinding
import com.nearbyshare.ui.fragments.IncomingTransferBottomSheet
import com.nearbyshare.ui.viewmodels.TransferViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiveBinding
    private val viewModel: TransferViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Wake the screen and show above lock screen ────────────────────
        // On API 27+ these flags are set via the Window API.
        // On older devices the legacy LayoutParams flags achieve the same effect.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        binding = ActivityReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Show the incoming transfer bottom-sheet ───────────────────────
        if (savedInstanceState == null) {
            IncomingTransferBottomSheet()
                .show(supportFragmentManager, IncomingTransferBottomSheet.TAG)
        }
    }
}
