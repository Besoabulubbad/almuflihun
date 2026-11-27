# 0017 — Astronomy Engine, vendored as common Kotlin

- **Status:** accepted
- **Date:** 2026-11-27
- **Deciders:** Mohammad Abu Lubad (delegated: "implemented the right way"), Claude
- **Tags:** astronomy, moon, dependencies, multiplatform
- **Settles:** the open choice in `docs/plan.md` Phase 4 and `docs/tech-stack.md` ("Astronomy Engine … JVM-only build; Phase 4 decides")

## Context and problem statement

The Moon screen, Home and «أوقات التسبيح» compute the moon phase, rise and set, sun times and notable moments on the device (docs/specs/screens.md §2). The plan named Astronomy Engine (MIT, accurate, widely used) and noted its Kotlin build might be JVM-only. Checked on 2026-11-27: the published Kotlin artifact is JVM-only (JitPack, 2.1.19, December 2023); its source is one file whose only imports are `kotlin.math`.

## Decision drivers

- The same numbers on Android and iOS, from one implementation.
- Accuracy checked against an authority, not assumed.
- No JVM-only or network dependency on a computation the app needs offline.
- Easy to update from upstream.

## Options considered

1. **A: vendor the Kotlin source into `core:astronomy` as common code**, pinned to a commit, with the minimum patches for Kotlin/Native.
2. **B: the JVM artifact on Android and the C or Swift version on iOS** (two implementations that can disagree; cinterop on iOS).
3. **C: another multiplatform astronomy library** (none found with comparable accuracy and tests).
4. **D: write our own** (Meeus-level formulas; months of work and less accurate than A).

## Decision outcome

**Chosen: A.** A spike compiled the file as common code with eleven errors, all fixed by three small, marked patches (explicit `kotlin.jvm` annotation imports, a `padStart` instead of `String.format`, a platform lock instead of `synchronized` for an unused Pluto cache).

### Checked against the US Naval Observatory

Astronomy Engine's repository publishes USNO reference data. Our build gives: **5,909 Sun and Moon rise and set times** at 15 places (tropics to poles, both hemispheres, 1944–2050), **5,907 within 1 minute and all within 1.2 minutes** (USNO rounds to whole minutes); **1,534 moon quarters** since 1800, **all within 1.4 minutes**. These run as a JVM test; a spot check of the same events runs on iOS.

Phase 4's exit criterion ("rise and set within 1 minute") is therefore stated as measured: 99.97% within 1 minute, all within 1.2.

### Consequences

- Good: one implementation, identical on both platforms, fully offline; no JitPack dependency.
- Good: accuracy is proven by test on every build, not assumed.
- Bad: we carry a 10,700-line vendored file; updates are manual (steps in its `SOURCE.md`). It is excluded from our style rules and kept as upstream wrote it.
- Neutral: the app wraps it in `core:astronomy`'s own API (moon phase, rise and set, sun times, notable moments); features never touch the engine directly.
