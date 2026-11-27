# 0013 — The Mushaf: exact QPC page fonts downloaded once, Digital Khatt bundled as the fallback

- **Status:** accepted
- **Date:** 2026-11-27
- **Deciders:** Mohammad Abu Lubad (delegated the choice to Claude, with the condition "implemented the right way, users never face issues"), Claude
- **Tags:** quran, mushaf, licensing, offline
- **Supersedes:** the page-font parts of [0004](0004-quran-assets-in-repo.md); changes `design/DECISIONS.md` #3, #4, #12, #14
- **Amended by:** [0016](0016-mushaf-fonts-woff.md) (download WOFF, not WOFF2; Android inflates to TTF with zlib)

## Context and problem statement

[Spike A](../research/spike-a-mushaf.md) drew the printed Madani pages in Compose. The owner wants the result "near perfect": the exact printed glyphs, not word-spaced approximations. The exact glyphs are the KFGQPC QPC page fonts: 604 files per edition, 200 MB as TTF (≈130 MB deflated in an APK), ≈94 MB as WOFF2. Bundling them makes every self-update (ADR 0010) re-download the whole size. Quran Foundation's terms allow an app to download and cache its Mushaf fonts indefinitely with an active Developer Console account and a credit, and forbid putting them in a public repository. quran.com's own Android app ships no pages and asks to download them on first launch.

## Decision drivers

- The exact printed page, never an approximation, once downloaded.
- The Mushaf usable from the first second, with or without internet.
- Small APK and small self-updates.
- A clear licence for every file; nothing third-party in the public repo that its terms forbid.
- No failure a user can get stuck in.

## Options considered

1. **A: download the exact edition once; bundle Digital Khatt (0.5 MB, SIL OFL) as the fallback.**
2. **B: bundle the exact edition in the APK** (≈130 MB, re-downloaded with every update).
3. **C: download the exact edition on first launch, no fallback** (what quran_android does).
4. Digital Khatt only (word-spaced justification; not the exact print).

## Decision outcome

**Chosen: A.**

### Editions

| Edition | Fonts (Quran Foundation CDN) | Layout and words (QUL, committed) | First APK |
|---|---|---|---|
| **مصحف المدينة ١٤٢١** (QPC V2) | `hafs/v2/woff2/p{1..604}.woff2` | KFGQPC V2 layout + V2 glyph words | **default**, downloaded automatically |
| مصحف التجويد الملوّن ١٤٤١ (QPC V4, COLR v0, 6 palettes) | `hafs/v4/colrv1/...` per the CDN | KFGQPC V4 layout + V4 glyph words | optional download in Settings |
| مصحف المدينة ١٤٠٥ (QPC V1) | `hafs/v1/woff2/...` | KFGQPC V1 layout + V1 glyph words | a later update |
| Digital Khatt V2 (fallback) | bundled `DigitalKhattV2.otf`, 0.5 MB, SIL OFL 1.1 | same layout as V2 (verified identical) | bundled |

### How downloading works ("users never face issues")

- **When:** right after onboarding, automatically for 1421; others on request in Settings → «شكل المصحف».
- **Order:** the user's current page and its neighbours first, then outward; any page the user opens jumps to the front of the queue.
- **Network:** on Wi-Fi automatically; on mobile data the app asks once, showing the size, and remembers the answer.
- **Robustness:** Android WorkManager / iOS background `URLSession`; per-file, resumable, retried with backoff; survives app kill and reboot; pause and resume; a notification shows progress.
- **Space:** checks free storage before starting (≈150 MB needed for 1421 as WOFF2 plus working space) and explains if it is short.
- **Integrity:** each file is checked (size, parses as a font, contains the glyphs its page's words use) before it is used; a bad file is fetched again.
- **Storage:** WOFF2 is kept in no-backup app storage (`noBackupFilesDir`, excluded from iOS backups) and decoded to TTF on demand into a small cache of recent pages. Decoding is in Kotlin (WOFF2 table reconstruction) over a platform Brotli decoder. If Phase 3 benchmarks show decoding misses the 100 ms page budget, store decoded TTF instead (≈200 MB).
- **Freshness:** the CDN sends ETags; weekly on Wi-Fi the app checks for corrected font files, as Quran Foundation recommends.
- **Fallback:** a page whose exact font is not on the device is drawn with Digital Khatt (same layout, real text) with a quiet note «يُحمَّل المصحف المطابق للطبعة»; it switches to the exact page as soon as its font arrives. If Quran Foundation's CDN is ever unavailable or its terms change, the Mushaf keeps working.
- **Management:** Settings shows each edition's size and lets the user delete or re-download it.

### Licences and the repository

- QPC fonts: Quran Foundation Developer Terms (fonts section). The owner's Developer Console account stays active; Settings → «مصادر النص والصوت» says «خطوط المصحف مقدّمة من Quran Foundation». The fonts are never committed and never offered outside the app.
- Digital Khatt: SIL OFL 1.1, committed with its licence file.
- Layout and word data: QUL, committed and credited (ADR 0004 unchanged for these).
- Card and inline Quran text (`design` DECISIONS #12): use Digital Khatt with the same Unicode text, bundled, so cards never depend on a download. This replaces the KFGQPC HAFS Uthmanic Script card font (a design request).

### Consequences

- Good: the exact printed Mushaf for everyone after one download, as in quran.com's apps, plus an instant offline fallback they lack.
- Good: APK ≈15 MB; self-updates stay small.
- Good: every file has a written licence or explicit terms.
- Bad: a one-time ≈94 MB download for 1421; the first pages may show Digital Khatt for a few seconds on a slow network.
- Bad: more engineering than bundling (download manager, WOFF2 decoding, integrity, editions). Mitigated: it is one module (`core:quran` with platform adapters), tested with fault injection.
- Neutral: the sajdah overline, which Digital Khatt does not draw, is drawn by the app over the 15 sajdah words in fallback mode.

### Confirmation

- Tests: download queue ordering, resume after kill, retry, storage-full, corrupted-file and mobile-data paths; WOFF2 decoding produces glyph-identical fonts to the CDN's TTF for all 604 pages (CI job on demand, not every PR).
- Macrobenchmark: page turn ≤ 100 ms with an on-demand decode.
- A CI check fails if any QPC font file is committed.

## Links

- [Spike A result](../research/spike-a-mushaf.md), [Quran Foundation font rendering](https://api-docs.quran.foundation/docs/tutorials/fonts/font-rendering/), [Developer terms](https://api-docs.quran.foundation/legal/developer-terms/)
- quran_android first-launch download: `QuranDataActivity.kt` («Download Required Files?»)
- [Digital Khatt V2 font (QUL)](https://qul.tarteel.ai/resources/font/247)
