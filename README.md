# TaskManager - d.light Android Engineer Take-Home Challenge

This is my solution for the d.light Android Engineer Take-Home Challenge, demonstrating an offline-first task management app with robust synchronization capabilities for network-limited environments.

## Challenge Overview

**Goal:** Build a Task Manager app that works fully offline, syncs automatically with a remote backend when online, and handles conflicts between local and remote updates in a predictable way.

**Key Features:**
- [x] **Offline-First:** All operations work without network and persist locally
- [x] **Intelligent Sync:** Background sync using WorkManager + immediate sync via NetworkMonitor
- [x] **Conflict Resolution:** Last-write-wins strategy based on `updatedAt` timestamps
- [x] **Clean Architecture:** MVVM pattern with Repository abstraction and Room as single source of truth
- [x] **Comprehensive Testing:** Unit, and Integration tests with MockWebServer

**Pending Features:**
- [ ] **Advanced Sync:** Manual conflict resolution with user choice dialogs *(in development on `feat-adv-sync` branch)*

## Development Setup

*This setup handles the complexity of Android network access across different environments. Emulators and physical devices require different IP configurations to reach your local server.*

### Prerequisites
- **Node.js** installed on your system

### 1. Server Setup
*Provides a local json-server API endpoint for testing sync functionality without requiring a production backend.*

```bash
# Start the mock server
cd server
npm install
npm start
# Server runs on http://localhost:3000
```

**Task Schema:** See `server/schema.json` for detailed field types and example data.

### Available Endpoints
- `GET /tasks`
- `POST /tasks`
- `PUT /tasks/:id`
- `DELETE /tasks/:id`
### Unavailable Endpoints
- `GET /tasks?since=<timestamp>` - once added, it'll be used to Fetch tasks modified since timestamp

### 2. Configure IP for Device Testing

**For Android Emulator:**
*Emulators use `10.0.2.2` to access the host machine's localhost, enabling API communication.*

```bash
./gradlew assembleEmulatorDebug
# Uses: http://10.0.2.2:3000
```

**For Physical Device:**
*Physical devices need your actual WiFi IP address since they can't access `localhost`. WiFi IPs change frequently, requiring updates. The script below automatically updates the BASE_URL in the gradle file.*

```bash
# IMPORTANT: Update IP before building
./update_device_ip.sh

# Option 1: Command line
./gradlew assembleDeviceDebug
adb install app/build/outputs/apk/device/debug/app-device-debug.apk

# Option 2: Android Studio
# Switch Build Variants panel to 'deviceDebug' before hitting Run
# Uses: http://YOUR_WIFI_IP:3000
```

**Manual IP Update:**
Edit `app/build.gradle.kts` → `device` flavor → Update IP address

## Sync Architecture

### Dual-Layer Sync: NetworkMonitor + WorkManager

**Problem:** Users in network-limited areas may only have brief connectivity windows (e.g., 2-minute WiFi at a bus stop).

**Solution:** 
- **NetworkMonitor** - Detects network changes instantly while app is open, triggers immediate sync
- **WorkManager** - Runs every 15 minutes in background, ensures eventual sync even if app is closed
- **Why both?** WorkManager alone would miss short connectivity windows; NetworkMonitor alone wouldn't sync when app is closed

**Benefits:** This architecture maximizes sync opportunities in network-limited environments while preserving battery life. Users get instant synchronization during brief connectivity windows when the app is open, while background sync ensures data consistency even when the app is closed. The offline-first approach with Room as the single source of truth guarantees the app remains fully functional without network access, making it reliable for users in areas with intermittent connectivity.

## Tests

Run tests: `./gradlew test` (unit) and `./gradlew connectedAndroidTest` (integration/UI)

<details>
<summary>Test Coverage</summary>

**Unit Tests:**
- `TaskMapperTest` - Data layer mapping (Entity ↔ Domain ↔ DTO)
- `AuthDataRepositoryTest` - Mock authentication with token management  
- `TaskDataRepositoryTest` - CRUD operations and Resource wrapper testing

**Integration Tests:**
- `TaskDaoTest` - Room database operations with in-memory testing
- `AuthRepositoryMockWebServerTest` - API integration with MockWebServer

**UI Tests:**
- `LoginAddUpdateTaskTest` - End-to-end user journey (Login → Create → Edit → Verify) ⚠️ *In development*

</details>

## Further Improvements

### Data Reset Handling
**Current Limitation:** When app data is cleared or user logs in on a new device, all server tasks are restored as the app treats this as a "first sync" scenario.

**Potential Solutions:**
- **User Intent Detection**: Distinguish between accidental data loss vs intentional reset
- **User Choice Dialog**: On fresh install, ask "Restore previous tasks?" vs "Start fresh?"
- **Server-Side Soft Delete**: Implement task deletion syncing with server cleanup

### Enhanced Conflict Resolution
- Support for field-level merging instead of whole-task overwrites
- User-driven conflict resolution for simultaneous edits
- Backup/restore functionality for critical data loss scenarios

### Performance Optimizations
- Incremental sync with delta updates
- Background sync frequency based on user activity
- Local caching improvements for faster app startup