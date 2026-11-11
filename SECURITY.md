# Security Policy

## Supported versions

| Version | Supported |
|---|---|
| 0.3.x (current) | Yes |
| 0.2.x and older | No — please upgrade |

## Reporting a vulnerability

**Do not open a public issue for security problems.**

Use GitHub's private **Report a vulnerability** flow on the repository's
[Security tab](https://github.com/eggBrainX/Seriee/security/advisories/new).
If that is unavailable, contact the maintainer through their GitHub profile and
ask for a private channel.

Please include:

- A description of the issue and its impact.
- Steps to reproduce (device/Android version, app version, and any relevant
  configuration).
- Whether the issue affects release builds, debug builds, or both.
- Any suggested fix, if you have one.

You can expect an acknowledgement within **7 days** and a status update within
**30 days**. Please allow time for a fix and a release before public disclosure.
Credit will be given in the release notes unless you prefer to remain anonymous.

## In scope

- APK distribution and release signing.
- Intent handling, exported components, and deep links.
- Local data storage, backup/restore handling, and API-key handling.
- Network communication with metadata providers.
- Dependency or build-pipeline compromise.

## Out of scope

- Bugs in third-party players, streaming services, or metadata providers.
- Issues that require a rooted device, a malicious accessibility service, or
  physical access to an unlocked device.
- Social engineering of the maintainer.
- Reports generated solely by automated scanners without a demonstrated impact.

## Security practices in this repository

- No signing keys or API keys are committed; keystores are git-ignored.
- Release APKs are signed in CI and published with SHA-256 checksums. Signing
  keys live in the `release` environment, which requires maintainer approval and
  only accepts `v*` tags; the job that compiles repository code never sees them.
- `main` requires a pull request and the CI check, and `v*` tags cannot be
  updated or deleted (`scripts/harden-repo.sh`).
- Debug APKs are build artifacts for contributors only and are no longer
  published to end users.
- Cleartext HTTP is disabled app-wide.
- API keys are stored in a separate preferences file excluded from Android
  backup and device transfer.
- GitHub Actions are pinned to commit SHAs and run with least privilege.
- Dependabot and dependency review keep dependencies current.
