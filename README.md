# 🎬 Seriee

**A watch-progress tracker** for series, anime, and movies. A native Android app built with **Kotlin + Jetpack Compose + Material 3** (no WebView).

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Material%203-6750A4)
![Version](https://img.shields.io/badge/version-0.4.0-blue)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![CI](https://github.com/eggBrainX/Seriee/actions/workflows/ci.yml/badge.svg)](https://github.com/eggBrainX/Seriee/actions/workflows/ci.yml)

Seriee is free software, licensed under the [Apache License 2.0](LICENSE). It is
local-first: there is no account, no server, no ads, and no analytics.

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Requirements](#-requirements)
- [Install](#-install)
- [Build](#-build)
- [Metadata Sources & API Keys](#-metadata-sources--api-keys)
- [Data, Storage & Backup](#-data-storage--backup)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Permissions](#-permissions)
- [Security & Privacy](#-security--privacy)
- [CI / CD](#-ci--cd)
- [Troubleshooting](#-troubleshooting)
- [Notes](#-notes)
- [Contributing](#-contributing)
- [Credits](#-credits)
- [License](#-license)

---

## ✨ Features

### 📈 Progress Tab
- Episode progress cards with completion percentage (computed across all seasons).
- Search plus filters: **All / Watching / Completed / Plan to Watch / Coming Soon / On Hold / Dropped**, with live counts.
- Swipe left or right to switch between **Progress** and **Library**; the floating navigation pill follows the drag.
- The **`+`** button advances one episode and automatically sets the status to *Completed* on the last one.
- **Daily streak**: watch something every day to build a streak, with a flame badge, a 7-day activity strip, and a best-streak record. The streak can be reset from the same card.

### 📚 Library Tab
- Title search and type filter (**Series / Movie**), plus the same seven status filters.
- **Adaptive grid** or **list** view, with real 2:3 poster proportions.
- **Pin** your favorites; pinned items are shown first.
- **Star rating** (0–5) and **tag/genre** badges on cards.

### 📝 Manage Media
- Add, edit, and delete media: title, type, status, poster, season, current episode, total episodes, description, and release date.
- Available statuses: `Watching`, `Completed`, `Plan to Watch`, `Coming Soon`, `On Hold`, `Dropped`.
- Duplicate guard: saving the same title, type, and season twice is blocked.
- **Multi-season tracking**: add/remove seasons, track progress per season, and attach local video files per episode.
- **Poster sources**: remote image URL or upload from device storage (copied into the app's private files).

### 🔎 Fetch Metadata
- Search once to auto-fill title, poster, description, genres, year, season counts, and release/air dates.
- Aggregates several sources in one query:
  - **AniList** — anime, no key required.
  - **TVmaze** — Western TV, no key required; provides precise next-episode airtimes.
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
- Seriee does not host, scrape, or provide copyrighted video. Links point to services you already use; local files are played by a third-party player you choose.

### 💾 Backup & Restore
- Export all your data to a **JSON** file (format version 3).
- Import a backup file in two modes:
  - **Merge**: adds items from the file without removing existing ones (recommended).
  - **Replace**: overwrites all data with the contents of the file.
- Item data and streak history are restored; active reminders are rescheduled automatically after a restore.
- The same sheet is where optional **TMDB** and **OMDb** API keys are configured.

### 📥 Collection Import
- Import an existing anime list so you do not have to retype your library.
- **AniList**: enter a public username (no token or OAuth needed).
- **MyAnimeList**: choose an `.xml` or `.xml.gz` export from *Profile → Export*.
- Preview new, updated, and unchanged counts before writing anything.
- **Merge** adds new titles, only raises progress and totals, and only upgrades statuses, so local edits survive. **Replace** asks for confirmation first.
- Entries are matched by source ID, then by normalized title and type. Imported IDs use the `anilist-…` / `mal-…` prefixes.

### 🎨 Themes & Language
- Seven themes: **Follow system**, **Dark**, **AMOLED**, **Frost Glass**, **Liquid Glass**, **Light**, and **Custom**.
- The theme menu is two levels deep: pick a theme, or open **Customize Theme…** for eight accent presets and a solid/gradient background with a live preview.
- Three language options: **System default**, **English**, and **Bahasa Indonesia** (applied immediately).
- Theme, custom accent/background, view mode, language, and API keys are saved on the device.

### 🆕 Changelog & About
- A **What's New** dialog appears once per version upgrade and can be reopened from the overflow menu.
- An **About** screen lists the version, data-source credits (TMDB, JustWatch, AniList, TVmaze, OMDb), the privacy policy, the license, and the source repository.

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
| Android Gradle Plugin | `8.13.2` |
| Gradle | Wrapper `9.5.1` (used locally and in CI) |
| Java | `17` |
| Min SDK / Target & Compile SDK | `26` (Android 8.0) / `36` (Android 16) |
| Minification | R8 + resource shrinking in release builds |

There is no DI framework, database, or navigation library — state lives in one `SerieeViewModel` and screens are plain Composables.

---

## ✅ Requirements

- **Android Studio** (current stable) or a command-line Android SDK setup.
- **JDK 17**.
- Android SDK Platform **36** (API 36) and Build Tools.
- A device or emulator running **Android 8.0 (API 26)** or newer.

---

## 📲 Install

Signed releases are published on [GitHub Releases](https://github.com/eggBrainX/Seriee/releases/tag/stable-latest).

| Channel | Source | Notes |
|---|---|---|
| **Stable** | Signed release builds from `v*` tags | [Latest stable release](https://github.com/eggBrainX/Seriee/releases/tag/stable-latest) |

Each release includes:

| Asset | Purpose |
|---|---|
| `Seriee-v<version>.apk` | The installable app |
| `SHA256SUMS.txt` | Checksum to verify the download |
| `signing-certificate.txt` | Certificate fingerprint used to sign the APK |

Verify the APK before installing:

```bash
sha256sum -c SHA256SUMS.txt
```

Debug APKs are **not** distributed to users. They are build artifacts for
contributors only (debug builds are debuggable and use the public AOSP debug
key, so they are not safe to recommend as an install channel).

> **Upgrading from 0.2.x:** version 0.3.0 changes the application ID from
> `com.aistudio.seriee.twvpmz` to `com.seriee.app`. Android will treat it as a
> new app. Export a backup from the old version first, install 0.3.0, then use
> **Backup & Restore → Merge** to import it.

---

## 🔨 Build

### Android Studio
1. Open the project folder in **Android Studio**.
2. Wait for the Gradle sync to finish.
3. Select the `app` run configuration and press **Run**.

The Gradle wrapper is checked in, so no extra setup is needed.

### Terminal
```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # minified release APK (signing: docs/RELEASING.md)
./gradlew lintDebug            # Android lint
./gradlew testDebugUnitTest    # unit tests
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

Release signing, publishing and repository settings are maintainer tasks; see
[docs/RELEASING.md](docs/RELEASING.md).

### Versioning
App version is a single source of truth in [`app/build.gradle.kts`](app/build.gradle.kts):

```kotlin
versionCode = 12
versionName = "0.4.0"
```

Both values are exposed to Kotlin through `BuildConfig` (for the changelog and About UI). Never hardcode version numbers elsewhere.

---

## 🔎 Metadata Sources & API Keys

Open the overflow menu → **Backup & Restore** to configure keys. Keys are stored locally on the device and are **not** included in backup exports or Android system backups.

| Source | Key required | What it adds |
|---|---|---|
| **AniList** (GraphQL) | No | Anime titles, covers, genres, episode counts, air schedule |
| **TVmaze** | No | Western TV details and exact next-episode airtimes |
| **TMDB** | Yes — [themoviedb.org](https://www.themoviedb.org/settings/api) | Movie/TV metadata and streaming-provider links (region `ID` by default) |
| **OMDb** | Yes — free at [omdbapi.com](https://www.omdbapi.com/apikey.aspx) | IMDb-backed movies/series (1,000 requests/day on the free tier) |

Search tolerates partial failures: if one source fails or has no result, the remaining sources still return data. If every source fails, the app falls back to manual entry.

> **Privacy:** metadata queries send the typed title to the selected providers. Nothing else is uploaded; all library data stays on-device. See [PRIVACY.md](PRIVACY.md) for details.

> **Attribution:** TMDB requires the notice shown in the in-app About screen, and
> watch-provider data must credit JustWatch. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)
> before publishing a fork.

---

## 💾 Data, Storage & Backup

### On-device storage
| Data | Location |
|---|---|
| Library, theme, custom accent/background, view mode, language, streak | `SharedPreferences` file `seriee` (JSON values) |
| TMDB / OMDb API keys | `SharedPreferences` file `seriee_secrets` (excluded from system backup) |
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
- API keys are deliberately excluded from exports and from Android Auto Backup / device transfer.

### Reinstalling
Uninstalling the app removes its private storage. Export a backup first; Android Auto Backup is enabled (`android:allowBackup="true"`) but should not replace an explicit export.

---

## 🏗️ Architecture

```
MainActivity
  └── SerieeTheme / SerieeApp (Compose UI)
        ├── ProgressScreen
        ├── LibraryScreen
        ├── MediaFormDialog / DetailDialog      (bottom sheets)
        ├── BackupRestoreDialog / ImportSheet   (bottom sheets)
        ├── ThemeMenu (themes, customize, language)
        ├── ChangelogDialog / AboutDialog
              │  state + actions
              ▼
        SerieeViewModel (AndroidViewModel)
              ├── SerieeRepository      → SharedPreferences + JSON
              ├── MetadataRepository    → AniList / TVmaze / TMDB / OMDb
              ├── AniListListClient     → public collection import
              ├── MalXmlParser          → on-device MAL export import
              ├── PosterStore           → local poster files
              └── ReminderScheduler     → AlarmManager + notifications
```

- **Single source of truth:** `SerieeViewModel.items` is a Compose `mutableStateOf` list. Every mutation commits to memory *and* persists immediately through `SerieeRepository`, so the UI recomposes from one place.
- **Swipe tabs:** `SerieeApp` hosts a `HorizontalPager`; the custom navigation pill interpolates `currentPage + currentPageOffsetFraction`, and filter chips use `FlowRow` so drags reach the pager.
- **No coroutines in the UI layer:** metadata search and collection import are the only suspending work and run on `Dispatchers.IO`.
- **Reminders are stateless broadcasts:** `ReminderScheduler` schedules an `AlarmManager` alarm with a `PendingIntent` keyed by `item.id.hashCode()`; `ReminderReceiver` builds the notification. Alarms are re-scheduled on app start and after import.
- **Movies vs series:** series count progress over `Seasons`; a movie is treated as a single unit and displays *Watched / Not watched*.
- **Import mapping is pure Kotlin:** `ImportMapper` handles mapping, previews, merge, and replace without Android APIs, so the full matrix is covered by JVM tests.
- **Localization:** user-visible strings live in `res/values/strings.xml` and `res/values-in/strings.xml`; language switching uses `AppCompatDelegate.setApplicationLocales`, with an empty locale list for *System default*.

---

## 📁 Project Structure

```
Seriee/
├── .github/
│   ├── workflows/ci.yml              # lint + tests + debug build, dependency review
│   ├── workflows/codeql.yml           # CodeQL analysis
│   ├── workflows/build-apk.yml        # signed releases from tags
│   ├── ISSUE_TEMPLATE/                # bug/feature forms
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── dependabot.yml
├── app/
│   ├── build.gradle.kts               # app config, dependencies, signing, R8
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/
│       │   │   ├── values/strings.xml        # English strings + changelog entries
│       │   │   ├── values-in/strings.xml     # Indonesian strings
│       │   │   ├── values/themes.xml, colors.xml
│       │   │   └── xml/                      # locales, backup, network security
│       │   └── java/com/seriee/app/
│       │       ├── MainActivity.kt            # entry point, permission + locale setup
│       │       ├── SerieeViewModel.kt         # UI state and all library actions
│       │       ├── data/
│       │       │   ├── Models.kt              # MediaItem, Season, EpisodeFile, enums
│       │       │   ├── SerieeRepository.kt    # SharedPreferences + JSON backup/restore
│       │       │   ├── MetadataRepository.kt  # AniList, TVmaze, TMDB, OMDb clients
│       │       │   ├── ImportModels.kt        # import state, DTOs, errors
│       │       │   ├── ImportMapper.kt        # mapping, preview, merge, replace
│       │       │   ├── AniListListClient.kt   # paginated public AniList list
│       │       │   ├── MalXmlParser.kt        # gzip sniff + MAL XML scanner
│       │       │   └── PosterStore.kt         # local poster copy/delete helpers
│       │       ├── reminder/
│       │       │   ├── ReminderScheduler.kt   # alarms, notifications, countdown text
│       │       │   └── ReminderReceiver.kt
│       │       └── ui/
│       │           ├── SerieeApp.kt           # scaffold, pager, top bar, overflow menu
│       │           ├── ProgressScreen.kt
│       │           ├── LibraryScreen.kt
│       │           ├── MediaFormDialog.kt
│       │           ├── DetailDialog.kt
│       │           ├── BackupRestoreDialog.kt
│       │           ├── ImportSheet.kt         # AniList / MAL import UX
│       │           ├── ThemeMenu.kt           # themes, customize, language pages
│       │           ├── DesignSystem.kt        # tokens and shared components
│       │           ├── ChangelogDialog.kt
│       │           ├── AboutDialog.kt         # version, credits, license links
│       │           ├── Components.kt
│       │           ├── Theme.kt
│       │           └── Actions.kt
│       └── test/java/com/seriee/app/          # JVM unit tests
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/                   # checked-in Gradle wrapper
├── keystore.properties.example       # release signing template
├── docs/RELEASING.md                 # maintainer release and repository guide
├── LICENSE, NOTICE
├── CHANGELOG.md
├── CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md, PRIVACY.md
├── THIRD_PARTY_NOTICES.md
└── metadata.json
```

---

## 🛡️ Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Load posters, fetch metadata, import an AniList collection, and open streaming links |
| `POST_NOTIFICATIONS` | Show release reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Schedule reminders at the exact time |
| `VIBRATE`, `WAKE_LOCK` | Vibrate and wake the device for reminders |

`POST_NOTIFICATIONS` is requested on first launch on Android 13+. Exact alarms cannot be forced: when the system denies them, `ReminderScheduler` degrades to `setAndAllowWhileIdle`, which may deliver the reminder slightly late.

Seriee does not request location, contacts, camera, microphone, or broad storage access. Files are selected through Android's system picker.

---

## 🔒 Security & Privacy

- [PRIVACY.md](PRIVACY.md) — what stays on the device, what a metadata query sends, and how to remove your data.
- [SECURITY.md](SECURITY.md) — how to report a vulnerability privately and what is in scope.
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) — libraries, data sources, and attribution.

Technical safeguards:

- Cleartext HTTP is disabled app-wide (`res/xml/network_security_config.xml`).
- API keys live in a separate preferences file excluded from cloud backup and device transfer.
- The only exported component under app control is the launcher activity. The reminder receiver is not exported and uses immutable `PendingIntent`s; the extra exported receiver (`androidx.profileinstaller.ProfileInstallReceiver`) ships with AndroidX and is protected by the system-only `DUMP` permission.
- Release keystores are git-ignored and supplied to CI through encrypted secrets.
- Release builds are minified with R8 and published with SHA-256 checksums and the signing certificate.

---

## ⚙️ CI / CD

Every push and pull request to `main` runs Android lint, the unit tests, a debug
build, and dependency review; CodeQL analyses the Kotlin sources on a weekly
schedule. Releases are built from `v*` tags by a two-job workflow: an
unprivileged job compiles and tests, and a separate, manually approved job signs
the APK and attaches `SHA256SUMS.txt` and `signing-certificate.txt`.

Maintainer procedures — release signing, CI secrets, the release checklist and
repository protection settings — are documented in
[docs/RELEASING.md](docs/RELEASING.md).

---

## 🩺 Troubleshooting

**Reminders don't fire**
- Check that notifications are allowed for Seriee (Android 13+).
- On Android 12+, allow *Alarms & reminders* in system settings for exact timing.
- Some OEM battery savers kill alarms; exempt the app from battery optimization.
- The release date must be in the future and **Reminder Alarm** must be on. Use **Test Alarm Now** to verify the channel works.

**Posters don't load**
- Remote posters need an internet connection and an `https://` URL. Cleartext `http://` URLs are blocked.
- If the poster was uploaded locally, the file may have been cleared by uninstalling/clearing app data.

**A restore didn't bring back local posters**
- The JSON backup stores `posters/…` paths, not image bytes. Re-upload covers on the new device or use URLs.

**Metadata search finds nothing**
- Verify the title spelling. TMDB/OMDb need keys; AniList and TVmaze work without one.
- A failed source is skipped, so partial results are normal.

**Build fails with "SDK location not found"**
- Set `sdk.dir` in `local.properties` or open the project in Android Studio once so it generates the file.
- Install Android SDK Platform 36.

**`./gradlew` is not executable**
- Run `chmod +x gradlew` (the file is executable in git; some archives drop the bit).

**Release APK is unsigned or R8 fails**
- Maintainers: create `keystore.properties` as described in
  [docs/RELEASING.md](docs/RELEASING.md).
- Do not disable minification; add a targeted rule to `app/proguard-rules.pro` instead.

---

## 📝 Notes

- Posters are loaded from URLs via Coil, so they need an internet connection.
- All data is stored **locally on your device**. Use **Backup & Restore** before switching phones or uninstalling the app.
- API keys are stored on-device and are not exported or auto-backed up.
- Collection import is explicit: AniList sends only the username you type to `graphql.anilist.co`, and a MyAnimeList export is parsed entirely on-device.
- The application ID is `com.seriee.app`; the Kotlin namespace is also `com.seriee.app`.
- Version 0.3.0 changed the application ID from `com.aistudio.seriee.twvpmz`. The old and new versions can coexist; export/import a backup to move your library.

---

## 🤝 Contributing

Pull requests and suggestions are welcome. Start with [CONTRIBUTING.md](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md). Report security issues privately via [SECURITY.md](SECURITY.md).

When contributing:
- Keep strings localized in both `values/strings.xml` and `values-in/strings.xml`.
- Update the `changelog_*` entries and `CHANGELOG.md` when adding user-visible changes.
- Run `./gradlew lintDebug testDebugUnitTest assembleDebug` before opening a PR.
- Update `THIRD_PARTY_NOTICES.md` and the About screen when adding a data source or library.
- Sign off commits with `git commit -s` (DCO).

---

## 🙏 Credits

Seriee relies on community data and open-source software:

- **TMDB** — this product uses the TMDB API but is not endorsed or certified by TMDB.
- **JustWatch** — watch-provider data is provided by JustWatch.
- **AniList** — anime metadata, cover images, and public collection import.
- **MyAnimeList** — the user-initiated XML export format used for collection import.
- **TVmaze** — TV metadata and episode schedules.
- **OMDb / IMDb** — additional movie and TV metadata.
- **Kotlin, AndroidX, Jetpack Compose, Coil** — Apache-2.0.

Posters and metadata remain the property of their respective rights holders and
are displayed for identification purposes only. See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the full list and
attribution requirements.

---

## 📄 License

Copyright 2026 The Seriee contributors.

Licensed under the [Apache License, Version 2.0](LICENSE). You may use, modify,
and distribute this software under the terms of that license, including its
patent grant and attribution requirements. See [NOTICE](NOTICE) for the
project and third-party attribution notices.

Seriee is an independent project and is not affiliated with Netflix, Bilibili,
YouTube, TMDB, JustWatch, AniList, TVmaze, OMDb, or IMDb.
