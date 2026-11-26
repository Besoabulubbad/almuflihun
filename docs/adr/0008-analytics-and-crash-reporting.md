# 0008 — Opt-in, purpose-limited analytics and crash reporting with Firebase

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** analytics, privacy, quality
- **Supersedes:** the "never add Analytics or Crashlytics" clause of [0005](0005-firebase-blaze-push-notifications.md) and `design/DECISIONS.md` #19

## Context and problem statement

The owner needs to know how the app is used in order to develop it: which screens people use and for how long, where they stop, the last page they read, what breaks, and who uninstalls. Earlier decisions (DECISIONS #19, ADR 0005) promised no usage data and no crash reports, and Settings says so. The owner reversed this with one condition: collect what is needed to develop the app, meaningfully, never to profile people or to sell anything.

## Decision drivers

- Every collected field answers a named product question.
- Honest with users: they choose, and Settings says what is collected.
- A Quran app's reading data is sensitive (religion is a special category of personal data in GDPR terms), so collection is opt-in and contains no identity.
- Low effort and cost: Firebase is already in the app.

## Options considered

1. **Firebase Analytics + Crashlytics, opt-in.**
2. **PostHog (EU), opt-in.**
3. **Our own anonymous counts** through a Cloud Function.

## Decision outcome

**Chosen: Option 1** (owner).

- **Consent first.** Nothing is collected until the user agrees in a one-time prompt (onboarding or first launch). Both SDKs start with collection disabled and are enabled only after consent. A Settings switch turns it off at any time, and turning it off also asks Firebase to stop collection on the device.
- **No identity.** No user ID is set, even for signed-in users. No names, emails, Google IDs, free text (posts, search queries, feedback), or precise location. The advertising ID is not collected; Google signals and ads personalisation are off; all Analytics data-sharing settings are off.
- **Purpose-limited.** Events and parameters are listed in the [tracking plan](../analytics/tracking-plan.md). Each event names the product question it answers. An event not in the plan is a defect; adding one needs the analysis agent's review and an update to the plan.
- **Retention** is set to 14 months in Analytics. Crashlytics keeps its default.
- **Analysis.** Analytics is linked to BigQuery (daily export, within the free tier) so the analysis agent can query it.
- **Uninstalls** come from Analytics' automatic `app_remove` on Android, cross-checked with push tokens that FCM reports as unregistered.
- **What users dislike** comes from asking, not tracking: a «رأيك» feedback form in Settings, stored in Firestore and readable in Admin.
- **Never sold or shared**, and never used for advertising. The privacy policy and Settings say so.

### Consequences

- Good: real evidence for what to build next; crashes found before users report them.
- Good: the plan doubles as the user-facing explanation of what is collected.
- Bad: data goes to Google. Mitigated by opt-in, no identity, sharing off, and the published plan.
- Bad: Settings copy, the privacy policy, and the designer's DECISIONS #19 must change (design requests).
- Neutral: users who decline are invisible in analytics; numbers describe consenting users only.

### Confirmation

- A unit test asserts the Analytics wrapper drops any event or parameter not in the tracking plan.
- An instrumented test asserts no event is sent before consent.
- The analysis agent reviews every change to the tracking plan.

## Links

- [Tracking plan](../analytics/tracking-plan.md), [analysis agent](../../.claude/agents/analytics-analyst.md)
- [Firebase Analytics privacy controls](https://firebase.google.com/docs/analytics/configure-data-collection), [Crashlytics opt-in reporting](https://firebase.google.com/docs/crashlytics/customize-crash-reports#enable-reporting)
