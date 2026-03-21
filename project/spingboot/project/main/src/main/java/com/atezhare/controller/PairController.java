package com.atezhare.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pair")
public class PairController {

    // POST /pair/receiver-qr
    // Called by: ui/receive/ReceiveViewModel.requestReceiverQr()
    @PostMapping("/receiver-qr")
    public ResponseEntity<Map<String, Object>> getReceiverQr(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        // Always use the verified userId from the JWT token
        // Never trust the userId from the request body
        String receiverUserId = authentication.getName();

        String existingSessionId = (String) body.get("sessionId");
        String sessionId = (existingSessionId != null && !existingSessionId.isBlank())
            ? existingSessionId
            : UUID.randomUUID().toString();

        String code = registerReceiverSession(sessionId, receiverUserId);
        String qrData = "atezhare://pair?session=" + sessionId + "&receiver=" + receiverUserId;

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "qrData",    qrData,
            "code",      code,
            "message",   "Show this QR code to the sender"
        ));
    }

    // POST /pair/scan-qr
    // Called by: ui/send/SendViewModel.onQrScanned()
    @PostMapping("/scan-qr")
    public ResponseEntity<Map<String, Object>> scanQr(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        String senderId  = authentication.getName(); // verified from JWT
        String qrContent = (String) body.get("qrContent");

        if (qrContent == null || qrContent.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code",      "",
                "status",    "ERROR",
                "message",   "QR content is required"
            ));
        }

        String sessionId = extractParam(qrContent, "session");
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code",      "",
                "status",    "ERROR",
                "message",   "Invalid QR format"
            ));
        }

        SessionController.ShareSession session =
            SessionController.getSessions().get(sessionId);

        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", sessionId,
                "code",      "",
                "status",    "ERROR",
                "message",   "Session not found or expired"
            ));
        }

        session.senderId = senderId;
        session.status   = "PAIRED";

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "code",      session.code != null ? session.code : "",
            "status",    "PAIRED",
            "message",   "QR scanned. Waiting for sender to confirm."
        ));
    }

    // POST /pair/submit-code
    // Called by: ui/receive/ReceiveViewModel.submitCode()
    @PostMapping("/submit-code")
    public ResponseEntity<Map<String, Object>> submitCode(
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        String receiverUserId = authentication.getName(); // verified from JWT
        String code           = (String) body.get("code");

        if (code == null || code.length() != 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "sessionId", "",
                "code",      "",
                "status",    "ERROR",
                "message",   "Valid 6-digit code is required"
            ));
        }

        String sessionId = SessionController.getCodeToSession().get(code);
        if (sessionId == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", "",
                "code",      code,
                "status",    "ERROR",
                "message",   "Invalid or expired code"
            ));
        }

        SessionController.ShareSession session =
            SessionController.getSessions().get(sessionId);

        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                "sessionId", "",
                "code",      code,
                "status",    "ERROR",
                "message",   "Session not found"
            ));
        }

        session.receiverId = receiverUserId;
        session.status     = "PAIRED";

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "code",      code,
            "status",    "PAIRED",
            "message",   "Code accepted. Waiting for sender to confirm."
        ));
    }

    // ===== Helpers =====

    private String registerReceiverSession(String sessionId, String receiverUserId) {
        String code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        while (SessionController.getCodeToSession().containsKey(code)) {
            code = String.format("%06d", new java.util.Random().nextInt(1_000_000));
        }

        SessionController.ShareSession session = new SessionController.ShareSession();
        session.sessionId  = sessionId;
        session.code       = code;
        session.receiverId = receiverUserId;
        session.senderId   = null;
        session.status     = "WAITING";
        session.createdAt  = java.time.LocalDateTime.now();

        SessionController.getSessions().put(sessionId, session);
        SessionController.getCodeToSession().put(code, sessionId);

        return code;
    }

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