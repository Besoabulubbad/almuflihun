# 0019 — The app is called «مجتمع القرآن»

- **Status:** accepted
- **Date:** 2026-11-28
- **Deciders:** Mohammad Abu Lubad (owner)
- **Tags:** identity, brand
- **Supersedes:** the display-name part of [0009](0009-app-identity.md) (the package and repository names stand)
- **Later change:** the package, application id and repository were renamed too, in [0021](0021-rename-to-qurancommunity.md)

## Decision

The name people see is **«مجتمع القرآن»** (design v7 #37), with the mark **ق** (v7 #65: app icon, splash, notification icon, stores and «عن التطبيق» only; no sacred text or places in any icon).

- Changed now: the Android label (`app_name`), the iOS product name, and the Home skeleton's title.
- **Unchanged:** the package and application id `app.almuflihun`, the Kotlin packages, the repository `almuflihun`, the signing identity and the self-update channel (ADR 0009, ADR 0010). An installed APK keeps updating in place.
- Earlier ADRs keep the old name as history.

## Consequences

- Good: no migration for users or code; only the visible name changes.
- Neutral: the repository and the code keep the codename `almuflihun`.
