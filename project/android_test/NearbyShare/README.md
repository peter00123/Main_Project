# NearbyShare — Android App (Kotlin)

A pixel-faithful replica of Google's **Nearby Share** for Android, written entirely in **Kotlin** with a clean, layered architecture. Supports Android 6.0 (API 23) and above — covering ~95 %+ of active devices.

---

## 📁 Project Structure

```
NearbyShare/
├── app/
│   ├── src/main/
│   │   ├── java/com/nearbyshare/
│   │   │   ├── NearbyShareApp.kt              ← Application class (Hilt entry point)
│   │   │   ├── data/
│   │   │   │   ├── models/
│   │   │   │   │   ├── NearbyDevice.kt        ← Discovered peer device model
│   │   │   │   │   ├── SharePayload.kt        ← Content being transferred
│   │   │   │   │   └── TransferSession.kt     ← Live transfer session model
│   │   │   │   └── repository/
│   │   │   │       └── NearbyShareRepository.kt ← Data source + BLE simulation
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt               ← Hilt DI bindings
│   │   │   ├── service/
│   │   │   │   └── NearbyShareService.kt      ← Foreground service (BLE + Wi-Fi)
│   │   │   ├── ui/
│   │   │   │   ├── activities/
│   │   │   │   │   ├── MainActivity.kt        ← Single-activity host
│   │   │   │   │   ├── ShareActivity.kt       ← System share-sheet target
│   │   │   │   │   ├── ReceiveActivity.kt     ← Wake-screen incoming transfer
│   │   │   │   │   └── SettingsActivity.kt    ← Settings host
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── HomeFragment.kt        ← Device discovery screen
│   │   │   │   │   ├── TransferFragment.kt    ← Progress / accept / done
│   │   │   │   │   ├── DevicePickerBottomSheet.kt ← Pick device when sharing
│   │   │   │   │   ├── TransferBottomSheet.kt ← Choose files from home
│   │   │   │   │   ├── IncomingTransferBottomSheet.kt ← Accept / Decline
│   │   │   │   │   └── SettingsFragment.kt    ← Preference screen
│   │   │   │   ├── adapters/
│   │   │   │   │   └── DevicesAdapter.kt      ← RecyclerView adapter for devices
│   │   │   │   └── viewmodels/
│   │   │   │       ├── MainViewModel.kt       ← Home screen state + actions
│   │   │   │       └── TransferViewModel.kt   ← Transfer progress state
│   │   │   └── utils/
│   │   │       ├── PermissionUtils.kt         ← Runtime permission helpers
│   │   │       └── FileUtils.kt               ← MIME type, size, file creation
│   │   ├── res/
│   │   │   ├── layout/          ← All XML layouts (activities, fragments, items)
│   │   │   ├── drawable/        ← 20+ vector icons
│   │   │   ├── navigation/      ← nav_graph.xml
│   │   │   ├── menu/            ← bottom_nav_menu.xml
│   │   │   ├── values/          ← strings, colors, themes
│   │   │   ├── anim/            ← slide/fade transition animations
│   │   │   └── xml/             ← preferences, file_paths, backup rules
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat
```

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| UI | Material Design 3 · ViewBinding · Navigation Component |
| Architecture | MVVM · Unidirectional Data Flow (UDF) |
| Async | Kotlin Coroutines · StateFlow · SharedFlow |
| DI | Hilt (Dagger) |
| Image loading | Glide 4 |
| Background | Foreground Service |
| Min SDK | 23 (Android 6.0) |
| Target SDK | 34 (Android 14) |

---

## 🚀 How to Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps
```bash
# 1. Open in Android Studio
File → Open → select the NearbyShare folder

# 2. Sync Gradle (automatic on first open)

# 3. Run on device or emulator
Run → Run 'app'

# Or build APK from terminal:
./gradlew assembleDebug
# APK output: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔑 Key Features

### Device Discovery
- Simulates BLE advertising and scanning
- Devices appear progressively (realistic scan behaviour)
- 2-column card grid with avatar, device name, signal label
- Avatar colour derived from device ID hash (consistent colours)

### File Transfer
- Full sender/receiver lifecycle: PENDING → CONNECTING → AWAITING_ACCEPT → TRANSFERRING → COMPLETED
- Circular progress ring with percentage, speed (MB/s), and ETA
- Accept / Decline bottom sheet wakes the screen even when locked
- Cancel from either side at any point

### Share Target
- Registered as an Android share-sheet target (`ACTION_SEND` / `ACTION_SEND_MULTIPLE`)
- Handles images, videos, documents, APKs, URLs, plain text
- Bottom-sheet device picker shows immediately after the user picks NearbyShare

### Settings
- Device name (editable, shown to peers during discovery)
- Visibility mode: Everyone / Your contacts / Hidden
- Wi-Fi only toggle
- Notification preferences

---

## 📐 Architecture

```
UI (Fragment/Activity)
       ↑ observes StateFlow
  ViewModel
       ↑ collects Flow        ← user events ↓ suspend calls
  Repository (interface)
       ↑ implements
  NearbyShareRepository
  (BLE + Wi-Fi simulation)
```

All ViewModel → Repository communication uses `suspend` functions running in `viewModelScope`. The Repository emits `StateFlow<List<NearbyDevice>>` and `StateFlow<TransferSession?>` which the ViewModels collect and map into UI-ready state objects.

---

## 🔒 Permissions

The app requests the minimum required permissions per API level:

| API | Permissions |
|---|---|
| 23–30 | `ACCESS_FINE_LOCATION`, `BLUETOOTH`, `BLUETOOTH_ADMIN`, `READ_EXTERNAL_STORAGE` |
| 31–32 | `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE` |
| 33+ | `NEARBY_WIFI_DEVICES`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO` |

---

## 📝 Comments & Documentation

Every Kotlin file begins with:
1. A **file header** listing the package, index of contents, and objective
2. **KDoc comments** on every class and public function
3. **Inline comments** explaining non-obvious logic blocks

---

## ⚡ Extending to a Real Implementation

To connect this to actual Bluetooth/Wi-Fi hardware, replace the simulation logic in `NearbyShareRepository` with:

- **BLE advertising**: `BluetoothLeAdvertiser.startAdvertising()`
- **BLE scanning**: `BluetoothLeScanner.startScan()`
- **Wi-Fi Direct**: `WifiP2pManager` for peer discovery and socket connection
- **Google Nearby Connections API** (`com.google.android.gms:play-services-nearby`) for a higher-level abstraction that handles both BLE and Wi-Fi automatically

The ViewModel and UI layers require **zero changes** — only the Repository implementation changes.
