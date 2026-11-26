# المفلحون: design system (strict)

This file is a specification, not a set of suggestions. Follow these four rules:

- Every size, gap, padding, radius, colour and text style in the app comes from `tokens.json`, by name.
- A value that is not in `tokens.json` is a bug.
- If a screen seems to need a new value, stop and ask. Do not invent one.
- When a screenshot and this file disagree, this file wins. When the data and a screenshot disagree, the data wins.

Other files:
- `tokens.json`: the closed token set.
- `code/AppTokens.kt`: the same tokens for Compose Multiplatform.
- `SCREENS.md`: each screen, in order.
- `screens/`: the approved images.
- `data/`: the content.

---

## 0. Enforcement (intent; the mechanism is the project's choice)

The intent is fixed: **no style value exists outside the token set, and no screen restyles a shared component.** Any machinery that guarantees this is fine. For Sojourn this means `core:designsystem`, the token generator, detekt rules and Lucide icons converted to vector files. See DECISIONS.md #8.

What must be true, however it is enforced:
1. Tokens are generated from `tokens.json`. `code/AppTokens.kt` is a reference output; the project's own generator may replace it.
2. Outside the design-system module there are no raw `dp`/`sp` numbers, no `Color(0x…)` and no ad-hoc `TextStyle`. A static check fails the build on them. The exceptions are hairline borders (`0.dp`, `1.dp`).
3. Text is set only with a role from §3. Colour comes only from the palette in §4, through the current theme.
4. The components in §6 live in the design-system module. Screens compose them and never restyle them.
5. A unit test asserts that every `quote` and `highlight` in `data/` is a substring of its verse, and that its `words` range points at the same words.

---

## 1. Principles that are UI rules

1. **The Quran is the only source.** Show no hadith, tafsir or names of people as authorities.
2. **The full verse, always.**
   - A phrase is highlighted *inside* the complete verse.
   - A pull-quote is allowed only above its full verse.
   - Never truncate a verse with an ellipsis. Never line-clamp Quran text.
3. **Three levels, labelled.**
   - From the text: verses.
   - «من اللغة»: the LangBox.
   - «تأمّل»: reflection, labelled «فهم شخصي، ليس نصاً».
   - Never mix two levels in one text block.
4. **Honesty.** Unknown means «لا نعرف». Sample data shows the caption «بيانات مثال».
5. **No preaching.**
   - Reminders are off by default.
   - No streaks, no guilt copy, no exclamation marks, no emoji.
6. **All ages.**
   - The minimum text is `caption` (12).
   - The minimum touch target is `size.touch` (44 × 44).

---

## 2. Grid and screen frame

| Item | Value |
|---|---|
| Base unit | 4 (spaces are multiples of 4, plus `s2` for hairline gaps) |
| Design width | `size.screenWidth` 390. All layouts are one column and stretch to width. |
| Screen side padding | `s16` on paper screens, `s20` on the night hero area |
| Top inset | Real safe-area inset + `s8`. The mockups show 48. |
| Bottom inset | Real safe-area inset + `s16` |
| Gap between blocks in a section | `s16` |
| Gap between items in a list or card | `s12` |
| Gap between text lines inside one card | `s4` (title → meta), `s8` (meta → body) |
| Gap between sections | `s24` |
| Direction | RTL. Start = right. Back = `chevron-right`. Forward = `chevron-left`. |
| Digits | Arabic-Indic only. Verse refs are written `سورة البقرة ١٧٢` / `البقرة ١٧٢`, and ranges as `١٤٣–١٤٤`. |

There are two grounds:
- **Night** (`color.night.bg`): the hero tops of Home, Moon, الذكر, الأبواب and the doors.
- **Paper** (`color.paper.bg`): every reading surface.

A paper sheet over night starts with top corners `r24` and no gap.

---

## 3. Text roles (the only text styles)

| Role | Font | Size | Weight | Line height | Used for |
|---|---|---|---|---|---|
| `display` | UI | 56 | 700 | 1.3 | Moon page: the lunar day number. Nothing else. |
| `titleXL` | UI | 36 | 700 | 1.3 | Screen title on a night hero (الذكر, door names) |
| `titleL` | UI | 28 | 700 | 1.3 | Screen title on paper. Home lunar night name. Station title. |
| `titleM` | UI | 24 | 700 | 1.3 | Section headings («الخلاصة», door section titles) |
| `titleS` | UI | 20 | 700 | 1.6 | Group headings (البثوث، الأبواب، وقتك مع القرآن) and card titles |
| `subhead` | UI | 18 | 700 | 1.6 | Sub-headings inside a section, tile and portal names |
| `bodyL` | UI | 18 | 400 | 1.9 | Intro paragraph under a screen title |
| `body` | UI | 16 | 400 | 1.9 | All running text |
| `bodyStrong` | UI | 16 | 700 | 1.9 | Emphasis inside body, list item names |
| `label` | UI | 14 | 600 | 1.6 | Buttons, chips, ref chips, filters, tab labels |
| `meta` | UI | 14 | 400 | 1.6 | Secondary lines (ranges, hosts, dates) |
| `caption` | UI | 12 | 400 | 1.6 | Small notes, chart axes, «بيانات مثال» |
| `captionStrong` | UI | 12 | 700 | 1.6 | Overlines («المحطة ٤ من ١١», «من اللغة», badges) |
| `quranWord` | Quran | 30 | 400 | 1.8 | One Quran word alone (trait cards) |
| `quranHero` | Quran | 26 | 400 | 2.15 | The large pull-quote on night (slider, دليل اليوم on الأبواب) and `compare` / `closing_quote` blocks |
| `quranBody` | Quran | 22 | 400 | 2.15 | Full verses in VerseCards, the EvidenceItem quote, and Home «دليل اليوم» |
| `quranInline` | Quran | 18 | 400 | 1.8 | A «quoted phrase» inside UI text. A verse under a pull-quote. Verses inside Community posts. |
| `quranSmall` | Quran | 16 | 400 | 1.8 | Quran text in dense lists (DoorTile first verse, Saved items) |

Rules:
- Quran text is set only with a `quran*` role, and UI text never uses one.
- `quranBody` paragraphs are justified. Every other text aligns to the start.
- Weight 500 does not exist. Use 400, 600 or 700 only.
- **Quran fonts** (DECISIONS.md #3–4):
  - Mushaf and Hizb pages render the printed Madani pages with the QPC page fonts (one font per page).
  - Every other Quran text (cards, slider, posts) uses **KFGQPC HAFS Uthmanic Script** with **the King Fahd Complex Hafs text** (not Tanzil Uthmani), so a verse looks the same everywhere.
  - The mockups use Amiri Quran only as a stand-in.
- **UI font:** IBM Plex Sans Arabic (OFL). All fonts are bundled in the app, not loaded from the network.
- **When a pull-quote sits above its verse** (EvidenceItem, slider, دليل اليوم), the verse below it drops one step: `quranInline` instead of `quranBody`.

---

## 4. Colour roles

| Use | Night | Paper |
|---|---|---|
| Background | `night.bg` | `paper.bg` |
| Card | `night.surface` | `paper.card` |
| Card border | none | `paper.line` (hairline) |
| Primary text | `night.text` | `paper.text` |
| Body text | `night.text` | `paper.textBody` |
| Secondary text | `night.textMuted` | `paper.textMuted` |
| Tertiary text (≥14 only) | `night.textFaint` | none |
| Action / link | `night.accent` | `accent.indigo` |
| Primary button | bg `paper.bg`, text `paper.text` | bg `accent.indigo`, text `#FFFFFF` (= `paper.card`) |
| Divider | `night.line` | `paper.line` |
| Quran highlight | `quran.highlight` with text `paper.text` | same |

**Themes** (DECISIONS.md #7):
- The modes are عادي (light), ليلي (dark) and حسب الجهاز (system, the default), chosen in Settings.
- `tokens.json → color.light` and `color.dark` have the same names. Code reads the current palette, never a fixed one.
- Night colours are the same in both modes.
- Text on an `accent.indigo`, `diyaa.green` or `diyaa.pomegranate` fill is `paper.card`. That is white in light mode and dark in dark mode, because the dark fills are light.
- Illustrations keep their own colours.

Colour rules:
- The الضياء colours (`color.diyaa.*`) appear only inside الضياء.
- Live badges use `status.*` only.
- There are no other colours, and no opacity tricks on text.

---

## 5. Shape, borders and sizes

| Item | Token |
|---|---|
| Quran highlight | radius `r8`, padding 2 vertical / 4 horizontal, decoration cloned across lines |
| Chip, ref chip, badge | radius `r12` (pill when height ≤ `size.chip`) |
| Inner box (LangBox, list rows, small cards) | radius `r16` |
| Card, verse card, tile, portal | radius `r24` |
| Sheet top corners | `r24` |
| Circles (dots, number badges, round buttons) | `pill` |
| Card border | `border.hairline`, `paper.line` |
| LangBox start edge | `border.accentEdge` (4), `accent.indigo`, radius `r4` on that side |
| Ayah selection | fill `accent.indigoSelect` + `border.selection` (2) `accent.indigo`, radius `r8` |
| Icon button | `size.touch` circle, icon `iconM`, border hairline `night.line` on night |
| Number badge | `size.badge` (32) circle, `label` text |
| Progress bar | height `size.progress` (4), pill, track `paper.line` or `night.line` |
| Primary / secondary button | height `size.button` (48), radius `r16`, `label` text, side padding `s16` |
| Station nav button | min-height `size.buttonNav` (64), radius `r16`, padding `s12`/`s16` |

---

## 6. Components (exact)

Measurements are listed in this order: padding top / horizontal / bottom, gap, radius, text roles.

**VerseCard**
- Container: `quran.mushafBg`, border hairline `quran.cardBorder`, radius `r24`.
- Spacing: padding `s20` / `s16` / `s16`, gap `s12`.
- Body: `quranBody`, justified. Each verse ends with `﴿n﴾` in `quran.gold`. Consecutive verses flow in one paragraph.
- Highlights: the exact substrings from data.
- Footer: a ref chip, aligned to the start.
  - Background `quran.refChipBg`, text `label` in `quran.brown`.
  - Padding `s4` / `s12`, radius `r12`.
  - Text: `سورة {name} {n}` or `{a}–{b}`.

**EvidenceItem** (the doors)
- Container: `paper.card`, border hairline, radius `r24`, padding `s20` / `s16` / `s16`, gap `s12`.
- Contents in order:
  1. The quote in `quranBody`, colour `accent.indigo`.
  2. A divider hairline `paper.line`.
  3. The full verse in `quranInline` with the quote highlighted.
  4. A ref line in `captionStrong`, `paper.textMuted`.

**SectionHeader** (doors)
- A number badge (`badge`, fill `paper.text`, text `night.text`), then `s12`, then the title in `titleM`.
- Optional sub in `meta`.
- Top margin `s8`.

**PortalCard** (Home)
- Grid of 3 columns with gap `s12`.
- Each card: min-height `portalMin` 112, radius `r24`, padding `s16` / `s12`, gap `s12`, centred.
- Icon: Lucide at `size.iconL` (24).
- Name in `subhead`.
- Selected: `accent.indigo` fill, white text. Others: `paper.card` with border.

**DailyVerseCard «دليل اليوم»**
- `paper.card`, border, radius `r24`, padding `s16` / `s20`, gap `s8`.
- Rows:
  1. Overline `captionStrong` «دليل اليوم» with a door badge (chip, `paper.text` bg, white `label`, height `chip`).
  2. The verse in `quranBody`.
  3. A row with the ref (`meta`) and the action «اقرأ الآية كاملة» (`label`, `accent.indigo`).

**DoorTile**
- `paper.card`, border, radius `r24`, padding `s16`, gap `s8`.
- Contents: number badge + name (`subhead`), the first verse (`quranSmall`, `accent.indigo`), the ref (`caption`).

**Slider** (الذكر)
- A horizontal pager with snap, one item per page, from `dhikr-slider.json`.
- Each page:
  - Door chip (`label` on `night.line` outline).
  - Quote in `quranHero`, `night.text`.
  - Full verse in a `night.surface` box: radius `r24`, padding `s16`, `quranInline`, quote highlighted with the `night.surfaceRaised` fill.
  - Ref plus «n من ١٤» in `meta`.
- Previous and next are icon buttons.

**HizbCard**
- `night.bg` card inside paper: radius `r24`, padding `s20`, gap `s8`.
- Rows:
  - The overline «حزبك» (`captionStrong`) on the same row as «من ٦٠» (`caption`).
  - «الحزب ٢» in `titleL`.
  - The range in `meta`.
  - A progress bar.
  - «وصلت إلى …» in `meta`.
  - A primary button on night: «أكمل الحزب».

**Tile** (الذكر 2 × 2)
- Gap `s12`, min-height `tileMin`, radius `r24`, padding `s16`, gap `s8`.
- Contents: icon `iconL` in `accent.indigo`, name in `subhead`, meta in `meta`.

**LiveCard**
- Width `liveCardWidth` (168), `night.bg`, radius `r24`, padding `s16`, gap `s8`.
- Contents:
  - Badge: either live (`status.*`, `captionStrong`) or a time (outline `night.lineSoft`).
  - Type in `caption`, title in `subhead`, host in `caption` `night.textFaint`, pinned to the bottom.

**TimeChart** «وقتك مع القرآن»
- Card padding `s16`, gap `s12`.
- Two figures in `titleL` with `caption` labels.
- 7 bars with gap `s8`, max height `chartMax`, radius `r8`. Today's bar is `accent.indigo`, the others `accent.indigoTint`.
- Day letters in `caption`.

**Mushaf page**
- Background `quran.mushafBg`.
- A double frame in `quran.gold`: outer `frameOuter`, inner `frameInner`, `s4` between them, radius `r4`.
- Surah header pill: border 1 gold, fill `quran.refChipBg`, `quranInline` `quran.brown`.
- Text: the printed Madani page (15 lines) drawn with that page's QPC font, scaled to the screen width. The page number is in the header.
- States, drawn behind the glyphs: selected ayah (§5), playing (`quran.highlight`), played (text `quran.played`).
- Word tap uses the word boundaries of the page data.

**Hizb page** (printed pages; DECISIONS.md #4)
- Printed Madani pages from the hizb's first page to its last (from `data/hizb-60.json` + the page map of the mushaf source).
- On the first and last page, ayahs outside the hizb use `quran.played`. A «۞ الحزب n» marker sits at the start.
- The header shows «الحزب n من ٦٠» and «{range} · الصفحات a–b». Below it, a progress bar with «صفحة i من N».
- The PlayerBar and the buttons «الحزب السابق» / «الحزب التالي».
- Position (page + ayah) is saved automatically.

**AyahSheet**
- A bottom sheet: `paper.bg`, top radius `r24`, padding `s16`, grab handle `grabWidth` × `progress`, pill.
- Title «الآية n · سورة» in `subhead`, hint in `caption`.
- 5 actions in a row, gap `s8`. Each action: radius `r16`, min-height `buttonNav`, icon `iconM` + `label`.
- The first action is filled `accent.indigo`, the second `accent.indigoTint`, the rest `paper.sunk`.

**PlayerBar**
- Fixed bottom, `night.bg`, padding `s12` / `s16`, height `playerBar` + inset.
- Reciter (`label`), previous / play-pause / next (play is a `button` circle in `paper.bg`), repeat.

**CommunityPost** (Firebase; DECISIONS.md #1–2)
- `paper.card`, border, radius `r24`, padding `s16`, gap `s12`.
- Header row: avatar circle `badge`, the name in `bodyStrong` with the time in `caption`, and the type chip (`label`: آية / سؤال / تأمّل).
- **Verses are attached, never typed.** The user picks the verse from the mushaf (key + optional word range). It renders as a VerseCard at `quranInline` (radius `r16`, padding `s12` / `s16`, gap `s8`).
- **A question** is user text in `body`, plus at least one attached verse.
- **An answer** sits in a `paper.bg` box (radius `r16`, padding `s12`). It has the overline «رد · مع آية» and **must** attach a verse. The send button stays disabled without one.
- **A reflection** is `body` text plus the dashed tag «فهم شخصي، ليس نصاً» (2 dashed `quran.brown`, radius `r12`).
- The rule box at the top of the screen uses `accent.indigoTint`, radius `r16`, title `label` `accent.indigo`, text `meta`.
- The footer is `meta` `paper.textMuted`. Every post has «إبلاغ» (report) in its overflow menu.

**LiveCard: platforms and the player** (Firebase list; DECISIONS.md #1)
- Shape as above, plus a line in `meta`: `[المقدّم] · {platform} · {how}`.
- **YouTube** streams show the button «شاهد هنا» and play in the in-app player at the top of the screen (16:9, radius `r24`).
- **TikTok** streams show «افتح في تيك توك» with the `external-link` icon.
- Upcoming streams show «ذكّرني», which sets a local notification.

**Settings**
- Groups have a `label` title in `paper.textMuted`, then a card (radius `r24`, hairline rows).
- A row has min-height 56, padding `s12` / `s16`, a label in `body` weight 600, an optional sub in `meta`, and a control: a switch (52 × 32 pill), a value with a chevron, or a value only.
- Group notes are in `caption`.
- **Groups, in order:**
  - المظهر: a 3-way segmented control.
  - التذكير: off by default.
  - الموقع والقمر: location switch, city, lunar method.
  - الاستماع: reciter, offline audio.
  - عن التطبيق: who we are, text and audio sources with their licences, version.

**Settings: account group and admin**
- The «حسابي في المجتمع» group:
  - The account (Google email, `meta`), with the note that only Community needs an account.
  - «تسجيل الخروج».
  - «الإدارة», shown to moderator accounts only.
  - «حذف حسابي ومشاركاتي» in `status.danger`.
  - The group note: «لا يجمع التطبيق أي بيانات استخدام ولا تقارير أعطال».

**Admin «الإدارة»** (moderators only)
- Tabs البثوث / البلاغات (n).
- **Add-stream form:**
  - Type segmented control.
  - Fields (label `label`, input height `button`, radius `r16`) for: title, link (the platform chip is detected from the URL), host, and date-time.
  - Timing segmented control: «مباشر الآن» / «موعد لاحق».
  - The primary button «انشر في القائمة».
- Then the current list, with «تعديل» on each row.

**MushafIndex**
- A search field (height `button`, radius `r16`).
- Tabs السور / الأحزاب / الأجزاء: a 3-column segmented control, height `touch`.
- **Hizb grid:** 4 columns with gap `s8`. Each cell shows the number in `subhead` and the start ayah in `caption`. The current hizb is filled `accent.indigo`.
- **Surah rows:** number badge, name in `body` weight 600, «n آية · ص n» in `meta`.

**LangBox** «من اللغة»
- `accent.indigoTint`, with a start edge (§5).
- Padding `s12` / `s16`, gap `s4`.
- Overline `captionStrong` `accent.indigo`.
- Word in `bodyStrong` (+ `quranInline` when the word is Quranic), then the meaning in `body`.

**RuleBox**
- `diyaa.green`, radius `r16`, padding `s16`, gap `s4`.
- Overline `captionStrong` `diyaa.greenOnDark`, sentence in `subhead` `night.text`.

**BulletList**
- Each row: a dot (`dotToday` circle, `quran.gold`) on the first line's centre, gap `s12`, text in `body`. Rows have gap `s4`.

**StationHeader** (الضياء)
- The station tint fills to the screen edges. Padding: inset / `s12` / `s24`, gap `s12`.
- Top row: back button, `captionStrong` «المحطة n من ١١», spacer.
- Progress: 11 segments, gap `s4`, height `progress`, filled `diyaa.green`, empty `diyaa.progressOff`, filling from the start (right).
- Illustration tile: `illustrationTile` (112), radius `r24`, fill `paper.card`. The illustration inside is `illustrationL` (88).
- Title in `titleL`, centred.

**StationNav**
- Two buttons in a row with gap `s12`, each `flex 1`.
- Previous: `paper.card` with border.
- Next: `diyaa.green`.
- Each button has an overline (`caption`: «→ السابق» / «التالي ←») and the station name (`label`).

**Diyaa blocks**, by `type` in `data/diyaa-food.json`:

| type | Spec |
|---|---|
| `verse` | VerseCard |
| `text` | `body` `paper.textBody`. «…» spans use `quranInline` `paper.text`, and `**…**` is `bodyStrong`. |
| `bullets` | BulletList |
| `lang` | LangBox |
| `rule` | RuleBox |
| `heading` | `subhead`, top margin `s8` |
| `trait_cards` | 2-column grid, gap `s12`. Card: radius `r16`, padding `s16` / `s12`, gap `s4`, min-height `traitMin`. Word in `quranWord`, meaning in `meta`, refs in `caption` at the bottom. Fills rotate `tintGreen`, `tintWheat`, `tintSky`, `tintRose`. |
| `animal_pairs` | 2-column grid, gap `s12`. Card: `pairCard`, radius `r16`, padding `s12` / `s8`, gap `s4`, centred. Illustration `illustrationM`, name in `subhead`, «ذكر وأنثى» in `caption`. |
| `compare` | 2 columns, gap `s12`. Card: radius `r16`, padding `s12`, gap `s8`, centred. First card `tintGreen`, second `neutralCard`. Quote in `quranHero`. |
| `contrast` | 2 labels, gap `s12`, radius `r16`, padding `s12`, `bodyStrong`. The first is filled `tintGreen` with text `diyaa.green`. The second has a 2 dashed border in `paper.lineDashed`. |
| `count_bars` | Card `paper.card`, radius `r16`, padding `s16` / `s12`, rows with gap `s12`. Row: illustration `illustrationS`, name `bodyStrong` (width `nameColumn`), bar height `bar`, pill, track `paper.sunk`, fill `diyaa.leaf` (the first row `diyaa.green`), count `subhead` aligned to the end. Then a `caption` note. |
| `steps` | 3 chips with `flex 1` and gap `s8`, separated by «←» in `caption`. Chip: radius `r12`, padding `s8`, `label`. The greens darken step by step. |
| `word_count` | Sentence in `body`, then 4 mini cards with gap `s8`. Card: radius `r16`, padding `s12` / `s4`, `label` + `caption`. Three are `accent.indigoTint`, one is `diyaa.honey`. |
| `numbered_list` | Box `tintRose`, radius `r24`, padding `s16`, gap `s8`. Title in `subhead` `diyaa.pomegranate`. Rows: `paper.card`, radius `r16`, padding `s12` / `s16`, a badge filled `diyaa.pomegranate`, name in `subhead`. |
| `closing_quote` | Box `tintGreen`, radius `r16`, padding `s16`, `quranHero` `diyaa.green`, centred. |

Where a mockup differs from this table, the table is correct.

---

## 7. Icons

- **The only UI icon set is Lucide** (ISC licence). For Compose Multiplatform use `com.composables:icons-lucide`.
- Rules:
  - Size `iconL` (24) by default, `iconM` (20) in dense rows and sheets.
  - Stroke 1.75, round caps and joins, colour from the text role it sits next to.
  - Mirror directional icons in RTL (chevrons, arrows, skip).
  - Never use filled variants, except `bookmark-check` for the saved state.
- The hand-drawn icons in the mockups are placeholders. Do not export them.

| Place | Meaning | Lucide |
|---|---|---|
| Home | profile | `user-round` |
| Home portals | الذكر / الضياء / الفرقان | `book-open` / `wheat` / `scale` |
| Global | back | `chevron-right` |
| Pager | previous / next | `chevron-right` / `chevron-left` |
| الذكر | settings; reminders inside | `settings`; `bell` |
| الذكر tiles | المصحف والاستماع / الأبواب / المجتمع / المحفوظات | `book-open-text` / `door-open` / `users-round` / `bookmark` |
| Mushaf | index | `list` |
| AyahSheet | استمع للآية / استمع من هنا / احفظ / شارك / انسخ | `play` / `list-start` / `bookmark` / `share-2` / `copy` |
| Word | استمع للكلمة | `volume-2` |
| PlayerBar | prev / play / pause / next / repeat / reciter | `skip-forward`* / `play` / `pause` / `skip-back`* / `repeat-1` / `mic` (*mirrored for RTL) |
| Live | live | `radio` |
| Community | شارك آية / اسأل / تأمّل | `quote` / `message-circle-question` / `lightbulb` |
| Moon | rise / set / direction | `sunrise` / `sunset` / `compass` |

**Illustrations** (`illustrations/*.svg`, الضياء only):
- 64 grid, flat fills, no outlines, a soft ground shadow, facing left.
- Colours from `color.diyaa` only.
- Shown at `illustrationS` (40), `illustrationM` (56) or `illustrationL` (88), never at other sizes.
- Never draw a pig, or anything described as forbidden.

---

## 8. Copy rules

- Use «» only around Quran words. Use no exclamation marks and no emoji.
- **Separators:** use «،» between parts of a meta line. Never use «·» next to Arabic-Indic digits, because it reads as ٠ (zero).
- Placeholders stay visible until real data exists: `[اسم المستخدم]`, `[المقدّم]`.
- **Button labels:** a verb, at most 3 words (افتح الباب، أكمل الحزب، انضم الآن).
- **Empty states:** say what will appear and how. For example: «لا محفوظات بعد. اضغط على أي آية ثم احفظ».

## 9. Accessibility

- Contrast meets WCAG AA at the sizes allowed above. `night.textFaint` and `paper.textMuted` are never used below 12.
- Every icon-only button has an Arabic content description (see the aria-labels in `mockups/`).
- The ﴿n﴾ ornament is read as «آية n». The highlight is announced as «مظلّل».
- Text scales with the system font size. Layouts must not clip at 130 %.
- Reduced motion turns off the pager animation and the sheet slide.


## 10. v4 components (exact)

**PlayerBar v2** (mini, fixed at the bottom on `night.bg`)
- Top: a progress line of height `progress`, `status.liveBg` on `night.line`.
- Then a row with padding `s12` / `s16` / `s24` and gap `s8`:
  - the title (`subhead`) and the reciter (`caption`, `night.textMuted`), which tap to open the PlayerSheet;
  - previous ayah: icon button `touch`;
  - play/pause: a 56 circle filled `status.liveBg`, glyph `status.liveText` at 24;
  - next ayah: icon button `touch`;
  - expand: chevron `iconM`.

**PlayerSheet** (full screen, `night.bg`)
- Close chevron, title `titleL`, sub `meta`.
- Slider: track `progress`, thumb 20.
- Controls: 56 previous and next on `night.surface`, and an **88** play/pause in `status.liveBg`.
- Reciter row on `night.surface`, radius `r24`. With one reciter it shows the name and «القارئ المتاح الآن». With more it becomes a picker.
- Speed: 4 segmented chips. Repeat: 3 segmented chips + a stepper (44 round buttons) + from/to fields, enabled only for «مقطع».

**AyahSheet v2**
- 5 tiles in a 5-column grid with gap `s8`. Each tile: min-height `buttonNav`, radius `r16`, icon `iconM` over a one-word `caption` label at weight 700.
- The first tile is filled `accent.indigo`; the others are `paper.sunk`.
- The sheet height is fixed by its content. The page scrolls so the selected ayah stays above the sheet.

**Reading header**
- **Mushaf:** paper (`quran.mushafBg`). Overline «قراءتك في المصحف» (`captionStrong`, indigo), then «surah، صفحة n» (`subhead`), then «الجزء، الحزب» (`caption`). Buttons: back, full-screen, index.
- **Hizb:** `night.bg`. Overline «قراءة حزبك» (`captionStrong`, `status.liveBg`), then «الحزب n من ٦٠», then a gold progress bar with the range and page count.

**Saved-page ribbon:** 20 × 32, `quran.gold`, notched bottom, at the top start corner of the page.

**Grade badge:** pill height `chip`, `accent.indigoTint`. 5 dots of 4 (filled `accent.indigo`, empty `paper.lineDashed`), gap 2, then the name in `captionStrong`.

**Thread:** depths 0–2 are indented 16 per level with a 2 start border in `paper.line`. From depth 3 on, items stay at depth 2 with the overline «ردّاً على [اسم]» (`captionStrong`, indigo).

**Best answer:** a 2 border in `diyaa.green` on the reply card, plus the badge (check icon and «أفضل رد») on `diyaa.tintGreen` and the label «اختاره السائل / المشرف» in `caption`. This is the one allowed use of the green outside الضياء.

**Item state banners** (author only):
- Pending: `paper.sunk` with the clock icon.
- Rejected: `diyaa.tintRose` with `status.danger` and the reasons in `meta`.
- Accepted: a check icon in `diyaa.green`.

**LiveCard v3** (TikTok)
- The badge is one of: «مباشر الآن» (`status.*`), the next occurrence (`paper.sunk`), or «أُلغي هذه المرة» (`diyaa.tintRose` / `status.danger`).
- Schedule in `meta`.
- The action is one of:
  - «افتح في تيك توك» (when live);
  - «ذكّرني بكل موعد» (outline indigo);
  - when reminded, «سنذكّرك كل أسبوع» and «أوقف التذكير».

**VideoCard:** a 128 × 72 thumbnail (radius `r12`) with the duration chip, then the title (`bodyStrong`) and type/host (`meta`). Playing state: fill `accent.indigoSelect`.

**Consent prompt:** title `titleM`, body `body`, link, two **secondary** buttons of equal weight side by side, and a `caption` note.

**Update banner:** an `accent.indigo` strip, radius `r16`, the download icon, «تحديث متوفر» with the version, and the button «التفاصيل» on `paper.card`.

**Disabled portal («قريباً»):** `paper.sunk` with a dashed `paper.lineDashed` border, muted icon and text, the chip «قريباً», and `aria-disabled`.

**Unknown value:** the text «لا نعرف» in `night.textFaint` or `paper.textMuted`, always next to a way to fix it («حدّد مدينتك»).
