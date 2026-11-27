# 0016 — Mushaf page fonts are downloaded as WOFF, not WOFF2

- **Status:** accepted
- **Date:** 2026-11-27
- **Deciders:** Mohammad Abu Lubad (delegated: "implemented the right way, users never face issues"), Claude
- **Tags:** quran, mushaf, offline, performance
- **Amends:** [0013](0013-mushaf-fonts-download.md) (the format and storage parts; everything else stands)

## Context and problem statement

ADR 0013 chose to download the exact QPC V2 page fonts as WOFF2 (≈94 MB) and, on platforms that need it, to decode them in Kotlin "over a platform Brotli decoder". Before building the download manager we checked that assumption on both platforms, with real page fonts from the Quran Foundation CDN (kept outside the repository):

| | WOFF2 | WOFF (v1) | TTF |
|---|---|---|---|
| iOS, Skia (what Compose draws with) | **loads** (family `QCF2001`, 37 glyphs, page 1's first word maps) | **loads** (same) | loads |
| Android, `Typeface` (what Compose draws with) | **does not load** (`Typeface.Builder` returns null) | does not load as is | loads |
| Size, pages 1 / 50 / 300 / 604 | 41 / 122 / 190 / 53 KB | 46 / 132 / 209 / 59 KB (**+8–12%**) | 619 / 323 / 346 / 163 KB |

On Android, WOFF2 would need a Brotli decoder **and** WOFF2's glyph-table reconstruction (the `glyf`/`loca` transforms): a large, error-prone piece of code on the path of every page. WOFF (v1) compresses each table with zlib; turning it into a TTF is a short, fully specified routine, and zlib is built into Java (`java.util.zip.Inflater`). A WOFF page decoded that way loads in Android's `Typeface`, and 35 of page 1's 36 glyph codes draw from the page font (the one left has the same width as the system fallback).

## Decision drivers

- No complex font code on the path of every page turn.
- Small download and storage (ADR 0013).
- One download format for both platforms, so integrity checks and the queue are shared.

## Options considered

1. **A: WOFF (v1) for both platforms;** iOS loads it directly; Android inflates it to TTF with `java.util.zip` when a page is needed.
2. **B: WOFF2 everywhere** with a Kotlin WOFF2 decoder on Android (ADR 0013's plan).
3. **C: TTF on Android** (≈200 MB download, no compression from the CDN), WOFF2 on iOS.

## Decision outcome

**Chosen: A.** About 104 MB for the whole Mushaf (≈10 MB more than WOFF2), no Brotli and no glyph reconstruction, the same files on both platforms.

### How it works

- **Storage:** the WOFF files in no-backup app storage (Android `noBackupFilesDir`; iOS Application Support, excluded from backup).
- **Android:** a page's WOFF is inflated to TTF into a small cache of recent pages (for example the last 30, a few MB) the first time it is drawn; inflating one page takes milliseconds. The TTF is rebuilt if the cache is cleared.
- **iOS:** Skia loads the WOFF bytes as they are.
- **Integrity:** each file must be a valid WOFF whose tables inflate to their declared lengths and whose table checksums match (WOFF stores the original checksum of every table), and the platform must load it and map the glyph codes its page's words use. A file that fails is deleted and downloaded again.

### The download manager (unchanged from 0013, now concrete)

- **Engine:** Android **WorkManager** (AndroidX; new dependency, recorded here): a unique, resumable job with network constraints and backoff, which survives the app being killed and reboots, and shows its progress in a notification. iOS: a **background `URLSession`**.
- **Order:** the reader's current page and its neighbours first, then outward; a page the reader opens jumps to the front.
- **Network:** automatic on Wi-Fi; on mobile data the app asks once, showing the size, and remembers the answer.
- **Space:** checks free storage before starting (≈130 MB with working space) and explains if it is short.
- **Fallback:** a page whose font is not yet on the device is drawn in Digital Khatt with the quiet note «يُحمَّل المصحف المطابق للطبعة» and switches as soon as its font arrives.
- **Freshness:** a weekly ETag check on Wi-Fi for corrected files.

### Consequences

- Good: no WOFF2 decoder to write or maintain; the Android decoder is about a hundred lines over the platform's zlib, and is unit-tested by round trip.
- Good: one format, one queue, one integrity check on both platforms.
- Bad: ≈10 MB more to download than WOFF2.
- Neutral: the CDN's TTF files are a different build from its WOFF files (larger, more tables), so the decoded TTF is checked by loading and glyph mapping, not by comparing bytes with the CDN's TTF.

## Build order

1. The WOFF reader (validation, and Android's WOFF → TTF) with round-trip tests, and the font store (paths, integrity, cache).
2. The download engine: queue and order, the network policy, WorkManager and background `URLSession`, progress state.
3. The Mushaf font provider for Compose: the page's font when present, Digital Khatt otherwise, switching live.
4. Settings and the first-launch prompts (Phase 6, after the designer's answer to `docs/design-requests.md` §3).
