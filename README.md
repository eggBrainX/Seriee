# Seriee

**A local-first watch-progress tracker** for series, anime, and movies — built natively for Android with **Kotlin + Jetpack Compose + Material 3**.

Seriee is free software under the [Apache License 2.0](LICENSE). It is local-first: no account, no server, no ads, and no analytics.

---

## Features

- **Progress**: Episode cards with completion percentage across seasons; search and filters for **All / Watching / Completed / Plan to Watch / Coming Soon / On Hold / Dropped** with live counts; quick `+1` episode advancement; daily streak tracker with 7-day strip, best record, and reset.
- **Library**: Title search and type filter (**Series / Movie**), Grid or List view with 2:3 posters, pinned favorites first, star ratings, and tag badges.
- **Insights**: Episodes watched, estimated watch time (24 min/episode), completion rate, series/movie counts, average rating, stacked status bar, rating bars, top genres, 12-week activity heatmap, and recent watch timeline with Clear History confirmation.
- **Manage Media**: Add, edit, and delete titles with duplicate title guard, multi-season editor, per-episode local video file matching (`S01E01`, `E01`), external video player launch, and URL or local gallery poster images.
- **Metadata Auto-Fetch**: Search **AniList** and **TVmaze** (no key required) plus optional **TMDB** and **OMDb** API keys.
- **Reminders**: Release date/time with calendar/time picker, exact/idle alarm scheduling (`AlarmManager`), countdown badges (`D-3`, `5H LEFT`), and instant notification testing.
- **Backup & Restore**: JSON export/import (version 4) with Merge or Replace options; older formats are accepted defensively. API keys are stored separately and never exported.
- **Collection Import**: Import anime lists from **AniList** (public username) or **MyAnimeList** (`.xml` / `.xml.gz` export parsed on-device) with preview and non-destructive merge or replace.
- **Themes & Localization**: Two-axis theme system — Appearance (**Follow system / Light / Dark**) and Style (**Neo-Brutalism, Classic, AMOLED, Frost Glass, Liquid Glass, Material You, Custom**), plus custom accent swatches, artwork color sampling, gradient presets, copy/paste `seriee-theme:1;...` codes, Reduce Transparency, Battery Saver AMOLED, and English / Bahasa Indonesia / System language switching.

---

## Build

```bash
./gradlew assembleDebug   # outputs app/build/outputs/apk/debug/app-debug.apk
./gradlew lint            # runs Android Lint checks
```

---

## Tech Stack

- **Language & UI**: Kotlin + Jetpack Compose (Material 3)
- **Images**: Coil (`coil-compose`)
- **Storage**: On-device `SharedPreferences` + Storage Access Framework JSON backup/restore
- **Alarms & Notifications**: Android `AlarmManager` + `NotificationManager`

---

## License & Notices

Licensed under the [Apache License 2.0](LICENSE). See [NOTICE](NOTICE), [PRIVACY.md](PRIVACY.md), and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
