# 🎬 Seriee

**A watch-progress tracker** for series, anime, and movies. A native Android app built with **Kotlin + Jetpack Compose + Material 3** (no WebView).

![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Material 3](https://img.shields.io/badge/UI-Material%203-6750A4)


---

## ✨ Features

### 📈 Progress Tab
- Episode progress cards with completion percentage.
- Filters: **All / Watching / Completed**.
- The **`+`** button adds one episode and automatically sets the status to *Completed* on the last one.

### 📚 Library Tab
- Title search and type filter (**Series / Movie**).
- **Grid** (3 columns) or **list** view.
- **Pin** your favorites; pinned items are shown first.
- **Star rating** and **tag/genre** badges on cards.

### 📝 Manage Media
- Add, edit, and delete media: title, type, status, poster URL, season, current episode, and total episodes.
- Available statuses: `Watching`, `Completed`, `Plan to Watch`, `Coming Soon`.
- Rating, tags, and release date.

### 🔔 Release Reminders
- Set a release date and time (presets: Tomorrow, 3 Days, 1 Week, 1 Month).
- Countdown to release (e.g. `D-3`, "2 days 5 hours left") on cards and in the detail view.
- Notification when the release time arrives (exact alarm, works even when the device is idle).
- A test button to check that reminders work.

### ▶️ Watch
- **Streaming link** with presets for Netflix, Bilibili, and YouTube, opened in the app or browser.
- **Offline video**: pick a file from storage, then open it in **VLC**, **MX Player**, or any other player.

### 💾 Backup & Restore
- Export all your data to a **JSON** file.
- Import a backup file in two modes:
  - **Merge**: adds items from the file without removing existing ones (recommended).
  - **Replace**: overwrites all data with the contents of the file.
- Active reminders are rescheduled automatically after a restore.

### 🎨 Themes
Four themes: **Dark**, **AMOLED**, **Frost Glass**, and **Light**. Your theme and view mode (grid/list) are saved on the device.

---

## 🛠️ Tech Stack

| Component | Version / Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 (Compose BOM `2024.09.00`) |
| Architecture | ViewModel (`lifecycle-viewmodel-compose`) |
| Images | [Coil](https://coil-kt.github.io/coil/) `2.7.0` |
| Storage | `SharedPreferences` (data stored as JSON, locally on the device) |
| Min SDK / Target SDK | 26 (Android 8.0) / 34 |
| Java / Gradle | 17 / 8.9 |

---

## 📲 install the APK

> Enable *Install from unknown sources* if Android asks for it.

---

## 🔨 Build Manually

### Android Studio
Open the project folder in **Android Studio (Koala or newer)**, wait for Gradle sync to finish, then press **Run**.

Optionally, create the Gradle wrapper once:
```bash
gradle wrapper --gradle-version 8.9
```

### Terminal
```bash
gradle assembleDebug        # or ./gradlew assembleDebug if the wrapper exists
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Project Structure

```
Seriee/
├── .github/workflows/build.yml      # CI: builds the debug APK
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       └── java/com/seriee/app/
│           ├── MainActivity.kt
│           ├── SerieeViewModel.kt
│           ├── data/
│           │   ├── Models.kt              # MediaItem, MediaType, WatchStatus, AppTheme
│           │   └── SerieeRepository.kt    # local storage + JSON backup/restore
│           ├── reminder/
│           │   ├── ReminderScheduler.kt   # alarms, notifications, countdown
│           │   └── ReminderReceiver.kt
│           └── ui/
│               ├── SerieeApp.kt
│               ├── ProgressScreen.kt
│               ├── LibraryScreen.kt
│               ├── MediaFormDialog.kt
│               ├── DetailDialog.kt
│               ├── BackupRestoreDialog.kt
│               ├── Components.kt
│               ├── Theme.kt
│               └── Actions.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔐 Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | Load posters and open streaming links |
| `POST_NOTIFICATIONS` | Show release reminder notifications |
| `SCHEDULE_EXACT_ALARM` | Schedule reminders at the exact time |
| `VIBRATE`, `WAKE_LOCK` | Vibrate and wake the device for reminders |

---

## 📝 Notes

- Posters are loaded from URLs via Coil, so they need an internet connection.
- All data is stored **locally on your device**. Use **Backup & Restore** before switching phones or uninstalling the app.
- The GitHub Actions workflow produces a **debug APK**. A Play Store release requires a signed release build (keystore).

---

## 🤝 Contributing

Pull requests and suggestions are welcome. Feel free to open an issue to report a bug or propose a feature.
