# Security policy

المفلحون handles accounts, moderation and a public community, so security reports matter.

## Reporting a vulnerability

Please **do not open a public issue**. Use GitHub's private vulnerability reporting: the **Security** tab of this repository → **Report a vulnerability**. You will get an answer within 7 days.

Useful to include: what is affected (app, Firestore rules, Cloud Functions, release pipeline), steps to reproduce, and the impact you expect.

## Scope

- The Android and iOS app code in this repository.
- `firestore.rules` and the Cloud Functions.
- The release and self-update pipeline (signing, `app/latest`, APK verification).

## Supported versions

Only the latest release receives fixes. The app updates itself (ADR 0010), and a version can be marked unsupported when a fix requires it.
