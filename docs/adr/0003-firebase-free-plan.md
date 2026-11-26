# 0003 — Firebase on the free Spark plan, with no server code

- **Status:** superseded by [0005](0005-firebase-blaze-push-notifications.md)
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** backend, privacy

## Context and problem statement

Live and Community need storage, sign-in and moderation (`design/DECISIONS.md` #1, #15–19). The owner chose Firebase with Google sign-in through the GitLive Kotlin Multiplatform SDK. The project is non-profit with few users at first, so it should run on the free Spark plan and move to the paid Blaze plan only when limits are reached. Two designed features normally use server code, which Spark does not allow: setting the `moderator` custom claim and deleting an account with all its posts.

## Decision drivers

- No cost and no card on file while usage is small.
- The repository is public, so the Firestore security rules are the only protection.
- Nothing except Community and Live talks to the network; no usage data at all.

## Options considered

1. **Spark, no Cloud Functions:** moderator claims set by a maintainer with a local Admin SDK script; account deletion done by the client (delete own posts and replies in batches, then the Auth user after a fresh sign-in); security rules enforce everything.
2. **Blaze with Cloud Functions:** claims and cascading deletion on the server.

## Decision outcome

**Chosen: Option 1.** Revisit with a superseding ADR when Spark limits are reached.

- Sign-in: Google only, required only for Community.
- Security rules: users write only their own posts, replies and reports; Live entries and moderation require the `moderator` claim; every post references a valid verse key (rules embed the ayah count of all 114 surahs).
- Never added: Analytics, Crashlytics, Performance, or any SDK that sends usage data.
- The Firebase config file may be committed; its API key is restricted to the app's package name and signing certificate fingerprint.

### Consequences

- Good: zero running cost; no server code to maintain.
- Good: the rules file is reviewable in the repo and tested with the Firebase emulator.
- Bad: client-side cascading deletion can be interrupted. Mitigated: deletion is retried at next launch until the account is gone, and the rules let a user delete only their own documents.
- Bad: granting moderators needs a maintainer with the service-account key on their machine (never committed).
- Neutral: Google sign-in in the signed APK requires the release certificate fingerprint registered in Firebase.

### Confirmation

- `firestore.rules` has emulator tests in CI.
- A Konsist or Gradle check fails the build if a Firebase Analytics, Crashlytics or Performance artifact appears in the dependency graph.

## Links

- `design/DECISIONS.md` #1, #15–19
- [Firebase pricing](https://firebase.google.com/pricing), [GitLive firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk)
