# Google Antigravity Android

A native, ultra-modern, lightweight Android application for **Google Antigravity**, built with **Kotlin**, **Jetpack Compose**, **Material 3 (Material You / Dynamic Colors)**, and **Clean Architecture**.

The app untethers developers from their desks, enabling them to drive active workstations & CLI daemons via **Remote Control**, orchestrate autonomous background agents in **Remote Cloud Projects**, approve sensitive tool executions from the notification shade, and inspect rich deliverables and artifacts anywhere.

---

## 🚀 Key Highlights & Standards

- **Target SDK 35 (Android 15) & Min SDK 26 (Android 8.0+)**: Built with the latest Android platform capabilities, edge-to-edge system insets, and predictive back support.
- **Modern Jetpack Compose & Material 3**: Seamless dynamic color theming with a developer-first dark mode aesthetic (Deep Space Slate, Electric Violet `#7C4DFF`, Neon Cyan `#00E5FF`, and Emerald `#00E676`).
- **Hybrid Powerhouse Architecture**:
  - **Native Shell & Dashboards**: Instance Switcher, Agent Reasoning Depth Dial, Cloud Project Manager, Deliverables/Artifacts Inspector, and Settings.
  - **Embedded High-Performance WebView Wrapper**: Loads `antigravity.google.com` surfaces with custom mobile layout optimizations and hardware acceleration.
  - **Bidirectional JavaScript Bridge (`AntigravityWebBridge`)**: Injects `antigravity_bridge.js` to observe DOM mutations, intercept Web Notifications, and wire web events directly into native Android APIs.
- **Proactive Push & Heads-up Notifications**:
  - Full **Android 13+ `POST_NOTIFICATIONS`** runtime permission integration.
  - **Direct Notification Actions**: When an agent requests tool approval (e.g., executing shell commands, file modifications), a heads-up alert appears with instant **"Approve (Y)"** and **"Reject (N)"** buttons. Approvals are transmitted without even unlocking the phone or launching the full UI!
  - `AntigravitySyncWorker` with Android `WorkManager` for periodic background health checks and task completion alerts.
- **Biometric Security Protection**: AndroidX `BiometricPrompt` gatekeeping for remote workstation execution and high-risk tool authorizations.
- **Deep Linking**: Seamless URL handling for `https://antigravity.google.com/r/*` (Remote Control tunnels), `/projects/*` (Cloud Sandboxes), and `/c/*` (Active Conversations).

---

## 📱 Navigation & Screen Breakdown

| Tab | Destination | Functionality |
| :--- | :--- | :--- |
| **0** | **Workspace** | Live terminal & agent conversation stream inside the modern WebView wrapper with a native quick-prompt floating bar and slash command shortcuts (`/goal`, `/plan`, `/remote-control`). |
| **1** | **Remote Control** | Real-time workstation and CLI daemon status dashboard (`romin-macbook-pro`, `devbox-swift-falcon`). Launch direct reverse tunnels or copy headless daemon commands. |
| **2** | **Cloud Projects** | Manage isolated Google-managed Linux cloud containers. Attach GitHub repositories, Google Drive files, and uploads staged at `/home/antigravity/upload`. |
| **3** | **Artifacts** | Tangible deliverables viewer: Implementation plans, code diffs, verification proofs, and generative UI mocks with syntax formatting. |
| **4** | **Settings** | Google Account tier management (Google AI Pro & Ultra), Dynamic Reasoning Depth adjustment (Fast / Balanced / Deep), biometric lock toggle, and notification rules. |

---

## 🛠️ Project Structure

```
antigravity-android/
├── .github/workflows/
│   └── android.yml                # CI/CD automated build & unit test workflow
├── app/
│   ├── build.gradle.kts           # App module configuration (Compose, Coroutines, Serialization)
│   ├── proguard-rules.pro         # R8 optimization rules for bridge, models, and workers
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/js/
│       │   │   └── antigravity_bridge.js  # Client-side observer & DOM scraper
│       │   ├── java/com/google/antigravity/mobile/
│       │   │   ├── AntigravityApp.kt      # Application initialization & notification channels
│       │   │   ├── bridge/
│       │   │   │   └── AntigravityWebBridge.kt # @JavascriptInterface bridge implementation
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   └── SessionManager.kt   # Persistent credentials & preference store
│       │   │   │   ├── model/
│       │   │   │   │   ├── AgentStatus.kt      # Agent state (Thinking, Executing, Idle)
│       │   │   │   │   ├── ArtifactItem.kt     # Deliverable models (Plans, Diffs, Proofs)
│       │   │   │   │   ├── CloudProject.kt     # Sandbox models & attached resources
│       │   │   │   │   ├── RemoteInstance.kt   # Workstations & CLI daemons
│       │   │   │   │   ├── ToolApprovalRequest.kt # Approval payload & risk levels
│       │   │   │   │   └── UserAccount.kt      # Google AI Pro/Ultra subscriber profiles
│       │   │   │   └── remote/
│       │   │   │       ├── AntigravityApiClient.kt # REST client for tunnels & dispatches
│       │   │   │       └── AntigravityScraper.kt   # Fallback DOM / regex scraper
│       │   │   ├── notification/
│       │   │   │   ├── ApprovalActionReceiver.kt   # BroadcastReceiver for 1-tap notification actions
│       │   │   │   └── NotificationHelper.kt       # Channel & notification builder
│       │   │   ├── security/
│       │   │   │   └── BiometricHelper.kt          # BiometricPrompt integration
│       │   │   ├── ui/
│       │   │   │   ├── MainActivity.kt             # Single activity host & deep links
│       │   │   │   ├── components/                 # TopBar, NavBar, ApprovalDialog, Cards
│       │   │   │   ├── screens/                    # Workspace, RemoteControl, Cloud, Artifacts, Settings
│       │   │   │   ├── theme/                      # Material 3 colors, typography, theme
│       │   │   │   └── viewmodel/
│       │   │   │       └── MainViewModel.kt        # StateFlow orchestration
│       │   │   └── worker/
│       │   │       └── AntigravitySyncWorker.kt    # Background WorkManager periodic sync
│       │   └── res/                            # Vector drawables, themes, and strings
│       └── test/java/com/google/antigravity/mobile/
│           ├── AntigravityScraperTest.kt       # Scraper & status extraction tests
│           └── ModelSerializationTest.kt       # JSON serialization tests
├── gradle/
│   ├── libs.versions.toml         # Gradle Version Catalog
│   └── wrapper/                   # Gradle 8.7 wrapper distribution
├── build.gradle.kts               # Root build script
├── gradle.properties              # JVM options & AndroidX properties
└── settings.gradle.kts            # Project repository definitions
```

---

## 🔌 Connecting Host Workstations to the Android App

To drive a physical desktop, Mac, or Linux development machine from this Android app:

### 1. Headless Background Daemon (Recommended)
Register an always-on OS background service on your host machine:
```bash
agy remote-control start --name "my-workstation"
```
Check status and instance name:
```bash
agy remote-control status
```
Open **Antigravity Android** -> **Remote Control** tab -> tap **"Drive Session"** next to your workstation.

### 2. Interactive Terminal Session
Enable reverse tunneling for an active terminal session:
```bash
agy --remote-control
# Or inside an existing prompt:
/remote-control on
```
The CLI prints a tunnel link:
```
Open https://antigravity.google.com/r/<instance-id>?p=c%2F<conversation-id>
```
Tap the link on your phone or paste `<instance-id>` into the app's **Direct Tunnel** modal to take over control.

---

## 🏗️ Building and Testing

### Prerequisites
- JDK 17
- Android SDK 35 (Platform 35, Build Tools 35.0.0)

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Build Debug APK
```bash
./gradlew assembleDebug
```
Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`

### Build Release Bundle (AAB)
```bash
./gradlew bundleRelease
```
