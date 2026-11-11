# Releasing Seriee

**Audience:** repository maintainers and admins. End users only need
[README](../README.md) and [CONTRIBUTING](../CONTRIBUTING.md) covers day-to-day
development. Nothing here is needed to build or use the app.

---

## 🔐 Release signing

Debug builds work without any setup. Release builds are **unsigned** until a
signing config exists.

1. Generate a keystore (keep it and its passwords safe — losing it means you
   cannot update the app on Play Store, and a leaked one lets anyone publish a
   fake update):

```bash
keytool -genkeypair -v \
  -keystore seriee-release.keystore \
  -alias seriee \
  -keyalg RSA -keysize 2048 -validity 10000
```

2. Copy the template and fill in your values:

```bash
cp keystore.properties.example keystore.properties
```

```properties
storePassword=CHANGE_ME
keyPassword=CHANGE_ME
keyAlias=seriee
storeFile=seriee-release.keystore
```

`storeFile` is resolved relative to the **repository root**.

3. Build:

```bash
./gradlew assembleRelease
```

Both `keystore.properties` and `*.keystore` are git-ignored, as are base64
exports (`*.keystore.b64`). Back the keystore and its passwords up off-machine:
they exist only on the release machine and in the GitHub environment secrets
described below. Never commit them, paste them into an issue, or attach them to
a release.

---

## 🚀 Cutting a release

1. Bump `versionCode` / `versionName` in [`app/build.gradle.kts`](../app/build.gradle.kts)
   (the only source of truth).
2. Update the `changelog_*` strings in both `values/strings.xml` and
   `values-in/strings.xml`; release notes are generated from them.
3. Update [CHANGELOG.md](../CHANGELOG.md).
4. Open a PR and wait for CI to pass on `main`.
5. Tag and push:

```bash
git tag -a v0.3.0 -m "Seriee 0.3.0"
git push origin v0.3.0
```

`build-apk.yml` builds the unsigned APK, waits for approval on the `release`
environment, signs it, and publishes the release with `Seriee-v<version>.apk`,
`SHA256SUMS.txt` and `signing-certificate.txt`.

> **The `release` environment must exist before the first tag.** A job that
> references a nonexistent environment creates it *without* protection,
> silently removing the approval gate. Run the hardening script first.

---

## ⚙️ CI / CD

| Workflow | Trigger | Result |
|---|---|---|
| [`ci.yml`](../.github/workflows/ci.yml) | Push/PR to `main` | Lint, unit tests, debug build, dependency review on PRs |
| [`codeql.yml`](../.github/workflows/codeql.yml) | Push/PR to `main`, weekly | CodeQL analysis for Kotlin |
| [`build-apk.yml`](../.github/workflows/build-apk.yml) | Push tag `v*`, manual | Signed release, checksums, certificate, GitHub Release, `stable-latest` |
| [`dependabot.yml`](../.github/dependabot.yml) | Weekly | Grouped dependency and Actions update PRs |

`build-apk.yml` is split into two jobs so that the keystore never touches a job
that compiles repository code:

| Job | Secrets | What it does |
|---|---|---|
| `build` | none | Lint, unit tests, unsigned release build, changelog-derived release notes |
| `publish` | signing keys | Signs with `apksigner`, verifies, computes checksums, publishes the release |

`publish` runs in the **`release` environment**, which requires your approval
and only accepts deployments from `v*` tags. Signing secrets therefore cannot
be reached from a branch push, even by a collaborator with write access.

`build-apk.yml` publishes:

| Artifact | Published |
|---|---|
| `Seriee-v<version>.apk` | Yes |
| `SHA256SUMS.txt` | Yes |
| `signing-certificate.txt` | Yes |
| Debug APK | No — workflow artifact only |

### Signing secrets

Signing keys are **environment** secrets, not repository secrets:

| Secret | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | `base64 -w0 seriee-release.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g. `seriee`) |
| `ANDROID_KEY_PASSWORD` | Key password |

```bash
base64 -w0 seriee-release.keystore > seriee-release.keystore.b64
gh secret set ANDROID_KEYSTORE_BASE64 --env release < seriee-release.keystore.b64
gh secret set ANDROID_KEYSTORE_PASSWORD --env release
gh secret set ANDROID_KEY_ALIAS --env release
gh secret set ANDROID_KEY_PASSWORD --env release
rm seriee-release.keystore.b64
```

The signing job restores the keystore, signs and verifies the APK, and fails if
a secret is missing. Passwords reach `apksigner` through the environment, never
the command line.

GitHub Actions are pinned to commit SHAs and run with least privilege
(`contents: read` by default; `contents: write` only in the signing job).

---

## 🔒 Repository hardening (admin only)

Run once, as an admin, after creating the repository. These settings require
the **Admin** role; collaborators with Write cannot apply them:

```bash
./scripts/harden-repo.sh          # DRY_RUN=1 to preview
```

It applies:

- Actions limited to GitHub-owned and verified Marketplace actions.
- A read-only default `GITHUB_TOKEN` (workflows opt in to more).
- `main` protection: pull request + CI check required, no force-push or deletion.
- `v*` tags protected from update and deletion.
- The `release` environment with you as required reviewer and only `v*` tags
  allowed to deploy.

One setting is UI-only: **Settings → Actions → General → Fork pull request
workflows → "Require approval for all outside collaborators"**.

> Collaborators with **Write** can edit workflow files. They cannot open
> Settings or read secrets, but they *can* run a modified workflow on a branch —
> which is exactly why signing keys live in the tag-restricted environment
> instead of repository secrets.

---

## 🩺 Troubleshooting

**Release APK is unsigned or R8 fails**
- Create `keystore.properties` as described above.
- Do not disable minification; add a targeted rule to
  [`app/proguard-rules.pro`](../app/proguard-rules.pro) instead.

**Publish job cannot find the keystore**
- Confirm the secrets are set on the `release` **environment**, not on the
  repository, and that `release` is still restricted to `v*` tags.
