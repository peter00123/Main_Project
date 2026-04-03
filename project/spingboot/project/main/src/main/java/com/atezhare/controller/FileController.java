package com.atezhare.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${atezhare.upload-dir:./uploads}")
    private String uploadDir;

    private final Map<String, List<FileEntry>> sessionFiles = new ConcurrentHashMap<>();
    private final Map<String, String> fileIndex = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication
    ) {
        String senderUserId = authentication.getName();

        SessionController.ShareSession session =
                SessionController.getSessions().get(sessionId);

        if (session == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Session not found"
            ));
        }

        if (!session.senderId.equals(senderUserId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Not your session"
            ));
        }

        List<String> savedFileIds = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            try {
                String fileId = UUID.randomUUID().toString();

                String originalName = file.getOriginalFilename() != null
                        ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_")
                        : "file_" + fileId;

                String storedName = fileId + "_" + originalName;
                Path targetPath = Paths.get(uploadDir).resolve(storedName);

                Files.copy(file.getInputStream(), targetPath);

                fileIndex.put(fileId, targetPath.toString());

                sessionFiles
                        .computeIfAbsent(sessionId, k -> new ArrayList<>())
                        .add(new FileEntry(fileId, originalName, targetPath.toString()));

                savedFileIds.add(fileId);

                FileLifecycleController.FileLifecycleEntry lifecycleEntry =
                        new FileLifecycleController.FileLifecycleEntry();

                lifecycleEntry.fileId = fileId;
                lifecycleEntry.filePath = targetPath.toString();
                lifecycleEntry.senderId = senderUserId;
                lifecycleEntry.mode = "LIVE";
                lifecycleEntry.deleted = false;

                FileLifecycleController.fileLifecycle.put(fileId, lifecycleEntry);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Failed to save file"
                ));
            }
        }

        SessionController.markFilesReady(sessionId, savedFileIds);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "fileIds", savedFileIds
        ));
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        String filePath = fileIndex.get(fileId);

        if (filePath == null) {
            return ResponseEntity.notFound().build();
        }

        FileLifecycleController.FileLifecycleEntry lifecycle =
                FileLifecycleController.fileLifecycle.get(fileId);

        if (lifecycle != null && lifecycle.deleted) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

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

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionFiles(
            @PathVariable String sessionId
    ) {
        List<FileEntry> entries = sessionFiles.getOrDefault(sessionId, List.of());

        List<Map<String, String>> result = entries.stream()
                .map(e -> Map.of(
                        "fileId", e.fileId,
                        "fileName", e.fileName
                ))
                .toList();

        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "files", result
        ));
    }

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