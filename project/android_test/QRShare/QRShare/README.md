# QRShare — Android File Sharing App

A **Nearby Share alternative** for Android that uses **QR code pairing** instead of proximity radar.
Transfer files between two Android devices over your local Wi-Fi network — no internet required.

---

## Features

- **QR Code Pairing** — Sender generates a QR code; receiver scans it to connect instantly
- **No Internet Required** — All transfers happen over your local Wi-Fi network (LAN)
- **Any File Type** — Photos, videos, audio, documents, APKs — anything
- **Multi-file Transfer** — Queue multiple files in one session
- **Live Progress** — Real-time per-file progress bars, speed (MB/s), and ETA
- **Background Transfer** — Foreground service keeps transfers alive when app is backgrounded
- **Light Theme** — Clean, modern Material Design 3 UI

---

## Architecture & Package Structure

```
com.qrshare/
├── activities/               ← All UI screens (Activities)
│   ├── MainActivity.kt       ← Home screen: Send / Receive buttons
│   ├── SendActivity.kt       ← File picker + Generate QR
│   ├── QRPairDisplayActivity.kt  ← Shows QR code for receiver to scan
│   ├── QRScanActivity.kt     ← Camera QR scanner (CameraX + ML Kit)
│   ├── ReceiveActivity.kt    ← Receive entry screen
│   └── TransferActivity.kt   ← Live transfer progress (send & receive)
│
├── network/                  ← All networking logic
│   ├── NetworkModels.kt      ← Data classes: SessionInfo, TransferFile, TransferProgress, etc.
│   ├── WiFiDirectManager.kt  ← Wi-Fi Direct P2P management
│   ├── WiFiDirectBroadcastReceiver.kt  ← System broadcast listener
│   ├── SocketServer.kt       ← TCP server — receives file bytes
│   ├── SocketClient.kt       ← TCP client — sends file bytes
│   └── FileTransferService.kt  ← Foreground Service orchestrating transfers
│
├── sharing/                  ← File & QR utilities
│   ├── QRCodeHelper.kt       ← ZXing QR encode/decode
│   ├── FilePickerHelper.kt   ← URI → TransferFile resolution, formatting
│   └── DownloadDirectoryHelper.kt  ← Manages Downloads/QRShare save folder
│
└── ui/                       ← UI components
    ├── QRScanOverlayView.kt  ← Custom canvas overlay for scanner screen
    ├── SelectedFilesAdapter.kt  ← RecyclerView: chosen files list
    └── TransferItemAdapter.kt   ← RecyclerView: per-file transfer progress
```

---

## How It Works

### Sender Flow
1. Open QRShare → tap **Send Files**
2. Tap **Add Files** and pick one or more files
3. Tap **Generate QR** — the app encodes `SessionInfo` (local IP + port + session ID) into a QR bitmap
4. The **QR Display** screen shows the code
5. Tap **"Receiver Connected → Start Sending"** after the receiver scans
6. Watch live progress in **TransferActivity**

### Receiver Flow
1. Open QRShare → tap **Receive Files**
2. Tap **Scan QR Code** — camera opens
3. Point camera at sender's QR code — auto-detected via ML Kit
4. App decodes `SessionInfo`, navigates to **TransferActivity** in receive mode
5. `SocketServer` starts on the session port, waits for sender to connect
6. Files arrive in **Downloads/QRShare**

### Transfer Protocol (TCP Socket)
```
Sender (SocketClient)                    Receiver (SocketServer)
       |  ——— TCP connect ————————————————→  |
       |  ——— {"fileName":…,"fileSize":…}\n → |   ← JSON metadata line
       |  ——— <raw file bytes> ————————————→  |   ← exact fileSize bytes
       |  (repeat for each file)              |
       |  ——— END_SESSION\n ————————————————→ |   ← signals completion
       |  ←——————— socket close ——————————— |
```

---

## Prerequisites

- Android Studio **Hedgehog** (2023.1.1) or newer
- Android SDK 34
- Kotlin 1.9+
- Two Android devices on the **same Wi-Fi network**
  (or the sender creates a Wi-Fi hotspot and the receiver connects to it)

---

## Setup & Build

```bash
# Clone / unzip the project
cd QRShare

# Open in Android Studio
# OR build from command line:
./gradlew assembleDebug

# Output APK:
# app/build/outputs/apk/debug/app-debug.apk
```

### Required Permissions (requested at runtime)
| Permission | Purpose |
|---|---|
| `CAMERA` | QR code scanning |
| `ACCESS_FINE_LOCATION` | Required by Wi-Fi Direct API |
| `NEARBY_WIFI_DEVICES` (API 33+) | Wi-Fi Direct on Android 13+ |
| `READ_MEDIA_IMAGES/VIDEO/AUDIO` (API 33+) | File picker |
| `READ_EXTERNAL_STORAGE` (API < 33) | File picker |
| `INTERNET` | Socket transfers |
| `FOREGROUND_SERVICE` | Background transfer service |

---

## Key Dependencies

| Library | Version | Use |
|---|---|---|
| ZXing Android Embedded | 4.3.0 | QR generation |
| ML Kit Barcode Scanning | 17.2.0 | QR scanning |
| CameraX | 1.3.1 | Camera preview |
| OkHttp | 4.12.0 | (available for HTTP fallback) |
| Kotlinx Coroutines | 1.7.3 | Async networking |
| Material Components | 1.11.0 | UI |
| Gson | 2.10.1 | JSON serialisation |

---

## Notes & Limitations

- **Same network required**: Both devices must be on the same Wi-Fi network, or one must be the hotspot.
- **IP detection**: The app picks the first non-loopback IPv4 address. On some devices you may need to ensure both are on the same subnet.
- **Large files**: The 64 KB socket buffer is efficient for files of any size. A 1 GB file over Wi-Fi typically transfers in under 2 minutes.
- **Security**: Transfers are unencrypted (cleartext TCP). For production use, wrap the socket in TLS.

---

## License
MIT — free to use, modify, and distribute.
