# Changelog

All notable changes to this project are documented here. The in-app "What's New"
dialog is generated from the `changelog_*` strings in
`app/src/main/res/values/strings.xml`.

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
