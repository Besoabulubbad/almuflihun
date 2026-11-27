# Screen walkthrough: what we agreed per screen

The owner and Claude go through every screen to agree what it shows, where its data comes from, its states, and where each action leads. `design/SCREENS.md` and `design/DESIGN.md` remain the visual source of truth; this page records the decisions on top of them. Open design questions go to [design-requests.md](../design-requests.md).

## 1. Home

**Agreed 2026-11-26.**

- **Night sky.** The app name, and a button that opens Settings. The moon is drawn at tonight's exact phase, computed on the device (not 30 images), so it is exact and works in both themes. Below it: the lunar night in words, the phase, the lit fraction, a strip of the month's nights, and three cards (moonrise, moonset, days until المحاق). Tapping the moon opens Moon.
- **No Gregorian date** anywhere on Home.
- **All values** are computed on the device from the location or the chosen city. Home works fully offline.

**«دليل اليوم»**

- **Pool:** every entry marked `home: true` in `design/data/abwab-4.json` (266), from visible and hidden doors alike.
- **Pick (revised by the owner, 2026-11-26):** a new random verse **each time the user opens the app**. A "visit" starts at launch, or on returning after 30 minutes or more in the background, so a quick switch to another app does not change it. Within a visit, Home and الأبواب show the same verse. A per-device shuffle bag avoids repeats until all 266 have been shown. (The earlier "same for everyone, changes at sunset" rule is withdrawn.)
- **Tap on Home:** opens the Mushaf at that verse, scrolled to it and highlighted (selection state). On الأبواب, see §6.
- **Badge:** the door's name from the data, including hidden doors.

**«أوقات التسبيح» (owner chose all three parts, 2026-11-26)**

The Quran names times for tasbih that are set by the sun and the stars. The app computes them for the user's location, offline, with the same astronomy engine as the moon.

| Window | Verses (text always from the verse data) | Computed as |
|---|---|---|
| إدبار النجوم | الطور ٤٩ | stars fading: from astronomical dawn to nautical dawn |
| قبل طلوع الشمس، بكرة، الإبكار، حين تصبحون | طه ١٣٠، ق ٣٩، الإنسان ٢٥، آل عمران ٤١، الروم ١٧ | dawn to early morning |
| حين تظهرون | الروم ١٨ | around solar noon |
| العشي، الأصيل، قبل الغروب | الروم ١٨، الأحزاب ٤٢، ق ٣٩ | late afternoon to sunset |
| حين تمسون | الروم ١٧ | after sunset |
| آناء الليل، ومن الليل | طه ١٣٠، ق ٤٠، الإنسان ٢٦ | night |

1. **The arc on Home.** Under the moon, a day arc shows today's windows. The current window is marked and its verse is shown in full. Tapping a verse opens it in the Mushaf.
2. **A sky that follows the sun.** The Home hero moves between night, dawn, day and dusk with the sun's real altitude. This needs sky tokens from the designer; reduced motion switches without animation.
3. **Sun-based reminders.** The morning and evening reminders fire at «قبل طلوع الشمس» and «قبل الغروب» for the user's location, not at a fixed clock time. Still off by default; no guilt copy.

**Honesty.** Mapping a word such as الأصيل to a clock time is a reading of the Arabic, not a time stated in the text. Each window carries the «من اللغة» label (DESIGN §1, three levels). The exact boundaries are to be confirmed by the content owners before release.

## 2. Moon

**Agreed 2026-11-26.**

- Every value is computed on the device for the user's location (or chosen city): phase, lit fraction, distance, sun and moon rise, set, highest point and azimuths, the nightly rise delay, the month's phases, and the notable moments.
- The engine is Astronomy Engine (MIT) or a port of its maths to common code if its Kotlin build is JVM-only (to verify).
- The explanatory sentences («في الشتاء تمشي الشمس منخفضة…») are a small set of Arabic templates with conditions, stored as data and approved by the content owners. No prose is hard-coded.
- Without a location or city, phase and lit fraction still show; rise, set, altitude and direction show «لا نعرف».

**«لحظات مميزة» (owner chose the full set):**

| Moment | Notes |
|---|---|
| بدر الشتاء | the full moon nearest the winter solstice |
| بدر الحصاد | the full moon nearest the autumn equinox |
| ليلة المحاق | the new-moon night |
| ليلة الهلال | night 1 of our lunar-month rule, with «يسألونك عن الأهلة…» (البقرة ١٨٩) |
| خسوف القمر، كسوف الشمس | only when visible from the user's location; start, peak and end times; «وخسف القمر» (القيامة ٨) |
| أقرب بدر، أبعد بدر | the year's closest and farthest full moons |
| اقتران القمر بكوكب | the moon passing close to a bright planet |

Each moment can raise an optional local notification («نبّهني باللحظات المميزة»), off by default.

## 3. الذكر (hub)

**Agreed 2026-11-26.**

1. **Header:** title; the gear opens Settings.
2. **Slider:** the 14 items of `dhikr-slider.json` in `order`. Quote large, full verse below with the quote highlighted, door chip, «n من ١٤». Swipe or previous/next. Tapping a page opens its door. Reduced motion turns off the pager animation.
3. **«حزبك»:** the current hizb (1 → 60 in order), its range, a progress bar, «وصلت إلى…», and «أكمل الحزب», which opens Hizb at the saved position. The position (page and ayah) is stored on the device.
4. **Tiles:** المصحف والاستماع → Mushaf at the last read page; الأبواب; المجتمع; المحفوظات.
5. **البثوث:** live and upcoming streams from Firestore, «الكل» → Live. Offline: the last loaded list, marked as possibly out of date.
6. **وقتك مع القرآن** (owner chose A):
   - Counts time while a screen showing Quran text is in the foreground and in use (Mushaf, Hizb, doors, الضياء stations, the slider), and while recitation audio plays, including with the screen off.
   - Reading while listening counts once.
   - Stops after 2 minutes with no touch and no audio.
   - Stored only on the device. No goals, no streaks, no guilt copy. The week runs Sunday to Saturday, as in the mockup.
   - The «بيانات مثال» caption is mockup-only and never shown with real data.
7. **«لماذا تقرأ القرآن»:** hidden until its content exists (pending the designer's confirmation).

## 4. Mushaf, listening, index, Hizb (in progress)

- **Mushaf and Hizb are separate readings, each with its own memory** (owner, 2026-11-26). The Mushaf remembers its last page and ayah; Hizb remembers its own hizb, page and ayah. Opening one never moves the other.
- Reciter for the first APK: **علي جابر** only; محمد أيوب later (not on quran.com, needs another source). Audio and timings come from the Quran Foundation API through our backend ([ADR 0007](../adr/0007-recitation-audio-quran-foundation.md)).
- «استمع للكلمة» stays hidden unless a licensed word-audio source is cleared (DECISIONS #21).

### Hizb (agreed 2026-11-26)

- The printed pages of one hizb (from `design/data/hizb-60.json` and the layout's page map). Ayahs outside the hizb on its first and last page use `quran.played`; «۞ الحزب n» marks the start.
- Header: «الحزب n من ٦٠», range, pages, and «صفحة i من N» with a progress bar.
- «استماع الحزب كاملاً» plays from the hizb's first ayah to its last, crossing surah files, then stops.
- **«حزبك» moves only when the user moves** (owner chose A). Reaching the end offers «الحزب التالي»; nothing advances by date; no "behind" state. The hub's «حزبك» reflects the Hizb screen's own saved position.

## 6. الأبواب

**Agreed 2026-11-26.**

- **Only the 7 visible doors are shown, to everyone** (owner, 2026-11-26). Hidden doors stay in the data and are released with later updates, once designed.
- **«دليل اليوم» at the top** is the same verse as Home's for the current visit (§1). Its main button:
  - the verse's door is visible to this user → «افتح الباب», which opens the door scrolled to that entry and highlights it;
  - otherwise → opens the Mushaf at the verse and highlights it.
- «احفظ» saves the verse to المحفوظات. «شارك» opens the system share sheet with **plain text: the full verse, then its reference** (e.g. «سورة النحل ٩٠»), nothing else (owner, 2026-11-26). The same everywhere in the app; «انسخ» copies the same text.
- The paper sheet lists the doors as DoorTiles: number and name only, no verse (owner). Tapping one opens the door.

**Name change (owner, 2026-11-26):** «دليل اليوم» becomes **«تذكرة»** on Home and on الأبواب, because the verse now changes on every visit. «تذكرة» is the Quran's own word for itself and the word the data's `meta.home` already uses («تذكرة اليوم»). The designer may refine it.

## 7. Door pages, «كل الآيات», الحكمة

As in `design/SCREENS.md` and the data, no extra decisions: sections in order; each item is the quote, the full verse with the quote highlighted, and the reference; الإنفاق starts with «قل العفو» and its 2:261 item has the illustration; every door ends with «كل الآيات» (all verses in Mushaf order, optional note); الحكمة is a chain of passages opening into HikmaPassage.

## 8. الضياء

**Agreed 2026-11-26** (owner raised no objection to these defaults).

- Hub and 11 stations exactly as `design/SCREENS.md` and DESIGN §6 (13 block types).
- The app remembers the last opened station; the hub button becomes «أكمل من المحطة n».
- Visited stations get a quiet mark on the map; no completion celebration, no streaks.
- The 20 illustrations are converted to vectors once and used only at `illustrationS/M/L`.
- Station 6's tree counts (النخل ٢٠ / ١٩، الأعناب ١١ / ١٠): **the owner delegated the check to engineering** (2026-11-26). Once Tanzil's text is in the repo, the counts are recomputed from the text with a written definition of what counts, the verse lists are published in `docs/content/station-6-counts.md`, and the data is set to the verified numbers. More broadly, a build-time test checks that every verse key in the app's data exists and every quote and highlight sits in its verse (ADR 0001 confirmation).

## 9. المحفوظات → Collections («مجموعات»)

**Superseded in part by ADR 0012 (2026-11-26):** the flat Saved list becomes named collections (S1). The sync rules below still apply, per collection.

**Agreed 2026-11-26** (owner chose B).

- Ayahs saved from the Mushaf and quotes saved from doors, with the type filter; each item opens its source.
- **Stored on the device, and synced to Firestore when the user is signed in.** Path `users/{uid}/saved/{itemId}`, readable and writable only by that user (rules). On sign-in, device and cloud items are merged (union, newest wins on the same item). On sign-out the device copy stays. Deleting the account deletes the synced items (ADR 0005's deletion function).
- Without an account it works fully, device only.

## 10. البثوث (Live) and videos

**Agreed 2026-11-26.**

- **Lives are on TikTok.** TikTok has no official way to play a LIVE inside another app (official embeds cover single videos and profile cards only; no public LIVE API; checked 2026-11-26, to re-check on TikTok's developer site before building). A live card's «افتح في تيك توك» opens the presenter's LIVE in the TikTok app, or the browser if TikTok is not installed.
- Lives keep the filter (الكل / تلاوة / أسئلة / تدبّر), «ذكّرني» (local notification) and the «مباشر الآن» push (ADR 0005).
- **A separate section of videos (owner):** YouTube videos whose links moderators add in Admin, played **inside the app** with YouTube's official embedded (IFrame) player. Android uses a maintained wrapper of the IFrame player; iOS a web view with the same player later. No YouTube API key is needed to embed; YouTube's branding and controls stay as YouTube requires.
- Anyone can watch, without an account. Moderators (`streams.manage`) add, edit and remove lives and videos.

**Live schedules (owner, 2026-11-26).**

- A live show is a **weekly schedule**, not a one-off: days of the week, a time window and the time zone it is set in (e.g. daily 17:00–19:00, or Sunday and Thursday 20:00–21:00, `Asia/Amman`). Stored with an IANA zone so daylight saving is handled.
- Each user sees the times **in their own local time**.
- **Status comes from the schedule (owner chose A):** inside a window the card shows «مباشر الآن» and the «مباشر الآن» push is sent at the window's start by a scheduled Cloud Function; outside, the card shows the next occurrence.
- Moderators can **cancel one occurrence** or **add a one-off** live outside the schedule.
- «ذكّرني» reminds before **every** occurrence of that show until the user turns it off (local notifications, rescheduled when the schedule changes).
- New YouTube videos send **no push** for now (no answer given; can be added later as its own Settings switch).

## 11. Mushaf index (الفهرس)

**Agreed 2026-11-26.** Tabs السور (ayah count, start page), الأحزاب (60, the user's own marked), الأجزاء (30); tapping opens the Mushaf there.

**Search (owner chose A), fully offline:**
- surah names and numbers; jumps typed as «٢:٢٥٥», «البقرة ٢٥٥» or «صفحة ٥٠»;
- words of the Quran, matched on the Tanzil simple text with diacritics removed and letter forms unified (the same letter skeleton as the quote matcher, README), so «الصابرين» finds every ayah containing it;
- results show the full ayah (in the card font) with the match highlighted; tapping opens the Mushaf at that ayah, highlighted.

## 12. Settings

**Agreed 2026-11-26.** Groups in order: المظهر; التذكير (sun-based, off by default); الإشعارات (a switch per push category, all on; new); الموقع والقمر; الاستماع (علي جابر only for now; downloaded surahs; the mushaf is bundled); حسابي في المجتمع; **الخصوصية** (new: the analytics and crash-report switch, a link to what is collected, «رأيك» feedback); عن التطبيق («من نحن وكيف نفهم ديننا» is **hidden in the first APK** (owner: not needed now); sources and licences; version and update check).

- **Location (owner chose A).** Only Android's approximate location (about 1–3 km), never precise; it is enough for sun and moon times. Without permission the user picks a city from an **offline list in the APK** from GeoNames (cities above 15,000 people, Arabic names where available, coordinates and time zone; about 2 MB; credited, CC BY 4.0).
- Location is used for: moonrise and moonset, rise and set directions, sun times, «أوقات التسبيح» and the sun-based reminders, night 1 of the lunar month, and which eclipses are visible. Moon phase works without it.
- **Analytics (ADR 0008):** off until the user agrees in a one-time prompt; the switch in الخصوصية turns it off at any time.

**App update in Settings (owner, 2026-11-26).** عن التطبيق always has a «تحديث التطبيق» row with the installed version. When an update is available and not installed, a red dot (`status.danger`) shows on the Settings gear on Home and the الذكر hub and on that row, which is highlighted, until the update is installed. Dismissing the banner does not clear the dot.

## 13. The study engine (ADR 0012)

**Agreed 2026-11-26.** المفلحون is a study app. Collections (S1), advanced search by word, root, lemma and phrase with combinations and scope (S2), word study (S3), related verses (S4), the ayah study panel (S5), the surah overview (S6), plus copy of the real text (P2), play scope (P5), recent pages (P6) and a sleep timer (P8). All offline. Tapping a word in the Mushaf opens word study. Screens wait for the designer ([design-requests.md §6](../design-requests.md)).

**Results by how the Mushaf writes them (owner, 2026-11-27).** The Mushaf spells some words differently in different places («إِبْرَٰهِيمَ» / «إِبْرَٰهِۦمَ», «عِبَادِ» in al-Furqan 63 / «عِبَٰدَ» in az-Zukhruf 19), and the letters searched for can belong to different words («عباد» inside «عِبَادَة»). Advanced search results can therefore be shown **«حسب الرسم»** as well as «حسب السورة»: one group per spelling, labelled with the Mushaf's own spelling of the word's stem (prefixes such as «و» left out), most frequent first, each listing only the matches written that way. The reader sees the difference before opening a verse.
