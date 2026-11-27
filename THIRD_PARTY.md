# Third-party content and assets

The MIT licence in [LICENSE](LICENSE) covers this project's source code only. Everything below keeps its own terms and is credited in the app under Settings → «مصادر النص والصوت». Each committed asset also has its source URL, download date and checksum in a `SOURCES.md` next to it.

| Asset | Source | Terms | Status |
|---|---|---|---|
| Quran text (simple), matching base | [Tanzil](https://tanzil.net/download/) `quran-simple.txt` | Verbatim copies with attribution; the file is shipped unmodified (normalisation happens at read time) | committed (`core/quran/sources/SOURCES.md`) |
| Surah, juz and hizb metadata | [Tanzil](https://tanzil.net/docs/quran_metadata) `quran-data.js` | CC BY 3.0, verbatim, with attribution; the sajda rulings it also contains are not used (DESIGN §1) | committed (`core/quran/sources/SOURCES.md`) |
| Quran text (Hafs, Uthmani) for cards | King Fahd Glorious Quran Printing Complex, via [QUL](https://qul.tarteel.ai/) | No written licence found; permission request planned (ADR 0004) | to add (Phase 3) |
| KFGQPC V2 page layout and glyph words | King Fahd Complex, via [QUL](https://qul.tarteel.ai/resources/mushaf-layout/10) | No written licence found; permission request planned (ADR 0004) | committed, with the Unicode words for Digital Khatt (`core/quran/sources/SOURCES.md`) |
| Digital Khatt V2 (Quran text on cards; Mushaf fallback) | Amine Anane, Tarteel ([QUL](https://qul.tarteel.ai/resources/font/247)) | SIL Open Font License 1.1 | bundled (ADR 0013) |
| QPC page fonts (exact Mushaf) | [Quran Foundation](https://api-docs.quran.foundation/legal/developer-terms/) | Developer terms: downloaded at runtime, credited, never committed | Phase 3 (ADR 0013) |
| Recitation audio and timings: علي جابر | [Quran Foundation](https://api-docs.quran.foundation/legal/developer-terms/) | Developer terms: streamed or downloaded on request, credited, not rehosted (ADR 0007) | Phase 8 |
| IBM Plex Sans Arabic (UI font) | [IBM Plex](https://github.com/IBM/plex) via google/fonts | SIL Open Font License 1.1 | bundled (`core/designsystem/licenses/SOURCES.md`) |
| Lucide icons (1.82.0) | [lucide.dev](https://lucide.dev) | ISC | bundled as vectors (`core/designsystem/licenses/SOURCES.md`) |
| City list | [GeoNames](https://www.geonames.org/) | CC BY 4.0 | to add (Phase 4) |
| الضياء illustrations | the project's designer (`design/illustrations/`) | project-owned | in `design/` |
