// network/ApiService.kt
// Retrofit interface defining all HTTP endpoints for the Atezhare Spring Boot backend.
// Base URL: http://localhost:8080/atezhare (see ApiConstants.BASE_URL)
// Called by ViewModels in: ui/auth, ui/send, ui/receive, ui/directory

package com.atezhare.network

import com.atezhare.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Authentication ---
    // Called by: ui/auth/LoginViewModel
    @POST(ApiConstants.ENDPOINT_LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Health check — used by TestingViewModel.checkBackendConnection()
    @GET("auth/test")
    suspend fun testConnection(): Response<String>

    // --- Session Management ---

    // Sender creates a new share session; returns sessionId + 6-digit code
    // Called by: ui/send/SendViewModel
    @POST(ApiConstants.ENDPOINT_CREATE_SESSION)
    suspend fun createSession(@Body request: CreateSessionRequest): Response<SessionResponse>

    // Receiver joins a session using a 6-digit code
    // Called by: ui/receive/ReceiveViewModel
    @POST(ApiConstants.ENDPOINT_JOIN_SESSION)
    suspend fun joinSession(@Body request: JoinSessionRequest): Response<SessionResponse>

    // Poll session status to detect when receiver has joined or sender has confirmed
    // Called by: ui/send/SendViewModel and ui/receive/ReceiveViewModel (polling loop)
    @GET(ApiConstants.ENDPOINT_SESSION_STATUS)
    suspend fun getSessionStatus(@Path("sessionId") sessionId: String): Response<SessionStatusResponse>

    // Sender confirms the file transfer after seeing receiver pairing
    // Called by: ui/send/SendViewModel (confirm dialog)
    @POST(ApiConstants.ENDPOINT_CONFIRM_SEND)
    suspend fun confirmSend(@Body request: ConfirmSendRequest): Response<GenericResponse>

    // --- File Transfer ---

    // Upload selected files as multipart form data
    // Called by: ui/send/SendViewModel after sender confirms
    @Multipart
    @POST(ApiConstants.ENDPOINT_UPLOAD_FILES)
    suspend fun uploadFiles(
        @Part("sessionId") sessionId: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>

    // Download a specific file by ID
    // Called by: ui/receive/ReceiveViewModel after transfer confirmed
    @GET(ApiConstants.ENDPOINT_DOWNLOAD_FILE)
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<okhttp3.ResponseBody>

    // Called by LiveFilesViewModel.stopFile() when sender taps Stop
    @DELETE(ApiConstants.ENDPOINT_DELETE_FILE)
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<DeleteFileResponse>

    // Called by ReceiveViewModel every 30s to check if file was deleted by sender
    @GET(ApiConstants.ENDPOINT_FILE_STATUS)
    suspend fun getFileStatus(@Path("fileId") fileId: String): Response<FileStatusResponse>

    // Called by SendViewModel after upload to register mode on server (LIVE only)
    @POST(ApiConstants.ENDPOINT_SET_EXPIRY)
    suspend fun setFileExpiry(@Body request: SetExpiryRequest): Response<GenericResponse>

    // --- Pairing ---

    // Receiver requests a QR code for the current session
    // Called by: ui/receive/ReceiveViewModel
    @POST(ApiConstants.ENDPOINT_GET_RECEIVER_QR)
    suspend fun getReceiverQr(@Body request: ReceiverQrRequest): Response<ReceiverQrResponse>

    // Sender submits the scanned QR content to the backend
    // Called by: ui/send/SendViewModel after QR scan
    @POST(ApiConstants.ENDPOINT_SCAN_QR)
    suspend fun scanQr(@Body request: ScanQrRequest): Response<SessionResponse>

    // Receiver submits the 6-digit code entered manually
    // Called by: ui/receive/ReceiveViewModel when user presses submit
    @POST(ApiConstants.ENDPOINT_SUBMIT_CODE)
    suspend fun submitCode(@Body request: SubmitCodeRequest): Response<SessionResponse>
}
