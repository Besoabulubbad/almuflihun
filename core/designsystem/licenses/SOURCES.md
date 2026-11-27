# Bundled fonts: sources and checksums

| File (`src/commonMain/composeResources/font/`) | Source | Licence | SHA-256 |
|---|---|---|---|
| `ibm_plex_sans_arabic_regular.ttf` | google/fonts `ofl/ibmplexsansarabic/IBMPlexSansArabic-Regular.ttf`, 2026-11-27 | SIL OFL 1.1 (`IBM-Plex-OFL.txt`) | `6f611412270a132bbac838da9259d4c68569b4175f3b3b8fa3fa36a30b56dab9` |
| `ibm_plex_sans_arabic_semibold.ttf` | same, `IBMPlexSansArabic-SemiBold.ttf` | SIL OFL 1.1 | `597bd5502e5997be4414e4c9c88834b30ff3784250c84f20bce2b20e53ebd467` |
| `ibm_plex_sans_arabic_bold.ttf` | same, `IBMPlexSansArabic-Bold.ttf` | SIL OFL 1.1 | `691e0c891a38637ae6bbdb69700f8042cb0724a137bee615068ffdb92244f61f` |
| `digital_khatt_v2.otf` | QUL font 247, `https://static-cdn.tarteel.ai/qul/fonts/dk/DigitalKhattV2.otf`, 2026-11-27 | SIL OFL 1.1 (`DigitalKhatt-OFL.txt`) | `0935c48269a57c9808e52dfae47864c189396452901c689977156036a72dd217` |

IBM Plex Sans Arabic is the UI font (DESIGN §3). Digital Khatt V2 is the Quran font for cards, the slider, the study panel and posts, and the Mushaf fallback (ADR 0013). The weight 500 is not bundled: the design uses only 400, 600 and 700.

## Icons

`src/commonMain/composeResources/drawable/ic_*.xml`: 67 Lucide icons and the filled `bookmark-check`, converted from `lucide-static` **1.82.0** (npm, 2026-11-27) by `tooling/scripts/lucide_to_vector.py` (stroke 1.75, DESIGN §7). Licence: ISC (`Lucide-ISC.txt`). Re-run the script after a version bump; `AppIcon` lists every icon by meaning.
