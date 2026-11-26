# 0010 — The directly distributed APK updates itself

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** release, android, distribution

## Context and problem statement

The first release is a signed APK handed out directly, not through a store (ADR 0002). Without a store, users only get fixes if the app tells them and makes installing easy. The owner wants the Telegram-style experience: a banner says an update exists, the app downloads it, and an «تحديث» button installs it.

## Decision drivers

- Updating takes as few steps as Android allows.
- Only an APK signed with our key can ever be installed as an update.
- Store distribution later must not break (Google Play forbids self-updating apps).
- Old versions must not silently break when the backend changes.

## Options considered (for required updates)

1. **Optional updates, plus an emergency minimum version.**
2. **Every update required.**
3. **Never required.**

## Decision outcome

**Chosen: Option 1** (owner), with this flow:

1. **Publish.** The release workflow builds the signed APK, attaches it to a GitHub Release, and writes `app/latest` in Firestore: `versionCode`, `versionName`, `apkUrl` (GitHub Release asset), `sha256`, `notes`, `minSupported`. Writing it triggers the update push (ADR 0005).
2. **Notice.** The app reads `app/latest` at launch and on the update push, and shows «تحديث متوفر» when `versionCode` is newer.
3. **Download** in the background into app-private storage, with progress; resumable.
4. **Verify** the file's SHA-256 against `app/latest`, and that the APK's signing certificate equals the running app's (`PackageManager.getPackageArchiveInfo` with signing certificates). Any mismatch deletes the file and reports a non-fatal error.
5. **Install** through Android's `PackageInstaller` session API. The first time, Android asks the user to allow «تثبيت تطبيقات غير معروفة» for المفلحون (`REQUEST_INSTALL_PACKAGES`); the app explains this before sending the user to that system screen. Each update shows Android's own confirmation. On Android 12+, once the app is the installer of record, `USER_ACTION_NOT_REQUIRED` may allow later updates without a prompt; this is verified on real devices before it is relied on.
6. **Required updates.** If the installed `versionCode` is below `minSupported`, a full screen «يلزم تحديث التطبيق» offers only the update. `minSupported` is raised only when old versions would break (e.g. Firestore rules or Cloud Function contracts changed). Offline, the app uses the last known value and never blocks on a failed check.

**Two Android build flavours** from the same code:

- `direct`: self-update as above; the APK handed out and attached to GitHub Releases.
- `play`: no self-update code and no `REQUEST_INSTALL_PACKAGES`; uses Play's in-app updates API. Built when the app goes to Google Play.

### Consequences

- Good: users get fixes without a store, in two taps at most after the first time.
- Good: signature and hash checks mean a tampered or foreign APK is never installed.
- Bad: the release signing key becomes critical. Losing it means no further updates for existing installs; leaking it lets others ship "updates". Mitigated by keeping it only in GitHub Actions secrets plus an offline backup held by the owner (release guide).
- Bad: `REQUEST_INSTALL_PACKAGES` makes the `direct` APK look more powerful; the explanation screen says why.
- Neutral: the `play` flavour keeps the store path open.

### Confirmation

- Unit tests: version comparison, `minSupported` gating, hash and certificate mismatch handling.
- A device test on Android 10, 12 and 14+ installs an update from a local test release.
- A dependency check asserts the `play` flavour contains no self-update code and no `REQUEST_INSTALL_PACKAGES`.

## Links

- [PackageInstaller](https://developer.android.com/reference/android/content/pm/PackageInstaller), [Play policy on self-updating apps](https://support.google.com/googleplay/android-developer/answer/9888379)
- ADR [0002](0002-first-apk-scope.md), [0005](0005-firebase-blaze-push-notifications.md)
