# 0020 — Analytics for design v7: remote settings, more events, zero-result searches; no A/B tests

- **Status:** accepted
- **Date:** 2026-11-28
- **Deciders:** Mohammad Abu Lubad (owner), Claude
- **Tags:** analytics, privacy
- **Amends:** [0008](0008-analytics-and-crash-reporting.md)

## Context

ADR 0008: analytics and crash reports are opt-in (off until the user agrees), only events in `docs/analytics/tracking-plan.md` are sent, and search terms are never collected. Design v7's UX strategy (§4) asks for remote config and feature flags, A/B tests by a random install id, a longer event list, and (v7 #52, open) the text of searches that found nothing. The owner chose after the options and their effects were laid out.

## Decision

1. **Remote settings (Firebase Remote Config): yes.** The app downloads a small settings file that can change, for example, the order of the الذكر cards or hide a section tile, without a new APK. It only receives; nothing about the user is sent. Defaults are bundled, so the app works offline and before any fetch.
2. **More events: yes,** as v7 lists them (tabs, cards, player actions, widgets, notifications, تلاوة progress, spelling forms, moderator messages, task times…), still anonymous counts and buckets, never names, location or what someone reads. Each goes into the tracking plan through the `analytics-analyst` review before it is coded.
3. **Searches that found nothing: their words are sent, only with consent and only those searches.** The query is sent in its normalised form, with no user or install link; every other search stays a count. This replaces ADR 0008's "never search terms" for this one case.
4. **A/B tests: no.** No random install id for experiments. A later ADR can revisit when there are enough users (about 1,000 a week).

## Consequences

- Good: the design can be tuned after release without a new APK; zero-result searches show what readers look for and cannot find.
- Bad: one more Firebase component (Remote Config) and a privacy note to update in Settings («ما نجمعه»); the consent text must mention zero-result searches.
- The tracking plan is rewritten for v7 with the analytics-analyst review before any of this ships.
