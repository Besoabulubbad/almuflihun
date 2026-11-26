# Screens

There are 62 approved screens and state boards. Each one has a PNG image in `screens/{Name}.png` and an HTML mockup in `mockups/`, which you can open in a browser. The mockups use the canvas template syntax: `{{ }}`, `<sc-for>`, `<sc-if>`. Read them as layout references, not as code to ship.

Navigation map:

```
Home ─┬─ Settings
      ├─ Moon
      ├─ الذكر (Quran hub) ─┬─ Mushaf ── MushafPlay
      │                     ├─ Hizb
      │                     ├─ الأبواب (doors) ── 7 door screens ── كل الآيات
      │                     ├─ Live
      │                     ├─ Community
      │                     └─ Saved
      ├─ الضياء (food) ── 11 stations (sequential)
      └─ الفرقان  (not designed yet)
```

## Home and Moon

**HomeB · الرئيسية**
- **Night sky:**
  - The app name, and a profile button.
  - The moon drawn at today's phase.
  - The lunar night in words («الليلة السادسة عشرة»), then the phase and illumination.
  - A 30-dot strip for the lunar month (past, today, future).
  - Three small cards: moonrise, moonset, and days until the new moon (المحاق).
  - Tapping the moon opens the Moon page.
- **Paper sheet:**
  - The «دليل اليوم» card, with today's verse from a door.
  - Three portals with icons: الذكر، الضياء، الفرقان.
- The top-left button opens Settings.
- **No Gregorian date.**

**Moon · القمر**
- Today's moon in detail: phase, illumination, distance, and its relation to the sun.
- Rise and set times with azimuths, shown as a compass.
- A strip of the 30 lunar nights.
- How late the moonrise is from night to night (a bar chart).
- Notable events: harvest moon, winter full moon, perigee.
- All values are computed for the user's location. The mockup uses Jerash, 26 Nov 2026, lunar day 16.

## الذكر (Quran)

**Quran · الذكر (hub)**, in this order:
1. **Night header:** the title, and a settings button that opens Settings.
2. **Slider:** 14 curated verses from `data/dhikr-slider.json`.
3. **HizbCard «حزبك».**
4. **Four tiles:** المصحف والاستماع، الأبواب، المجتمع، المحفوظات.
5. **البثوث:** horizontal LiveCards, plus «الكل».
6. **وقتك مع القرآن:** today, this week, and a 7-day bar chart.
7. **«لماذا تقرأ القرآن»:** an empty dashed placeholder. Its content comes later.

**Mushaf · المصحف: تحديد آية**
- **Printed Madani pages** (604), drawn with the QPC page fonts. quran.com's Android app is the visual reference only; its code is GPL and is not reused (DECISIONS.md #5).
- Shows the selected ayah and the AyahSheet.
- The list icon opens **MushafIndex**.

**MushafPlay · أثناء الاستماع**
- The playing ayah is highlighted. Earlier ayahs are dimmed.
- The word tooltip and the PlayerBar.
- Audio with ayah-level timing comes from an open-source source (check the licence).

**Hizb · الحزب**
- The printed pages of one hizb (hizb 2 = pages 11–21). Ayahs before the start are dimmed, and «۞ الحزب ٢» marks the start.
- A page progress bar, the PlayerBar, «الحزب السابق» and «الحزب التالي».
- The position is saved. Hizbs run in order 1 → 60.

**MushafIndex · الفهرس**
- Search, plus the tabs السور / الأحزاب / الأجزاء.
- The hizb grid marks the current hizb. Surah rows show the ayah count and the start page.

**Live · البثوث** (Firebase list, DECISIONS.md #1)
- The in-app YouTube player at the top, then the filter الكل / تلاوة / أسئلة / تدبّر, then the LiveCards.
- YouTube plays inside the app. TikTok opens the TikTok app.
- Upcoming streams have «ذكّرني».

**Community · المجتمع** (Firebase, DECISIONS.md #1–2)
- Buttons: شارك آية / اسأل / تأمّل.
- The rules box.
- Posts with **attached verses** (key + word range), shown in full.
- Every answer must attach a verse. Every reflection carries «فهم شخصي، ليس نصاً».
- Posts can be reported.

**Saved · المحفوظات**
- A filter by type: ayahs from the mushaf, and quotes from doors.
- Each item links back to its source.

## الأبواب (Doors)

Data: `data/abwab-4.json`.
- Show only `meta.visible_doors`, in that order: awamir، nawahi، infaq، dhikr، shukr، qawl، hikma.
- The other doors stay in the file with `visible: false`.

**RemindB · الأبواب**
- Night top: «دليل اليوم», a full verse with the quote large, and the buttons افتح الباب / احفظ / شارك.
- Paper sheet: 7 DoorTiles.

** الأوامر، النواهي، الإنفاق، ذكر الله، الشكر، القول الحسن**
- Each one renders `abwab[].design` (`type: "evidence"`): sections in order, then EvidenceItems.
- الإنفاق starts with «قل العفو» (2:219), and its 2:261 item has the parable illustration.
- Each door ends with a link to «كل الآيات».

**Hikma · الحكمة** (`type: "passages"`)
- A chain of passages.
- Tapping a passage opens **HikmaPassage**, which shows the passage verses in sequence. The mockup shows مقطع الإسراء.

**BabAll · كل الآيات**
- Every verse of the door (`abwab[].all_verses`) in mushaf order, with an optional note.
- The door comes from where the user came from.

## الضياء (Food in the Quran)

Data: `data/diyaa-food.json`. It is built from `data/food-app-page-2.source.md`, verse by verse.

**Diyaa · الطعام في القرآن (hub)**
- A dawn hero illustration, the title and the intro.
- The journey map: 11 stations. Each is tappable and opens its station.
- The button «ابدأ الرحلة من المحطة ١».
- The summary (الخلاصة): 11 lines, each with its illustration.

**DiyaaS1…S11:** one station per screen.
- StationHeader, then the station's `blocks` rendered in order, then StationNav (previous / next).
- The stations:
  1. ماذا أُحلّ لنا؟
  2. كُلوا من الطيبات
  3. أصحاب الكهف: أيها أزكى طعاماً
  4. اللحوم: بهيمة الأنعام
  5. طعام البحر
  6. الفاكهة والنخيل والثمرات
  7. العسل: فيه شفاء للناس
  8. الحبوب
  9. الأدنى
  10. كلوا واشربوا ولا تسرفوا
  11. المحرّمات
- Station 11 keeps only the «لحم الخنزير: فإنه رجس» explanation under «صفاتها بكلمات القرآن». The group removed the other two lines, so do not add them back.

**Counting note:** the tree counts in station 6 (النخل ٢٠، الأعناب ١١، الزيتون ٦، الرمان ٣، التين ١) were recounted against the verse text. The source file says ١٩ and ١٠. The JSON keeps both numbers under `count_bars` (`differs_from_source_file`), so the content owners can confirm.

## Settings
**Settings · الإعدادات**
- المظهر: عادي / ليلي / حسب الجهاز.
- التذكير: morning and evening, off by default.
- الموقع والقمر: location switch, city fallback, and «بداية الشهر القمري: حساب فلكي» with its rule.
- الاستماع: reciter, offline audio.
- حسابي في المجتمع: the Google account, sign out, «الإدارة» for moderators, and delete account and posts.
- عن التطبيق: who we are, text and audio sources with their licences, version.

**Admin · الإدارة** (moderators only, from Settings)
- Add or edit streams with type, title, link, host and time.
- The reports tab.

## Dark mode
- Every paper screen has a dark version from `tokens.json → color.dark`.
- In the prototype, switch with «ليلي» in the side panel or in Settings.

## Added in v4
Screens and state boards. A «لوحة حالات» (states board) shows the variants of one area with labels. See `REQUESTS-ANSWERED.md` for what each one answers.
- **Home and Moon:** HomeStates, MoonStates, Prompts (consent, notification pre-prompt, update banner and sheet).
- **الذكر:**
  - QuranStates.
  - Mushaf (reading header, ribbon, AyahSheet v2), MushafPlay (PlayerBar v2), PlayerSheet, MushafFull, MushafSearch.
  - Hizb (night header «قراءة حزبك»), HizbEnd.
  - SavedStates.
- **الأبواب:** RemindB (تذكرة, tiles without verses), RemindStates.
- **البثوث:** Live (TikTok, weekly), LiveVideos, VideoPlayer.
- **المجتمع:** Community, CommunityCompose, VersePicker, CommunityThread, CommunityStates, Profile, ProfileMine, Grades.
- **Settings:** Settings v4, CityPicker, WhatWeCollect, Feedback.
- **Admin:** Admin hub, AdminReview, AdminLive, AdminUser, AdminAnnounce, AdminFeedback, AdminRoles.

## Not designed yet
- «أوقات التسبيح» on Home (waits for its spec).
- الفرقان (boycott list and economic awareness).
- Onboarding (location permission, theme, reminder opt-in).
- The content of «لماذا تقرأ القرآن».
