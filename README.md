# Splitwise POC - README

This is a proof-of-concept Android application that replicates core Splitwise functionality with **dual sync strategies**: Firebase for internet sync and P2P for local sync.

## Features

- **Group Management**: Create and join expense groups
- **Expense Tracking**: Add expenses split among group members
- **Balance Calculation**: Automated debt calculation and settlement tracking
- **Dual Sync Modes**:
  - **Firebase variant**: Internet sync anywhere via Firebase Realtime Database
  - **P2P variant**: Local sync via Nearby Connections (WiFi Direct/Bluetooth)

## Build Variants

This app supports **two sync modes** via Android build flavors:

| Variant | Sync Method | Requirements | Use Case |
|---------|------------|--------------|----------|
| **`firebase`** | Firebase Realtime Database | Firebase project + `google-services.json` | Remote teams, internet sync |
| **`p2p`** | Nearby Connections API | Location permission | Local groups, no setup |

## Architecture & Technology

**Local-First Design**:
- Works completely offline
- Syncs when connection is available
- Your data stays on your device

**Tech Stack**:
- **UI**: Jetpack Compose + Material3
- **Database**: Room (SQLite)
- **Sync**: Firebase (Internet) or Nearby Connections (P2P)

> **For Developers**: Detailed architecture, database schema, and implementation plans are available in [splitwise_poc_plan.md](splitwise_poc_plan.md).


## Building and Running

### Prerequisites

1.  **JDK 11+**
    ```bash
    java -version
    ```

2.  **Android SDK** (if not using Android Studio)
    -   Download from [Android developer website](https://developer.android.com/studio#command-tools)
    -   Set `ANDROID_HOME` environment variable

### Build Commands

Navigate to project directory:
```bash
cd /Users/tanyf/Documents/SplitOpen/splitwise-android
```

**Build P2P variant** (recommended for quick start - no Firebase setup):
```bash
./gradlew assembleP2pDebug
```
Output: `app/build/outputs/apk/p2p/debug/app-p2p-debug.apk`

**Build Firebase variant** (requires Firebase setup):
```bash
./gradlew assembleFirebaseDebug
```
Output: `app/build/outputs/apk/firebase/debug/app-firebase-debug.apk`

### Installing

Connect Android device with USB debugging enabled:
```bash
# Install P2P variant
adb install app/build/outputs/apk/p2p/debug/app-p2p-debug.apk

# OR install Firebase variant
adb install app/build/outputs/apk/firebase/debug/app-firebase-debug.apk
```

## Firebase Setup (Firebase Variant Only)

If building the Firebase variant:

1.  Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2.  Add an Android app to your project
3.  Download `google-services.json`
4.  Place it in `app/src/firebase/google-services.json`
5.  Enable Firebase Realtime Database in the console
6.  Set security rules (see `implementation_plan.md`)

## P2P Sync Usage

**Creating a Group**:
1.  Open app on Device A
2.  Tap "Create Group"
3.  App starts advertising the group

**Joining a Group**:
1.  Open app on Device B (must be nearby)
2.  App auto-discovers nearby groups
3.  Tap to connect
4.  Devices exchange all events

**Syncing**:
-   Changes sync automatically when devices are connected
-   Works without internet
-   Range: WiFi Direct (~100m) or Bluetooth (~10m)
-   **Topology**: `P2P_CLUSTER` (mesh) - any peer can sync with any peer
-   **Encryption**: UKEY2 authenticated encryption (automatic)

**Sync Status UI**:
-   "Looking for nearby groups..." (Discovering)
-   "Device 'John's Phone' requesting to join" (Authenticating)
-   "Syncing 45/100 events..." (Progress)
-   "All synced ✓ 2 minutes ago" (Up to date)

## Firebase Sync Usage

**Creating a Group**:
1.  Open app
2.  Tap "Create Group"
3.  Copy group code

**Joining a Group**:
1.  Open app on another device (anywhere in the world)
2.  Tap "Join Group"
3.  Enter group code
4.  Data syncs automatically via internet

**Syncing**:
-   Real-time sync over internet
-   Works from anywhere
-   Automatic offline queueing

## Conflict Resolution

### Firebase Variant
**Strategy**: Event Sourcing + Server Timestamps (LWW)

-   Events are immutable (append-only log)
-   Firebase server timestamps determine order
-   For conflicting edits, latest timestamp wins
-   Soft deletes prevent data resurrection
-   Idempotent event processing

### P2P Variant
**Strategy**: Event Sourcing + Hybrid Logical Clocks (HLC)

-   **Problem**: Device clocks may be out of sync (clock skew)
-   **Solution**: Hybrid Logical Clocks combine wall clock + logical counter
-   Events ordered by: `(wallClock, logicalCounter, nodeId)`
-   Ensures causal ordering even with clock skew
-   Example:
    ```
    Device A (clock 5 min behind): Edit → HLC(10:00, 0, A)
    Device B (correct clock): Edit → HLC(10:05, 0, B)
    Resolution: B wins, but A's next edit gets HLC(10:05, 1, A)
    ```

## Advanced Features

### Cross-Variant Migration
**Export/Import groups between P2P and Firebase variants**

**Why**:
- Start with P2P (no setup) → migrate to Firebase when needed
- Camping trip (P2P) → back to city (Firebase for remote access)
- Event sourcing makes this trivial (export = JSON event log)

**Usage**:
```kotlin
// Export group (P2P variant)
val file = groupExporter.exportGroup(groupId)
shareFile(file)  // Share via any app

// Import group (Firebase variant)
val file = selectFile()
groupExporter.importGroup(file)
// Group now syncs via Firebase!
```

### Event Log Snapshots (Optional)
**Performance optimization for long-lived groups**

**Problem**: Replaying 5,000 events on app launch = slow

**Solution**: Snapshot group state every 100 events
- Load latest snapshot + replay only recent events
- 1000 events: ~2-3s → ~200ms load time
- Snapshot strategy: Every 100 events, keep last 3 snapshots

**Scope**: Optional for POC, recommended for production

## Project Structure

```
app/src/
├── main/java/com/example/splitwise/
│   ├── data/local/          # Room database
│   ├── data/sync/           # SyncManager interface
│   ├── data/user/           # UserIdManager
│   ├── data/repository/     # Repositories
│   ├── domain/              # Use cases & models
│   ├── ui/                  # Compose screens
│   └── viewmodel/           # ViewModels
├── firebase/java/...        # Firebase + server timestamps
└── p2p/java/...             # P2P + hybrid clocks
```

## Development

**Run tests**:
```bash
./gradlew test
```

**Build both variants**:
```bash
./gradlew assembleDebug
```

**Clean build**:
```bash
./gradlew clean build
```

## Troubleshooting

**Firebase variant build fails**:
- Ensure `google-services.json` is in `app/src/firebase/`
- Check Firebase project configuration

**P2P connection fails**:
- Grant location permissions (required by Android for Nearby)
- Ensure both devices have WiFi/Bluetooth enabled
- Devices must be within range (~10-100m)

**Sync not working**:
- Check internet connection (Firebase variant)
- Check log output with `adb logcat`

## Future Enhancements

- [ ] Unequal expense splits
- [ ] Group chat
- [ ] Settlement suggestions (minimize transactions)
- [ ] QR code group joining for P2P
- [ ] Export to CSV
- [ ] Push notifications (Firebase variant)
- [ ] Multi-currency support
- [ ] Receipt photo attachments
- [x] Cross-variant migration (Export/Import) - **Implemented**
- [x] Granular sync states - **Implemented**
- [ ] Event log snapshots (optional performance optimization)

## License

MIT License - Educational/POC purposes

---

**Note**: This is a proof-of-concept application. Not recommended for production use without additional security, testing, and error handling.
