# 🎬 Seriee

**A watch-progress tracker** for series, anime, and movies. A native Android app built with **Kotlin + Jetpack Compose + Material 3** (no WebView).

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Material%203-6750A4)
![Version](https://img.shields.io/badge/version-0.2.7-blue)

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Requirements](#-requirements)
- [Install](#-install)
- [Build](#-build)
- [Release Signing](#-release-signing)
- [Metadata Sources & API Keys](#-metadata-sources--api-keys)
- [Data, Storage & Backup](#-data-storage--backup)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Permissions](#-permissions)
- [CI / CD](#-ci--cd)
- [Troubleshooting](#-troubleshooting)
- [Notes](#-notes)
- [Contributing](#-contributing)

---

## ✨ Features

### 📈 Progress Tab
- Episode progress cards with completion percentage (computed across all seasons).
- Search plus filters: **All / Watching / Completed / Coming Soon**.
- The **`+`** button advances one episode and automatically sets the status to *Completed* on the last one.
- **Daily streak**: watch something every day to build a streak, with a flame badge, a 7-day activity strip, and a best-streak record. The streak can be reset from the same card.

### 📚 Library Tab
- Title search and type filter (**Series / Movie**).
- **Adaptive grid** or **list** view, with real 2:3 poster proportions.
- **Pin** your favorites; pinned items are shown first.
- **Star rating** (0–5) and **tag/genre** badges on cards.

### 📝 Manage Media
- Add, edit, and delete media: title, type, status, poster, season, current episode, total episodes, description, and release date.
- Available statuses: `Watching`, `Completed`, `Plan to Watch`, `Coming Soon`.
- Duplicate guard: saving the same title, type, and season twice is blocked.
- **Multi-season tracking**: add/remove seasons, track progress per season, and attach local video files per episode.
- **Poster sources**: remote image URL or upload from device storage (copied into the app's private files).

### 🔎 Fetch Metadata
- Search once to auto-fill title, poster, description, genres, year, season counts, and release/air dates.
- Aggregates several sources in one query:
  - **AniList** — anime, no key required.
  - **TVMaze** — Western TV, no key required; provides precise next-episode airtimes.
  - **TMDB** — optional API key; adds official streaming-provider links.
  - **OMDb** — optional API key; IMDb-backed movies and series.
- Season numbers are detected from titles/queries (e.g. *Season 4*, *S4*, *Part 2*).

### 🔔 Release Reminders
- Set a release date and time (presets: Tomorrow, 3 Days, 1 Week, 1 Month).
- Countdown to release (e.g. `D-3`, "2 days 5 hours left") on cards and in the detail view.
- Notification when the release time arrives (exact alarm; falls back to an inexact alarm when exact alarms are not permitted).
- A **Test Alarm Now** button to verify that reminders work.
- Reminders that are already in the past are skipped.

### ▶️ Watch
- **Streaming link** with presets for Netflix, Bilibili, and YouTube, opened in the app or browser.
- **Offline video**: pick a file from storage, assign it to a specific episode, then open it in **VLC**, **MX Player**, or any other installed player.

### 💾 Backup & Restore
- Export all your data to a **JSON** file (format version 3).
- Import a backup file in two modes:
  - **Merge**: adds items from the file without removing existing ones (recommended).
  - **Replace**: overwrites all data with the contents of the file.
- Item data and streak history are restored; active reminders are rescheduled automatically after a restore.
- The same dialog is where optional **TMDB** and **OMDb** API keys are configured.

### 🎨 Themes & Language
- Four themes: **Dark**, **AMOLED**, **Frost Glass**, and **Light**.
- Two UI languages: **English** and **Bahasa Indonesia** (in-app switcher, applied immediately).
- Theme, view mode, language, and API keys are saved on the device.

### 🆕 Changelog
- A **What's New** dialog appears once per version upgrade, and can be reopened from the overflow menu.

---

## 🛠️ Tech Stack

| Component | Version / Library |
|---|---|
| Language | Kotlin `2.0.20` |
| UI | Jetpack Compose + Material 3 (Compose BOM `2024.09.00`) |
| Architecture | Single-Activity, `AndroidViewModel` + unidirectional state (`lifecycle-viewmodel-compose` `2.8.6`) |
| Images | [Coil](https://coil-kt.github.io/coil/) `2.7.0` |
| Storage | `SharedPreferences` (data serialized to JSON, stored locally) |
| Networking | `HttpURLConnection` + `org.json` (no Retrofit/OkHttp) |
| Android Gradle Plugin | `8.5.2` |
| Gradle | Wrapper `9.8.2` (CI pins `8.9`) |
| Java | `17` |
| Min SDK / Target & Compile SDK | `26` (Android 8.0) / `34` |

There is no DI framework, database, or navigation library — state lives in one `SerieeViewModel` and screens are plain Composables.

---

## ✅ Requirements

- **Android Studio Koala or newer** (or a command-line Android SDK setup).
- **JDK 17**.
- Android SDK Platform **34** (API 34) and Build Tools.
- A device or emulator running **Android 8.0 (API 26)** or newer.

---

## 📲 Install

The project publishes two rolling channels on GitHub Releases:

| Channel | Source | Notes |
|---|---|---|
| **Stable** | Signed release builds from `v*` tags | [Latest stable release](https://github.com/eggBrainX/Seriee/releases/tag/stable-latest) |
| **Debug** | Every push to `main` | [Latest debug build](https://github.com/eggBrainX/Seriee/releases/tag/debug-latest) |

Download the `.apk` asset and open it on the device.

> Enable *Install from unknown sources* if Android asks for it.

---

## 🔨 Build

### Android Studio
1. Open the project folder in **Android Studio (Koala or newer)**.
2. Wait for the Gradle sync to finish.
3. Select the `app` run configuration and press **Run**.

The Gradle wrapper is checked in, so no extra setup is needed. To regenerate it:
```bash
gradle wrapper --gradle-version 8.9
```

### Terminal
```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # release APK (requires signing config)
```

Outputs:

| Build | Path |
|---|---|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

Useful variants:
```bash
./gradlew installDebug         # build + install on a connected device
./gradlew clean                # clear build outputs
```

### Versioning
App version is a single source of truth in [`app/build.gradle.kts`](app/build.gradle.kts):

```kotlin
versionCode = 10
versionName = "0.2.7"
```

Both values are exposed to Kotlin through `BuildConfig` (for the changelog and About UI). Never hardcode version numbers elsewhere.

---

## 🔐 Release Signing

Debug builds work without any setup. Release builds are **unsigned** until a signing config exists.

1. Generate a keystore (keep it and its passwords safe — losing it means you cannot update the app on Play Store):

   ```bash
   keytool -genkeypair -v \
     -keystore seriee-release.keystore \
     -alias seriee \
     -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Copy the template and fill in your values:

   ```bash
   cp keystore.properties.example keystore.properties
   ```

   ```properties
   storePassword=CHANGE_ME
   keyPassword=CHANGE_ME
   keyAlias=seriee
   storeFile=app/seriee-release.keystore
   ```

   `storeFile` is resolved relative to the **repository root**, so place the keystore at `app/seriee-release.keystore` (or adjust the path).

3. Build:

   ```bash
   ./gradlew assembleRelease
   ```

Both `keystore.properties` and `*.keystore` are git-ignored. For CI, see [CI / CD](#-ci--cd).

---

## 🔎 Metadata Sources & API Keys

Open the overflow menu → **Backup & Restore** to configure keys. Keys are stored locally on the device and are **not** included in backup exports.

| Source | Key required | What it adds |
|---|---|---|
| **AniList** (GraphQL) | No | Anime titles, covers, genres, episode counts, air schedule |
| **TVMaze** | No | Western TV details and exact next-episode airtimes |
| **TMDB** | Yes — [themoviedb.org](https://www.themoviedb.org/settings/api) | Movie/TV metadata and streaming-provider links (region `ID` by default) |
| **OMDb** | Yes — free at [omdbapi.com](https://www.omdbapi.com/apikey.aspx) | IMDb-backed movies/series (1,000 requests/day on the free tier) |

Search tolerates partial failures: if one source fails or has no result, the remaining sources still return data. If every source fails, the app falls back to manual entry.

> **Privacy:** metadata queries send the typed title to the selected providers. Nothing else is uploaded; all library data stays on-device.

---

## 💾 Data, Storage & Backup

### On-device storage
| Data | Location |
|---|---|
| Library, theme, view mode, language, API keys, streak | `SharedPreferences` file `seriee` (JSON values) |
| Locally uploaded posters | `filesDir/posters/` (stored as `posters/<id>_<timestamp>.<ext>`) |
| Remote posters | Plain URLs, loaded by Coil and cached by the image library |

### Backup format (version 3)
```json
{
  "version": 3,
  "appName": "Seriee",
  "timestamp": 1730000000000,
  "streakDays": 4,
  "bestStreak": 12,
  "lastWatchDay": "2026-11-11",
  "watchDays": ["2026-11-05", "2026-11-09", "2026-11-10", "2026-11-11"],
  "items": [
    {
      "id": "…",
      "title": "Arcane",
      "type": "Series",
      "status": "Watching",
      "poster": "https://…",
      "seasons": [
        { "number": 1, "currentEp": 5, "totalEp": 9, "episodes": [] }
      ]
    }
  ]
}
```

Notes:
- Older backups (a bare JSON array, or objects without streak fields) are still accepted.
- Imports are validated defensively; unknown/missing fields fall back to defaults.
- Local poster files are **not embedded** in the backup. A backup from one device keeps the `posters/…` paths, which will not resolve after moving to a new phone — re-upload those covers or switch to image URLs before switching devices.

### Reinstalling
Uninstalling the app removes its private storage. Export a backup first; if you rely on Android Auto Backup, it is enabled via `android:allowBackup="true"` but should not replace an explicit export.

---

## 🏗️ Architecture

```
MainActivity
  └── SerieeTheme / SerieeApp (Compose UI)
        ├── ProgressScreen
        ├── LibraryScreen
        ├── MediaFormDialog / DetailDialog
        ├── BackupRestoreDialog
        └── ChangelogDialog
              │  state + actions
              ▼
        SerieeViewModel (AndroidViewModel)
              ├── SerieeRepository      → SharedPreferences + JSON
              ├── MetadataRepository    → AniList / TVMaze / TMDB / OMDb
              ├── PosterStore           → local poster files
              └── ReminderScheduler     → AlarmManager + notifications
```

- **Single source of truth:** `SerieeViewModel.items` is a Compose `mutableStateOf` list. Every mutation commits to memory *and* persists immediately through `SerieeRepository`, so the UI recomposes from one place.
- **No coroutines in the UI layer:** metadata search is the only suspending work and runs on `Dispatchers.IO`.
- **Reminders are stateless broadcasts:** `ReminderScheduler` schedules an `AlarmManager` alarm with a `PendingIntent` keyed by `item.id.hashCode()`; `ReminderReceiver` builds the notification. Alarms are re-scheduled on app start and after import.
- **Movies vs series:** series count progress over `Seasons`; a movie is treated as a single unit and displays *Watched / Not watched*.
- **Localization:** user-visible strings live in `res/values/strings.xml` and `res/values-in/strings.xml`; language switching uses `AppCompatDelegate.setApplicationLocales`.

---

## 📁 Project Structure

```
Seriee/
├── .github/workflows/build-apk.yml   # CI: debug on main, signed release on tags
├── app/
│   ├── build.gradle.kts              # app config, dependencies, signing
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/strings.xml        # English strings + changelog entries
│       │   ├── values-in/strings.xml     # Indonesian strings
│       │   ├── values/themes.xml, colors.xml
│       │   └── xml/locales_config.xml
│       └── java/com/seriee/app/
│           ├── MainActivity.kt            # entry point, permission + locale setup
│           ├── SerieeViewModel.kt         # UI state and all library actions
│           ├── data/
│           │   ├── Models.kt              # MediaItem, Season, EpisodeFile, enums
│           │   ├── SerieeRepository.kt    # SharedPreferences + JSON backup/restore
│           │   ├── MetadataRepository.kt  # AniList, TVMaze, TMDB, OMDb clients
│           │   └── PosterStore.kt         # local poster copy/delete helpers
│           ├── reminder/
│           │   ├── ReminderScheduler.kt   # alarms, notifications, countdown text
│           │   └── ReminderReceiver.kt
│           └── ui/
│               ├── SerieeApp.kt           # scaffold, tabs, top bar, overflow menu
│               ├── ProgressScreen.kt
│               ├── LibraryScreen.kt
│               ├── MediaFormDialog.kt
│               ├── DetailDialog.kt
│               ├── BackupRestoreDialog.kt
│               ├── ChangelogDialog.kt
│               ├── Components.kt
│               ├── Theme.kt
│               └── Actions.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/                   # checked-in Gradle wrapper
├── keystore.properties.example       # release signing template
└── metadata.json
```

---

## 🛡️ Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Load posters and open streaming links |
| `POST_NOTIFICATIONS` | Show release reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Schedule reminders at the exact time |
| `VIBRATE`, `WAKE_LOCK` | Vibrate and wake the device for reminders |

`POST_NOTIFICATIONS` is requested on first launch on Android 13+. Exact alarms cannot be forced: when the system denies them, `ReminderScheduler` degrades to `setAndAllowWhileIdle`, which may deliver the reminder slightly late.

---

## ⚙️ CI / CD

[`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) runs on:

| Trigger | Result |
|---|---|
| Push to `main` | Builds the debug APK, uploads an artifact, and updates the rolling `debug-latest` prerelease |
| Push tag `v*` | Builds a signed release APK, creates a GitHub Release, and moves `stable-latest` to it |
| Manual (`workflow_dispatch`) | Runs the debug pipeline |

Release builds require these repository secrets:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 seriee-release.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g. `seriee`) |
| `ANDROID_KEY_PASSWORD` | Key password |

The workflow restores the keystore, writes `keystore.properties`, and fails fast on tag builds if a secret is missing. Debug release notes are generated from the `changelog_*` strings in `values/strings.xml`, so update those strings when you bump the version.

---

## 🩺 Troubleshooting

**Reminders don't fire**
- Check that notifications are allowed for Seriee (Android 13+).
- On Android 12+, allow *Alarms & reminders* in system settings for exact timing.
- Some OEM battery savers kill alarms; exempt the app from battery optimization.
- The release date must be in the future and **Reminder Alarm** must be on. Use **Test Alarm Now** to verify the channel works.

**Posters don't load**
- Remote posters need an internet connection and a valid `http(s)` URL.
- If the poster was uploaded locally, the file may have been cleared by uninstalling/clearing app data.

**A restore didn't bring back local posters**
- The JSON backup stores `posters/…` paths, not image bytes. Re-upload covers on the new device or use URLs.

**Metadata search finds nothing**
- Verify the title spelling. TMDB/OMDb need keys; AniList and TVMaze work without one.
- A failed source is skipped, so partial results are normal.

**Build fails with "SDK location not found"**
- Set `sdk.dir` in `local.properties` or open the project in Android Studio once so it generates the file.

**Release APK is unsigned**
- Create `keystore.properties` as described in [Release Signing](#-release-signing).

---

## 📝 Notes

- Posters are loaded from URLs via Coil, so they need an internet connection.
- All data is stored **locally on your device**. Use **Backup & Restore** before switching phones or uninstalling the app.
- API keys are stored in plain `SharedPreferences` and are not exported in backups.
- The **debug** channel is unsigned-for-release but installable; the **stable** channel contains signed release builds.
- The application ID is `com.aistudio.seriee.twvpmz`; the Kotlin namespace is `com.seriee.app`.

---

## 🤝 Contributing

Pull requests and suggestions are welcome. Feel free to open an issue to report a bug or propose a feature.

When contributing:
- Keep strings localized in both `values/strings.xml` and `values-in/strings.xml`.
- Update the `changelog_*` entries when adding user-visible changes.
- Run `./gradlew assembleDebug` before opening a PR.
