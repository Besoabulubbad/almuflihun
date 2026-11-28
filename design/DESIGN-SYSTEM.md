# Design system: مجتمع القرآن (v7)

This file and `tokens.json` are the only source of truth for how the app looks. They replace every older design file (v1–v6).

**Structure (v7):** four tabs: **الذكر** (first), **المجتمع**, **القرآن**, **الإعدادات**. A mini player sits above the tab bar on every tab. Outside the app: home-screen widgets, the notification player, the lock screen and notifications (§10).

**Rules for Claude Code**
1. **No raw values in screens.** Every colour, size, space, radius, font and duration comes from `tokens.json` through `code/AppTokens.kt`. A hex code, `.dp`, `.sp` or millisecond literal in a screen file is a bug.
2. **Screens only compose components.** Screens use the components in §6 and never restyle them. If a screen needs something new, add a component or a variant here first.
3. **Change the file, not the code.** To change a value, edit `tokens.json`, regenerate `AppTokens.kt`, and update this file. Never patch a value in one place.
4. **When in doubt, do less.** No shadows, no gradients, no decoration. One accent colour. The Quran text is the strongest thing on any screen.

---

## 1. Principles

| Principle | What it means on screen |
|---|---|
| The Quran first | Quran text uses the Quran fonts and nothing else does. It is never smaller than 17 sp outside the Mushaf. |
| Full verses, never cut | A verse is always shown whole. A quote inside it is marked with `accent` colour, never cut out. |
| Honest levels | «من النص», «من اللغة», «تأمّل» are labelled. Reflections carry «فهم شخصي، ليس نصًا». |
| Honest data | Unknown values show «لا نعرف». Sample data shows «بيانات مثال». |
| Simple surface, deep layers | Each screen shows what most people need. Options for deeper study sit one level down (a sheet or a link), never on the first screen. |
| One look | Paper, white cards, hairlines and one blue accent, everywhere except the Moon page. |
| The text as written | Search shows how each word is written in the Mushaf (spelling forms), not only where it is. |
| Built to grow | New content goes into existing patterns (a row, a section, a door) instead of new screens (§8). |

---

## 2. Colour

Two themes: `light` (عادي, the default) and `dark` (ليلي). «حسب الجهاز» follows the system. The **Moon page is always dark**.

Every text pair below passes WCAG AA (4.5:1 for text, 3:1 for icons), checked with the WCAG formula.

| Token | Light | Dark | Use | Contrast (light / dark) |
|---|---|---|---|---|
| `bg` | #F5F2EC | #121417 | Screen background | — |
| `surface` | #FFFFFF | #1C1F24 | Cards, sheets, search field | — |
| `surfaceSunk` | #ECE8E0 | #23272D | Segmented track, attached verse, thumbnails | — |
| `mushafBg` | #FBF8F1 | #15161A | Reader page | — |
| `line` | #E4DFD5 | #2A2E35 | Hairlines between rows, card borders | — |
| `lineStrong` | #D9D3C7 | #3A3D44 | Field and chip borders, grab handle | — |
| `text` | #1D1C1A | #ECE8E0 | All main text, Quran text | 15.2 / 15.1 on bg |
| `textMuted` | #57534C | #A9A49A | Subtitles, refs, counts | 6.8 / 7.4 on bg |
| `textFaint` | #6B665D | #959086 | Placeholders, «بيانات مثال» | 5.1 / 5.8 on bg |
| `iconQuiet` | #8A857B | #7F7A71 | Chevrons and inactive icons only. **Never text.** | 3.3 / 4.3 on bg |
| `accent` | #2F4A7A | #9DB4DE | The one accent: continue card, quoted words, links, active states | 7.9 / 8.8 on bg |
| `accentTint` | #E3E8F2 | #24324A | Icon circles, tags, badges | accent on it 7.2 / 6.1 |
| `onAccent` | #FFFFFF | #121417 | Text on `accent` | 8.8 / 8.8 |
| `onAccentMuted` | #C9D4EA | #24324A | Secondary text on `accent` | 5.9 / 6.1 |
| `selection` | #F3E6BF | #4A3D1A | Selected ayah in the reader | text on it 13.7 / 8.7 |
| `reflectTint` / `onReflectTint` | #F3E6BF / #6B4E0F | #3D3218 / #E8CF8F | «تأمّل» tag | 6.2 / 8.2 |
| `noteBg` / `noteText` | #FFF7E6 / #7A5212 | #2E2716 / #E8CF8F | Private notes «تأمّلي (خاص بي)» | 6.5 / 9.7 |
| `danger` | #A3332B | #F08A80 | Delete account, errors | 6.8 / 6.8 on surface |
| `live` | #C2410C | #FB8A5C | The «مباشر الآن» dot only | 5.2 / 7.0 |
| `inverse` / `onInverse` | #1D1C1A / #F5F2EC | #ECE8E0 / #121417 | Player bar, primary buttons, toasts, tooltips | 15.2 / 15.1 |
| `onInverseMuted` | #B5B0A6 | #57534C | Reciter name and inactive icons in the player | 7.9 / 6.3 |
| `inverseTrack` | #4A4740 | #B5B0A6 | Player progress track | — |
| `scrim` | #1D1C1A at 40% | #000000 at 60% | Behind sheets | — |
| `formAlt` | #8A4B0F | #E8A86B | The second spelling form of a word in search results, always with a wavy underline and a label | 6.8 / 8.1 on surface |
| `formAltTint` | #F6E9D8 | #3A2A18 | Label behind «بالألف الصغيرة» etc. | formAlt on it 5.7 / 6.7 |
| `accentSoft` | #9DB4DE | #3A4D6E | WeekStrip «started» days | (non-text) |
| `accentRing` | #4A6496 | #3A4D6E | The track of the تلاوة ring on `accent` | (non-text) |

**Moon page:** `color.dark` plus `moon.lit` #F1E9D2 and `moon.shadow` #2B2A27. Tonight is marked with dark `accent`.

**Rules**
- One accent. Use `accent` for at most one filled element per screen (the تلاوة card, the continue card, the live banner, or the «استمع» action). Everything else is outline, tint or text.
- Status colours (`live`, `danger`) are never used for decoration.
- Pure black and pure white text are not used; long reading glare is lower with `text` on `bg`.

---

## 3. Type

**Fonts (all bundled, no network):**
- UI: **IBM Plex Sans Arabic**, weights 400 and 500 only.
- Verses outside the Mushaf: **KFGQPC HAFS Uthmanic Script** with the KFGQPC text.
- Mushaf pages: **QPC page fonts (604)**, fixed layout, pinch to zoom.

| Role | Size (sp) | Weight | Line height | Use |
|---|---|---|---|---|
| `titleL` | 22 | 500 | 1.4 | Onboarding titles, the Moon night line |
| `title` | 18 | 500 | 1.4 | Screen and sheet titles |
| `subhead` | 15 | 500 | 1.5 | Row and card titles, section headers |
| `body` | 15 | 400 | 1.6 | Post text, door summaries |
| `bodyS` | 14 | 400 | 1.6 | Secondary paragraphs |
| `label` | 13 | 500 | 1.4 | Buttons, chips, segmented controls |
| `meta` | 12 | 400 | 1.5 | Refs, counts, times, row subtitles |
| `caption` | 11 | 400 | 1.4 | Non-essential text only («بيانات مثال», video durations) |
| `quranHero` | 20 | — | 2.15 | تذكرة |
| `quranCard` | 18 | — | 2.15 | Search results, doors, collections, stations |
| `quranAttach` | 17 | — | 2.15 | Verses attached to posts |
| `verseNumber` | 13 | 400 | — | ﴿١٥٢﴾ markers in cards |

**Rules**
- Text scales with the system font size up to 200%. Layouts must wrap, never clip. Test at 100%, 130% and 200%.
- Quran text is never bold, never italic, never coloured except `accent` for a quoted part and `textMuted` for ayahs outside the current range.
- Quran card text is justified with the last line centred.
- Digits are Arabic-Indic (٠١٢٣…). Separators are «،». Refs read «البقرة ١٥٢» in lists and «٢:١٥٢» in search.
- A root or lemma is «من اللغة»: it is set in the UI font, never the Quran font.

---

## 4. Space, size, radius

**Space scale:** 0, 2, 4, 8, 12, 16, 20, 24, 32, 40. Nothing else.

| Layout token | Value |
|---|---|
| `screenPadding` | 16 |
| `cardPadding` | 12 |
| `cardGap` | 8 |
| `sectionGap` | 16 |
| `rowPaddingV` / `rowPaddingH` | 12 / 12 |
| `sheetPadding` | 16 |

**Radius:** `s` 8 (thumbnails), `m` 12 (toasts, notes, attached verses), `l` 16 (cards, player bar, buttons in grids), `xl` 24 (sheet top corners), `pill` (search field, chips, buttons, badges).

**Sizes**
- Every tappable thing is at least **48 × 48** (`touchMin`), even when it looks smaller.
- Rows: 48 minimum; settings rows 56.
- Top bar 56. Tab bar 64. Mini player 48. Search field 48. Buttons 44 visible, 48 touch. Chips 32 visible, 48 touch.
- Icons: 16 (inside chips), 20 (default), 24 (max).
- Mini player play button 32. تلاوة ring 56. Avatars 32 and 44. Video thumbnails 80 × 45. Notification artwork 56.
- Progress lines 3. Switch 44 × 26.

**No shadows.** Separation is `surface` on `bg` plus a `line` hairline. Sheets sit over `scrim`.

---

## 5. Motion and haptics

| Token | ms | Use |
|---|---|---|
| `fast` | 120 | Press states, chip toggles, cross-fades |
| `standard` | 200 | Most transitions |
| `sheet` | 280 | Sheets in and out |
| `emphasised` | 320 | الذكر moon row → Moon page (shared element) |

Easing: `standard` (0.2, 0, 0, 1) for most; `decelerate` for things entering; `accelerate` for things leaving.

- **Reduced motion:** every movement becomes a `fast` cross-fade.
- **Mushaf page turn:** horizontal drag, no 3D curl.
- **Haptics:** light on save and toggle; confirm on post sent and hizb finished; none on scroll and page turn.

---

## 6. Components

Each component lists its tokens. States: default, pressed (`surfaceSunk` overlay at `fast`), focused (2 dp `accent` ring), disabled (avoid; prefer explaining why).

**TopBar.** Height `topBar`. Back icon (mirrored in RTL), `title`, up to two icon buttons on the far side. Background `bg`. No divider.

**SearchField.** Height `searchField`, `pill`, `surface`, `lineStrong` border; active border 1.5 `accent`. Leading search icon, text `body`, placeholder `textFaint`, clear button when filled. On الذكر: «ابحث في القرآن», no options icon. On القرآن: «آية، عبارة، كلمة، سورة، ٢:٢١٩» with the options icon (opens «خيارات البحث»). المجتمع has its own post search behind an icon; الإعدادات has none.

**SearchSuggestion row.** Icon, phrase in `subhead`, count in `meta` («٨٩ آية»). Recent searches use the clock icon and are stored on the device only.

**ContinueCard** (القرآن tab). The one `accent`-filled card on القرآن. Overline «تابع القراءة» (`meta`, `onAccentMuted`), surah (`title`, `onAccent`), «الصفحة n ، الحزب n» (`meta`). A white circle with the book icon.

**MoonRow.** Card with a `moonRow` moon drawn at today's phase, «القمر: phase» in `subhead`, «مضيء n٪ ، عمره n يومًا ، المحاق بعد n ليلة» in `meta`, chevron.

**ScopeTile.** The tile used by SectionTiles. Icon `iconM` in `accent`, name `subhead`, one line `meta`. A scope that is not ready uses a dashed `lineStrong` border and «قريبًا».

**Card.** `surface`, `line` hairline, radius `l`, padding `cardPadding`, gap `cardGap`.

**ListRow.** Min height `rowMin`. Optional leading icon in `accent`, title `subhead`, optional subtitle `meta`, trailing: chevron (`iconQuiet`), value (`meta`), badge, switch or icon button. Rows inside one card are split by `line`.

**SectionHeader.** `subhead` title on the start side, optional link («الكل») in `label` `accent` on the end side.

**VerseCard.** The single way to show a verse outside the Mushaf.
- Ref in `meta` (e.g. «إبراهيم ٧»), optional play button (`iconS`, `accent`), optional drag handle.
- The full verse in `quranCard` (or `quranHero` / `quranAttach`).
- `highlight`: a list of word ranges `[start, end]` (1-based, KFGQPC word order). Highlighted words use `accent` colour only, no background.
- Tap opens the AyahSheet. Long verses are shown in full; the list scrolls.

**AyahSheet.** Sheet, radius `xl` top, `surface`.
- Title «البقرة ، الآية ١٥٢» in `subhead`; the doors the ayah is in, in `meta` («في بابين: ذكر الله ، الشكر»).
- **Five actions** in one row, each a 56-high tile, radius `m`, icon `iconM` over `caption`/`label`: استمع (the only filled tile, `accent`), من هنا, احفظ, شارك, انسخ.
- A link row under a hairline: «اسأل عنها في المجتمع» (`label`, `accent`).
- The page scrolls so the selected ayah stays above the sheet.

**WordTooltip.** `inverse` pill above the tapped word: «استمع للكلمة» | «ابحث بجذرها». The word gets an `accent` background with `onAccent` text. Dismiss on tap outside.

**TabBar.** Height `tabBar`, `surface`, `line` on top. Four items, in this order from the start side: الذكر (moon icon), المجتمع (message), القرآن (book), الإعدادات (user). Each: icon `iconM` over `caption`. Active: `accent` icon and label, a `tabIndicatorW × tabIndicatorH` pill of `accentTint` behind the icon. Tapping the active tab scrolls to top, then pops to the tab root. Each tab keeps its own back stack. Hidden in full-screen reading, the video player, sheets and onboarding.

**MiniPlayer.** Height `miniPlayer`, `inverse`, radius `l`, margin 8, sits directly above the TabBar (or at the bottom in full-screen reading). One row: «surah n» (`label`, `onInverse`) on the start side, then repeat (off → ayah → surah), previous ayah, play/pause (`miniPlayButton` circle, `onInverse` fill), next ayah, speed (٠٫٧٥ → ١ → ١٫٢٥ → ١٫٥). Nothing else: the reciter is changed in Settings. Hidden until something has been played, and while a video plays. Media icons are not mirrored in RTL.

**TilawaCard** (الذكر). The one `accent`-filled card on الذكر. Overline «تلاوة اليوم» and the reminder time with a bell (`onAccentMuted`). A ProgressRing (`tilawaRing`, track `accentRing`, fill `onAccent`, «٣/١٠» in the centre) beside the portion name («الحزب ٣», `title`), «البقرة ، الصفحات ٢٢–٣١» and «قرأت ٣ صفحات من ١٠» (`meta`). A full-width white pill: «أكمل من الصفحة n» (`label`, `accent`).

**WeekStrip.** Seven bars of 6 dp, radius pill, gap 6, letters under them (ح ن ث ر خ ج س from the start side). Finished = `accent`, started = `accentSoft`, none = `line`. Title «هذا الأسبوع» and «n أيام من ٧ ، time» (`meta`). **No streak numbers, no badges.**

**SectionTiles** (الذكر). Three tiles in a row: ادرس، الضياء، الفرقان. Same as ScopeTile; a section not ready is dashed with «قريبًا». Driven by data (§8).

**CommunitySegments.** A row of chips under the المجتمع title: النقاش، البثوث، الفيديوهات، المشرفون. One is on. Swiping the content moves between them.

**FAB** («اسأل أو شارك»). `inverse` pill, height `fab`, on the end side above the MiniPlayer, only on النقاش. Opens Compose.

**SignInSheet.** Shown only when a signed-out user acts (ask, answer, send, message moderators). Icon circle, «سجّل دخولك لتشارك», one line on why, «المتابعة بحساب Google» (primary), «ليس الآن». Returns the user to the exact place after sign-in.

**ModeratorInbox** (المشرفون). Segmented سؤال / بلاغ / اقتراح, a message field with «أرفق آية», «أرسل»; then «رسائلك» rows with a status tag (`accentTint` «تم الرد», `reflectTint` «قيد المراجعة»).

**SpellingForms** (search results). Under the count: «كيف كُتبت في المصحف» and one card per written form, each showing the form in `quranCard` at 22 sp and «description ، count». The primary form uses `accent`; every other form uses `formAlt` + wavy underline (1 dp, offset 8). The selected form card has a 1.5 `accent` border; tapping filters the results. In results, the matched word takes its form's mark, and an alternate form adds a label («بالألف الصغيرة») on `formAltTint` next to the ref. Shown only when a search has more than one form.

**LiveBanner.** `accent` card at the top of المجتمع › النقاش, only when a live is on now or starts within 24 hours. Shows: «مباشر الليلة ، على تيك توك», title, «مع host ، audience», time. Action «ذكّرني» (white pill) or «افتح في تيك توك» when live.

**LiveRow.** Title `subhead`, host `meta`, then an audience tag (`accentTint`) and the time. Trailing bell («ذكّرني»), `accent` when set.

**VideoRow.** Thumbnail `thumbW × thumbH`, radius `s`, duration badge (`caption` on `inverse`); title `subhead`; type `meta`.

**VideoPlayer.** Official YouTube player, 16:9, radius `l`. Under it: title, «افتح الآية», «في يوتيوب» (fallback). The Quran player pauses and hides while a video plays.

**Chip / FilterChips.** Height `chip`, `pill`, `label`. Off: `surface` + `lineStrong`. On: `inverse` + `onInverse`. A row of filter chips scrolls horizontally.

**SegmentedControl.** `surfaceSunk` track, `pill`; the selected segment is `surface` with `text`; others `textMuted`.

**Buttons.** Primary: `inverse` fill, `onInverse`, `pill`, `label`. Secondary: `surface`, `lineStrong` border. Text button: `accent`. One primary button per screen.

**Tag / Badge.** `accentTint` + `accent`, `pill`, `caption`/`meta`. «تأمّل» uses `reflectTint` + `onReflectTint`.

**PostCard.** Name (`subhead`), type tag, ⋯ menu (report). Text `body`. Attached verse: a VerseCard on `surfaceSunk`, radius `m`, `quranAttach`. Reflections end with «فهم شخصي، ليس نصًا» (`caption`).

**NoteBox.** «تأمّلي (خاص بي)» on `noteBg` with `noteText`, radius `m`. Never shared or published.

**RuleBox** (الضياء). `surface`, a 3 dp `accent` line on the start side, **no rounded corners**, overline in `accent`.

**Toast.** `inverse`, radius `m`, icon + text + optional action in `onAccentMuted`. Above the player bar. 4 seconds.

**Switch.** `switchW × switchH`; off `lineStrong`, on `accent`.

**HizbProgress.** A 3 dp line under the reader top bar: `line` track, `accent` fill. Only when reading by hizb.

**OnboardingStep.** Icon circle (64, `accentTint`), `titleL`, `bodyS` `textMuted`, progress dots (active 24 wide, `accent`), «n من ٤», actions at the bottom. «تخطَّ» on steps 2–4.

**EmptyState.** An invitation, not an apology: title `subhead`, one line `bodyS`, one button.

---

## 7. Patterns

- **The ayah is the hub.** Anywhere a verse appears (reader, search, doors, collections, posts, تذكرة), tapping it opens the same AyahSheet.
- **Spelling forms.** Matching runs on a normalised copy (§ Search) where the small alif becomes ا. Grouping uses the KFGQPC Uthmani word with harakat and Quranic marks removed but letters and the small alif (U+0670) kept. Forms are named from a fixed list: بالألف / بالألف الصغيرة / بالتاء المفتوحة (رحمت) / بالواو (الصلوٰة) / other. Example: «عباد» = 12 places: عِبَادُ ×11, عِبَٰدُ ×1 (الزخرف ١٩).
- **Search.** One field. The default search is **forgiving**: it ignores tashkeel and tatweel, treats أ إ آ ٱ as ا, ى as ي, ة as ه, ؤ as و, ئ as ي, and matches phrases across word boundaries. It runs on the Tanzil simple text, normalised in a copy, never edited. «يا ايها الذين امنوا» must return 89 ayahs in 20 surahs. Results: count, surah chips, then VerseCards grouped by surah in Mushaf order, matched words highlighted. **Options one level down** («خيارات البحث» sheet): type (عبارة default / كلمة كاملة / جذر / مطابق), range (كل القرآن / سورة / جزء / حزب), extra words (مع / أو / بدون).
- **Going back.** After any jump into the reader (from search, تذكرة, a door, a video), the back button returns to where the user came from, and the last reading position is never overwritten by a jump.
- **Navigation.** Four tabs with their own back stacks. Deep links: `/dhikr`, `/moon`, `/community/{segment}`, `/live/{id}`, `/quran`, `/quran/page/{n}`, `/quran/ayah/{s}:{a}`, `/search?q=`, `/settings/tilawa`. Widgets and notifications only use these.
- **تلاوة (daily reading).** Default one hizb a day, in order 1 → 60 (60 days for the whole Quran); options ربع حزب / نصف حزب / حزب / جزء. A page counts as read after 20 s on it or when its last ayah is played. Finishing the portion shows «أتممت تلاوة اليوم». Reading past it counts toward tomorrow. Progress is on the device and syncs when signed in.
- **Lives.** Each live has: title, host, audience (للجميع / للمبتدئين / للأخوات / للأطفال), topic (a verse range, a door or free text), type (تلاوة / أسئلة / تدبّر), time, repeat rule, platform link. TikTok lives open TikTok (browser fallback).
- **Saving.** «احفظ» opens the add-to sheet with checkmarks; the toast confirms and offers «غيّر». Collections live on the device and sync when signed in.
- **Unknown and sample data.** «لا نعرف» for missing location-based values. «بيانات مثال» on any sample content.

---

## 8. Built to grow (cheap to extend)

The structure is data-driven so new content costs little.

| What grows | How it is built | Adding one costs |
|---|---|---|
| Section tiles in الذكر (ادرس، الضياء، الفرقان…) | A list of `Section { id, title, subtitle, icon, enabled }` | One entry and its screen |
| Cards on الذكر | A list of `DhikrCard` (tilawa, week, moon, tadhkira, tiles), ordered by remote config | One entry; reorder without a release |
| المجتمع segments | A list of `CommunitySegment` (النقاش، البثوث، الفيديوهات، المشرفون) | One entry |
| Widgets | Each widget reads the same repositories as its screen and opens one deep link | One widget class |
| ادرس | A list of `StudyEntry { type: door | tool, … }`. Today: doors only | A door = JSON in `abwab` using the one door template; a tool = one entry + its screen |
| Doors | One `DoorTemplate` renders sections of VerseCards from JSON | Content only, no code |
| الضياء stations | Blocks from JSON (verse, note, rule, heading, trait cards) | Content only |
| Lives and videos | Firestore documents edited in «الإدارة» | No code |
| Text | All UI strings in one Arabic strings file | No code |

**Feature flags** (remote config): each scope, each hub section, the live banner, and any test variant. Off means hidden, not broken.

**Tabs:** the four tabs are fixed. Anything new goes inside a tab: a section tile in الذكر, a segment in المجتمع, a tool in القرآن («خيارات البحث» or a row), a row in الإعدادات.

**Experiment slots:** four places are built so a variant can be swapped by flag: the الذكر card order, the AyahSheet action order, the search results layout (grouped vs flat), and the المجتمع default segment. See `UX-STRATEGY.md`.

---

## 9. Accessibility checklist (every screen)

- Contrast from §2 only; no new colour pairs without checking 4.5:1.
- 48 × 48 touch targets.
- Font scaling to 200% without clipping.
- Every icon button has a spoken label in Arabic (TalkBack and VoiceOver). Spelling forms are spoken with their label («عباد، بالألف الصغيرة»).
- Widgets and notifications have the same labels.
- Verses are read as one block with their ref; the Quran text is not split into words for screen readers except in the WordTooltip.
- RTL: back and chevrons mirror; media controls do not.
- Reduced motion respected.
- Nothing is shown by colour alone (live has the word «مباشر الآن», selected chips also change fill).

---

## 10. Outside the app

### Home-screen widgets
Android with Jetpack Glance, iOS with WidgetKit. Radius 24, padding 12, `surface` + `line` (dark: `color.dark`). No logos. Each opens one deep link.

| Widget | Android | iOS | Shows | Opens |
|---|---|---|---|---|
| Quick bar | 4×1 | Lock screen + Control Center button | «ابحث في القرآن» field and a play/pause for the last recitation | `/search` with the keyboard open; play toggles without opening the app |
| تلاوة | 2×2 | Small, and a lock-screen ring | ProgressRing (64), «تلاوة اليوم», the portion | `/quran/page/{n}` at the right page |
| Moon | 2×2 | Small | Tonight's moon (always `color.dark`), «الليلة n», lit % | `/moon` |
| تذكرة | 4×2 | Medium | A short full verse (only verses that fit in full), its ref; changes daily | `/quran/ayah/{s}:{a}` |
| المجتمع | 4×2 | Medium | Next live (time, title), new answers to your posts, «اسأل» and «المجتمع» | `/community/discussion`, compose (sign-in if needed) |

Widgets refresh at most every 30 minutes, at midnight (تلاوة, تذكرة, moon) and when the app writes new data.

### Notification player
- Android: Media3 `MediaSession` with the system media notification.
- iOS: `MPNowPlayingInfoCenter` and `MPRemoteCommandCenter`.

| Field | Value |
|---|---|
| Small icon | A simple book outline (monochrome), not a logo |
| Title | «البقرة ، الآية ١٥٣» |
| Subtitle | The reciter |
| Artwork | 56 dp (and 512 px for the system): `mushafBg` square, the surah name in the Quran font, the ayah number in `meta` |
| Collapsed actions | Previous ayah, play/pause, next ayah |
| Expanded actions | + repeat (custom action, shows its state) and speed (custom action, shows «١×») |
| Progress | The current ayah's position |
| Headphones, car, lock screen | Next and previous = ayah |

The notification is dismissible only when paused, and disappears when playback stops.

### Notifications
Five channels. All except playback are **off until the user turns them on at the moment they matter** (DECISIONS #29), never on first launch.

| Channel | When | Text | Action |
|---|---|---|---|
| `tilawa_reminder` | At the chosen time, only if today's portion is unfinished; max once a day | «تلاوة اليوم» / «الحزب ٤ ينتظرك ، ١٠ صفحات.» | «اقرأ الآن», «بعد ساعة» |
| `live_reminder` | 15 min before a live the user set «ذكّرني» on | «البث يبدأ بعد ١٥ دقيقة» / title, host, platform | «افتح في تيك توك» |
| `answers` | Someone answers the user's post | «جواب جديد على سؤالك» / quote, who | «اعرض» |
| `moderator_replies` | Moderators reply in المشرفون | «ردّ المشرفين» / the subject | «اقرأ الرد» |
| `playback` | While playing | The notification player | — |

Notification copy states facts and offers one action. No preaching, no guilt, no exclamation marks.

---

## 11. Identity: the mark, the app icon, the splash

**No branding inside the app.** The mark appears only where the phone requires it: the app icon, the splash screen, the notification small icon, the stores, and «عن التطبيق». Screens never show a logo.

**The mark is the letter ق**, the first letter of القرآن, built from simple geometry:
- a round head;
- a deep bowl that curves to the left;
- two dots;
- a round-cap stroke of 7 on a 108 grid.

The master is `brand/svg/mark.svg`. Its colour comes from `currentColor`.

**Rules**
- **No sacred text in any icon:** no Quran words, no «الله», no mosque or Kaaba shapes. Icons get moved, deleted and shown everywhere on the phone, so sacred words don't belong there. The letter ق on its own is a letter, not text.
- **Clear space:** at least the height of the dots (8 on the 108 grid) on every side. The mark stays inside the 66-wide safe circle of Android adaptive icons.
- **Minimum size:** 16 px. At 16 and 24 px use the same drawing; never thin the stroke.
- **Colours:** paper `#F5F2EC` on `accent` (the app icon), `accent` on paper (splash, light), dark `accent` `#9DB4DE` on dark `bg` (splash and iOS icon, dark), and white on transparent (notification). No other colour pairs, no gradients, no shadows, no outlines.
- **Wordmark:** «مجتمع القرآن» in IBM Plex Sans Arabic 500, set to the start of the mark (RTL: the mark on the right). Use it only in the stores and «عن التطبيق». The mark and the words are never redrawn, stretched or rotated.

| Asset | Spec | File |
|---|---|---|
| Android adaptive icon | Background `#2F4A7A`; foreground = paper mark; monochrome layer for themed icons | `brand/android/res/mipmap-anydpi-v26/ic_launcher.xml`, `drawable/ic_launcher_foreground.xml`, `drawable/ic_launcher_monochrome.xml`, `values/brand_colors.xml` |
| Android splash (12+) | Background `bg` (light `#F5F2EC`, night `#121417`); icon = the mark in `accent` (night `#9DB4DE`); no text, no branding image; no animation | `values/splash.xml`, `drawable/splash_mark.xml`, `values-night/brand_colors.xml` |
| Notification small icon | White mark, 24 dp, transparent; accent colour `#2F4A7A` | `drawable/ic_stat_notify.xml` |
| iOS app icon | 1024 px: light (paper on accent), dark (`#9DB4DE` on `#121417`), tinted (white on black, the system tints it) | `brand/png/ios-1024*.png`, `brand/svg/app-icon*.svg` |
| iOS launch screen | `bg` colour with the mark at 96 pt in `accent`, centred; dark mode as Android | Built in the storyboard / `UILaunchScreen` from the SVG |
| Play Store icon | 512 px, square (the store rounds it) | `brand/png/play-512.png` |
| Play feature graphic | 1024 × 500: the icon, «مجتمع القرآن», «اقرأ، استمع، ابحث، وتعلّم مع غيرك» | `brand/png/feature-1024x500.png` |
| Lockup | Icon + wordmark for «عن التطبيق» | `brand/png/lockup.png` |

The app icon follows the system shape (squircle, circle, etc.). Never bake rounded corners into the Android foreground or the iOS 1024 file.
