# Contributing to Seriee

Thanks for helping. This document covers the practical steps; by participating
you agree to the [Code of Conduct](CODE_OF_CONDUCT.md).

## Before you start

- Search existing issues and pull requests first.
- For large features or refactors, open an issue to agree on the approach before
  writing code.
- Never include API keys, keystores, `local.properties`, or personal data.

## Requirements

- JDK 17
- Android SDK Platform 36 and Build Tools 36
- Android Studio or a command-line SDK

## Build and test

The Gradle wrapper is checked in, so use `./gradlew` (not a system Gradle):

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # Android lint
./gradlew assembleRelease        # minified release build (needs signing config)
```

Release builds use R8. Do not disable minification to work around a crash — add a
targeted rule to `app/proguard-rules.pro` and explain why.

## Code style

- Kotlin, Jetpack Compose, and Material 3. Keep the existing single-ViewModel,
  no-DI structure unless an issue explicitly proposes a change.
- Prefer small, focused composables and pure functions that can be unit-tested.
- Unsaved or experimental code must not be committed.
- Comments explain *why*, not *what*.

## Localization

Every user-visible string must exist in both:

- `app/src/main/res/values/strings.xml` (English)
- `app/src/main/res/values-in/strings.xml` (Indonesian)

Do not hardcode UI text.

## Changelog and versioning

- Version numbers live only in `app/build.gradle.kts`.
- Add user-visible changes to the `changelog_*` strings in both languages.
- Update `CHANGELOG.md` for releases.

## Attribution and third-party data

If you add a new metadata provider, image source, or library, update:

- `THIRD_PARTY_NOTICES.md`
- the About screen (`AboutDialog.kt` and `about_*` strings)
- `PRIVACY.md` if data leaves the device

Never remove TMDB or JustWatch attribution.

## Submitting a pull request

1. Branch from `main`.
2. Keep commits focused; conventional prefixes (`feat:`, `fix:`, `docs:`, `ci:`)
   are appreciated.
3. Run `./gradlew lintDebug testDebugUnitTest assembleDebug` before pushing.
4. Fill in the pull request template, including how you tested on a device.
5. Sign off your commits with `git commit -s` to certify the Developer
   Certificate of Origin (DCO).

## Licensing of contributions

The project is licensed under the **Apache License 2.0**. By submitting a
contribution you agree that it is your original work (or that you have the right
to submit it) and that it may be distributed under the same license. Do not
paste code from projects with incompatible licenses.

If you used an AI assistant to write a substantial part of a change, say so in
the pull request. You are still responsible for reviewing and testing it.
