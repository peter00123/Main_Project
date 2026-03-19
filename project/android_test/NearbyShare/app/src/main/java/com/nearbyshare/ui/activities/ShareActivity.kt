// =============================================================================
// FILE: ShareActivity.kt
// Package: com.nearbyshare.ui.activities
// =============================================================================
// INDEX OF CONTENTS:
//   1. Intent parsing (ACTION_SEND / ACTION_SEND_MULTIPLE)
//   2. Payload construction from incoming Intent data
//   3. Bottom-sheet style presentation of the device picker
//   4. ViewModel wiring for device selection and send initiation
//
// OBJECTIVE:
//   This Activity is registered as an Android share target, meaning it
//   appears in the system share sheet when the user taps "Share" in
//   any other app (Photos, Files, Chrome, etc.).
//
//   When launched, it:
//     1. Extracts the shared content from the incoming Intent
//     2. Wraps it in a SharePayload
//     3. Presents the NearbyShare device-picker BottomSheet
//     4. Initiates the transfer once the user taps a device
//
//   Styled as a translucent bottom-sheet Activity (dim behind it)
//   to match the Nearby Share UX on stock Android.
// =============================================================================

package com.nearbyshare.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nearbyshare.data.models.SharePayload
import com.nearbyshare.databinding.ActivityShareBinding
import com.nearbyshare.ui.fragments.DevicePickerBottomSheet
import com.nearbyshare.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShareBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Parse the incoming share Intent ──────────────────────────────
        val payload = parseShareIntent(intent)

        if (payload == null) {
            // Nothing usable in the Intent — bail out immediately
            finish()
            return
        }

        // ── Show the device picker bottom-sheet ───────────────────────────
        if (savedInstanceState == null) {
            DevicePickerBottomSheet.newInstance(payload)
                .show(supportFragmentManager, DevicePickerBottomSheet.TAG)
        }

        // ── Observe navigation/completion events ──────────────────────────
        observeNavigation()
    }

    // ============================================================
    // Intent Parsing
    // ============================================================

    /**
     * Extracts shared content from [intent] and wraps it as a [SharePayload].
     *
     * Handles three cases:
     *   1. ACTION_SEND with a stream URI (file/image/video/document)
     *   2. ACTION_SEND with plain text or URL
     *   3. ACTION_SEND_MULTIPLE with a list of URIs
     *
     * Returns null if the intent does not carry any shareable content.
     */
    private fun parseShareIntent(intent: Intent): SharePayload? {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                val mimeType = intent.type ?: "*/*"

                when {
                    uri != null -> {
                        // File share — resolve display name from ContentResolver
                        val displayName = resolveFileName(uri) ?: "Shared file"
                        val size = resolveFileSize(uri)
                        SharePayload.fromFile(uri, displayName, mimeType, size)
                    }
                    text != null -> {
                        // Text or URL share
                        if (text.startsWith("http://") || text.startsWith("https://")) {
                            SharePayload.fromUrl(text)
                        } else {
                            SharePayload.fromText(text)
                        }
                    }
                    else -> null
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                // Multiple files share (e.g. selecting multiple photos)
                @Suppress("UNCHECKED_CAST")
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris.isNullOrEmpty()) return null

                val totalSize = uris.sumOf { resolveFileSize(it) }
                SharePayload.fromMultipleFiles(uris, totalSize)
            }

            else -> null
        }
    }

    // ============================================================
    // Content Resolver Helpers
    // ============================================================

    /**
     * Queries the ContentResolver to get the display name of a file URI.
     * Works with content:// URIs from MediaStore and FileProvider.
     *
     * @param uri The content URI to query.
     * @return The filename string, or null if not resolvable.
     */
    private fun resolveFileName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex)
                else null
            }
        } catch (e: Exception) { null }
    }

    /**
     * Queries the ContentResolver to get the byte size of a file URI.
     *
     * @param uri The content URI to query.
     * @return File size in bytes, or 0 if not resolvable.
     */
    private fun resolveFileSize(uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (cursor.moveToFirst() && sizeIndex >= 0) cursor.getLong(sizeIndex)
                else 0L
            } ?: 0L
        } catch (e: Exception) { 0L }
    }

    // ============================================================
    // Navigation
    // ============================================================

    /**
     * Observes the ViewModel for the transfer-start event.
     * Once a transfer begins, the ShareActivity finishes itself
     * and the main NearbyShare app shows transfer progress.
     */
    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToTransfer.collect {
                    // Transfer started — close ShareActivity
                    finish()
                }
            }
        }
    }
}
