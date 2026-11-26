# Answers to `Requests to the designer` (design v4, 26 Nov 2026)

Each line names the screen that answers the request; open the screen's PNG in `screens/` or its mockup in `mockups/`.

What the marks mean:
- ✅ Designed in v4. Delete it from your list.
- ◐ Answered as a rule in this file or in DESIGN.md, with no new screen.
- ⏳ Still open, and why.

## ADR 0005: push notifications

| Request | Answer |
|---|---|
| Settings notifications group | ✅ `Settings`, group «الإشعارات»:<br>• البثوث، منشورات جديدة، ردود على منشوراتي، ردود على تعليقاتي، التحديثات، الإعلانات. All on by default.<br>• «لحظات القمر المميزة», off by default and local.<br>• Group «للمشرفين»: قائمة المراجعة، البلاغات. Shown only with the permission. |
| When to ask the Android permission | ✅ `Prompts`, card «قبل إذن الإشعارات».<br>An in-app pre-prompt appears **the first time a notification is needed**: a live reminder, the first Community post or reply, or turning on a reminder. Then the system dialog. Never at first launch. |
| «تحديث متوفر» | ✅ `Prompts`.<br>A banner at the top of Home and of الذكر. «التفاصيل» opens a sheet with the version, a short list of changes, «حمّل التحديث» and «لاحقاً». |
| Push copy | ✅ `COPY.md`, every category and the post-result messages. |
| Announcement composer | ✅ `AdminAnnounce`: title, body, then a confirmation step («إرسال لنحو n جهازاً؟», a preview, «لا يمكن سحب الإشعار»). Only with `announcements.send`. |

## ADR 0006: community users and permissions

| Request | Answer |
|---|---|
| Compose per type | ✅ `CommunityCompose`.<br>• **Verse post:** type selector, then «أرفق آية من المصحف».<br>• **Question:** text, plus at least one verse.<br>• **Answer:** send is disabled until a verse is attached.<br>• **Reflection:** the tag is added automatically and the verse is optional.<br>• **Comment:** short text with no verse.<br>Verse picking is in `VersePicker`: a mushaf page where you tap the ayah, then tap words to highlight («امسح التظليل»), then «أرفق الآية». |
| Author-visible states | ✅ `CommunityStates`:<br>• «بانتظار المراجعة. لا يراه غيرك بعد».<br>• «مرفوض» with the list reasons and the moderator's text, plus «عدّل وأرسل من جديد».<br>• «مقبول ومنشور».<br>Also the tabs in `ProfileMine`. |
| Threads | ✅ `CommunityThread`.<br>• Depth 0, 1 and 2 are indented 16 px per level, with a 2 px start line.<br>• From depth 3 on, replies stay at depth 2 and carry the overline «ردّاً على [اسم]». |
| Like, save, report, best answer | ✅ Action row on every post: heart, reply count, «احفظ», and «المزيد» → «إبلاغ» (sheet in `CommunityStates`).<br>«أفضل رد» is a green badge with a green border on the reply, labelled «اختاره السائل» or «اختاره المشرف». The author sees «اختر أفضل رد» under each reply. |
| Public profile and my profile | ✅ `Profile`: photo or initial, name, grade, «عضو منذ», points, posts, best answers, tabs.<br>`ProfileMine`: display name, «أظهر صورتي» (off by default), points log, progress to the next grade, and the tabs منشوراتي / بانتظار المراجعة / مرفوضة. |
| Grades | ✅ `Grades`. One badge style: 5 small dots (filled = level) and the name, on `accent.indigoTint`. It sits next to the name on every post and under the name on profiles. The thresholds shown are examples; they are set in Admin. |
| Admin suite | ✅ `Admin` hub: each tile shows its permission.<br>• `AdminReview`: accept, or reject with checkbox reasons plus an optional message; the reports tab: keep or delete, open the author's history.<br>• `AdminUser`: search, summary, trust switches per content type, role, history log, ban.<br>• `AdminRoles`: owner only; roles × permissions.<br>• `AdminFeedback`. |

## Screen walkthrough

### Home
| Request | Answer |
|---|---|
| «دليل اليوم» cut the verse | ✅ `HomeB`. The card is now «تذكرة»: the pull-quote (`quranBody`, indigo), a divider, then the **full verse** (`quranInline`) with the quote highlighted, the ref, and «افتح في المصحف». |
| Badge not in data | ✅ The badge is the **door name** from data («الأوامر»). Hidden doors' names show too (`HomeStates`, «القول الحسن»). |
| Card opens the Mushaf | ✅ The whole card links to the Mushaf at that verse. The action reads «افتح في المصحف». |
| 29-night month | ✅ `HomeStates`: 29 dots across the same width, with slightly wider gaps. |
| No location yet | ✅ `HomeStates`: rise and set show «لا نعرف» in `night.textFaint`, plus «حدّد مدينتك لنعرف أوقات القمر» → `CityPicker`. Phase, lit fraction and nights to the new moon still show. |
| الفرقان before design | ✅ A disabled tile: sunk fill, dashed border, muted icon, chip «قريباً», `aria-disabled`. |
| **«أوقات التسبيح»**, day arc, sky tokens, reminder change | ⏳ **Needs `specs/screens.md → Home`**, which is not in the handoff. Send it and v5 will design the arc, the windows with their full verses and «من اللغة», and the night/dawn/day/dusk sky tokens in both themes.<br>The Settings part is already done: the reminders are «قبل طلوع الشمس» and «قبل الغروب», with an **offset** picker («قبل بـ ٣٠ دقيقة») and no clock time. |

### Moon
| Request | Answer |
|---|---|
| One term | ✅ «الليلة» everywhere: Home «الليلة السادسة عشرة», Moon «الليلة» over «١٦». |
| 29-night منازل | ✅ `MoonStates`: 29 cells in the same 6-column grid. |
| «نبّهني» on state | ✅ `MoonStates`: filled `night.accent` with «سننبّهك باللحظات المميزة» and a note. It is the same switch as Settings → «لحظات القمر المميزة» (local, off by default). |
| No location | ✅ `MoonStates`. Shown: phase, lit fraction, distance, events. «لا نعرف»: rise, set, azimuth, altitude, and «visible from your location». |
| New event cards | ✅ On `Moon` and in `MoonStates`:<br>• ليلة الهلال, with البقرة ١٨٩ in full and its first sentence highlighted.<br>• خسوف القمر and كسوف الشمس, with start, peak and end; shown only when visible from the user's location; القيامة ٨.<br>• أقرب بدر وأبعد بدر.<br>• القمر قرب الزهرة.<br>Their dates are marked «بيانات مثال». |

### الذكر hub
| Request | Answer |
|---|---|
| «لماذا تقرأ القرآن» | ✅ Hidden in the APK (agreed). It is removed from `Quran`. |
| البثوث offline | ✅ `QuranStates`: a sunk notice with the wifi-off icon, «لا إنترنت. هذه القائمة من آخر تحديث، {time}…», and dimmed cards. |
| «وقتك مع القرآن» empty | ✅ `QuranStates`: 7 flat bars, plus the sentence in `COPY.md`. |
| Hub strip: lives or videos? | ◐ **Lives only**. Videos live under البثوث → فيديوهات. |

### Mushaf and listening
| Request | Answer |
|---|---|
| PlayerBar redesign | ✅ The mini bar (`MushafPlay`, `Hizb`) has:<br>• a 4 px progress line on top;<br>• the title and reciter, which tap to open the full player;<br>• previous and next ayah;<br>• a **56 px gold play/pause** (`status.liveBg`);<br>• a chevron to expand.<br>Full player (`PlayerSheet`):<br>• the position slider with times;<br>• an 88 px play/pause;<br>• the reciter row, which works with one reciter («القارئ المتاح الآن») and becomes a picker when there are two;<br>• speed ×٠٫٧٥ to ×١٫٥;<br>• repeat: بلا تكرار / الآية / مقطع, with a count stepper and from/to fields for a range. |
| Separate reading positions | ✅ Two headers.<br>• **Mushaf:** paper header with the overline «قراءتك في المصحف» (indigo), then surah and page, then juz and hizb.<br>• **Hizb:** night header with the overline «قراءة حزبك» (gold), hizb n of 60, and the gold progress. |
| Weak spots | ✅<br>• **Text size:** the frame is now one hairline in gold.<br>• **Full-screen:** `MushafFull`, tap to hide the bars.<br>• **AyahSheet:** 5 equal tiles with the icon over a one-word label (استمع / من هنا / احفظ / شارك / انسخ), so labels never wrap. The sheet is short, and the page scrolls so the selected ayah sits above it.<br>• **Marks:** page number under the page; juz and hizb in the header; ۞ quarter marks inline (see `Hizb`); a saved-page ribbon (gold, top corner).<br>• **Jump:** index search takes a surah name or page number (`MushafIndex`), and `MushafSearch` handles words.<br>• **Return:** the chip «عُد إلى قراءتك: البقرة، صفحة ٤٤» (`QuranStates`).<br>◐ **Landscape and tablet:** a phone in landscape shows one page scaled to the width, scrolling vertically. A tablet in landscape shows two facing pages (even page on the left), like the printed mushaf. |
| Morning reminder copy | ✅ «لقراءة حزبك». |
| Hizb end | ✅ `HizbEnd`: a sheet «وصلت إلى آخر الحزب ٢», «الحزب التالي: ٣» with its range and pages, «ابدأ الحزب ٣», «ليس الآن», and the note that the hizb moves only when the user does. |

### الأبواب
| Request | Answer |
|---|---|
| DoorTiles without verse | ✅ `RemindB`: number badge, name and chevron. |
| Rename to «تذكرة», changes per visit | ✅ Overline «تذكرة» with the caption «تتغيّر مع كل زيارة». |
| Main button by door visibility | ✅ `RemindStates`. A visible door gets «افتح الباب» (primary) and «افتح في المصحف». A hidden door gets «افتح في المصحف» only. |
| Sharing is plain text | ✅ `RemindStates` shows what is sent: the full verse, then «سورة النحل ٩٠». |

### المحفوظات
| Request | Answer |
|---|---|
| Sync note | ✅ `SavedStates` and Settings → حسابي في المجتمع. |
| Empty state | ✅ `SavedStates`: icon, «لا محفوظات بعد», how to save, and «افتح المصحف». |

### البثوث and videos
| Request | Answer |
|---|---|
| Lives are TikTok only | ✅ `Live`: every card opens TikTok. There is no in-app player for lives. |
| Videos section | ✅ `LiveVideos`: a second tab «فيديوهات» with the **same filter** (الكل / تلاوة / أسئلة / تدبّر), video cards (16:9 thumbnail, duration, title, type and host), and an empty state.<br>`VideoPlayer`: the YouTube player at the top, full-screen goes landscape, then the next videos. |
| Admin video kind | ✅ `AdminLive`: the switch «بث على تيك توك / فيديو يوتيوب», and a video form with link detection. |
| Weekly schedules | ✅<br>• **`AdminLive`:** أسبوعي / مرة واحدة, day chips, start and end, time zone, «ألغِ هذه المرة فقط», and delete.<br>• **The card (`Live`):** «مباشر الآن» inside the window; outside it the next occurrence in local time; «أُلغي هذه المرة» with the next date.<br>• **Reminder:** «ذكّرني بكل موعد». Its on state is «سنذكّرك كل أسبوع» plus «أوقف التذكير». |

### Mushaf index
| Request | Answer |
|---|---|
| Word search results | ✅ `MushafSearch`: query field, «n آية فيها «…». البحث يتجاهل التشكيل.», and rows with the full ayah, the match highlighted, and the ref with its page. The no-results state is on the same screen. |

### Settings, privacy and analytics (ADR 0008)
| Request | Answer |
|---|---|
| Reversal of DECISIONS #19 | ✅ The note is changed to the suggested text. Also recorded as DECISIONS #23. |
| Consent prompt | ✅ `Prompts`, shown **once at first launch**.<br>• Plain list of what is collected.<br>• A link to «ماذا نجمع».<br>• «نعم، أوافق» and «لا، شكراً» with **equal weight** (both secondary buttons).<br>The default is off until the user chooses. |
| «الخصوصية» group | ✅ `Settings`: the switch, «ماذا نجمع» (`WhatWeCollect`, with a نجمع / لا نجمع أبداً list) and «رأيك». |
| «رأيك» | ✅ `Feedback`: an optional category (يعجبني / لا يعجبني / مشكلة) and text. `AdminFeedback` has filter tabs. |
| City picker | ✅ `CityPicker`: search over the offline list with Arabic names (city and country), «استخدم موقعي الحالي», a check on the current city, and a no-results state. |
