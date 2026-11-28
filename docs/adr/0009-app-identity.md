# 0009 — App identity: `almuflihun`, application ID `app.almuflihun`

- **Status:** accepted
- **Later change:** Display name superseded by [0019](0019-app-name.md) («مجتمع القرآن»); package and repository unchanged
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** identity, release

## Context and problem statement

The app's name was spelled three ways in Latin letters: `AlMoflihon` (local folder), `almuflihun` (design handoff), `Almoflihon` (Quran Foundation app). The Android application ID can never change after people install the app, and it should not contain a person's name because the repository will move to an organisation.

## Options considered

1. `almuflihun`, ID `app.almuflihun`.
2. `almoflihon`, ID `app.almoflihon`.
3. Another spelling under `org.`, e.g. `org.almuflihun.app`.

## Decision outcome

**Chosen: Option 1** (owner).

- Display name on devices: **المفلحون**.
- Latin name: `almuflihun` (standard transliteration of al-mufliḥūn), used for the GitHub repository, the iOS bundle ID `app.almuflihun`, Gradle `rootProject.name`, and the Firebase project.
- Android `applicationId` and Kotlin package root: `app.almuflihun`. Module packages are `app.almuflihun.<layer>.<module>`.
- The local folder name does not matter and stays as it is.

### Consequences

- Good: one spelling everywhere, matching the design.
- Good: no personal name in any permanent identifier.
- Neutral: owning the `almuflihun.app` domain is optional; worth registering for a future website.

## Links

- ADR [0002](0002-first-apk-scope.md) (first APK)
