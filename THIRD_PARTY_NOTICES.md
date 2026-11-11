# Third-Party Notices

Seriee is licensed under the Apache License 2.0 (see [LICENSE](LICENSE)). It
depends on the following software and data sources.

## Runtime libraries

| Component | License |
|---|---|
| Kotlin standard library and coroutines | Apache-2.0 |
| AndroidX Core, AppCompat, Activity, Lifecycle | Apache-2.0 |
| Jetpack Compose (UI, Foundation, Material 3, extended icons) | Apache-2.0 |
| Coil (image loading) | Apache-2.0 |
| Android `org.json` | Apache-2.0 |

Test-only:

| Component | License |
|---|---|
| JUnit 4 | Eclipse Public License 1.0 |

Full license texts are available from each project and from the dependency
artifacts resolved by Gradle.

## Data sources and services

### TMDB

This product uses the TMDB API but is not endorsed or certified by TMDB.

TMDB's terms require the notice above and the TMDB logo in an About or Credits
section of the application. This repository shows the notice and links to TMDB
in the in-app About screen, but does **not** bundle the TMDB logo, because the
mark is a trademark and must be used unmodified from TMDB's approved assets:

- <https://www.themoviedb.org/about/logos-attribution>

Distributors of a public build must add an approved TMDB logo to the About
screen. Keep it less prominent than the Seriee name and do not imply
endorsement.

### JustWatch

Watch-provider availability data shown by the TMDB integration is provided by
JustWatch. The app displays "Watch-provider data is provided by JustWatch." in
the About screen.

### AniList

Anime metadata and cover images are provided by the AniList GraphQL API.
<https://anilist.co>

### TVmaze

TV metadata and episode schedules are provided by the TVmaze API.
<https://www.tvmaze.com/api>

### OMDb / IMDb

Optional movie and TV metadata is provided by OMDb, based on IMDb data.
<https://www.omdbapi.com>

## Images and metadata

Posters, titles, descriptions, and other metadata displayed in the app remain
the property of their respective rights holders. Seriee stores URLs or
user-provided files and displays this content for identification and
informational purposes only. The app does not host or distribute copyrighted
video content.

If you are a rights holder and believe content shown by the app infringes your
rights, open an issue at <https://github.com/eggBrainX/Seriee/issues> with
details, and the maintainer will respond promptly.
