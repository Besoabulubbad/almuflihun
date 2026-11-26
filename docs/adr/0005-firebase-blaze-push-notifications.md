# 0005 — Firebase on Blaze with Cloud Functions for push notifications

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** backend, notifications, privacy
- **Supersedes:** [0003](0003-firebase-free-plan.md)
- **Partly superseded by:** [0008](0008-analytics-and-crash-reporting.md) (the "never add Analytics or Crashlytics" clause)

## Context and problem statement

ADR 0003 kept Firebase on the free Spark plan with no server code. The owner then asked for push notifications that only a server can send: «مباشر الآن» when a stream goes live, a reply to your Community post, a new app version, and any announcement a moderator writes, all started from the in-app Admin screen. Sending through Firebase Cloud Messaging needs the project's service-account credentials, which can never ship inside the app (anyone can extract an APK). So the sending code must run on a server.

## Decision drivers

- Push is started from the Admin screen, with no manual console step.
- The service-account key never leaves the server.
- Cost stays at or near zero at this scale, and a runaway bill is impossible to miss.
- The design's principles still hold: no preaching, no guilt copy, and users choose what they receive.

## Options considered

1. **A free Cloudflare Worker** holding the key, called by the app with the user's Firebase ID token.
2. **Firebase Blaze with Cloud Functions**, triggered by Firestore writes.
3. **Manual pushes from the Firebase console.**

## Decision outcome

**Chosen: Option 2**, the owner's decision.

Blaze keeps the Spark free allowance; a billing budget with email alerts at small amounts is set on day one. Functions are TypeScript under `firebase/functions`, deployed by CI, with tests against the Firebase emulator.

| Notification | Trigger | Sent to |
|---|---|---|
| Live now | A moderator saves a stream with «مباشر الآن» in Admin | topic `live` |
| New post | A post (verse, question or reflection) is published | topic `posts` (one topic per post type if the design splits them) |
| Reply to your post | A reply is created under a post | the post author's devices |
| Reply to your comment | A reply is created under a comment | the comment author's devices |
| App update | A release writes `app/latest` (version, notes, APK link) | topic `updates`; tapping opens the release page |
| Announcement | A sender composes one in Admin | topic `announcements` |

Also decided here:

- **Roles and permissions.** Who may manage streams, handle reports and send announcements is defined by the user and permission system (a separate ADR, in progress). Until it lands, the minimum is two claims: `moderator` (streams, reports) and `admin` (also announcements).
- **Server-side checks.** Functions re-check permissions, the payload length, and a rate limit on announcements; the client is never trusted.
- **Defaults: every push category is on** (owner's decision), each with its own switch in Settings to turn it off. On Android 13+ the system permission is asked once, at a moment the design chooses (onboarding or first Community use). The local morning and evening reminders stay **off** by default, as `DESIGN.md` §1 requires.
- **In-app update check.** On launch the app also reads `app/latest`, so a user who turned update pushes off still sees «تحديث متوفر».
- **Local notifications stay local.** Morning and evening reminders and «ذكّرني» are scheduled on the device.
- **iOS** push waits for the paid Apple account (APNs key); Android only for now.
- **Still never added:** Analytics, Crashlytics, Performance, or any SDK that sends usage data. FCM device tokens are stored only under the signed-in user's own document (for replies) or not at all (topics).

ADR 0003's other points carry over unchanged: Google sign-in only for Community, rules as the main protection, a restricted API key, and account deletion that also removes the user's tokens.

### Consequences

- Good: every push starts from the app's own screens; no console work.
- Good: cascading account deletion can now run in a function, which is more reliable than the client loop in 0003.
- Bad: a card on file and a billing account. Mitigated by budget alerts and the free allowance.
- Bad: a TypeScript codebase beside the Kotlin one, with its own CI job and tests.
- Bad: an announcement reaches everyone who opted in, so a compromised `admin` account is dangerous. Mitigated by a separate `admin` claim held by very few people, the rate limit, and a confirmation step before sending.

### Confirmation

- Emulator tests for `firestore.rules` and every function run in CI.
- A dependency check fails the build if Analytics, Crashlytics or Performance artifacts appear.
- The release checklist includes "budget alert set; admins and moderators listed".

### Design needed (to the designer)

- Admin: a third tab to compose an announcement (title, body, confirmation step), shown to `admin` only.
- Settings: a notifications group with a switch per category (live, new posts, replies, updates, announcements), all on by default.
- When to ask for the Android notification permission.
- The «تحديث متوفر» banner or sheet.
- Copy for each push, following DESIGN §8 (no exclamation marks, no emoji).

## Links

- `design/DECISIONS.md` #1, #15–19
- [Firebase pricing](https://firebase.google.com/pricing), [Cloud Functions for Firebase](https://firebase.google.com/docs/functions), [FCM HTTP v1](https://firebase.google.com/docs/cloud-messaging/send/v1-api)
