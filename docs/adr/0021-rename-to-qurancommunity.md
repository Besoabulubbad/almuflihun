# 0021 — The codebase, application id and repository are renamed to `qurancommunity`

- **Status:** accepted
- **Date:** 2026-11-28
- **Deciders:** Mohammad Abu Lubad (owner)
- **Tags:** identity
- **Supersedes:** the identity part of [0009](0009-app-identity.md) and the "unchanged" list of [0019](0019-app-name.md)

## Context

ADR 0019 changed only the visible name to «مجتمع القرآن» and kept the codename `almuflihun`. The owner confirmed the name is final and asked for one name everywhere. Nothing has been released and the Firebase project does not exist yet, so this is the cheapest moment: after the first install, an application id can never change without breaking in-place updates.

## Decision

The Latin name is **`qurancommunity`**:

- Android application id and namespace, and iOS bundle id: **`app.qurancommunity`**.
- Kotlin packages `app.qurancommunity.*`; convention plugins `qurancommunity.*`; the detekt rule set `qurancommunity`; the Gradle root project `qurancommunity`.
- GitHub repository: **`Besoabulubbad/quran-community`** (GitHub redirects the old URL).
- The Firebase project (Phase 7) is created as `quran-community`.

The application id is **permanent from the first install** (ADR 0009's rule still holds for the new id). Accepted ADRs before this one keep the old name as history.

## Consequences

- Good: one name for users, code, store and repository.
- Neutral: the signing key is unaffected (it is not tied to the id). Any debug build installed under `app.almuflihun` is a separate app; uninstall it.
