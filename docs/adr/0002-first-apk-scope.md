# 0002 — The first APK ships every designed area, including Live and Community

- **Status:** accepted
- **Later change:** Amended by [0018](0018-design-v7.md) (first APK adds widgets, the notification player, the moderators' inbox and live details)
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** scope, release

## Context and problem statement

The first release is a signed APK handed to people around the owner (Android first; iOS stays a build target, not distributed). The design covers Home and Moon, Settings, الذكر (slider, hizb, mushaf with audio, the index, 7 doors, saved, Live, Community), Admin, and الضياء. Live, Community and Admin need a network backend, accounts and named moderators; everything else works offline. We must decide whether the first APK waits for the networked areas.

## Decision drivers

- People should get a complete experience, not a hub with hidden tiles.
- Moderation is a human job and must be staffed before posts can exist.
- Effort and time to the first handout.

## Options considered

1. **Offline core first:** Live, Community and Admin in a later phase.
2. **Everything in the first APK.**

## Decision outcome

**Chosen: Option 2**, the owner's decision. The first APK contains every designed screen. Still open and outside this decision: onboarding, الفرقان, and the «من نحن وكيف نفهم ديننا» content.

### Consequences

- Good: the الذكر hub matches the design exactly; no interim layout.
- Bad: the first handout waits for Firebase, its security rules and the Admin screen. Mitigated by building the offline areas first in the plan, so they are testable early.
- Bad: moderators must be named before the APK is handed out (`design/DECISIONS.md` #18). This is a release gate.
- Neutral: Firebase setup is described in ADR 0003.

### Confirmation

- The release checklist in `docs/guides/release.md` includes "moderators named and given the `moderator` claim".

## Links

- `design/SCREENS.md`, `design/DECISIONS.md` #1, #9, #18
