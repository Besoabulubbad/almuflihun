# Research: a Quran engine better than quran.com

- **Status:** first pass, 2026-11-26 (issue: "Research: make the Quran engine better than quran.com"). Feeds Spike A, Phase 3 and Phase 8, and owner decisions on scope.
- **Method:** quran.com's product updates and source (see [mushaf-rendering.md](mushaf-rendering.md)), the App Store listings of Quran (by Quran.com, iOS) and Tarteel, and quran_android's repository page. Tarteel's site and quran_android's feature list could not be read; Ayah (iOS) not covered yet. Items marked *unconfirmed* were not found in a primary source.

## 1. What the best apps do today

| Area | Best current practice | Where |
|---|---|---|
| Editions | Several Madani prints (1405, 1421, 1439, 1440, 1441), IndoPak 15-line, Tajweed Hafs | Quran iOS |
| Tajweed colours | Tajweed Mushaf; fixed clipping at large sizes (Sep 2026) | quran.com |
| Word interaction | Tap a word: meaning or transliteration tooltip; menu to play from the word, repeat the verse | quran.com |
| Playing word | A word pointer that follows recitation | Quran iOS (word-level accuracy *unconfirmed*) |
| Speed | 0.25×–2×, with 0.9× and 1.1× steps; remembered | Quran iOS |
| Repeat | Verse or range, 1–100×, looping, pauses between repeats, inline ayah picker, remembered between sessions | Quran iOS |
| Play scope | To the end of the hizb, quarter, or the whole Quran | Quran iOS |
| Positions | Up to 3 named reading bookmarks with undo; 10 recent pages; monthly reading calendar | quran.com, Quran iOS |
| Memorisation | Hide verses and recite; word-level mistake detection by AI; random-start test mode | Tarteel (premium, needs internet and microphone) |
| Search | Arabic with translation; tolerant of other keyboards; voice search by recitation | quran.com, Quran iOS, Tarteel |
| Offline | Downloaded audio and pages, excluded from device backups | Quran iOS |
| Accessibility | **No accessibility statement in any listing read** | all |

## 2. Where المفلحون can be clearly better

These come from the gaps above and from how glyph-font Mushafs work.

### Already decided (keep and do exceptionally well)

1. **Word-accurate sync with a gliding highlight.** The playing word moves smoothly from word to word, within 50 ms of the timing data (quality bar), and the page turns just before the next page's first ayah, never after.
2. **No blank pages.** The next and previous pages' fonts are loaded before they are needed, so a page turn never shows fallback text. Budget: fully drawn ≤ 100 ms.
3. **Separate reading positions** (Mushaf and Hizb), «عُد إلى قراءتك» after any jump, the saved-page ribbon (v4).
4. **Repeat for memorisers:** an ayah or a range, a count, speed 0.75×–1.5× (v4 PlayerSheet).
5. **Correct by construction:** every quote placed by test; the «Quran content error» issue form; nothing cut.
6. **Offline and private:** the whole Mushaf bundled; no account needed to read, listen, save or search.

### Proposed improvements (need owner decisions)

| # | Proposal | Why it beats current apps | Cost |
|---|---|---|---|
| P1 | **A Mushaf that screen readers can read.** Glyph fonts store each word as a private code that TalkBack cannot pronounce. We attach the Unicode Hafs text to every line and ayah as its accessibility text, announce «آية n», and let a screen-reader user move ayah by ayah. | No listing read claims accessibility; this makes the Mushaf usable by blind and low-vision readers | Medium; engineering only, small design note |
| P2 | **Copy and share the real text.** Copying from a glyph page gives private codes in many apps; we always copy the Unicode Hafs text with its reference (already the share format). | Correctness users notice | Small |
| P3 | **Memorisation without AI or internet:** hide the words of an ayah or range and reveal them word by word on tap, or show first letters only; combined with repeat. Fully offline, nothing recorded. | Tarteel's equivalent is premium, online, and records the voice | Medium; needs design |
| P4 | **Pauses between repeats** (e.g. 2 s, 5 s, or the ayah's own length, for repeating after the reciter) and **remembered repeat settings**. | Matches Quran iOS's best feature; "repeat after the reciter" is the classic memorisation method | Small; small design addition |
| P5 | **Play scope:** to the end of the surah, the hizb, the quarter, or a chosen ayah. | Matches Quran iOS | Small; design addition to PlayerSheet |
| P6 | **Recent pages** (last 10) in the index. | Matches Quran iOS; useful after jumps | Small; design addition |
| P7 | **Root-aware search:** find «صبر» in «الصابرين، اصبروا، صبراً» using morphology data. | Beyond diacritic-insensitive search; most apps match letters only | Medium; needs a morphology source with a clear licence (to find) |
| P8 | **Sleep timer** for listening at night. | Common request; fits the moon and night theme | Small; design addition |
| P9 | **Tajweed colours** as a second Mushaf style. | quran.com has it | Large; QUL's V4 tajweed font is disabled and its licence is unclear; later |
| P10 | **Two-page spread** on tablets in landscape. | Already in v4 as a rule; just build it well | Medium |

## 3. Next steps

- Owner decides P1–P8 (P9 deferred); accepted items become design requests where they need UI, and issues in the tracker.
- Spike A measures the rendering path these depend on (fonts prefetch, per-word composables, semantics for P1).
- Second research pass: Ayah (iOS), Tarteel's web help, and a morphology data source and its licence for P7.
