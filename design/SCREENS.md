# Screens (v7)

Each screen has a code that matches `screens/<code>.png` and the board (`board/index.html`). Build them only from the components in `DESIGN-SYSTEM.md` §6 and §10.

## Navigation

```
TabBar: الذكر │ المجتمع │ القرآن │ الإعدادات      (each tab keeps its own back stack)
MiniPlayer: above the TabBar on every tab and sub-screen; at the bottom in full-screen reading.
AyahSheet: from any verse, anywhere.

الذكر (A1) ─┬─ تلاوة card → Reader at the right page (C2)
            ├─ Moon row → Moon page (A2, always dark)
            ├─ ادرس (A3) → Door (A4) → كل الآيات
            ├─ الضياء (A5) → Station 1–11 (A6)
            └─ الفرقان (قريبًا)
المجتمع ─┬─ النقاش (B1) → Thread, Profile; FAB → Compose (B6)
         ├─ البثوث (B2) → Live details (B3)
         ├─ الفيديوهات (B4)
         └─ المشرفون (B5)
         Sign-in sheet (B7) whenever a signed-out user acts.
القرآن (C1) ─┬─ Search: typing (C5) → results with spelling forms (C6) → options sheet (C7)
             ├─ تابع القراءة → Reader (C2), word tap (C3), تلاوة finished (C4)
             ├─ Index: السور / الأجزاء / الأحزاب
             └─ مجموعاتي (C8) → Collection (C9)
الإعدادات (D1) ─┬─ Profile (my posts, sign in/out, delete)
                ├─ التلاوة اليومية (D2)
                ├─ وقتي مع القرآن (the old 7-day chart)
                ├─ القارئ، الصوت دون اتصال، المظهر، الإشعارات، الموقع والقمر، عن التطبيق
                └─ الإدارة (moderators only)
Outside the app: widgets (E1, E2), notification player (E3), lock screen (E4), notifications (E5).
```

## Tab 1 · الذكر

**A1 الذكر.** The cards come from remote config, in this order:
1. Title and the lunar night («السبت ، الليلة ١٨ من الشهر»). No Gregorian date.
2. SearchField «ابحث في القرآن», which opens the القرآن search.
3. **TilawaCard**: today's portion, a ring of pages read, the reminder time, «أكمل من الصفحة n».
4. **WeekStrip**: «هذا الأسبوع», days read, total time.
5. MoonRow, computed for today.
6. تذكرة: a VerseCard (`quranHero`) with the quoted words highlighted and its door named. Swipe for the next of the 14 in `data/dhikr-slider.json`.
7. SectionTiles: ادرس (٧ أبواب), الضياء, الفرقان (قريبًا).

**A2 Moon page (always dark)**
- Tonight's moon, phase and age; lit %, distance, nights until the new moon.
- The 30 nights.
- The rise and set compass. Location is used on the device only; without it, «لا نعرف».
- How much later it rises each night.
- Coming full moon and new moon, counted in nights.

**A3 ادرس.** The 7 visible doors in `meta.visible_doors` order, with their counts.

**A4 Door.** One template for all doors:
- the summary
- sections of VerseCards with the `words` ranges highlighted
- «كل الآيات»

**A5 الضياء hub.**
- Illustration and intro.
- The 11 stations.
- «ابدأ من المحطة ١».
- الخلاصة.

**A6 Station.**
- Blocks from `data/diyaa-food.json` in order.
- Previous and next.
- Station 11 keeps only «لحم الخنزير».

## Tab 2 · المجتمع (visible to all; sign-in only to act)

**B1 النقاش**
- Title with a post-search icon.
- CommunitySegments.
- LiveBanner, only when a live is on or within 24 h.
- RuleBox line «كل جواب يُرفق آية».
- PostCards.
- FAB «اسأل أو شارك».

**B2 البثوث**
- Audience filter.
- A live-now card: host, audience, topic, length, «افتح في تيك توك».
- «هذا الأسبوع» LiveRows.
- Weekly schedules.

**B3 Live details**
- Tags, title, time and platform.
- Host card.
- Rows: لمن، عن ماذا (links to the verses), المدة، يتكرر.
- «اكتب سؤالك» (Compose with those verses) and «ذكّرني».

**B4 الفيديوهات**
- In-app YouTube player, title, «افتح الآية», «في يوتيوب».
- VideoRows.
- The MiniPlayer hides and pauses while a video plays.

**B5 المشرفون (new)**
- ModeratorInbox: سؤال / بلاغ / اقتراح, the message, «أرفق آية», «أرسل».
- «رسائلك» with status.
- Replies arrive in the `moderator_replies` channel.
- Admin side: الإدارة gains an inbox.

**B6 Compose**
- Type: شارك آية / اسأل / تأمّل.
- The text.
- The attached VerseCard with «غيّر» (the verse picker).
- The moderation note and «أرسل».

**B7 SignInSheet**
- When a signed-out user taps اسأل، أجب، أرسل, or messages the moderators.
- Google sign-in, then back to the same place.

**Admin form for lives:** add «لمن» (للجميع / للمبتدئين / للأخوات / للأطفال) and «عن ماذا» (a verse range, a door id or text).

## Tab 3 · القرآن (the Quran engine)

**C1 القرآن**
- SearchField with the options icon.
- ContinueCard.
- مجموعاتي row.
- Segmented السور / الأجزاء / الأحزاب.
- Index rows with play.

**C2 Reader (full-screen)**
- Printed Madani pages with QPC page fonts; pinch to zoom.
- The TabBar hides; the MiniPlayer stays.
- HizbProgress when reading the تلاوة portion.
- Tap an ayah to get the AyahSheet: استمع، من هنا، احفظ، شارك، انسخ, «اسأل عنها في المجتمع», and its doors.

**C3 Word tap.** «استمع للكلمة» | «ابحث بجذرها».

**C4 تلاوة finished**
- «أتممت تلاوة اليوم», the time, and tomorrow's start.
- «تابع إلى الحزب n الآن» (counts toward tomorrow), «يكفي اليوم».

**C5 While typing.** Phrase suggestions with counts, surah names, references, and recent searches (on the device only).

**C6 Results with spelling forms**
- Count and «خيارات البحث».
- SpellingForms: «كيف كُتبت في المصحف», one card per written form with its count.
- VerseCards grouped by surah in Mushaf order, each match marked by its form.
- Real example: «عباد» = 12 places: عِبَادُ ×11 (e.g. الفرقان ٦٣, الأنبياء ٢٦) and عِبَٰدُ ×1 (الزخرف ١٩).

**C7 Search options**
- Type: عبارة / كلمة كاملة / جذر / مطابق.
- Range.
- «أظهر أشكال الكتابة» (on by default).
- مع / أو / بدون.
- «اعرض n آية».

**C8 مجموعاتي / C9 Collection.** As in v5:
- create and rename collections
- drag to order
- private notes
- شارك نصًا / انشر / استمع

## Tab 4 · الإعدادات

**D1 الإعدادات**
- The profile card at the top (signed out: «سجّل الدخول»).
- القراءة: التلاوة اليومية, وقتي مع القرآن.
- الاستماع: القارئ, الصوت دون اتصال.
- التطبيق: المظهر, الإشعارات, الموقع والقمر, عن التطبيق (ما نجمعه, أرسل ملاحظة, من نحن, المصادر, تحديث التطبيق).
- الإدارة for moderators.

**D2 التلاوة اليومية**
- Amount: ربع حزب / نصف حزب / حزب (default) / جزء, with the time to finish the Quran.
- Start point and «ابدأ ختمة جديدة».
- Reminder on/off, time and days.
- Sync note.

## Outside the app

**E1 / E2 Widgets** (light and dark): the quick bar 4×1, تلاوة 2×2, moon 2×2, تذكرة 4×2, and المجتمع 4×2. Full specs are in DESIGN-SYSTEM §10.

**E3 Notification player.** The system media notification:
- Collapsed: previous, play/pause, next.
- Expanded: adds repeat and speed, and the progress.
- Artwork: the surah name on paper.

**E4 Lock screen.** The Android media notification; on iOS, Now Playing with the same content.

**E5 Notifications:** the تلاوة reminder, live reminders, answers, and moderator replies.

## First launch

**F1** has four steps: welcome → usage-data consent (equal buttons) → location or city → theme. «تخطَّ» is on steps 2–4. Notifications are asked later, in context.

## System boards (from v5, restyle only)

Loading, errors, update available and update required, the Admin suite, and the state boards.

## Not designed yet

- «أوقات التسبيح» (waits for its spec).
- الفرقان content.
