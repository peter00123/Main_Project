# Atezhare — Android App

Secure peer-to-peer document sharing app. Pairs a sender and receiver via QR code or 6-digit code, then transfers files through a Spring Boot backend.

---

## Project Structure

```
app/src/main/java/com/atezhare/
├── model/
│   └── Models.kt              — All request/response data classes + LocalFile
├── network/
│   ├── ApiConstants.kt        — Base URL + all endpoint constants
│   ├── ApiService.kt          — Retrofit interface (all HTTP endpoints)
│   └── RetrofitClient.kt      — Retrofit singleton + SessionTokenHolder
├── utils/
│   ├── SessionManager.kt      — SharedPreferences login session
│   ├── QrUtils.kt             — ZXing QR bitmap generation + code validation
│   └── FileUtils.kt           — URI → LocalFile, LocalFile → MultipartBody
└── ui/
    ├── auth/
    │   ├── SplashActivity.kt  — Entry point, session check → route to Login or Main
    │   ├── LoginActivity.kt   — Login screen (hardcoded: admin/1234)
    │   └── LoginViewModel.kt  — Login logic + API call
    ├── home/
    │   ├── MainActivity.kt    — Toolbar + DrawerLayout + BottomNav host
    │   └── HomeFragment.kt    — Landing: Send / Receive buttons
    ├── directory/
    │   ├── DirectoryFragment.kt — File picker + checked list + Send button
    │   ├── DirectoryViewModel.kt — File list state
    │   └── DirectoryAdapter.kt   — RecyclerView adapter with checkboxes
    ├── send/
    │   ├── SendActivity.kt    — QR scanner (CameraX) + 6-digit code display
    │   └── SendViewModel.kt   — createSession → poll → scanQr → confirmAndUpload
    ├── receive/
    │   ├── ReceiveFragment.kt — QR display + 6-box code entry + submit
    │   └── ReceiveViewModel.kt — requestReceiverQr → submitCode → poll
    └── profile/
        ├── ProfileFragment.kt  — User info display
        └── SettingsFragment.kt — Settings placeholder
```

---

## Backend API Contract

Base URL: `http://10.0.2.2:8080/atezhare/`  
(Use `10.0.2.2` for Android emulator → localhost. Change to real IP for device.)

| Method | Endpoint                        | Used By           | Purpose                                   |
|--------|---------------------------------|-------------------|-------------------------------------------|
| POST   | `/auth/login`                   | LoginViewModel    | Authenticate user, get token              |
| POST   | `/session/create`               | SendViewModel     | Sender creates session → gets code        |
| POST   | `/session/join`                 | ReceiveViewModel  | Receiver joins session via code           |
| GET    | `/session/status/{sessionId}`   | Both ViewModels   | Poll session state (WAITING/PAIRED/DONE)  |
| POST   | `/session/confirm`              | SendViewModel     | Sender confirms transfer                  |
| POST   | `/files/upload`                 | SendViewModel     | Multipart upload of selected files        |
| GET    | `/files/download/{fileId}`      | ReceiveViewModel  | Download a received file                  |
| POST   | `/pair/receiver-qr`             | ReceiveViewModel  | Receiver gets their QR data + sessionId   |
| POST   | `/pair/scan-qr`                 | SendViewModel     | Sender submits scanned QR content         |
| POST   | `/pair/submit-code`             | ReceiveViewModel  | Receiver submits 6-digit code             |

### Session Status Flow
```
WAITING → PAIRED → TRANSFERRING → DONE
                               ↘ ERROR
```

---

## Transfer Flows

### QR Flow
```
Receiver opens app          →  GET /pair/receiver-qr  →  QR displayed
Sender scans receiver's QR  →  POST /pair/scan-qr     →  Session paired
Sender sees confirm dialog  →  POST /session/confirm  →  Transfer starts
                            →  POST /files/upload     →  Files sent
Receiver polls status       →  GET /session/status    →  Status = DONE
```

### Code Flow
```
Sender opens SendActivity   →  POST /session/create   →  6-digit code shown
Receiver enters code        →  POST /pair/submit-code →  Session paired
Sender polling detects PAIRED → confirm dialog pops up
Sender confirms             →  POST /session/confirm  →  Transfer starts
                            →  POST /files/upload     →  Files sent
```

---

## Setup Instructions

### 1. Open in Android Studio
Open the `atezhare/` folder as an existing Android Studio project.

### 2. Sync Gradle
Android Studio will prompt to sync. Click **Sync Now**.

### 3. Add a font (optional)
Download `Poppins-Bold.ttf` from Google Fonts and place it in:
`app/src/main/res/font/app_font_bold.ttf`
Or remove the `fontFamily` reference from `activity_login.xml`.

### 4. Configure Backend URL
Edit `network/ApiConstants.kt`:
```kotlin
const val BASE_URL = "http://YOUR_SERVER_IP:8080/atezhare/"
```
- Emulator: `10.0.2.2`
- Physical device: your PC's LAN IP (e.g., `192.168.1.100`)

### 5. Run
Select a device/emulator and click **Run**.

---

## Credentials (Hardcoded)
| Field    | Value  |
|----------|--------|
| User ID  | admin  |
| Password | 1234   |

These are validated locally in `LoginViewModel`. When the Spring Boot backend is ready, replace with server-side auth.

---

## Key Dependencies
| Library              | Purpose                              |
|----------------------|--------------------------------------|
| Retrofit 2 + Gson    | HTTP client + JSON serialization     |
| OkHttp + Logging     | HTTP layer + debug logging           |
| CameraX              | Camera preview for QR scanning       |
| ZXing                | QR code generation + decoding        |
| Navigation Component | Fragment navigation + bottom nav     |
| Material 3           | UI components and theming            |
| Kotlin Coroutines    | Async/await for network calls        |
| Lifecycle ViewModel  | UI state management                  |
