# 0012 — A Quran study engine: collections, advanced search, word study, related verses

- **Status:** accepted
- **Later change:** Amended by [0018](0018-design-v7.md) (word tap off for now; word and ayah study entry points decided later)
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** quran, product, search, data

## Context and problem statement

The owner set the product's direction: المفلحون is for **studying** the Quran, not for memorising it, and it must beat quran.com on ease of use, on grouping saved verses, and on search ([research](../research/quran-engine.md)). With the Quran as the only source (DESIGN §1), study means letting the text explain itself: where words occur, which verses share phrases, how themes run through the surahs. The v4 design has a flat Saved list and letter-based search only.

## Decision drivers

- Study tools built only from the Quran's text and its linguistic data; no tafsir, no authorities.
- Fully offline.
- Ease of use: one place per ayah, reachable from where the user already is.
- Data with honest attribution.

## Options considered

1. **All of S1–S6** in the first APK.
2. **S1–S3 first**, S4–S6 in the next update.
3. **S1 and S2 only.**

## Decision outcome

**Chosen: Option 1** (owner), plus the ease-of-use items P2, P5, P6 and P8.

| # | Feature |
|---|---|
| S1 | **Collections** («مجموعات») replace the flat Saved list: named collections, a verse in several, reorder, a private «تأمّل» note per verse, share as plain text or post to Community, synced when signed in |
| S2 | **Advanced search:** words without diacritics, roots, lemmas, exact phrase, and/or/not, scope (surah, juz, hizb, range, Meccan or Medinan), results grouped by surah with counts, save results to a collection, local history |
| S3 | **Word study:** tap a word for its root, lemma, number of occurrences, and every occurrence as a full verse with the word highlighted |
| S4 | **Related verses:** shared phrases computed from the text, and shared rarer roots |
| S5 | **Ayah study panel:** position, words and roots, related verses, the doors that include it, the user's collections and notes; opened from the AyahSheet |
| S6 | **Surah overview:** ayah count, place and order of revelation, most frequent roots, the doors that quote it |
| P2 | Copying always yields the real Unicode text with its reference |
| P5 | Play scope: to the end of the surah, hizb, quarter, or a chosen ayah |
| P6 | The last 10 pages read, in the index |
| P8 | A sleep timer |

**Data.**

- Surah metadata from Tanzil `quran-data` (CC BY 3.0, verbatim, credited).
- Roots, lemmas and stems per word from **QUL morphology** (word root, lemma, stem sets), committed and credited on the same terms as ADR 0004 and added to the permission request to Tarteel. The Quranic Arabic Corpus is not used (GPL v3 with a no-modification clause).
- Related phrases are computed from the text at build time; no external data.
- All indexes are built at build time into the app's bundled database; the app never needs the network for study.

**Positioning.** Memorisation features (hide and reveal, repeat-after-reciter pauses) are out of scope. The v4 PlayerSheet's repeat stays as designed but is not extended.

### Consequences

- Good: study tools no mainstream Quran app combines, all offline and Quran-only.
- Good: Collections answer the owner's most direct request and connect to Community.
- Bad: substantial new design (Collections, search filters and results, word study, the study panel, surah overview, the sleep timer, play scope, recent pages). Requests go to the designer before Phase 6 builds these screens.
- Bad: a new data source without a written licence (mitigated as in ADR 0004).
- Bad: more build-time processing and a larger bundled database; size is measured in Spike A and on every release.
- Neutral: QUL's morphology has 77,432 word entries; aligning them with the layout's word list is checked by test.

### Confirmation

- Build-time tests: every word in the layout has a morphology entry or a documented exception; root and lemma search returns known reference counts for chosen roots; related-phrase results are reproducible.
- Search stays within the quality bar's 300 ms budget on the whole Quran.

## Links

- [Research: a Quran engine better than quran.com](../research/quran-engine.md)
- ADR [0004](0004-quran-assets-in-repo.md), [0006](0006-community-users-and-permissions.md); `design/DESIGN.md` §1
- [QUL morphology](https://qul.tarteel.ai/resources/morphology), [Tanzil text licence](https://tanzil.net/docs/Text_License)
