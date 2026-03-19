package com.qrshare.network

enum class TransferState {
    IDLE,
    WAITING_FOR_CONNECTION,
    HANDSHAKING,
    VERIFYING_QR,
    CONNECTED,
    TRANSFERRING,
    COMPLETED,
    FAILED
}
