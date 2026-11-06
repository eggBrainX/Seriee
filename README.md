# Seriee (Native Android)

Native Android rewrite of the Seriee Media & Progress Tracker.
**Kotlin + Jetpack Compose + Material 3**, no WebView. Min Android 8.0 (API 26).

## Features
- **Progress** tab: episode progress cards, filter (Semua / Watching / Completed), `+` button adds 1 episode and auto-sets *Completed* on the last one
- **Library** tab: search, type filter (Series / Movie), grid (3 columns, square) or list view, pin items (pinned shown first)
- Add / edit / delete media (title, type, status, poster URL, season, current & total episode)
- Streaming link with presets (Netflix, Bilibili, YouTube) - opens in the app/browser
- Offline video: pick a file from storage, then open it in VLC, MX Player, or any player
- 4 themes: Dark, AMOLED, Frost Glass, Light
- Data, theme and view mode are saved on-device (SharedPreferences)

## Get the APK without Android Studio
1. Push this repo to GitHub (branch `main`).
2. Open **Actions → Build Android APK** (auto-runs on push, or *Run workflow*).
3. Download **seriee-debug-apk** from the run's Artifacts and install `app-debug.apk`.

## Build with Android Studio
Open the folder in Android Studio (Koala or newer), let Gradle sync, press Run.
Optional: create the Gradle wrapper once with `gradle wrapper --gradle-version 8.9`.

## Build from terminal
```bash
gradle assembleDebug          # or ./gradlew assembleDebug after creating the wrapper
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Structure
```
app/src/main/java/com/seriee/app/
  MainActivity.kt, SerieeViewModel.kt
  data/   Models.kt, SerieeRepository.kt
  ui/     SerieeApp.kt, ProgressScreen.kt, LibraryScreen.kt,
          MediaFormDialog.kt, DetailDialog.kt, Components.kt, Theme.kt, Actions.kt
```

## Notes
- Posters are loaded from URLs with Coil, so they need internet.
- For a Play Store release you need a signed release build (keystore); the workflow builds a debug APK.
