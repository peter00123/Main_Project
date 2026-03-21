// network/ApiConstants.kt
// Central location for all backend URL constants used by RetrofitClient and ApiService.
// Base URL points to the Spring Boot backend at http://localhost:8080/atezhare
// Change BASE_URL to your actual server IP/domain when deploying.

package com.atezhare.network

object ApiConstants {

    // Base URL for the Spring Boot backend
    // Replace with actual server IP for physical device testing (e.g., http://192.168.1.x:8080/atezhare/)
    const val BASE_URL = "http://192.168.10.34:8080/atezhare/"  // 10.0.2.2 = localhost from Android emulator

    // --- Auth Endpoints ---
    const val ENDPOINT_LOGIN = "auth/login"

    // --- Session Endpoints ---
    const val ENDPOINT_CREATE_SESSION = "session/create"       // Sender creates a share session
    const val ENDPOINT_JOIN_SESSION = "session/join"           // Receiver joins via code or QR
    const val ENDPOINT_SESSION_STATUS = "session/status/{sessionId}"  // Poll session state
    const val ENDPOINT_CONFIRM_SEND = "session/confirm"        // Sender confirms sharing

    // --- File Transfer Endpoints ---
    const val ENDPOINT_UPLOAD_FILES = "files/upload"           // Multipart file upload
    const val ENDPOINT_DOWNLOAD_FILE = "files/download/{fileId}"

    // --- Pairing Endpoints ---
    const val ENDPOINT_GET_RECEIVER_QR = "pair/receiver-qr"   // Get receiver QR data
    const val ENDPOINT_SCAN_QR = "pair/scan-qr"               // Sender scans receiver QR
    const val ENDPOINT_SUBMIT_CODE = "pair/submit-code"        // Receiver submits 6-digit code

    // Network timeouts (seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 60L

    // Polling interval for session status (milliseconds)
    const val POLL_INTERVAL_MS = 2000L
}
