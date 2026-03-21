package com.atezhare.controller;

import java.util.Map;
import java.util.UUID;

// PairController.java
// Handles the two pairing flows between sender and receiver:
//
//   Flow A — QR Code:
//     1. Receiver calls POST /pair/receiver-qr  → gets a sessionId + QR data string
//        (consumed by: ui/receive/ReceiveViewModel.requestReceiverQr())
//     2. Sender scans QR and calls POST /pair/scan-qr → links sender to receiver's session
//        (consumed by: ui/send/SendViewModel.onQrScanned())
//
//   Flow B — 6-digit Code:
//     1. Receiver calls POST /pair/submit-code with the sender's code
//        (consumed by: ui/receive/ReceiveViewModel.submitCode())
//        This joins the session the same way as POST /session/join
//
// Both flows result in the session status becoming "PAIRED",
// which the sender detects via GET /session/status polling.

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pair")
public class PairController {

    // =========================================================
    // POST /pair/receiver-qr
    // Called by: ui/receive/ReceiveViewModel.requestReceiverQr()
    // Body: { "receiverUserId": "admin", "sessionId": null }
    //
    // Creates a new session owned by the RECEIVER (opposite of sender flow).
    // The QR data string is what gets encoded into a bitmap by utils/QrUtils on the Android side.
    // Returns: { sessionId, qrData, code, message }
    // =========================================================
    @PostMapping("/receiver-qr")
    public ResponseEntity<Map<String, Object>> getReceiverQr(
            @RequestBody Map<String, Object> body,
            Authentication authentication   // ← from JWT
    ) {
        // Use verified userId from token instead of trusting request body
        String receiverUserId = authentication.getName();
        String existingSessionId = (String) body.get("sessionId");
    
        String sessionId = (existingSessionId != null && !existingSessionId.isBlank())
            ? existingSessionId
            : UUID.randomUUID().toString();
    
        String code = registerReceiverSession(sessionId, receiverUserId);
        String qrData = "atezhare://pair?session=" + sessionId + "&receiver=" + receiverUserId;
    
        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "qrData", qrData,
            "code", code,
            "message", "Show this QR code to the sender"
        ));
    }

    // =========================================================
    // POST /pair/scan-qr
    // Called by: ui/send/SendViewModel.onQrScanned()
    // Body: { "senderId": "admin", "qrContent": "atezhare://pair?session=...&receiver=..." }
    //
    // Parses the QR string, finds the receiver's session, and pairs the sender to it.
    // After this, polling GET /session/status will return status = "PAIRED"
    // Returns: { sessionId, code, status, message }
    // =========================================================
    @PostMapping("/scan-qr")
    public ResponseEntity<Map<String, Object>> scanQr(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        String senderId = authentication.getName(); // from JWT
        String qrContent = (String) body.get("qrContent");
        if (qrContent == null || qrContent.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code", "",
                "status", "ERROR",
                "message", "QR content is required"
            ));
        }

        // Parse the QR data: "atezhare://pair?session=<sessionId>&receiver=<userId>"
        String sessionId = extractParam(qrContent, "session");
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code", "",
                "status", "ERROR",
                "message", "Invalid QR code format"
            ));
        }

        // Find the session in SessionController's store and pair the sender
        SessionController.ShareSession session = SessionController.getSessions().get(sessionId);
        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", sessionId,
                "code", "",
                "status", "ERROR",
                "message", "Session not found or expired"
            ));
        }

        // Pair: mark session as PAIRED with sender's ID
        session.senderId = senderId;
        session.status = "PAIRED";

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "code", session.code != null ? session.code : "",
            "status", "PAIRED",
            "message", "QR scanned successfully. Waiting for sender to confirm."
        ));
    }

    // =========================================================
    // POST /pair/submit-code
    // Called by: ui/receive/ReceiveViewModel.submitCode()
    // Body: { "code": "123456", "receiverUserId": "admin" }
    //
    // This is the code-entry path. The receiver types the sender's 6-digit code.
    // Delegates to SessionController's join logic.
    // Returns: { sessionId, code, status, message }
    // =========================================================
    @PostMapping("/submit-code")
    public ResponseEntity<Map<String, Object>> submitCode(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        String receiverUserId = authentication.getName(); // from JWT
        String code = (String) body.get("code");
        if (code == null || code.length() != 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code", "",
                "status", "ERROR",
                "message", "A valid 6-digit code is required"
            ));
        }

        // Look up session by code from SessionController's codeToSession map
        String sessionId = SessionController.getCodeToSession().get(code);
        if (sessionId == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", "",
                "code", code,
                "status", "ERROR",
                "message", "Invalid or expired code"
            ));
        }

        SessionController.ShareSession session = SessionController.getSessions().get(sessionId);
        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", "",
                "code", code,
                "status", "ERROR",
                "message", "Session not found"
            ));
        }

        // Pair the receiver
        session.receiverId = receiverUserId;
        session.status = "PAIRED";

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "code", code,
            "status", "PAIRED",
            "message", "Code accepted. Waiting for sender to confirm the transfer."
        ));
    }

    // ===== Helpers =====

    /**
     * Creates a receiver-initiated session in SessionController's store.
     * Returns the generated 6-digit code for display as backup on the receiver screen.
     */
    private String registerReceiverSession(String sessionId, String receiverUserId) {
        String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));

        // Ensure uniqueness
        while (SessionController.getCodeToSession().containsKey(code)) {
            code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        }

        SessionController.ShareSession session = new SessionController.ShareSession();
        session.sessionId = sessionId;
        session.code = code;
        session.receiverId = receiverUserId;
        session.senderId = null;        // Sender unknown until QR is scanned
        session.status = "WAITING";
        session.createdAt = java.time.LocalDateTime.now();

        SessionController.getSessions().put(sessionId, session);
        SessionController.getCodeToSession().put(code, sessionId);

        return code;
    }

    /** Extracts a query param value from a URI-style string */
    private String extractParam(String url, String paramName) {
        try {
            String search = paramName + "=";
            int start = url.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = url.indexOf('&', start);
            return end == -1 ? url.substring(start) : url.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
