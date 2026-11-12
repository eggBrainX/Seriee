# 🎬 Seriee

**A watch-progress tracker** for series, anime, and movies — a native Android app built with **Kotlin + Jetpack Compose + Material 3** (no WebView).

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Material%203-6750A4)
![Version](https://img.shields.io/badge/version-0.4.0-blue)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![CI](https://github.com/eggBrainX/Seriee/actions/workflows/ci.yml/badge.svg)](https://github.com/eggBrainX/Seriee/actions/workflows/ci.yml)

Seriee is free software under the [Apache License 2.0](LICENSE). It is local-first: no account, no server, no ads, no analytics.

---

## ✨ Features

**Progress** — episode cards with completion percentage across seasons; search and filters for **All / Watching / Completed / Plan to Watch / Coming Soon / On Hold / Dropped** with live counts; the **`+`** button advances an episode and marks the last one *Completed*; a daily streak with flame badge, 7-day strip, best record, and reset.

**Library** — title search and type filter (**Series / Movie**), adaptive grid or list view with 2:3 posters, pinned favorites first, star ratings, and tag/genre badges.

**Manage media** — add, edit, and delete titles (title, type, status, poster, season, progress, description, release date). Six statuses, duplicate guard, multi-season tracking, per-episode local video files, and posters from URL or device storage.

**Metadata** — one search auto-fills title, poster, description, genres, year, seasons, and dates from **AniList** and **TVmaze** (no key) plus optional **TMDB** and **OMDb** keys. Season numbers are detected from titles/queries; failed sources are skipped.

**Reminders** — release date/time with presets, countdown (`D-3`, "2 days 5 hours left"), exact-alarm notification with an inexact fallback, **Test Alarm Now**, and past reminders skipped.

**Watch** — streaming links (Netflix, Bilibili, YouTube presets) opened in-app or in a browser, and local episode files opened in VLC, MX Player, or any installed player. Seriee hosts, scrapes, and provides no copyrighted video.

**Backup & restore** — JSON export (format version 3) with merge or replace import; streak history is restored and reminders rescheduled. TMDB/OMDb keys are configured in the same sheet.

**Collection import** — bring an existing list from **AniList** (public username, no token) or a **MyAnimeList** `.xml`/`.xml.gz` export. Preview new/updated/unchanged counts, merge without losing local edits, or replace after confirmation. Entries match by source ID, then normalized title and type; IDs use `anilist-…` / `mal-…`.

**Themes & language** — Follow system, Dark, AMOLED, Frost Glass, Liquid Glass, Light, and Custom; a two-level menu with eight accent presets and solid/gradient backgrounds (live preview), plus System default, English, and Bahasa Indonesia. Theme, accent, background, view mode, language, and API keys persist on-device.

**Changelog & About** — a What's New dialog once per version (reopenable from the overflow menu), and an About screen with version, data-source credits, privacy policy, license, and repository links.

---

## 🛠️ Tech Stack

| Component | Version / Library |
|---|---|
| Language | Kotlin `2.0.20` |
| UI | Jetpack Compose + Material 3 (BOM `2024.09.00`) |
| Architecture | Single-Activity, `AndroidViewModel` + unidirectional state (`lifecycle-viewmodel-compose` `2.8.6`) |
| Images | [Coil](https://coil-kt.github.io/coil/) `2.7.0` |
| Storage | `SharedPreferences` (JSON values) |
| Networking | `HttpURLConnection` + `org.json` |
| AGP / Gradle / Java | `8.13.2` / wrapper `9.5.1` / `17` |
| SDK | min `26` (Android 8.0), target & compile `36` (Android 16) |
| Minification | R8 + resource shrinking (release) |

No DI framework, database, or navigation library: state lives in one `SerieeViewModel` and screens are plain Composables.

---

## ✅ Requirements

Android Studio (current stable) or a command-line SDK setup, JDK 17, Android SDK Platform 36 with build tools, and a device or emulator on Android 8.0 (API 26)+.

---

## 📲 Install

Signed builds are published on [GitHub Releases](https://github.com/eggBrainX/Seriee/releases/tag/stable-latest). Each release ships `Seriee-v<version>.apk`, `SHA256SUMS.txt`, and `signing-certificate.txt`:

```bash
sha256sum -c SHA256SUMS.txt
```

Debug APKs are contributor build artifacts only — they use the public AOSP debug key and are not an install channel.

> **Upgrading from 0.2.x:** 0.3.0 changed the application ID from `com.aistudio.seriee.twvpmz` to `com.seriee.app`, so Android treats it as a new app. Export a backup first, install, then **Backup & Restore → Merge**.

---

## 🔨 Build

Open the project in Android Studio and run the `app` configuration, or use the checked-in wrapper:

```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # minified release APK (signing: docs/RELEASING.md)
./gradlew lintDebug            # Android lint
./gradlew testDebugUnitTest    # unit tests
./gradlew installDebug         # build + install on a connected device
```

| Build | Path |
|---|---|
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

Release signing and publishing are maintainer tasks; see [docs/RELEASING.md](docs/RELEASING.md). The version has a single source of truth in [`app/build.gradle.kts`](app/build.gradle.kts):

```kotlin
versionCode = 12
versionName = "0.4.0"
```

Both values reach Kotlin through `BuildConfig` for the changelog and About UI — never hardcode them elsewhere.

---

## 🔎 Metadata Sources & API Keys

Configure keys via overflow menu → **Backup & Restore**. Keys stay on-device and are excluded from exports and Android system backups.

| Source | Key | Adds |
|---|---|---|
| **AniList** (GraphQL) | No | Anime titles, covers, genres, episode counts, air schedule |
| **TVmaze** | No | Western TV details and exact next-episode airtimes |
| **TMDB** | Yes — [themoviedb.org](https://www.themoviedb.org/settings/api) | Movie/TV metadata and streaming-provider links (region `ID` by default) |
| **OMDb** | Yes — free at [omdbapi.com](https://www.omdbapi.com/apikey.aspx) | IMDb-backed movies/series (1,000 requests/day free) |

Search tolerates partial failures; if every source fails, the app falls back to manual entry.

> **Privacy:** metadata queries send only the typed title to the selected providers; library data stays on-device. See [PRIVACY.md](PRIVACY.md). **Attribution:** TMDB requires the in-app notice, and watch-provider data must credit JustWatch — see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) before publishing a fork.

---

## 💾 Data, Storage & Backup

| Data | Location |
|---|---|
| Library, theme, accent/background, view mode, language, streak | `SharedPreferences` file `seriee` (JSON values) |
| TMDB / OMDb API keys | `SharedPreferences` file `seriee_secrets` (excluded from system backup) |
| Uploaded posters | `filesDir/posters/` (`posters/<id>_<timestamp>.<ext>`) |
| Remote posters | URLs loaded and cached by Coil |

Backups are JSON (version 3) containing items and streak history. Older formats — a bare array or objects without streak fields — are still accepted, and imports are validated defensively. Local poster files are **not embedded**: `posters/…` paths will not resolve on a new device, so re-upload covers or use URLs. API keys are deliberately never exported.

Uninstalling removes private storage; export a backup first. Android Auto Backup is enabled (`android:allowBackup="true"`) but should not replace an explicit export.

---

## 🏗️ Architecture

```
MainActivity
  └── SerieeTheme / SerieeApp (Compose UI)
        ├── ProgressScreen · LibraryScreen
        ├── MediaFormDialog · DetailDialog          (bottom sheets)
        ├── BackupRestoreDialog · ImportSheet       (bottom sheets)
        ├── ThemeMenu · ChangelogDialog · AboutDialog
              │ state + actions
              ▼
        SerieeViewModel (AndroidViewModel)
              ├── SerieeRepository    → SharedPreferences + JSON
              ├── MetadataRepository  → AniList / TVmaze / TMDB / OMDb
              ├── AniListListClient   → public collection import
              ├── MalXmlParser        → on-device MAL export import
              ├── PosterStore         → local poster files
              └── ReminderScheduler   → AlarmManager + notifications
```

- **Single source of truth:** `SerieeViewModel.items` is a `mutableStateOf` list; every mutation commits to memory and persists immediately through `SerieeRepository`.
- **Swipe tabs:** `SerieeApp` hosts a `HorizontalPager`; the nav pill interpolates `currentPage + currentPageOffsetFraction`, and filter chips use `FlowRow` so drags reach the pager.
- **Concurrency:** metadata search and collection import are the only suspending work and run on `Dispatchers.IO`; there are no coroutines in the UI layer.
- **Reminders:** stateless broadcasts — an `AlarmManager` alarm with a `PendingIntent` keyed by `item.id.hashCode()`, rebuilt on app start and after import.
- **Import mapping is pure Kotlin:** `ImportMapper` handles mapping, previews, merge, and replace without Android APIs, so the full matrix runs as JVM tests.
- **Movies vs series:** series count progress over seasons; a movie is one unit showing *Watched / Not watched*.
- **Localization:** strings live in `res/values/strings.xml` and `res/values-in/strings.xml`; switching uses `AppCompatDelegate.setApplicationLocales` (empty list = System default).

---

## 📁 Project Structure

```
Seriee/
├── .github/                      # CI, CodeQL, releases, issue/PR templates, Dependabot
├── app/
│   ├── build.gradle.kts          # config, dependencies, signing, R8
│   └── src/
│       ├── main/
│       │   ├── res/values/       # English strings + changelog entries
│       │   ├── res/values-in/    # Indonesian strings
│       │   └── java/com/seriee/app/
│       │       ├── MainActivity.kt · SerieeViewModel.kt
│       │       ├── data/         # models, repository, metadata, import, posters
│       │       ├── reminder/     # scheduler + receiver
│       │       └── ui/           # screens, sheets, theme menu, design system
│       └── test/                 # JVM unit tests
├── gradle/wrapper/               # checked-in wrapper
├── docs/RELEASING.md             # maintainer release and repository guide
├── CHANGELOG.md · CONTRIBUTING.md · CODE_OF_CONDUCT.md
├── SECURITY.md · PRIVACY.md · THIRD_PARTY_NOTICES.md
└── LICENSE · NOTICE · metadata.json
```

---

## 🛡️ Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Posters, metadata, AniList import, streaming links |
| `POST_NOTIFICATIONS` | Release reminders (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Exact reminder timing |
| `VIBRATE`, `WAKE_LOCK` | Vibration and wake-up for reminders |

`POST_NOTIFICATIONS` is requested on first launch on Android 13+. Exact alarms cannot be forced; without them `ReminderScheduler` falls back to `setAndAllowWhileIdle`, which may deliver slightly late. Seriee requests no location, contacts, camera, microphone, or broad storage access — files come through Android's system picker.

---

## 🔒 Security & Privacy

- [PRIVACY.md](PRIVACY.md) — what stays on-device, what a query sends, and how to delete your data.
- [SECURITY.md](SECURITY.md) — private vulnerability reporting and scope.
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) — libraries, data sources, and attribution.

Cleartext HTTP is disabled app-wide (`res/xml/network_security_config.xml`); API keys live in a preferences file excluded from cloud backup and device transfer. The only exported component under app control is the launcher activity — the reminder receiver is not exported and uses immutable `PendingIntent`s, while AndroidX's `ProfileInstallReceiver` is protected by the system-only `DUMP` permission. Release keystores are git-ignored and supplied to CI through encrypted secrets; release builds are minified with R8 and published with SHA-256 checksums and the signing certificate.

---

## ⚙️ CI / CD

Every push and pull request to `main` runs lint, unit tests, a debug build, and dependency review; CodeQL analyses Kotlin sources weekly. Releases from `v*` tags use a two-job workflow: an unprivileged build-and-test job, then a manually approved job that signs the APK and attaches `SHA256SUMS.txt` and `signing-certificate.txt`. Maintainer procedures are in [docs/RELEASING.md](docs/RELEASING.md).

---

## 🩺 Troubleshooting

- **Reminders don't fire** — allow notifications (Android 13+), allow *Alarms & reminders* (Android 12+), exempt the app from OEM battery savers, and make sure the date is in the future with **Reminder Alarm** on. Verify with **Test Alarm Now**.
- **Posters don't load** — remote posters need internet and an `https://` URL; cleartext `http://` is blocked. Locally uploaded files disappear if app data is cleared or the app is uninstalled.
- **Restore lost local posters** — the JSON backup stores paths, not image bytes. Re-upload covers or use URLs.
- **Metadata search finds nothing** — check the spelling; TMDB/OMDb need keys, AniList and TVmaze do not. Failed sources are skipped, so partial results are normal.
- **"SDK location not found"** — set `sdk.dir` in `local.properties` or open the project once in Android Studio; install SDK Platform 36.
- **`./gradlew` is not executable** — run `chmod +x gradlew`.
- **Unsigned release APK or R8 failure** — maintainers create `keystore.properties` per [docs/RELEASING.md](docs/RELEASING.md); never disable minification, add a targeted rule to `app/proguard-rules.pro` instead.

---

## 📝 Notes

- Remote posters load through Coil and need an internet connection.
- All data is local. Use **Backup & Restore** before switching phones or uninstalling.
- Collection import is explicit: AniList receives only the username you type at `graphql.anilist.co`, and a MyAnimeList export is parsed entirely on-device.
- Application ID and Kotlin namespace are both `com.seriee.app`; the pre-0.3.0 ID was `com.aistudio.seriee.twvpmz`, and both versions can coexist.

---

## 🤝 Contributing

Pull requests and suggestions are welcome — start with [CONTRIBUTING.md](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md); report security issues privately via [SECURITY.md](SECURITY.md).

- Keep strings localized in both `values/strings.xml` and `values-in/strings.xml`.
- Update the `changelog_*` entries and `CHANGELOG.md` for user-visible changes.
- Run `./gradlew lintDebug testDebugUnitTest assembleDebug` before opening a PR.
- Update `THIRD_PARTY_NOTICES.md` and the About screen when adding a source or library.
- Sign off commits with `git commit -s` (DCO).

---

## 🙏 Credits

- **TMDB** — this product uses the TMDB API but is not endorsed or certified by TMDB.
- **JustWatch** — watch-provider data.
- **AniList** — anime metadata, covers, and public collection import.
- **MyAnimeList** — the user-initiated XML export format used for import.
- **TVmaze** — TV metadata and episode schedules.
- **OMDb / IMDb** — additional movie and TV metadata.
- **Kotlin, AndroidX, Jetpack Compose, Coil** — Apache-2.0.

Posters and metadata remain the property of their rights holders and are shown for identification only. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the full list and attribution requirements.

---

## 📄 License

Copyright 2026 The Seriee contributors.

Licensed under the [Apache License, Version 2.0](LICENSE), including its patent grant and attribution requirements. See [NOTICE](NOTICE) for project and third-party attribution.

Seriee is an independent project and is not affiliated with Netflix, Bilibili, YouTube, TMDB, JustWatch, AniList, TVmaze, OMDb, or IMDb.
