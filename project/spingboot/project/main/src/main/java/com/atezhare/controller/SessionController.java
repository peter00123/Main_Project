package com.atezhare.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/session")
public class SessionController {

    @Value("${atezhare.session.expiry-minutes:30}")
    private int expiryMinutes;

    private static final Map<String, ShareSession> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> codeToSession = new ConcurrentHashMap<>();

    public static void markFilesReady(String sessionId, List<String> fileIds) {
        ShareSession session = sessions.get(sessionId);
        if (session != null) { session.status = "DONE"; session.fileIds = fileIds; }
    }

    public static Map<String, ShareSession> getSessions() { return sessions; }
    public static Map<String, String> getCodeToSession() { return codeToSession; }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSession(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "unknown");
        String sessionId = UUID.randomUUID().toString();
        String code = generateUniqueCode();
        ShareSession session = new ShareSession();
        session.sessionId = sessionId; session.code = code; session.senderId = userId;
        session.status = "WAITING"; session.createdAt = LocalDateTime.now();
        sessions.put(sessionId, session); codeToSession.put(code, sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "code", code, "status", "WAITING", "message", "Session created."));
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinSession(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String receiverUserId = (String) body.getOrDefault("receiverUserId", "unknown");
        if (code == null || code.isBlank()) return ResponseEntity.badRequest().body(errBody("Code required"));
        String sessionId = codeToSession.get(code);
        if (sessionId == null) return ResponseEntity.status(404).body(errBody("Invalid code"));
        ShareSession session = sessions.get(sessionId);
        if (session == null || session.isExpired(expiryMinutes)) { codeToSession.remove(code); return ResponseEntity.status(404).body(errBody("Session expired")); }
        session.receiverId = receiverUserId; session.status = "PAIRED";
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "code", code, "status", "PAIRED", "message", "Paired successfully."));
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@PathVariable String sessionId) {
        ShareSession s = sessions.get(sessionId);
        if (s == null) return ResponseEntity.status(404).body(Map.of("sessionId", sessionId, "status", "ERROR", "receiverId", "", "fileIds", List.of(), "message", "Not found"));
        if (s.isExpired(expiryMinutes)) s.status = "ERROR";
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "status", s.status, "receiverId", Optional.ofNullable(s.receiverId).orElse(""), "fileIds", Optional.ofNullable(s.fileIds).orElse(List.of()), "message", statusMsg(s.status)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmSend(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        if (sessionId == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "sessionId required"));
        ShareSession s = sessions.get(sessionId);
        if (s == null) return ResponseEntity.status(404).body(Map.of("success", false, "message", "Not found"));
        s.status = "TRANSFERRING";
        return ResponseEntity.ok(Map.of("success", true, "message", "Confirmed. Upload the files now."));
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(e -> { if (e.getValue().isExpired(expiryMinutes)) { codeToSession.values().remove(e.getKey()); return true; } return false; });
    }

    private String generateUniqueCode() {
        String code; do { code = String.format("%06d", new Random().nextInt(1_000_000)); } while (codeToSession.containsKey(code)); return code;
    }
    private Map<String, Object> errBody(String msg) { return Map.of("sessionId", "", "code", "", "status", "ERROR", "message", msg); }
    private String statusMsg(String s) { return switch(s) { case "WAITING" -> "Waiting for receiver."; case "PAIRED" -> "Receiver connected."; case "TRANSFERRING" -> "Transferring."; case "DONE" -> "Done."; default -> "Error."; }; }

    public static class ShareSession {
        public String sessionId, code, senderId, receiverId, status;
        public List<String> fileIds;
        public LocalDateTime createdAt;
        public boolean isExpired(int m) { return createdAt != null && LocalDateTime.now().isAfter(createdAt.plusMinutes(m)); }
    }
}
