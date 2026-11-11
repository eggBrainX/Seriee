# Changelog

All notable changes to this project are documented here. The in-app "What's New"
dialog is generated from the `changelog_*` strings in
`app/src/main/res/values/strings.xml`.

## [0.4.0] — 2026-11-12

### Added

- Seven themes: Follow system, Dark, AMOLED, Frost Glass, Liquid Glass, Light,
  and Custom (eight accent presets plus a solid or gradient background).
- Two-level theme menu with live previews, descriptions, radio semantics, and
  back-navigation through the page stack.
- **On Hold** and **Dropped** as real statuses, with colours, filters, and
  import mapping (`PAUSED` / `On-Hold` and `DROPPED`).
- Horizontal swipe between Progress and Library, with a floating navigation pill
  that follows the drag, haptic settle, and TalkBack next/previous actions.
- Collection import from a public **AniList** username or a **MyAnimeList**
  `.xml` / `.xml.gz` export, with a preview and a merge/replace choice.
- Non-destructive merge: progress and totals only grow, statuses only upgrade,
  and local-only fields survive. Replace requires explicit confirmation.
- Pure-JVM tests for import mapping, MAL parsing (including gzip), merge
  behaviour, and enum/status persistence.
- System default as a third language option.

### Changed

- Detail, add/edit, backup/restore, and import screens are now bottom sheets
  with drag handles, IME padding, and independently scrolling content.
- Redesigned header with the launcher mark, gradient wordmark, and search /
  schedule quick actions.
- Filled search pill, gradient progress bars, status-coloured badges, richer
  empty states, and a press-scale FAB that hides while scrolling.
- Filter chips wrap with `FlowRow`, so horizontal pager swipes are no longer
  swallowed by horizontal chip rows.
- Metadata and import errors, empty states, and theme descriptions are localised
  in English and Indonesian.

### Fixed

- Imported entries are matched by source ID first, then by normalized title and
  type, so re-importing the same list is idempotent.
- Movies imported from either source are normalised to one watched/unwatched
  unit instead of an episode count.

## [0.3.0] — 2026-11-11

### Added

- Apache-2.0 license, `NOTICE`, and contributor/security/privacy documentation.
- In-app **About** screen with version information, data-source credits, privacy
  summary, and links to the license and third-party notices.
- TMDB, JustWatch, AniList, TVmaze, and OMDb attribution.
- Unit tests for season parsing and episode filename parsing.
- CI workflows for lint, unit tests, dependency review, and CodeQL.
- Dependabot configuration and issue/PR templates.

### Changed

- Application ID is now `com.seriee.app` (previously the AI Studio default
  `com.aistudio.seriee.twvpmz`). **Android treats this as a new app** — export a
  JSON backup before installing.
- `compileSdk` / `targetSdk` 36 (Android 16); AGP 8.13.2.
- Release builds are minified and resource-shrunk with R8.
- Releases now publish a `SHA256SUMS.txt` asset and the signing certificate
  fingerprint.
- API keys moved to an isolated preferences file and excluded from Android
  backup and device transfer.
- Cleartext HTTP is blocked app-wide.

### Removed

- The public `debug-latest` APK channel. Debug builds are CI artifacts for
  contributors only; end users should install signed releases.

### Fixed

- `gradlew` is now executable in the repository.
- README build instructions match the Gradle wrapper and CI.

## [0.2.7] — 2026-11-06

- OMDb as an optional metadata source.
- README refresh, overflow-menu cleanup.

## [0.2.6] — 2026-11-05

- Daily streak tracking.
- TVmaze next-episode schedule fallback.

## [0.2.5] — 2026-11-04

- Library UI polish, movie labels, duplicate guard.

## [0.2.4] and earlier

- Initial progress tracking, multi-season support, backups, reminders, themes,
  localization, and metadata search (AniList, TVmaze, TMDB).
