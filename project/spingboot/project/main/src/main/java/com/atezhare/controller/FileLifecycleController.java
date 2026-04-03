package com.atezhare.controller;

import com.atezhare.model.SetExpiryRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/files")
public class FileLifecycleController {

    public static final Map<String, FileLifecycleEntry> fileLifecycle =
            new ConcurrentHashMap<>();

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileId,
            Authentication authentication
    ) {
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

        entry.deleted = true;

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "File deleted"
        ));
    }

    @GetMapping("/status/{fileId}")
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

    @PostMapping("/set-expiry")
    public ResponseEntity<?> setExpiry(
            @RequestBody SetExpiryRequest request,
            Authentication authentication
    ) {
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

    public static class FileLifecycleEntry {
        public String fileId;
        public String filePath;
        public String senderId;
        public String mode = "LIVE";
        public Long expiresAt;
        public boolean deleted;
    }
}