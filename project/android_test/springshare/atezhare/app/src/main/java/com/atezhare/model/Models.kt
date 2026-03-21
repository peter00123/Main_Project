// model/Models.kt
// All data classes used for Retrofit request bodies and response parsing.
// Used by: network/ApiService, all ViewModels in ui/ package.
// Serialization handled by Gson (see network/RetrofitClient).

package com.atezhare.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// ==================== AUTH MODELS ====================

/** POST /auth/login request body */
data class LoginRequest(
    val userId: String,
    val password: String
)

/** POST /auth/login response */
data class LoginResponse(
    val success: Boolean,
    val token: String?,          // JWT or session token stored in SessionManager
    val userId: String?,
    val message: String?
)

// ==================== SESSION MODELS ====================

/** POST /session/create request — sender initiates a transfer session */
data class CreateSessionRequest(
    val userId: String,
    val fileCount: Int
)

/** Generic session creation/join response */
data class SessionResponse(
    val sessionId: String,
    val code: String,            // 6-digit pairing code shown to sender
    val status: String,          // "WAITING", "PAIRED", "TRANSFERRING", "DONE", "ERROR"
    val message: String?
)

/** POST /session/join — receiver joins with a 6-digit code */
data class JoinSessionRequest(
    val code: String,
    val receiverUserId: String
)

/** GET /session/status/{sessionId} polling response */
data class SessionStatusResponse(
    val sessionId: String,
    val status: String,          // "WAITING" | "PAIRED" | "TRANSFERRING" | "DONE" | "ERROR"
    val receiverId: String?,     // Populated when receiver joins
    val fileIds: List<String>?,  // Populated when transfer completes
    val message: String?
)

/** POST /session/confirm — sender confirms the transfer */
data class ConfirmSendRequest(
    val sessionId: String,
    val senderId: String
)

// ==================== FILE MODELS ====================

/** Response from POST /files/upload */
data class UploadResponse(
    val success: Boolean,
    val fileIds: List<String>,
    val message: String?
)

// ==================== PAIRING MODELS ====================

/** POST /pair/receiver-qr request — receiver gets their QR data */
data class ReceiverQrRequest(
    val receiverUserId: String,
    val sessionId: String?       // Optional: if receiver already has a session
)

/** POST /pair/receiver-qr response — contains QR payload for ZXing */
data class ReceiverQrResponse(
    val sessionId: String,
    val qrData: String,          // String encoded into QR code bitmap in ReceiveFragment
    val code: String,            // Same 6-digit code as backup
    val message: String?
)

/** POST /pair/scan-qr — sender sends scanned QR content */
data class ScanQrRequest(
    val senderId: String,
    val qrContent: String        // Raw string scanned from receiver's QR code
)

/** POST /pair/submit-code — receiver submits the 6-digit code */
data class SubmitCodeRequest(
    val code: String,
    val receiverUserId: String
)

// ==================== GENERIC ====================

data class GenericResponse(
    val success: Boolean,
    val message: String?
)

// ==================== LOCAL MODELS ====================

/** Represents a file selected in DirectoryFragment — not sent to backend directly */
@Parcelize
data class LocalFile(
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val isChecked: Boolean = true
) : Parcelable

// Session status enum mirrors backend values
enum class SessionStatus {
    WAITING, PAIRED, TRANSFERRING, DONE, ERROR, UNKNOWN;

    companion object {
        fun from(value: String?): SessionStatus =
            values().find { it.name == value } ?: UNKNOWN
    }
}
