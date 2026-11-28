# Astronomy Engine (vendored)

- **Upstream:** https://github.com/cosinekitty/astronomy, `source/kotlin/src/main/kotlin/io/github/cosinekitty/astronomy/astronomy.kt`
- **Pinned at:** commit `826e26ff3a6dc03ee46658b1138fef582d96c5d9` (2025-01-27), the file's latest change.
- **Licence:** MIT, © 2019-2025 Don Cross; the notice is kept at the top of `astronomy.kt`.
- **Why vendored:** the published Kotlin build is JVM-only (JitPack 2.1.19); the source uses only `kotlin.math`, so it runs as common code for Android and iOS (ADR 0017).

## Changes (each marked `[qurancommunity]` in the code)

1. Explicit imports of `kotlin.jvm.JvmName`, `JvmOverloads`, `JvmStatic` (implicit only on the JVM).
2. `Time.toString`: `String.format` (JVM-only) replaced by `padStart`, with the same output.
3. The Pluto cache's `synchronized` replaced by `withPlatformLock` (`PlatformLock*.kt`): `synchronized` on Android, `NSRecursiveLock` on iOS.

## Checked

Against the US Naval Observatory data published with Astronomy Engine (`generate/riseset/riseset.txt`, `generate/moonphase/moonphases.txt`, copied into `src/androidHostTest/resources/usno`): 5,909 Sun and Moon rise and set times at 15 places, 5,907 within 1 minute and all within 1.2 (USNO rounds to whole minutes); 1,534 moon quarters since 1800, all within 1.4 minutes. A spot check runs the same on iOS.

To update: replace `astronomy.kt` from upstream, re-apply the three patches, update the commit above, and run the tests.
