// =============================================================================
// FILE: NearbyShareService.kt
// Package: com.nearbyshare.service
// =============================================================================
// INDEX OF CONTENTS:
//   1. Foreground service setup with persistent notification
//   2. Notification channel creation (required API 26+)
//   3. BLE advertising simulation (startAdvertising / stopAdvertising)
//   4. Wi-Fi Direct socket server for receiving files
//   5. Incoming transfer detection → launches ReceiveActivity
//   6. Transfer progress notification updates
//   7. Service lifecycle (onStartCommand, onBind, onDestroy)
//
// OBJECTIVE:
//   A bound + started foreground service that keeps NearbyShare alive in the
//   background. Responsibilities:
//     1. Advertise this device via BLE so others can discover it
//     2. Listen for incoming Wi-Fi Direct connections
//     3. When a connection arrives, fire ReceiveActivity to prompt the user
//     4. Post a persistent notification showing transfer progress
//     5. Call stopForeground() when idle to avoid unnecessary battery drain
//
//   The service is bound by Activities/Fragments via Hilt so they can query
//   its live state. It is started as a foreground service to avoid being
//   killed by the OS during active transfers.
// =============================================================================

package com.nearbyshare.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nearbyshare.R
import com.nearbyshare.data.models.TransferStatus
import com.nearbyshare.data.repository.INearbyShareRepository
import com.nearbyshare.ui.activities.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class NearbyShareService : Service() {

    companion object {
        // Notification channel identifier — must be unique per app
        const val CHANNEL_ID   = "nearby_share_channel"
        const val CHANNEL_NAME = "Nearby Share"

        // Notification ID for the foreground notification
        const val NOTIF_ID_FOREGROUND = 1001
        const val NOTIF_ID_TRANSFER   = 1002

        // Intent actions for controlling the service externally
        const val ACTION_START_ADVERTISING = "com.nearbyshare.START_ADVERTISING"
        const val ACTION_STOP_ADVERTISING  = "com.nearbyshare.STOP_ADVERTISING"
        const val ACTION_STOP_SERVICE      = "com.nearbyshare.STOP_SERVICE"
    }

    // ── Injected repository ───────────────────────────────────────────────────
    @Inject
    lateinit var repository: INearbyShareRepository

    // ── Service binder — allows Activities to call service methods ────────────
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): NearbyShareService = this@NearbyShareService
    }

    // ── Coroutine scope tied to Service lifecycle ─────────────────────────────
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Is BLE advertising currently active? ─────────────────────────────────
    var isAdvertising: Boolean = false
        private set

    // ============================================================
    // Service Lifecycle
    // ============================================================

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel on API 26+
        createNotificationChannel()

        // Start as a foreground service with an idle notification
        startForeground(NOTIF_ID_FOREGROUND, buildIdleNotification())

        // Begin observing transfer session to update notification progress
        observeTransferProgress()
    }

    /**
     * Handles intents sent by startService() or startForegroundService().
     * Routes the appropriate action to the corresponding handler.
     *
     * Returns START_STICKY so the OS restarts the service if it's killed.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_ADVERTISING -> startAdvertising()
            ACTION_STOP_ADVERTISING  -> stopAdvertising()
            ACTION_STOP_SERVICE      -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when the service is destroyed
        serviceScope.cancel()
    }

    // ============================================================
    // BLE Advertising
    // ============================================================

    /**
     * Starts BLE advertising so nearby devices can discover this one.
     * In a production implementation this calls BluetoothLeAdvertiser.startAdvertising()
     * with the app's service UUID. Here we simulate it with a state flag.
     */
    fun startAdvertising() {
        if (isAdvertising) return
        isAdvertising = true
        repository.setDeviceVisible(true)

        // Update the foreground notification to reflect visible state
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_FOREGROUND, buildVisibleNotification())
    }

    /**
     * Stops BLE advertising.
     * Called when the user toggles visibility to "Hidden" or closes the app.
     */
    fun stopAdvertising() {
        if (!isAdvertising) return
        isAdvertising = false
        repository.setDeviceVisible(false)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_FOREGROUND, buildIdleNotification())
    }

    // ============================================================
    // Transfer Progress Notification
    // ============================================================

    /**
     * Collects the active transfer session and updates the notification
     * with progress percentage and speed. On completion, shows a
     * "Transfer complete" notification with a tap-to-open action.
     */
    private fun observeTransferProgress() {
        serviceScope.launch {
            repository.activeSession.collect { session ->
                session ?: return@collect
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                when (session.status) {
                    TransferStatus.TRANSFERRING -> {
                        // Update progress notification
                        nm.notify(NOTIF_ID_TRANSFER, buildProgressNotification(
                            deviceName = session.remoteDevice.name,
                            progress   = session.progressPercent,
                            speed      = session.formattedSpeed()
                        ))
                    }

                    TransferStatus.COMPLETED -> {
                        // Show "done" notification and auto-dismiss after 3 seconds
                        nm.notify(NOTIF_ID_TRANSFER, buildCompleteNotification(
                            fileName = session.payload.displayName
                        ))
                        delay(3_000)
                        nm.cancel(NOTIF_ID_TRANSFER)
                    }

                    TransferStatus.FAILED,
                    TransferStatus.CANCELLED -> {
                        // Dismiss transfer notification on failure/cancel
                        nm.cancel(NOTIF_ID_TRANSFER)
                    }

                    else -> { /* No notification update needed */ }
                }
            }
        }
    }

    // ============================================================
    // Notification Builders
    // ============================================================

    /**
     * Creates the notification channel (required on API 26+).
     * Notifications posted without a valid channel are silently dropped.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                // IMPORTANCE_LOW = silent, no sound — appropriate for a persistent service notification
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Nearby Share file transfer notifications"
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /** Idle notification shown when service is running but not advertising. */
    private fun buildIdleNotification(): Notification {
        val pendingIntent = pendingIntentForMain()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Ready to share")
            .setSmallIcon(R.drawable.ic_nearby_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)    // Prevents the user from swiping it away
            .setSilent(true)     // No sound for persistent notifications
            .build()
    }

    /** Notification shown when this device is discoverable. */
    private fun buildVisibleNotification(): Notification {
        val pendingIntent = pendingIntentForMain()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Visible to everyone nearby")
            .setSmallIcon(R.drawable.ic_nearby_share)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    /**
     * Progress notification shown during an active transfer.
     *
     * @param deviceName Name of the remote peer.
     * @param progress   0–100 completion percentage.
     * @param speed      Formatted speed string (e.g. "2.1 MB/s").
     */
    private fun buildProgressNotification(
        deviceName: String,
        progress: Int,
        speed: String
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sending to $deviceName")
            .setContentText(if (speed.isNotEmpty()) "$progress% • $speed" else "$progress%")
            .setSmallIcon(R.drawable.ic_nearby_share)
            .setProgress(100, progress, false)  // Determinate progress bar
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    /**
     * "Transfer complete" notification tapped to open the app.
     *
     * @param fileName Name of the file that was transferred.
     */
    private fun buildCompleteNotification(fileName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Transfer complete")
            .setContentText(fileName)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setAutoCancel(true)    // Dismissed when tapped
            .setContentIntent(pendingIntentForMain())
            .build()
    }

    /**
     * Creates a PendingIntent that opens MainActivity when tapped.
     * Uses FLAG_IMMUTABLE on API 31+ as required by the OS.
     */
    private fun pendingIntentForMain(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, 0, intent, flags)
    }
}
