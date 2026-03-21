package com.atezhare.controller;

// FileController.java
// Handles file upload and download endpoints for the Atezhare Android app.
// Endpoints consumed by: ui/send/SendViewModel (upload) and ui/receive/ReceiveViewModel (download)
//
// POST /atezhare/files/upload   — sender uploads selected files after confirming transfer
// GET  /atezhare/files/download/{fileId} — receiver downloads a specific file by ID
//
// Files are stored in the local filesystem under the configured upload directory.
// In-memory map (storage) tracks sessionId → list of saved file paths.
// Replace with a database-backed solution for production.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/files")
public class FileController {

    // Injected from application.properties: atezhare.upload-dir
    @Value("${atezhare.upload-dir:./uploads}")
    private String uploadDir;

    // Maps sessionId → list of saved {fileId, filePath} entries
    // fileId is what gets sent back to the Android app for download
    // In production, replace with a JPA repository (see repository/ package pattern)
    private final Map<String, List<FileEntry>> sessionFiles = new ConcurrentHashMap<>();

    // Maps fileId → absolute file path (for download lookup)
    private final Map<String, String> fileIndex = new ConcurrentHashMap<>();

    // Ensure upload directory exists on startup
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * POST /atezhare/files/upload
     *
     * Called by: ui/send/SendViewModel.confirmAndUpload()
     * Expects:   multipart/form-data with:
     *              - sessionId (text field)
     *              - files     (one or more file parts)
     * Returns:   JSON { success, fileIds[], message }
     *
     * The returned fileIds are stored in the session and polled by the receiver.
     * SessionController.getSessionStatus() returns these fileIds once transfer is DONE.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication   // ← from JWT
    ) {
        String senderUserId = authentication.getName(); // verified from token
    
        // Validate that sender owns this session
        SessionController.ShareSession session = SessionController.getSessions().get(sessionId);
        if (session == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Session not found"));
        }
        if (!session.senderId.equals(senderUserId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Not your session"));
        }

        List<String> savedFileIds = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            try {
                // Generate unique fileId and safe filename
                String fileId = UUID.randomUUID().toString();
                String originalName = file.getOriginalFilename() != null
                    ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                    : "file_" + fileId;
                String storedName = fileId + "_" + originalName;
                Path targetPath = Paths.get(uploadDir).resolve(storedName);

                Files.copy(file.getInputStream(), targetPath);

                // Register in indexes
                fileIndex.put(fileId, targetPath.toString());
                sessionFiles.computeIfAbsent(sessionId, k -> new ArrayList<>())
                            .add(new FileEntry(fileId, originalName, targetPath.toString()));

                savedFileIds.add(fileId);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to save file: " + file.getOriginalFilename()
                ));
            }
        }

        // Notify SessionController that this session has files ready
        // SessionController.markFilesReady() updates the session status to DONE
        // and stores the fileIds so the receiver can retrieve them via getSessionStatus()
        SessionController.markFilesReady(sessionId, savedFileIds);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "fileIds", savedFileIds,
            "message", "Uploaded " + savedFileIds.size() + " file(s) successfully"
        ));
    }

    /**
     * GET /atezhare/files/download/{fileId}
     *
     * Called by: ui/receive/ReceiveViewModel (after session status = DONE)
     * Returns:   The file as a downloadable attachment.
     *
     * The receiver gets the fileIds from GET /session/status/{sessionId}
     * and calls this endpoint for each one.
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        String filePath = fileIndex.get(fileId);

        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Extract original filename (everything after the first underscore-separated UUID)
        String storedName = resource.getFilename();
        String originalName = storedName != null && storedName.contains("_")
            ? storedName.substring(storedName.indexOf('_') + 1)
            : storedName;

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + originalName + "\"")
            .header(HttpHeaders.CONTENT_TYPE,
                    "application/octet-stream")
            .body(resource);
    }

    /**
     * GET /atezhare/files/session/{sessionId}
     *
     * Utility endpoint — returns list of fileIds for a session.
     * Not called directly by the Android app (it uses /session/status instead),
     * but useful for debugging from a browser or Postman.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionFiles(@PathVariable String sessionId) {
        List<FileEntry> entries = sessionFiles.getOrDefault(sessionId, List.of());
        List<Map<String, String>> result = entries.stream()
            .map(e -> Map.of("fileId", e.fileId, "fileName", e.fileName))
            .toList();

        return ResponseEntity.ok(Map.of(
            "sessionId", sessionId,
            "files", result
        ));
    }

    // ===== Inner class for file tracking =====
    private static class FileEntry {
        final String fileId;
        final String fileName;
        final String filePath;
        FileEntry(String fileId, String fileName, String filePath) {
            this.fileId = fileId;
            this.fileName = fileName;
            this.filePath = filePath;
        }
    }
}
