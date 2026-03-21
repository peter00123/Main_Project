package com.project.controller;

@RestController
@RequestMapping("/api")
public class FileController {

    private final Map<String, String> storage = new ConcurrentHashMap<>();

    private final String UPLOAD_DIR = "uploads/";

    @GetMapping("/session")
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        return sessionId;
    }

    @PostMapping("/upload/{sessionId}")
    public ResponseEntity<String> uploadFile(
            @PathVariable String sessionId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String filePath = UPLOAD_DIR + sessionId + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(filePath));

        storage.put(sessionId, filePath);

        return ResponseEntity.ok("Uploaded");
    }

    @GetMapping("/download/{sessionId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String sessionId
    ) throws IOException {

        String path = storage.get(sessionId);

        if (path == null) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + resource.getFilename())
                .body(resource);
    }
}