# Requests to the designer

What engineering still needs from the design. Design v4 (26 Nov 2026, `design/REQUESTS-ANSWERED.md`) answered every earlier request except «أوقات التسبيح»; those answered items were removed from this list. When an item is designed, delete it here and note the design version that answers it.

## 1. «أوقات التسبيح» (waiting since v4)

The spec the designer asked for is [specs/screens.md → Home](specs/screens.md#1-home) («أوقات التسبيح»). Please send that section with this list. It covers the day arc under the moon, each window with its full verse and the «من اللغة» label, and the sky tokens for night, dawn, day and dusk in both themes, with text contrast held on each.

## 2. Self-update (ADR 0010)

The v4 banner and sheet exist (`Prompts`). Still needed:

- **Settings → «تحديث التطبيق» row** (owner, 2026-11-26): always present in عن التطبيق, showing the installed version and, when an update exists, the new version and «حمّل التحديث».
- **Red dot** (owner): while an update is available and not installed, a `status.danger` dot sits on the Settings gear (Home and the الذكر hub) and on the «تحديث التطبيق» row, which is also highlighted. It disappears only when the update is installed. Dismissing the banner does not remove the dot.
- In-app download: progress, pause or cancel, then «ثبّت التحديث».
- A one-time explanation before Android's «تثبيت تطبيقات غير معروفة» system screen.
- The required-update screen «يلزم تحديث التطبيق»: full screen, only the update action.
- Download failure and "the file could not be verified" states.

## 3. Motion and interaction (owner: "make sure the experience is seamless")

DESIGN.md has one motion line (§9: reduced motion) and `motion.sheetMs: 250`. A production app needs a motion and interaction spec. Please add a DESIGN section and tokens for:

- **Screen transitions:** push and pop (direction mirrored for RTL; Android predictive back), hub ↔ detail, night-hero screens ↔ paper screens.
- **Signature moments:** Home moon → Moon page (the moon continues into the Moon hero); «تذكرة» → Mushaf at the verse (how the verse lands highlighted); slider paging; Mushaf page turn; the HizbEnd sheet; the playing-word highlight moving through a line.
- **Sheets and dialogs:** enter, exit, drag, scrim.
- **Press states** for every tappable component (cards, tiles, chips, buttons, rows, icon buttons), and the **focus** state for keyboard and switch access.
- **Haptics:** which actions give feedback (save, report sent, hizb finished, long-press on an ayah), and none for scrolling or paging.
- **Durations and easings as tokens** (e.g. `motion.fast/standard/emphasised` and easing curves), so code never invents them. Reduced motion: what each becomes.

## 4. Loading and error states

v4 covers empty, offline (البثوث) and no-location states. Not yet designed:

- **Loading:** first open of Community, a profile, Admin lists, videos; audio buffering; the first Mushaf page while its font loads (a skeleton, or a fallback text style).
- **Errors:** a post that fails to send, a failed sign-in, failed Admin actions, a video that will not play, TikTok not installed, audio stream failure, Quran Foundation unavailable.
- **Pull to refresh** on Community, Live and Admin lists: yes or no.

## 5. Onboarding (open since v1)

What a first launch shows, in order: welcome, the consent prompt (v4 places it at first launch), location or city, theme. Keep it short; notifications are asked later (DECISIONS #29).

## 6. The study engine (ADR 0012, owner, 2026-11-26)

The owner's direction: المفلحون is a **study** app, not a memorisation app; ease of use first; the design may change to win. Please design:

- **S1 Collections («مجموعات»)**, replacing the flat المحفوظات: the collections list (name, count, last change), a collection's page (verses in the user's order, a private «تأمّل» note per verse, reorder, remove), create and rename, "add to collection" from any verse (a sheet that lists collections with checkmarks; a verse can be in several), share a collection as plain text, post it to Community, empty states, the sync note.
- **S2 Advanced search**, extending MushafSearch: a mode or chips for كلمة / جذر / لفظ (lemma) / عبارة, combining terms (و / أو / ليس), a scope filter (سورة، جزء، حزب، نطاق، مكية / مدنية), results grouped by surah with counts and a total, «احفظ النتائج في مجموعة», recent searches, no-results and too-many-results states.
- **S3 Word study:** what tapping a word in the Mushaf now opens (it replaced «استمع للكلمة», which stays hidden): the word, its root and lemma, «وردت n مرة», occurrences as full verses with the word highlighted, grouped by form, each opening the Mushaf there.
- **S4 Related verses:** a section listing verses that share a phrase with this ayah (the shared phrase highlighted in both) and verses sharing rarer roots.
- **S5 Ayah study panel:** opened from the AyahSheet (a new action, e.g. «تدبّر الآية»): the full ayah, its position (surah، جزء، حزب، صفحة، مكية / مدنية), its words with roots (each opens S3), related verses (S4), the doors that include it, the user's collections and notes for it. Full screen or a tall sheet; how it fits beside the page on tablets.
- **S6 Surah overview:** from the index or the Mushaf header: ayah count, place and order of revelation, most frequent roots, the doors that quote it, «ابدأ القراءة».
- **Ease of use:** P5 play scope in the PlayerSheet (to the end of the surah, hizb, quarter, or a chosen ayah); P6 «آخر الصفحات» (10) in the index; P8 a sleep timer in the PlayerSheet.
- The «من اللغة» level (DESIGN §1) applies to roots and lemmas: label them so they are never mistaken for the text.
