# Atezhare Backend — What To Change
# =====================================
# Apply these changes to your existing Spring Boot project folder.
# Your package is com.project — keep that as-is, just add the new files.

## SUMMARY OF CHANGES

| File                        | Action    | Why                                           |
|-----------------------------|-----------|-----------------------------------------------|
| application.properties      | EDIT      | Add context path /atezhare + upload dir       |
| ProjectApplication.java     | EDIT      | Add @EnableScheduling                         |
| TestController.java         | REPLACE   | Becomes AuthController.java                   |
| FileController.java         | REPLACE   | Match Android API contract                    |
| SessionController.java      | ADD NEW   | Session lifecycle + status polling            |
| PairController.java         | ADD NEW   | QR and 6-digit code pairing                   |
| SecurityConfig.java         | ADD NEW   | Allow all requests (disable Spring auth lock) |

---

## CHANGE 1 — application.properties

Find your existing file and ADD these lines:

```properties
# ADD — Android app calls http://host:8080/atezhare/...
server.servlet.context-path=/atezhare

# ADD — upload storage directory
atezhare.upload-dir=./uploads

# ADD — session auto-expiry
atezhare.session.expiry-minutes=30
```

Your full application.properties should look like:

```properties
spring.application.name=project

server.address=0.0.0.0
server.port=8080
server.servlet.context-path=/atezhare

spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=500MB

atezhare.upload-dir=./uploads
atezhare.session.expiry-minutes=30
```

---

## CHANGE 2 — ProjectApplication.java

Add ONE annotation to your existing file:

```java
@SpringBootApplication
@EnableScheduling   // <-- ADD THIS
public class ProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
}
```

Also add the import:
```java
import org.springframework.scheduling.annotation.EnableScheduling;
```

---

## CHANGE 3 — DELETE TestController.java

Delete it completely. Its /api/test endpoint is replaced by AuthController's /auth/test.

---

## CHANGE 4 — REPLACE FileController.java

Replace your existing FileController.java with the new one provided.

Key differences from your current version:
- Endpoint changed: POST /api/upload/{sessionId}  →  POST /files/upload
  (sessionId is now a form field, not a path variable — matches Android SendViewModel)
- Download endpoint: GET /api/download/{sessionId}  →  GET /files/download/{fileId}
  (uses fileId UUID, not sessionId — multiple files per session)
- Calls SessionController.markFilesReady() on upload complete
  so the receiver's status polling sees DONE with the fileIds
- Upload dir read from application.properties: atezhare.upload-dir

---

## CHANGE 5 — ADD SessionController.java

New file. Place in: src/main/java/com/project/controller/SessionController.java

Change package line at top to:
```java
package com.project.controller;
```

This provides the 4 endpoints the Android app polls constantly:
  POST /session/create   — sender starts a session, gets 6-digit code
  POST /session/join     — receiver joins via code
  GET  /session/status/{id} — both sides poll this every 2 seconds
  POST /session/confirm  — sender confirms before upload

---

## CHANGE 6 — ADD PairController.java

New file. Place in: src/main/java/com/project/controller/PairController.java

Change package line at top to:
```java
package com.project.controller;
```

This provides the 3 pairing endpoints:
  POST /pair/receiver-qr  — receiver gets QR data + sessionId
  POST /pair/scan-qr      — sender submits scanned QR content
  POST /pair/submit-code  — receiver enters 6-digit code manually

---

## CHANGE 7 — ADD AuthController.java

New file. Place in: src/main/java/com/project/controller/AuthController.java

Change package line at top to:
```java
package com.project.controller;
```

Provides:
  POST /auth/login  — validates admin/1234, returns token
  GET  /auth/test   — connection health check

---

## CHANGE 8 — ADD SecurityConfig.java

New file. Place in: src/main/java/com/project/config/SecurityConfig.java
(Create the config/ folder if it doesn't exist)

Change package line at top to:
```java
package com.project.config;
```

CRITICAL: Without this file, Spring Security will block ALL requests from the
Android app with 401 Unauthorized. This config disables that default behavior.

---

## FINAL ENDPOINT MAP

After all changes, your backend exposes these endpoints at http://HOST:8080/atezhare/

| Method | URL                              | Called By (Android)              |
|--------|----------------------------------|----------------------------------|
| POST   | /auth/login                      | LoginViewModel                   |
| GET    | /auth/test                       | Manual test / health check       |
| POST   | /session/create                  | SendViewModel.createSession()    |
| POST   | /session/join                    | ReceiveViewModel                 |
| GET    | /session/status/{sessionId}      | Both ViewModels (polling)        |
| POST   | /session/confirm                 | SendViewModel.confirmAndUpload() |
| POST   | /files/upload                    | SendViewModel.uploadFiles()      |
| GET    | /files/download/{fileId}         | ReceiveViewModel                 |
| POST   | /pair/receiver-qr                | ReceiveViewModel.requestQr()     |
| POST   | /pair/scan-qr                    | SendViewModel.onQrScanned()      |
| POST   | /pair/submit-code                | ReceiveViewModel.submitCode()    |

---

## TESTING WITH CURL (after startup)

# 1. Health check
curl http://localhost:8080/atezhare/auth/test

# 2. Login
curl -X POST http://localhost:8080/atezhare/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"admin","password":"1234"}'

# 3. Create session (sender)
curl -X POST http://localhost:8080/atezhare/session/create \
  -H "Content-Type: application/json" \
  -d '{"userId":"admin","fileCount":1}'

# 4. Submit code (receiver) — use the code from step 3
curl -X POST http://localhost:8080/atezhare/pair/submit-code \
  -H "Content-Type: application/json" \
  -d '{"code":"XXXXXX","receiverUserId":"admin"}'

# 5. Poll status
curl http://localhost:8080/atezhare/session/status/<sessionId-from-step-3>

---

## ANDROID APP — One Setting to Verify

In the Android app: network/ApiConstants.kt

For emulator testing (Android emulator → localhost):
  const val BASE_URL = "http://10.0.2.2:8080/atezhare/"

For physical device testing (device and PC on same WiFi):
  const val BASE_URL = "http://192.168.X.X:8080/atezhare/"
  (replace with your PC's actual LAN IP — find with ipconfig/ifconfig)

For Docker deployment:
  const val BASE_URL = "http://YOUR_SERVER_IP:8080/atezhare/"
  (your Dockerfile already exposes port 8080 correctly)
