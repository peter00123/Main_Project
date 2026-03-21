# Atezhare — Complete Technical Documentation

**Version:** 1.0  
**Stack:** Android (Kotlin) + Spring Boot (Java)  
**Purpose:** Peer-to-peer document sharing between two Android devices via a Spring Boot relay server.

---

## Table of Contents

1. [System Architecture Overview](#1-system-architecture-overview)
2. [Project Structure](#2-project-structure)
3. [App Startup & Authentication](#3-app-startup--authentication)
4. [Navigation & UI Shell](#4-navigation--ui-shell)
5. [How a Session Works — Core Concept](#5-how-a-session-works--core-concept)
6. [Sending Files — Complete Flow](#6-sending-files--complete-flow)
7. [Receiving Files — Complete Flow](#7-receiving-files--complete-flow)
8. [Pairing in Detail — QR vs Code](#8-pairing-in-detail--qr-vs-code)
9. [Session Lifecycle & State Machine](#9-session-lifecycle--state-machine)
10. [File Transfer — How Data Moves](#10-file-transfer--how-data-moves)
11. [The Shared Data Page](#11-the-shared-data-page)
12. [Network Layer](#12-network-layer)
13. [Backend — Spring Boot Deep Dive](#13-backend--spring-boot-deep-dive)
14. [Local Database — Room](#14-local-database--room)
15. [Complete API Reference](#15-complete-api-reference)
16. [Data Flow Diagrams](#16-data-flow-diagrams)

---

## 1. System Architecture Overview

```
┌─────────────────────┐          ┌──────────────────────┐          ┌─────────────────────┐
│   DEVICE A (Sender) │          │   SPRING BOOT SERVER  │          │ DEVICE B (Receiver) │
│                     │          │  localhost:8080        │          │                     │
│  SendActivity       │◄────────►│  /atezhare            │◄────────►│  ReceiveFragment    │
│  SendViewModel      │  HTTP    │                       │  HTTP    │  ReceiveViewModel   │
│  DirectoryFragment  │  REST    │  SessionController    │  REST    │  SharedDataFragment │
│                     │          │  PairController       │          │                     │
│  Files on disk      │─────────►│  FileController       │─────────►│  Files saved to     │
│  (selected by user) │  Upload  │  ./uploads/ folder    │  Download│  /files/received/   │
└─────────────────────┘          └──────────────────────┘          └─────────────────────┘
```

**Key design principle:** The two devices never talk directly to each other. All
communication goes through the Spring Boot backend. The server acts as a relay:
it holds the files temporarily until the receiver downloads them.

---

## 2. Project Structure

### Android App (`com.atezhare`)

```
com.atezhare/
├── data/                        ← Local Room database (received files)
│   ├── ReceivedFile.kt          ← @Entity: one DB row per received file
│   ├── ReceivedFileDao.kt       ← SQL queries (insert, getAll, getByType, markViewed)
│   ├── ReceivedFileDatabase.kt  ← Room DB singleton
│   └── ReceivedFileRepository.kt← Saves bytes to disk + inserts DB records
│
├── model/
│   └── Models.kt                ← All Retrofit request/response data classes
│
├── network/
│   ├── ApiConstants.kt          ← Base URL + all endpoint strings + timeouts
│   ├── ApiService.kt            ← Retrofit interface (all HTTP method declarations)
│   └── RetrofitClient.kt        ← OkHttp + Retrofit singleton, auth token interceptor
│
├── utils/
│   ├── SessionManager.kt        ← SharedPreferences: save/restore login state
│   ├── QrUtils.kt               ← ZXing: generate QR bitmap, validate 6-digit code
│   └── FileUtils.kt             ← URI → LocalFile conversion, MultipartBody prep
│
└── ui/
    ├── auth/
    │   ├── SplashActivity.kt    ← App entry point, routes to Login or Main
    │   ├── LoginActivity.kt     ← Login form UI
    │   └── LoginViewModel.kt    ← Login logic + POST /auth/login
    ├── home/
    │   ├── MainActivity.kt      ← Toolbar + DrawerLayout + BottomNav host
    │   └── HomeFragment.kt      ← Landing page: Send / Receive buttons
    ├── directory/
    │   ├── DirectoryFragment.kt ← File picker UI
    │   ├── DirectoryViewModel.kt← File list state management
    │   └── DirectoryAdapter.kt  ← RecyclerView adapter with checkboxes
    ├── send/
    │   ├── SendActivity.kt      ← QR scanner screen (CameraX)
    │   └── SendViewModel.kt     ← createSession → poll → scan → confirm → upload
    ├── receive/
    │   ├── ReceiveFragment.kt   ← QR display + 6-box code entry
    │   └── ReceiveViewModel.kt  ← requestQR → submitCode → poll → download → save
    ├── shareddata/
    │   ├── SharedDataFragment.kt← Received files list with filter tabs
    │   ├── SharedDataViewModel.kt← Reads Room DB, handles open/delete
    │   └── SharedDataAdapter.kt ← RecyclerView adapter for received files
    └── profile/
        ├── ProfileFragment.kt   ← User info display
        └── SettingsFragment.kt  ← Settings (placeholder)
```

### Spring Boot Backend (`com.project`)

```
com.project/
├── ProjectApplication.java      ← @SpringBootApplication + @EnableScheduling
├── config/
│   └── SecurityConfig.java      ← Disables Spring Security default block
└── controller/
    ├── AuthController.java      ← POST /auth/login
    ├── SessionController.java   ← Session lifecycle: create/join/status/confirm
    ├── PairController.java      ← QR pairing + 6-digit code pairing
    └── FileController.java      ← Upload + download file storage
```

---

## 3. App Startup & Authentication

### Entry Point — `SplashActivity.kt`

When the app launches, Android starts `SplashActivity` (declared as the LAUNCHER
activity in `AndroidManifest.xml`). Its entire job is routing:

```
SplashActivity.onCreate()
    │
    ├── SessionManager(context).restoreSession()
    │       Reads saved auth token from SharedPreferences
    │       Writes it to SessionTokenHolder.token so Retrofit can use it
    │
    ├── SessionManager.isLoggedIn() == true?
    │       YES → startActivity(Intent → MainActivity)
    │       NO  → startActivity(Intent → LoginActivity)
    │
    └── finish()   ← removes SplashActivity from back stack
```

**File:** `utils/SessionManager.kt`  
**Key method:** `isLoggedIn()` — reads `is_logged_in` boolean from SharedPreferences named `atezhare_session`.  
**Key method:** `restoreSession()` — copies stored token into `SessionTokenHolder.token` so OkHttp interceptor can attach it to headers.

---

### Login — `LoginActivity.kt` + `LoginViewModel.kt`

The login screen has two input fields: User ID and Password.

When the user taps Login:

1. `LoginActivity.setupClickListeners()` reads the two input fields.
2. Calls `LoginViewModel.login(userId, password)`.

Inside `LoginViewModel.login()`:

**Step 1 — Local validation:**
```
if (userId != "admin" || password != "1234")
    → post LoginResponse(success=false) → UI shows "Invalid credentials"
```

**Step 2 — API call:**
```
RetrofitClient.apiService.login(LoginRequest(userId, password))
    → POST http://host:8080/atezhare/auth/login
    → Body: { "userId": "admin", "password": "1234" }
```

**Backend — `AuthController.java` `login()`:**
- Compares against hardcoded `VALID_USER="admin"` and `VALID_PASS="1234"`.
- Returns `{ success: true, token: "atezhare-token-<UUID>", userId: "admin" }`.

**Step 3 — Save session:**
```
SessionManager.saveSession(userId, token)
    → writes to SharedPreferences:
        is_logged_in = true
        user_id      = "admin"
        token        = "atezhare-token-..."
    → SessionTokenHolder.token = token   ← Retrofit will now send this in every request header
```

**Step 4 — Navigate:**
```
startActivity(Intent → MainActivity)
finish()   ← LoginActivity removed from back stack
```

If the backend is unreachable (network error), `LoginViewModel` still allows login
using the hardcoded credentials and sets `token = null`. This is intentional for
offline development.

---

## 4. Navigation & UI Shell

### `MainActivity.kt` — The Container

`MainActivity` is a `DrawerLayout` that contains three layers:

```
DrawerLayout
├── LinearLayout (main content)
│   ├── Toolbar (top bar)
│   │   └── ActionBarDrawerToggle ← burger icon that opens the drawer
│   ├── FragmentContainerView (NavHostFragment)
│   │   └── NavController hosts: Home, Directory, Receive, SharedData, Profile, Settings
│   └── BottomNavigationView
│       └── Items: Home | Directory | Shared | Profile
└── NavigationView (drawer panel, slides from left)
    ├── Header: nav_header.xml (app name + logo)
    └── Menu: drawer_menu.xml (About | Settings | Logout)
```

**Bottom navigation** is wired to the NavController via:
```kotlin
binding.bottomNav.setupWithNavController(navController)
```
This means tapping "Shared" in the bottom nav automatically navigates to
`sharedDataFragment` because the menu item ID matches the fragment ID in
`nav_graph.xml`.

**Burger menu logout — `onNavigationItemSelected()`:**
```
R.id.nav_logout →
    SessionManager.clearSession()     ← wipes SharedPreferences + token
    startActivity(Intent → LoginActivity)
    finishAffinity()                  ← removes all activities from stack
```

---

## 5. How a Session Works — Core Concept

A "session" is the fundamental unit of a file transfer in Atezhare. Before any
file can move between devices, a session must be created and both devices must
be paired to it.

**What is a session?**  
A session is an object stored in `SessionController.java`'s in-memory
`ConcurrentHashMap<String, ShareSession>`. It lives on the server.

**The `ShareSession` object (inside `SessionController.java`):**
```java
public static class ShareSession {
    public String sessionId;    // UUID like "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    public String code;         // 6-digit number like "482917"
    public String senderId;     // "admin" (the sender's userId)
    public String receiverId;   // "admin" (the receiver's userId, set when they pair)
    public String status;       // "WAITING" | "PAIRED" | "TRANSFERRING" | "DONE" | "ERROR"
    public List<String> fileIds;// ["uuid1", "uuid2"] — set when upload completes
    public LocalDateTime createdAt; // for expiry checking
}
```

**Two maps that power all lookups:**
```java
// Primary store: sessionId → session object
static final Map<String, ShareSession> sessions = new ConcurrentHashMap<>();

// Lookup table: 6-digit code → sessionId (for code-based pairing)
static final Map<String, String> codeToSession = new ConcurrentHashMap<>();
```

**Session expiry:**  
`ShareSession.isExpired(int expiryMinutes)` checks if `LocalDateTime.now()` is
after `createdAt + expiryMinutes`. The default is 30 minutes from
`application.properties`. A `@Scheduled` task in `SessionController.cleanupExpiredSessions()`
runs every 5 minutes and removes expired sessions from both maps.

---

## 6. Sending Files — Complete Flow

### Step 1 — File Selection (`DirectoryFragment.kt`)

The user taps "Send" on the HomeFragment. `HomeFragment.setupClickListeners()` calls:
```kotlin
findNavController().navigate(R.id.action_home_to_directory)
```

`DirectoryFragment` opens with an empty list and a FAB (+) button.

**User taps (+):**
```
DirectoryFragment.openFilePicker()
    → Intent(ACTION_GET_CONTENT, type="*/*", EXTRA_ALLOW_MULTIPLE=true)
    → System file picker opens
    → User selects files
    → filePickerLauncher result callback fires
```

**For each selected URI:**
```
FileUtils.uriToLocalFile(context, uri)
    → queries ContentResolver for DISPLAY_NAME, SIZE
    → copies file bytes from ContentResolver to app cache directory
    → returns LocalFile(name, path, size, mimeType, isChecked=true)

DirectoryViewModel.addFile(localFile)
    → adds to _fileList MutableLiveData
    → RecyclerView updates automatically via LiveData observation
```

**`DirectoryAdapter.kt`** renders each file as a card with:
- File type icon (gallery/play/agenda/save based on MIME)
- File name and size (`FileUtils.formatFileSize()`)
- Checkbox (checked by default, stored as `LocalFile.isChecked`)

When the user taps "Send Selected Files":
```
DirectoryFragment.setupClickListeners() btn_send click:
    → DirectoryViewModel.getCheckedFiles() returns List<LocalFile> where isChecked=true
    → Intent(context, SendActivity::class.java)
        .putParcelableArrayListExtra(EXTRA_FILES, ArrayList(selectedFiles))
    → startActivity(intent)
```

---

### Step 2 — Session Creation (`SendActivity.kt` + `SendViewModel.kt`)

`SendActivity.onCreate()`:
1. Receives the file list from the Intent extra.
2. Calls `SendViewModel.setSelectedFiles(files)`.
3. Calls `SendViewModel.createSession()`.
4. Requests camera permission and starts the QR scanner.

**`SendViewModel.createSession()`:**
```kotlin
RetrofitClient.apiService.createSession(
    CreateSessionRequest(userId = sessionManager.getUserId(), fileCount = files.size)
)
→ POST /atezhare/session/create
→ Body: { "userId": "admin", "fileCount": 2 }
```

**Backend — `SessionController.createSession()`:**
```java
String sessionId = UUID.randomUUID().toString();   // e.g. "f47ac10b-..."
String code = generateUniqueCode();                // e.g. "482917"
ShareSession session = new ShareSession();
session.status = "WAITING";
session.createdAt = LocalDateTime.now();
sessions.put(sessionId, session);
codeToSession.put(code, sessionId);
// Returns: { sessionId, code: "482917", status: "WAITING" }
```

Back in `SendViewModel`, on success:
```kotlin
sessionId = body.sessionId     ← stored in ViewModel for later use
_shareCode.value = body.code   ← "482917" displayed on screen
startPollingStatus()           ← begins polling loop
```

**`SendActivity` observes `viewModel.shareCode`:**
```kotlin
binding.tvShareCode.text = code   // "482917" shown at bottom of screen
```

---

### Step 3 — Polling for Receiver (`SendViewModel.startPollingStatus()`)

Once the session is created, the sender's app polls the backend every 2 seconds:

```kotlin
pollJob = viewModelScope.launch {
    while (isActive) {
        RetrofitClient.apiService.getSessionStatus(sessionId)
        → GET /atezhare/session/status/{sessionId}

        val status = SessionStatus.from(body.status)
        _sessionStatus.value = status

        if (status == PAIRED || status == DONE || status == ERROR) break

        delay(2000)   // ApiConstants.POLL_INTERVAL_MS
    }
}
```

The backend `SessionController.getSessionStatus()` simply reads from the `sessions`
map and returns the current state. As long as no receiver has joined,
it returns `status: "WAITING"`.

---

### Step 4 — Confirm and Upload (`SendViewModel.confirmAndUpload()`)

Once the session becomes PAIRED (receiver has connected), `SendActivity` shows
a confirmation dialog. If the user taps "Send":

**Sub-step 4a — Confirm:**
```kotlin
RetrofitClient.apiService.confirmSend(
    ConfirmSendRequest(sessionId = sid, senderId = sessionManager.getUserId())
)
→ POST /atezhare/session/confirm
→ Body: { "sessionId": "f47ac10b-...", "senderId": "admin" }
```

**Backend — `SessionController.confirmSend()`:**
```java
session.status = "TRANSFERRING";
// Returns: { success: true }
```

The receiver is polling and will now see `status = "TRANSFERRING"` and show
"Transfer in progress..." in their UI.

**Sub-step 4b — Upload files:**
```kotlin
val parts = FileUtils.localFilesToMultipart(selectedFiles)
// Converts each LocalFile to a MultipartBody.Part using the cached file path

RetrofitClient.apiService.uploadFiles(sessionIdBody, parts)
→ POST /atezhare/files/upload
→ Content-Type: multipart/form-data
→ Parts:
    - sessionId: "f47ac10b-..."  (text field)
    - files:     [file1bytes, file2bytes, ...]  (binary parts)
```

**`FileUtils.localFilesToMultipart()`** (in `utils/FileUtils.kt`):
```kotlin
files.filter { it.isChecked }.mapNotNull { localFile ->
    val file = File(localFile.path)       // reads from app cache
    val mediaType = localFile.mimeType.toMediaTypeOrNull()
    val requestBody = file.asRequestBody(mediaType)
    MultipartBody.Part.createFormData("files", file.name, requestBody)
}
```

**Backend — `FileController.uploadFiles()`:**

For each file in the multipart request:
```java
String fileId = UUID.randomUUID().toString();     // e.g. "3f2504e0-..."
String storedName = fileId + "_" + originalName;  // "3f2504e0-..._photo.jpg"
Path targetPath = Paths.get(uploadDir).resolve(storedName);
Files.copy(file.getInputStream(), targetPath);    // saved to ./uploads/

fileIndex.put(fileId, targetPath.toString());     // for download lookup
sessionFiles.get(sessionId).add(new FileEntry(fileId, originalName, path));
savedFileIds.add(fileId);
```

After all files are saved:
```java
SessionController.markFilesReady(sessionId, savedFileIds);
// This sets: session.status = "DONE"
//            session.fileIds = ["3f2504e0-...", "..."]
```

Back in `SendViewModel`, on successful upload:
```kotlin
_sessionStatus.value = SessionStatus.DONE
```
`SendActivity` observes this and calls `finish()` — the sender's work is done.

---

## 7. Receiving Files — Complete Flow

### Step 1 — Opening the Receive Screen

The user taps "Receive" on `HomeFragment`. This navigates to `ReceiveFragment`
(it is a Fragment within MainActivity, not a separate Activity):
```kotlin
findNavController().navigate(R.id.action_home_to_receive)
```

### Step 2 — Getting the Receiver QR (`ReceiveViewModel.requestReceiverQr()`)

`ReceiveFragment.onViewCreated()` immediately calls `viewModel.requestReceiverQr()`:

```kotlin
RetrofitClient.apiService.getReceiverQr(
    ReceiverQrRequest(receiverUserId = sessionManager.getUserId(), sessionId = null)
)
→ POST /atezhare/pair/receiver-qr
→ Body: { "receiverUserId": "admin", "sessionId": null }
```

**Backend — `PairController.getReceiverQr()`:**
```java
String sessionId = UUID.randomUUID().toString();
String code = registerReceiverSession(sessionId, receiverUserId);
// registerReceiverSession creates a ShareSession with status="WAITING"
// and stores it in SessionController's maps

String qrData = "atezhare://pair?session=" + sessionId + "&receiver=" + receiverUserId;
// e.g. "atezhare://pair?session=f47ac10b-...&receiver=admin"

// Returns: { sessionId, qrData, code: "193847" }
```

Back in `ReceiveViewModel`:
```kotlin
sessionId = body.sessionId          ← stored for polling
_qrData.value = body.qrData         ← triggers QR rendering
startPollingStatus()                ← begins polling loop
```

**`ReceiveFragment` observes `viewModel.qrData`:**
```kotlin
val bitmap = QrUtils.generateQrBitmap(qrData, 600)
// QrUtils uses ZXing MultiFormatWriter to encode the string into a 600x600 pixel Bitmap
binding.ivQrCode.setImageBitmap(bitmap)
```

The QR code is now displayed on the receiver's screen.

### Step 3 — Polling for DONE (`ReceiveViewModel.startPollingStatus()`)

Same as the sender, the receiver polls every 2 seconds:
```kotlin
RetrofitClient.apiService.getSessionStatus(sessionId)
→ GET /atezhare/session/status/{sessionId}
```

When the status changes, the fragment observes `viewModel.sessionStatus`:
- `PAIRED` → shows "Sender Connected" dialog
- `TRANSFERRING` → shows progress bar + "Receiving files..."
- `DONE` → calls `downloadAndSaveFiles()`
- `ERROR` → shows error toast

### Step 4 — Download and Save (`ReceiveViewModel.downloadAndSaveFiles()`)

When status becomes DONE, the `SessionStatusResponse` includes `fileIds`:
```json
{
  "sessionId": "f47ac10b-...",
  "status": "DONE",
  "fileIds": ["3f2504e0-...", "7a9b2c1d-..."]
}
```

For each fileId:
```kotlin
// 1. Skip if already downloaded (prevents duplicate on repeat polls)
if (repository.getByFileId(fileId) != null) continue

// 2. Download from backend
RetrofitClient.apiService.downloadFile(fileId)
→ GET /atezhare/files/download/{fileId}

// 3. Extract metadata from response headers
val contentDisposition = response.headers()["Content-Disposition"]
// "attachment; filename=\"photo.jpg\""
val fileName = Regex("""filename="?([^";\n]+)"?""").find(...)?.groupValues?.get(1)
val mimeType = response.headers()["Content-Type"]

// 4. Read raw bytes
val bytes = body.bytes()

// 5. Save to disk + DB
repository.saveDownloadedFile(fileId, fileName, mimeType, bytes, sessionId, senderId)
```

**`ReceivedFileRepository.saveDownloadedFile()`:**
```kotlin
// Save bytes to internal storage
val destFile = File(receivedDir, "${fileId}_${fileName}")
// receivedDir = /data/user/0/com.atezhare/files/received/
FileOutputStream(destFile).use { it.write(bytes) }

// Insert record into Room DB
dao.insert(ReceivedFile(
    fileId = fileId,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = bytes.size.toLong(),
    localPath = destFile.absolutePath,
    sessionId = sessionId,
    senderId = senderId,
    receivedAt = System.currentTimeMillis(),
    isViewed = false
))
```

The Room database update is observed by `SharedDataViewModel.fileList` (a LiveData),
so the Shared Data page **automatically shows the new file** without any manual refresh.

---

## 8. Pairing in Detail — QR vs Code

There are two independent ways to pair a sender and receiver. Both result in the
session reaching `status = "PAIRED"`.

### Flow A — Receiver Shows QR, Sender Scans It

```
RECEIVER (ReceiveFragment)                  SENDER (SendActivity)
─────────────────────────                   ────────────────────
POST /pair/receiver-qr
← { sessionId, qrData: "atezhare://pair?session=X&receiver=Y" }
Show QR bitmap on screen

                                            POST /session/create
                                            ← { sessionId: S2, code: "482917" }
                                            Open camera, point at receiver's QR

                                            Camera decodes: "atezhare://pair?session=X&receiver=Y"
                                            SendViewModel.onQrScanned("atezhare://pair?...")

                                            POST /pair/scan-qr
                                            Body: { senderId: "admin", qrContent: "atezhare://pair?session=X..." }

BACKEND PairController.scanQr():
    extractParam(qrContent, "session") → sessionId = "X"
    sessions.get("X").senderId = "admin"
    sessions.get("X").status = "PAIRED"
                                            ← { sessionId: "X", status: "PAIRED" }
                                            Sender's poll detects PAIRED → confirm dialog

Receiver's poll detects PAIRED → "Sender Connected" dialog
```

**Important:** In the QR flow, the receiver owns the session (they created it).
The sender's original session (`S2`) is effectively abandoned — `sessionId` in
`SendViewModel` is updated to the receiver's sessionId `X` when `scanQr` returns.

### Flow B — Sender Shows Code, Receiver Types It

```
SENDER (SendActivity)                       RECEIVER (ReceiveFragment)
─────────────────────                       ─────────────────────────
POST /session/create
← { sessionId: "f47ac10b", code: "482917" }
Display "482917" on screen

                                            User types "4", "8", "2", "9", "1", "7"
                                            ReceiveFragment.submitCode(boxes)
                                            → joins all 6 EditText boxes into "482917"
                                            → validates: QrUtils.isValidCode("482917") == true

                                            ReceiveViewModel.submitCode("482917")
                                            POST /pair/submit-code
                                            Body: { code: "482917", receiverUserId: "admin" }

BACKEND PairController.submitCode():
    codeToSession.get("482917") → sessionId = "f47ac10b"
    sessions.get("f47ac10b").receiverId = "admin"
    sessions.get("f47ac10b").status = "PAIRED"
                                            ← { sessionId: "f47ac10b", status: "PAIRED" }
                                            Receiver starts polling with sessionId = "f47ac10b"

Sender polling detects PAIRED → confirm dialog
```

**6-digit code input mechanics (`ReceiveFragment.setupCodeInputBoxes()`):**
- 6 separate `EditText` views: `et_code_1` through `et_code_6`.
- `TextWatcher.afterTextChanged()`: when a digit is typed, focus auto-advances to
  the next box. When the 6th digit is entered, `submitCode()` fires automatically.
- Backspace on an empty box moves focus to the previous box via `setOnKeyListener`.

---

## 9. Session Lifecycle & State Machine

```
                    ┌─────────┐
                    │ WAITING │  ← session created (either sender or receiver initiated)
                    └────┬────┘
                         │  receiver pairs (via QR scan or code entry)
                    ┌────▼────┐
                    │  PAIRED  │  ← both devices connected, waiting for sender confirm
                    └────┬────┘
                         │  POST /session/confirm called by sender
                    ┌────▼──────────┐
                    │ TRANSFERRING  │  ← sender is uploading files
                    └────┬──────────┘
                         │  FileController.markFilesReady() called after upload
                    ┌────▼────┐
                    │  DONE   │  ← files uploaded, receiver can download
                    └─────────┘

At any state:
    isExpired() == true  →  ERROR
    Any API returns ERROR → ERROR
```

**State transitions happen in two places:**

| Transition | Where | How |
|---|---|---|
| `→ WAITING` | `SessionController.createSession()` | Set on object creation |
| `→ PAIRED` (code flow) | `PairController.submitCode()` | `session.status = "PAIRED"` |
| `→ PAIRED` (QR flow) | `PairController.scanQr()` | `session.status = "PAIRED"` |
| `→ TRANSFERRING` | `SessionController.confirmSend()` | `session.status = "TRANSFERRING"` |
| `→ DONE` | `FileController.uploadFiles()` calls `SessionController.markFilesReady()` | `session.status = "DONE"` |
| `→ ERROR` | `SessionController.getSessionStatus()` when `isExpired()` | `session.status = "ERROR"` |

**How both devices see the state change:**

Both `SendViewModel.startPollingStatus()` and `ReceiveViewModel.startPollingStatus()`
run a `while(isActive)` coroutine loop, calling `GET /session/status/{sessionId}`
every 2000ms. The server reads from the same `sessions` map object. So when
the receiver's action changes `session.status` on the server, the sender's next
poll (within 2 seconds) will see the new value.

This is called **short polling** — simple but creates one HTTP request per device
every 2 seconds while a session is active.

---

## 10. File Transfer — How Data Moves

### Upload Path (Sender → Server)

```
SENDER DEVICE                          SPRING BOOT SERVER
─────────────                          ──────────────────
File on device:                        FileController.uploadFiles()
  /cache/photo.jpg (3.2 MB)
                                       For each MultipartFile:
FileUtils.localFilesToMultipart()        fileId = UUID.randomUUID()
  → reads File from disk                 storedName = fileId + "_photo.jpg"
  → creates RequestBody                  targetPath = ./uploads/storedName
  → wraps in MultipartBody.Part          Files.copy(inputStream, targetPath)
                                         fileIndex.put(fileId, targetPath)
POST /files/upload                       sessionFiles.put(sessionId, [entry])
  multipart/form-data                    savedFileIds.add(fileId)
  - sessionId: "f47ac10b"
  - files: [photo.jpg bytes]           After all files:
                                         SessionController.markFilesReady(sessionId, fileIds)
                                         → session.status = "DONE"
                                         → session.fileIds = ["3f2504e0-..."]
```

**Where files are stored on the server:**
```
./uploads/3f2504e0-4aef-4857-a7c6-0e02b2c3d479_photo.jpg
```

The `fileIndex` map (`HashMap<String, String>`) provides O(1) lookup:
`fileId → absolute file path`.

### Download Path (Server → Receiver)

```
RECEIVER DEVICE                         SPRING BOOT SERVER
───────────────                         ──────────────────
ReceiveViewModel poll returns DONE      FileController.downloadFile(fileId)
fileIds = ["3f2504e0-..."]

For each fileId:                          filePath = fileIndex.get(fileId)
                                          resource = new FileSystemResource(filePath)
GET /files/download/3f2504e0-...          → reads from ./uploads/
                                          Response headers:
                                            Content-Disposition: attachment; filename="photo.jpg"
                                            Content-Type: application/octet-stream
                                          Response body: raw file bytes

val bytes = body.bytes()
ReceivedFileRepository.saveDownloadedFile()
  destFile = /files/received/3f2504e0-..._photo.jpg
  FileOutputStream(destFile).write(bytes)
  ReceivedFileDao.insert(ReceivedFile(...))
```

**File naming on receiver device:**
```
/data/user/0/com.atezhare/files/received/3f2504e0-..._photo.jpg
```

The UUID prefix ensures uniqueness even if the same filename is received multiple times.

---

## 11. The Shared Data Page

### How files appear automatically

The key is Room's LiveData integration. The full chain:

```
ReceivedFileDao.getAllFiles() 
    → returns LiveData<List<ReceivedFile>>
    → Room automatically emits a new list whenever the DB changes

ReceivedFileRepository.allFiles 
    → wraps the DAO's LiveData

SharedDataViewModel.fileList 
    → switchMap on _currentFilter:
        if "all"  → repository.allFiles
        if "image/" → repository.getFilesByType("image/")
        ...
    → LiveData<List<ReceivedFile>>

SharedDataFragment.observeViewModel()
    → viewModel.fileList.observe(viewLifecycleOwner) { files → adapter.submitList(files) }
```

When `ReceiveViewModel.downloadAndSaveFiles()` inserts a new `ReceivedFile` into
the DB, Room notifies all active observers. If `SharedDataFragment` is on screen,
its RecyclerView updates immediately. If it's in the background, it updates the
next time the user navigates to it.

### Filter Tabs

```
btn_filter_all     → viewModel.setFilter("all")         → repository.allFiles
btn_filter_images  → viewModel.setFilter("image/")      → DAO: WHERE mimeType LIKE 'image/%'
btn_filter_docs    → viewModel.setFilter("application/")→ DAO: WHERE mimeType LIKE 'application/%'
btn_filter_videos  → viewModel.setFilter("video/")      → DAO: WHERE mimeType LIKE 'video/%'
```

### Opening a file

`SharedDataViewModel.openFile()`:
```kotlin
// 1. Mark viewed in DB
repository.markViewed(file.id)
// → UPDATE received_files SET isViewed = 1 WHERE id = :id
// → hides "NEW" badge on the card

// 2. Get file from internal storage
val localFile = File(file.localPath)

// 3. Generate content URI (required for Android 7+)
val uri = FileProvider.getUriForFile(context, "${packageName}.provider", localFile)
// FileProvider config: res/xml/file_paths.xml
// Authority: "com.atezhare.provider"

// 4. Launch system app
Intent(ACTION_VIEW).setDataAndType(uri, file.mimeType)
// → PDF opens in PDF viewer, image opens in Gallery, etc.
```

### Unviewed badge

```kotlin
ReceivedFileDao.getUnviewedCount()
→ SELECT COUNT(*) FROM received_files WHERE isViewed = 0
→ LiveData<Int>

SharedDataFragment observes viewModel.unviewedCount:
    tvNewCount.visibility = if (count > 0) VISIBLE else GONE
    tvNewCount.text = "$count new"
```

---

## 12. Network Layer

### `RetrofitClient.kt` — The HTTP Engine

`RetrofitClient` is a Kotlin `object` (singleton). The Retrofit instance is
created lazily — only on first use.

**OkHttp interceptor — auth token:**
```kotlin
.addInterceptor { chain ->
    val token = SessionTokenHolder.token   // set by SessionManager.saveSession()
    val request = if (token != null) {
        original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    } else original
    chain.proceed(request)
}
```

Every API call automatically includes `Authorization: Bearer atezhare-token-...`
if a login token exists.

**Timeouts (from `ApiConstants.kt`):**
- Connect: 30 seconds
- Read: 60 seconds (important — file downloads can be slow)
- Write: 60 seconds (important — file uploads can be slow)

### `ApiService.kt` — Retrofit Interface

Each function in this interface represents one HTTP endpoint. Retrofit generates
the actual implementation at runtime. All functions are `suspend` — they run in
a coroutine and return when the HTTP response arrives.

Key annotations:
- `@GET`, `@POST` — HTTP method
- `@Path("sessionId")` — replaces `{sessionId}` in the URL
- `@Body` — serialized to JSON by Gson
- `@Multipart` + `@Part` — for file upload
- `Response<T>` return type — gives access to HTTP status code and headers

### `SessionTokenHolder.kt` — Token Bridge

`SessionTokenHolder` is an `object` in `RetrofitClient.kt`:
```kotlin
object SessionTokenHolder {
    var token: String? = null
}
```

This is the bridge between `SessionManager` (which reads from SharedPreferences)
and the OkHttp interceptor (which runs in a background thread during HTTP calls).
When `SessionManager.saveSession()` is called after login, it writes to both
SharedPreferences AND this holder.

---

## 13. Backend — Spring Boot Deep Dive

### Application Startup — `ProjectApplication.java`

```java
@SpringBootApplication   // enables component scanning, auto-configuration
@EnableScheduling        // activates @Scheduled methods (session cleanup)
public class ProjectApplication { ... }
```

Context path `/atezhare` set in `application.properties`:
```
server.servlet.context-path=/atezhare
```
All endpoints are therefore under `http://host:8080/atezhare/...`

### Security — `SecurityConfig.java`

By default, Spring Security blocks all incoming requests with HTTP 401 unless
configured. `SecurityConfig.filterChain()` disables this:
```java
http.csrf(AbstractHttpConfigurer::disable)
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
    .formLogin(AbstractHttpConfigurer::disable)
    .httpBasic(AbstractHttpConfigurer::disable);
```

Without this file, every request from the Android app would receive 401 and the
app would silently fail.

### `SessionController.java` — Session State Machine

This is the most important backend class. It manages all session state.

**Data structures:**
```java
// Both are static — shared across all HTTP request threads
static final Map<String, ShareSession> sessions = new ConcurrentHashMap<>();
static final Map<String, String> codeToSession = new ConcurrentHashMap<>();
```

`ConcurrentHashMap` is used (not `HashMap`) because multiple HTTP requests from
different devices arrive simultaneously in different threads. `ConcurrentHashMap`
is thread-safe.

**`generateUniqueCode()`:**
```java
String code;
do {
    code = String.format("%06d", new Random().nextInt(1_000_000));
    // Generates a number between 0 and 999999, zero-padded to 6 digits
    // e.g. 482917, 003421, 999001
} while (codeToSession.containsKey(code));  // retry if collision
return code;
```

**`cleanupExpiredSessions()` — runs every 5 minutes:**
```java
@Scheduled(fixedDelay = 300_000)  // 300000 ms = 5 minutes
public void cleanupExpiredSessions() {
    sessions.entrySet().removeIf(e -> {
        if (e.getValue().isExpired(expiryMinutes)) {
            codeToSession.values().remove(e.getKey());
            return true;  // removes from sessions map
        }
        return false;
    });
}
```

**Static accessors (`getSessions()`, `getCodeToSession()`):**
`PairController` and `FileController` need access to the session state. Since
both maps are private, static accessor methods expose them:
```java
public static Map<String, ShareSession> getSessions() { return sessions; }
public static Map<String, String> getCodeToSession() { return codeToSession; }
```

This is simple but has a limitation: it only works within one JVM instance. In
a clustered/multi-server production setup, these would need to be replaced with
a shared database (Redis or a SQL table).

### `PairController.java` — Pairing Logic

**QR flow — `getReceiverQr()`:**
The receiver calls this first. It builds a QR data string:
```
atezhare://pair?session=<sessionId>&receiver=<userId>
```
This is a custom URI scheme. The ZXing library on the sender's Android app
decodes this string from the QR image. It is not a real URL — it is just a
structured string that both apps agree on.

**QR flow — `scanQr()`:**
The sender sends the decoded string back to the server:
```java
String sessionId = extractParam(qrContent, "session");
// extractParam finds "session=" in the string and returns everything until "&" or end
SessionController.getSessions().get(sessionId).status = "PAIRED";
```

**Code flow — `submitCode()`:**
```java
String sessionId = SessionController.getCodeToSession().get(code);
// Looks up which session has this 6-digit code
SessionController.getSessions().get(sessionId).receiverId = receiverUserId;
SessionController.getSessions().get(sessionId).status = "PAIRED";
```

### `FileController.java` — File Storage

**Upload — `uploadFiles()`:**
Files are stored in the `./uploads/` directory (relative to where the JAR runs,
or `/app/uploads/` inside Docker). The stored filename format:
```
{fileId}_{sanitized_original_name}
e.g. 3f2504e0-4aef-4857-a7c6-0e02b2c3d479_my_document.pdf
```

Sanitization: `replaceAll("[^a-zA-Z0-9._-]", "_")` prevents path traversal attacks.

After storing each file, `SessionController.markFilesReady()` is called as a
cross-controller static method call — the session's status becomes DONE and the
fileIds list is populated.

**Download — `downloadFile()`:**
```java
String filePath = fileIndex.get(fileId);         // O(1) lookup by UUID
FileSystemResource resource = new FileSystemResource(filePath);
return ResponseEntity.ok()
    .header(CONTENT_DISPOSITION, "attachment; filename=\"" + originalName + "\"")
    .header(CONTENT_TYPE, "application/octet-stream")
    .body(resource);                              // Spring streams the file
```

Spring Boot handles the streaming automatically — it reads the file from disk
in chunks and writes to the HTTP response. The Android app reads these bytes
via `body.bytes()`.

---

## 14. Local Database — Room

Room is Android's SQLite wrapper. It handles all DB operations.

### Table: `received_files`

| Column | Type | Description |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | Local row ID |
| fileId | TEXT | Backend UUID for this file |
| fileName | TEXT | Original filename (e.g. "photo.jpg") |
| mimeType | TEXT | e.g. "image/jpeg", "application/pdf" |
| fileSize | INTEGER | Bytes |
| localPath | TEXT | Absolute path on device |
| sessionId | TEXT | Which transfer session this came from |
| senderId | TEXT | Who sent it |
| receivedAt | INTEGER | Unix timestamp in milliseconds |
| isViewed | INTEGER | 0 = not opened, 1 = opened |

### Query flow for Shared Data page

```
SharedDataFragment opens
    → SharedDataViewModel.fileList LiveData is observed
    → _currentFilter is "all" (initial value)
    → switchMap activates ReceivedFileRepository.allFiles
    → ReceivedFileDao.getAllFiles() returns LiveData
    → Room executes: SELECT * FROM received_files ORDER BY receivedAt DESC
    → Results posted to UI thread
    → SharedDataAdapter.submitList(files) called
    → RecyclerView renders cards
```

When a new file arrives:
```
ReceivedFileDao.insert(newFile)
    → Room writes to SQLite
    → Room invalidates the LiveData query
    → getAllFiles() re-executes automatically
    → New list emitted to all observers
    → RecyclerView updates (DiffUtil handles smooth animations)
```

---

## 15. Complete API Reference

**Base URL:** `http://<server>:8080/atezhare/`

| Method | Endpoint | Request Body | Response | Called By |
|---|---|---|---|---|
| POST | `/auth/login` | `{ userId, password }` | `{ success, token, userId }` | `LoginViewModel.login()` |
| GET | `/auth/test` | — | `"Atezhare backend connected"` | Manual test |
| POST | `/session/create` | `{ userId, fileCount }` | `{ sessionId, code, status }` | `SendViewModel.createSession()` |
| POST | `/session/join` | `{ code, receiverUserId }` | `{ sessionId, code, status }` | Direct join (unused in current app) |
| GET | `/session/status/{sessionId}` | — | `{ sessionId, status, receiverId, fileIds[] }` | Both ViewModels (polling) |
| POST | `/session/confirm` | `{ sessionId, senderId }` | `{ success, message }` | `SendViewModel.confirmAndUpload()` |
| POST | `/files/upload` | multipart: sessionId + files | `{ success, fileIds[] }` | `SendViewModel.confirmAndUpload()` |
| GET | `/files/download/{fileId}` | — | Binary file bytes | `ReceiveViewModel.downloadAndSaveFiles()` |
| GET | `/files/session/{sessionId}` | — | `{ sessionId, files[] }` | Debug only |
| POST | `/pair/receiver-qr` | `{ receiverUserId, sessionId? }` | `{ sessionId, qrData, code }` | `ReceiveViewModel.requestReceiverQr()` |
| POST | `/pair/scan-qr` | `{ senderId, qrContent }` | `{ sessionId, status }` | `SendViewModel.onQrScanned()` |
| POST | `/pair/submit-code` | `{ code, receiverUserId }` | `{ sessionId, status }` | `ReceiveViewModel.submitCode()` |

---

## 16. Data Flow Diagrams

### Complete Transfer — Code Flow (numbered steps)

```
SENDER DEVICE          SPRING BOOT              RECEIVER DEVICE
─────────────          ──────────               ───────────────

1. Select files
   (DirectoryFragment)

2. POST /session/create ──────────►  sessions["f47ac10b"] = {
   { userId, fileCount }              status: "WAITING",
                                      code: "482917"
   ◄── { sessionId, code }          }
   Show "482917" on screen

                                                 3. POST /pair/receiver-qr
                                                 { receiverUserId: "admin" }
                                      ◄──────────
                                      registerReceiverSession()
                                      → separate session "aabbcc" created
                                      ──────────► { sessionId: "aabbcc", qrData, code }
                                                 Show QR on screen

                                                 4. Start polling /session/status/aabbcc
                                                 ← { status: "WAITING" } (every 2s)

5. Camera scans QR
   decodes: "atezhare://pair?session=aabbcc&receiver=admin"

6. POST /pair/scan-qr ────────────► sessions["aabbcc"].status = "PAIRED"
   { senderId, qrContent }          sessions["aabbcc"].senderId = "admin"
   ◄── { sessionId: "aabbcc" }
   sessionId updated to "aabbcc"

7. Start polling /session/status/aabbcc         (receiver poll returns PAIRED)
   ← { status: "PAIRED" }                       Show "Sender Connected" dialog

8. Confirm dialog → "Send"

9. POST /session/confirm ─────────► sessions["aabbcc"].status = "TRANSFERRING"
   { sessionId: "aabbcc" }
   ◄── { success: true }
                                                 (receiver poll returns TRANSFERRING)
                                                 Show progress bar

10. POST /files/upload ───────────► fileIndex["uuid1"] = ./uploads/uuid1_photo.jpg
    multipart: sessionId + files     SessionController.markFilesReady()
    ◄── { fileIds: ["uuid1"] }       → sessions["aabbcc"].status = "DONE"
    DONE → SendActivity.finish()     → sessions["aabbcc"].fileIds = ["uuid1"]

                                                 11. Poll returns DONE + fileIds
                                                 GET /files/download/uuid1
                                                 ◄── binary bytes
                                                 ReceivedFileRepository.saveDownloadedFile()
                                                 → saved to /files/received/uuid1_photo.jpg
                                                 → Room DB insert
                                                 → SharedDataFragment updates automatically
                                                 Show success dialog
```

---

*End of Atezhare Technical Documentation*
