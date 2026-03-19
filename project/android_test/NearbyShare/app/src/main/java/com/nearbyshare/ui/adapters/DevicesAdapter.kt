// =============================================================================
// FILE: DevicesAdapter.kt
// Package: com.nearbyshare.ui.adapters
// =============================================================================
// INDEX OF CONTENTS:
//   1. DiffUtil.ItemCallback for efficient list diffing
//   2. DeviceViewHolder — binds a single NearbyDevice to a card layout
//   3. DevicesAdapter — ListAdapter using the DiffUtil callback
//   4. Click handling via lambda passed from Fragment
//   5. Avatar circle drawing (initials + background color)
//   6. Device type icon selection
//   7. Connection state visual feedback (spinner, checkmark, progress)
//
// OBJECTIVE:
//   RecyclerView adapter that renders the list (or grid) of discovered
//   nearby devices. Each card shows:
//     • Circular avatar with the device's initials (coloured by device ID hash)
//     • Device name
//     • Device type icon (phone, tablet, laptop, watch)
//     • Signal strength indicator
//     • Connection state overlay (spinner while connecting, checkmark when done)
//   Uses ListAdapter + DiffUtil so only changed items are re-drawn,
//   keeping animations smooth during discovery.
// =============================================================================

package com.nearbyshare.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nearbyshare.R
import com.nearbyshare.data.models.ConnectionState
import com.nearbyshare.data.models.DeviceType
import com.nearbyshare.data.models.NearbyDevice
import com.nearbyshare.databinding.ItemDeviceCardBinding

/**
 * ListAdapter for nearby devices.
 *
 * @param onDeviceTapped Lambda invoked when the user taps a device card.
 *                       Receives the tapped [NearbyDevice] as its argument.
 */
class DevicesAdapter(
    private val onDeviceTapped: (NearbyDevice) -> Unit
) : ListAdapter<NearbyDevice, DevicesAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    // ============================================================
    // DiffUtil Callback
    // ============================================================

    /**
     * Tells RecyclerView how to compute the difference between two lists,
     * enabling efficient incremental updates instead of full redraws.
     */
    class DeviceDiffCallback : DiffUtil.ItemCallback<NearbyDevice>() {

        /**
         * Two devices are the "same item" if they share the same [NearbyDevice.id].
         * This determines whether to animate a move or just update in place.
         */
        override fun areItemsTheSame(oldItem: NearbyDevice, newItem: NearbyDevice): Boolean =
            oldItem.id == newItem.id

        /**
         * Two items have the "same content" if all their displayed fields are equal.
         * When this returns false, onBindViewHolder is called to refresh the view.
         */
        override fun areContentsTheSame(oldItem: NearbyDevice, newItem: NearbyDevice): Boolean =
            oldItem == newItem
    }

    // ============================================================
    // Adapter Overrides
    // ============================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        // Inflate the card layout using ViewBinding for type safety
        val binding = ItemDeviceCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ============================================================
    // ViewHolder
    // ============================================================

    inner class DeviceViewHolder(
        private val binding: ItemDeviceCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [NearbyDevice] to the card views.
         * Called by [onBindViewHolder] for each visible item.
         */
        fun bind(device: NearbyDevice) {

            // ── Device name ───────────────────────────────────────────────
            binding.tvDeviceName.text = device.name

            // ── Signal label (Close / Nearby / Far) ───────────────────────
            binding.tvSignalLabel.text = device.signalLabel()

            // ── Avatar circle with initials ───────────────────────────────
            // Background color is derived from the device ID hash so each
            // device gets a consistent, unique color across sessions.
            binding.tvAvatarInitials.text = device.resolvedInitials()
            binding.cvAvatar.setCardBackgroundColor(colorFromId(device.id))

            // ── Device type icon ──────────────────────────────────────────
            // Maps DeviceType enum to the appropriate drawable resource
            val iconRes = when (device.deviceType) {
                DeviceType.PHONE   -> R.drawable.ic_device_phone
                DeviceType.TABLET  -> R.drawable.ic_device_tablet
                DeviceType.LAPTOP  -> R.drawable.ic_device_laptop
                DeviceType.WATCH   -> R.drawable.ic_device_watch
                DeviceType.UNKNOWN -> R.drawable.ic_device_phone
            }
            binding.ivDeviceTypeIcon.setImageResource(iconRes)

            // ── Connection state overlay ──────────────────────────────────
            updateConnectionStateUi(device.connectionState, device.transferProgress)

            // ── Tap listener ──────────────────────────────────────────────
            binding.root.setOnClickListener {
                // Only allow tapping devices that aren't already busy
                if (device.connectionState == ConnectionState.DISCOVERED) {
                    onDeviceTapped(device)
                }
            }
        }

        /**
         * Updates the visual overlay on the card to reflect the device's
         * current connection/transfer state.
         *
         * States and their visuals:
         *   DISCOVERED      → No overlay, normal tap target
         *   CONNECTING      → Spinning progress indicator
         *   AWAITING_ACCEPT → Pulsing "waiting" icon
         *   TRANSFERRING    → Horizontal progress bar + percentage
         *   COMPLETED       → Green checkmark icon
         *   REJECTED/FAILED → Red error icon
         */
        private fun updateConnectionStateUi(state: ConnectionState, progress: Int) {
            // Hide all state views first, then show the relevant one
            binding.progressConnecting.visibility = View.GONE
            binding.ivStateIcon.visibility        = View.GONE
            binding.progressTransfer.visibility   = View.GONE

            when (state) {
                ConnectionState.DISCOVERED -> {
                    /* Normal state — no overlay */
                }

                ConnectionState.CONNECTING,
                ConnectionState.AWAITING_ACCEPT -> {
                    // Show a circular indeterminate spinner
                    binding.progressConnecting.visibility = View.VISIBLE
                }

                ConnectionState.TRANSFERRING -> {
                    // Show a determinate horizontal progress bar
                    binding.progressTransfer.visibility = View.VISIBLE
                    binding.progressTransfer.progress   = progress
                }

                ConnectionState.COMPLETED -> {
                    // Show green checkmark
                    binding.ivStateIcon.visibility = View.VISIBLE
                    binding.ivStateIcon.setImageResource(R.drawable.ic_check_circle)
                }

                ConnectionState.REJECTED,
                ConnectionState.FAILED -> {
                    // Show red error icon
                    binding.ivStateIcon.visibility = View.VISIBLE
                    binding.ivStateIcon.setImageResource(R.drawable.ic_error_circle)
                }

                else -> { /* no-op for DISCONNECTED */ }
            }
        }
    }

    // ============================================================
    // Helper: Avatar Color
    // ============================================================

    /**
     * Generates a deterministic pastel background color from a device ID string.
     * Using the hash ensures the same device always gets the same color,
     * which helps users identify familiar devices at a glance.
     *
     * The hue is spread across 360° using the hash; saturation and lightness
     * are fixed to keep the color visually consistent with Material Design.
     *
     * @param id The device's unique identifier string.
     * @return An ARGB color integer.
     */
    private fun colorFromId(id: String): Int {
        val hue = (id.hashCode().and(0xFFFFFF) % 360).toFloat()
        return Color.HSVToColor(floatArrayOf(hue, 0.45f, 0.85f))
    }
}
