package com.atezhare.controller;

import com.atezhare.model.SetExpiryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class FileLifecycleController {

    /**
     * Global lifecycle map
     * fileId -> lifecycle metadata
     *
     * Shared with FileController during upload
     */
    public static final Map<String, FileLifecycleEntry> fileLifecycle =
            new ConcurrentHashMap<>();

    /**
     * DELETE file by sender
     * Android sender Stop button calls this
     */
    @DeleteMapping("/files/delete/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileId,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        String senderId = authentication.getName();
        FileLifecycleEntry entry = fileLifecycle.get(fileId);

        if (entry == null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Already deleted"
            ));
        }

        if (!entry.senderId.equals(senderId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You are not allowed to delete this file"
            ));
        }

        try {
            Files.deleteIfExists(Paths.get(entry.filePath));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete physical file"
            ));
        }

        // IMPORTANT: keep entry for receiver polling
        entry.deleted = true;

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "File deleted"
        ));
    }

    /**
     * Receiver polls this every 30s
     */
    @GetMapping("/files/status/{fileId}")
    public ResponseEntity<?> getFileStatus(@PathVariable String fileId) {
        FileLifecycleEntry entry = fileLifecycle.get(fileId);

        if (entry == null) {
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "exists", false,
                    "deleted", true,
                    "expiresAt", null
            ));
        }

        return ResponseEntity.ok(Map.of(
                "fileId", fileId,
                "exists", !entry.deleted,
                "deleted", entry.deleted,
                "expiresAt", entry.expiresAt
        ));
    }

    /**
     * Called after upload to store mode
     * LIVE / COUNTDOWN
     */
    @PostMapping("/files/set-expiry")
    public ResponseEntity<?> setExpiry(
            @RequestBody SetExpiryRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Unauthorized"
            ));
        }

        String senderId = authentication.getName();
        FileLifecycleEntry entry = fileLifecycle.get(request.getFileId());

        if (entry == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "File not found"
            ));
        }

        if (!entry.senderId.equals(senderId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Not your file"
            ));
        }

        entry.mode = request.getMode();
        entry.expiresAt = request.getExpiresAt();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lifecycle updated"
        ));
    }

    /**
     * Shared lifecycle metadata
     */
    public static class FileLifecycleEntry {
        public String fileId;
        public String filePath;
        public String senderId;
        public String mode = "LIVE";
        public Long expiresAt;
        public boolean deleted;
    }
}