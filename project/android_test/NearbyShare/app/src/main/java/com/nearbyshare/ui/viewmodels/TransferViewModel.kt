// =============================================================================
// FILE: TransferViewModel.kt
// Package: com.nearbyshare.ui.viewmodels
// =============================================================================
// INDEX OF CONTENTS:
//   1. TransferUiState data class
//   2. TransferViewModel with Hilt injection
//   3. Session observation and progress computation
//   4. Accept / decline / cancel actions
//
// OBJECTIVE:
//   ViewModel for the transfer-progress screen (TransferFragment).
//   Observes the active TransferSession from the repository and maps it
//   into a TransferUiState that the fragment can render directly —
//   computing formatted strings (speed, ETA, size) and determining
//   which UI components are visible (progress bar, action buttons, icon).
// =============================================================================

package com.nearbyshare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearbyshare.data.models.TransferSession
import com.nearbyshare.data.models.TransferStatus
import com.nearbyshare.data.repository.INearbyShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Distilled, display-ready state for the transfer progress screen.
 *
 * @property session           The raw session model (may be null before first update).
 * @property progressPercent   0–100 integer for the ProgressBar.
 * @property formattedSpeed    "2.1 MB/s" or "" if not transferring.
 * @property estimatedTime     "12s left" or "" if not deterministic.
 * @property formattedSize     "3.2 MB" or "" for text payloads.
 * @property showAcceptDecline True on receiver side in AWAITING_USER state.
 * @property showCancel        True while transfer is in progress.
 * @property showDone          True when transfer reaches a terminal state.
 * @property statusLabel       Short human-readable status string.
 */
data class TransferUiState(
    val session: TransferSession? = null,
    val progressPercent: Int = 0,
    val formattedSpeed: String = "",
    val estimatedTime: String = "",
    val formattedSize: String = "",
    val showAcceptDecline: Boolean = false,
    val showCancel: Boolean = false,
    val showDone: Boolean = false,
    val statusLabel: String = "Preparing…"
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val repository: INearbyShareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        // Observe active session and map to display-ready state
        viewModelScope.launch {
            repository.activeSession.collect { session ->
                _uiState.value = mapSessionToUiState(session)
            }
        }
    }

    // ============================================================
    // State Mapping
    // ============================================================

    /**
     * Converts a raw [TransferSession] into a flat [TransferUiState]
     * with pre-computed display strings and boolean visibility flags.
     *
     * This keeps all formatting logic out of the Fragment,
     * making it easier to unit-test without Android framework.
     */
    private fun mapSessionToUiState(session: TransferSession?): TransferUiState {
        session ?: return TransferUiState()

        // Determine human-readable status label per session state
        val label = when (session.status) {
            TransferStatus.PENDING          -> "Preparing…"
            TransferStatus.CONNECTING       -> "Connecting to ${session.remoteDevice.name}…"
            TransferStatus.AWAITING_ACCEPT  -> "Waiting for ${session.remoteDevice.name} to accept…"
            TransferStatus.AWAITING_USER    -> "${session.remoteDevice.name} wants to share"
            TransferStatus.TRANSFERRING     -> "Transferring…"
            TransferStatus.COMPLETED        -> "Transfer complete!"
            TransferStatus.DECLINED         -> "${session.remoteDevice.name} declined"
            TransferStatus.CANCELLED        -> "Transfer cancelled"
            TransferStatus.FAILED           -> "Transfer failed"
        }

        return TransferUiState(
            session           = session,
            progressPercent   = session.progressPercent,
            formattedSpeed    = session.formattedSpeed(),
            estimatedTime     = session.estimatedTimeRemaining(),
            formattedSize     = session.payload.formattedSize(),
            // Show accept/decline only when this device is the receiver
            showAcceptDecline = session.status == TransferStatus.AWAITING_USER,
            // Show cancel while actively connecting or transferring
            showCancel        = session.status in listOf(
                                    TransferStatus.CONNECTING,
                                    TransferStatus.AWAITING_ACCEPT,
                                    TransferStatus.TRANSFERRING
                                ),
            // Show "Done" once in any terminal state
            showDone          = session.isTerminal,
            statusLabel       = label
        )
    }

    // ============================================================
    // User Actions
    // ============================================================

    /** Accept an incoming file transfer (receiver side). */
    fun acceptTransfer() {
        viewModelScope.launch {
            _uiState.value.session?.sessionId?.let {
                repository.acceptTransfer(it)
            }
        }
    }

    /** Decline an incoming file transfer (receiver side). */
    fun declineTransfer() {
        viewModelScope.launch {
            _uiState.value.session?.sessionId?.let {
                repository.declineTransfer(it)
            }
        }
    }

    /** Cancel an in-progress transfer from either side. */
    fun cancelTransfer() {
        viewModelScope.launch {
            _uiState.value.session?.sessionId?.let {
                repository.cancelTransfer(it)
            }
        }
    }
}
