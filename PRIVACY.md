# Privacy Policy

**Last updated: 12 November 2026**

Seriee is a local-first Android app. There is no Seriee account, server, ads, or
analytics, and the project does not receive your data.

## What is stored on your device

- Your library (titles, progress, ratings, tags, descriptions, poster URLs or
  local poster files, streaming links, and episode file references).
- App settings (theme, custom accent and background, view mode, language) and
  streak history.
- Optional TMDB and OMDb API keys that you enter yourself.

This data is stored in the app's private storage using `SharedPreferences` and
local files. It is not uploaded to the project.

## Network requests

When you use **Fetch metadata**, the title you type (and nothing else) is sent to
the providers you have enabled:

| Provider | When | Data sent |
|---|---|---|
| AniList | Always | Search title |
| TVmaze | Always | Search title |
| TMDB (optional key) | When a key is configured | Search title, your key, region code |
| OMDb (optional key) | When a key is configured | Search title, IMDb ID, your key |

The selected provider returns metadata and poster URLs. Poster images are then
loaded from the provider's image CDN. Requests use HTTPS; cleartext HTTP is
blocked by the app. Each provider has its own privacy policy and terms.

### Collection import

- **AniList import** sends the username you type to `graphql.anilist.co` and
  reads the public anime list in pages. No OAuth token is requested, and the
  username is not stored after the import.
- **MyAnimeList import** reads the `.xml` or `.xml.gz` file you choose directly
  on your device. The file and its contents are never uploaded.

No other network requests are made. Opening a streaming link or an external
video player hands the URL to the app you choose.

## Permissions

| Permission | Why |
|---|---|
| `INTERNET` | Load posters, fetch metadata, and import an AniList collection |
| `POST_NOTIFICATIONS` | Show release reminders (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Deliver reminders on time |
| `VIBRATE`, `WAKE_LOCK` | Vibrate and wake for reminders |

The app does not request location, contacts, camera, microphone, or broad
storage access. Files you pick are accessed through Android's system picker.

## Backups

- **Exported JSON backups** are created only when you ask for them and stay
  wherever you save them. They do not include API keys.
- **Android Auto Backup / device transfer** may include your library and
  settings, but the API-key preferences file is explicitly excluded. You can
  disable system backup in Android settings.

## Children

The app is not directed at children and collects no personal data.

## Removing your data

Uninstalling the app removes its private storage. Export a backup first if you
want to keep it. Local poster files are also removed.

## Changes

Material changes to this policy will be noted in `CHANGELOG.md` and in the
release notes.

## Contact

Open an issue at <https://github.com/eggBrainX/Seriee/issues> or use the
repository's Security tab for private reports.
