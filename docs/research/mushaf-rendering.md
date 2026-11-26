# Research: how quran.com renders the mushaf, and what we can use

Researched 2026-11-26 for DECISIONS.md #3, #4, #14 and #21 (`design/DECISIONS.md`). Sources are listed at the end. Anything marked **unconfirmed** was not found in a primary source.

## 1. How quran.com draws a page (web, `quran.com-frontend-next`)

The reading view works from **words**, not verses:

1. It fetches the verses of one page (`/verses/by_page/{page}`) with word fields: `code_v1`/`code_v2` (glyph codes), `page_number`, `line_number`, `position` (word index in the ayah), `char_type_name` (`word`, `end`, `pause`, `sajdah`, `rub-el-hizb`).
2. `groupLinesByVerses` flattens the words and groups them by `Page{n}-Line{m}`. **Line breaks come from the data**, so nothing is laid out locally. One line often holds words from several ayahs.
3. Each line is a `Line` component, and each word is a `QuranWord`. For the QCF fonts, `GlyphWord` sets the word's glyph code as its text in the font of that page.
4. **One font per page.** The face is named `p{page}-v2`, loaded on demand (`useQcfFont`, `FontFace` + `document.fonts.add`) from `/fonts/quran/hafs/v2/{woff2|woff|ttf}/p{page}.*`. Until it loads, the word shows `qpcUthmaniHafs` Unicode text instead.
5. **Headings aren't in the word data.** The surah-name line and the basmala line are drawn by the app. Surah names come from a ligature font (the text `004` renders as al-Nisa). Across 604 pages there are 226 empty heading lines: 114 surah names and 112 basmalas. Pages 1 and 2 have 8 lines.
6. Ayah-end markers (`end`) use the Unicode Hafs font, not the page font.

**Highlighting during audio.** The audio API returns, for each ayah, `timestamp_from`/`timestamp_to` and `segments` as `[wordPosition, startMs, endMs]`. The player finds the active ayah from the current time (with a 200 ms tolerance), then finds the active word by `position`. The line is highlighted when its ayah is active, and the word when its position matches. Tapping a word while audio plays seeks to that word's `startMs`.

**quran_android** (the reference app in SCREENS.md) is GPL-3.0 and draws pages from **images** (the quran.com-images project), not fonts. Its README says its data is usually CC BY-NC-ND. Its code isn't reused (DECISIONS #5). We read it for behaviour only.

## 2. What this means for us (Compose Multiplatform)

The same model fits Compose well, and it supports every state the design asks for:

- **Page** = 15 lines from layout data. **Line** = a row of word composables, **justified** (`is_centered = false`) or **centred** (`is_centered = true`). A line can also be a `surah_name` line or a `basmallah` line.
- **Word** = one glyph in that page's font, with its own `Modifier`. That gives us tap (AyahSheet, «استمع للكلمة»), selection (`accent.indigoSelect`), playing (`quran.highlight`), played or outside-the-hizb (`quran.played`), and colour in dark mode. V2 glyphs are single-colour, so the theme colours them.
- **Scale**: the font size is computed so the page's lines fit the screen width.
- **Fonts**: 604 bundled `.ttf` files (DECISIONS #14), loaded per page with Compose Resources, plus the surah-name font and KFGQPC HAFS for cards and ayah markers.
- **Audio sync**: the same `[wordPosition, startMs, endMs]` segments drive the playing word and «استمع من هنا».

## 3. Where the data can come from, and the licence problem

| Need | Quran Foundation (quran.com's provider) | QUL (Tarteel's Quranic Universal Library) |
|---|---|---|
| Page fonts (QCF V2, 604) | CDN `verses.quran.foundation/fonts/quran/hafs/v2/…`. **Bundling is allowed** if we keep a Developer Console account and credit «Quran fonts provided by Quran Foundation». Fonts may ship only inside the app, never offered separately. | `resources/font/249`, ttf/woff2/woff. Licence page: «We don't have copyright information for this resource». |
| Page layout (lines → words) | Only through the API or **Content Sync**. A prepackaged database or a build-time bundle is **not allowed**, and putting API data in a **public repo counts as redistribution**. | `resources/mushaf-layout/10` (KFGQPC V2, 604 pages, 15 lines), SQLite: `pages(page_number, line_number, line_type, is_centered, first_word_id, last_word_id, surah_number)` + `words(word_index, word_key, surah, ayah, text)`. QUL says its resources are «intended for you to download and package with your own project». Licence page: no copyright information. |
| Word timings | API (`segments=true`). Needs client credentials. | 59 recitations «with segments», json/sqlite, e.g. Mishari al-Afasy (414), one file per surah with word timings. Licence page: no copyright information. Audio host URLs **unconfirmed**. |
| Owner of fonts and layout | King Fahd Complex (KFGQPC). Its site (`qurancomplex.gov.sa/fonts`) offers free downloads but shows **no licence text**. The HAFS and QCF fonts aren't on its list. | same |

**Conclusion.** The Quran Foundation route is the clearest on fonts, but it rules out what we need: an offline layout bundled in the APK, built from a public repo. QUL is built for packaging, but has no written licence for these resources. In both cases the real owner is the King Fahd Complex, and it publishes no terms.

## 4. Open technical points

- **Unconfirmed:** whether `words.text` in QUL's V2 layout database holds the PUA glyph codes or Unicode text. Check by opening the SQLite file.
- **Unconfirmed:** that QUL's word IDs line up with the glyph-code resource (QUL «Glyph word-by-word» V2, id 61).
- The surah-name font and its licence.
- Whether there's a word-by-word audio source with a licence (DECISIONS #21: otherwise hide «استمع للكلمة»).

## Sources

- Quran Foundation, [Integrating Quran font rendering](https://api-docs.quran.foundation/docs/tutorials/fonts/font-rendering/)
- Quran Foundation, [Developer terms (mushaf fonts and images)](https://api-docs.quran.foundation/legal/mushaf-fonts-and-images/), last updated 2026-11-20
- [quran.com-frontend-next](https://github.com/quran/quran.com-frontend-next) (`production` branch): `ReadingView/Page.tsx`, `groupLinesByVerses.ts`, `Line.tsx`, `dls/QuranWord/GlyphWord.tsx`, `utils/fontFaceHelper.ts`, `hooks/useQcfFont.ts`, `xstate/actors/audioPlayer/audioPlayerMachineHelper.ts`. The README badge says MIT; the LICENSE file itself wasn't found.
- [quran_android README](https://github.com/quran/quran_android)
- QUL: [glyph-based fonts](https://qul.tarteel.ai/docs/glyph-based), [mushaf layout docs](https://qul.tarteel.ai/docs/mushaf-layout), [end-to-end tutorial](https://qul.tarteel.ai/docs/tutorial-mushaf-layout-end-to-end), [QPC V2 font](https://qul.tarteel.ai/resources/font/249), [KFGQPC V2 layout](https://qul.tarteel.ai/resources/mushaf-layout/10), [segments format](https://qul.tarteel.ai/docs/with-segments), [recitations](https://qul.tarteel.ai/resources/recitation), [FAQ](https://qul.tarteel.ai/faq)
- King Fahd Complex, [computer fonts](https://qurancomplex.gov.sa/fonts)
