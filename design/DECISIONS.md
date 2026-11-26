# Decisions (26 Nov 2026)

These answer the ten review points. **Owner** marks a decision taken by the product owner. **Design** marks one taken in the design and approved through this file.

| # | Problem | Decision | By |
|---|---|---|---|
| 1 | Live and Community need a server | **No own server. Use Firebase.**<br>**Live:** an admin list of streams (Firestore). YouTube Live streams play inside the app through the official YouTube player. TikTok streams open in the TikTok app: TikTok's official embed covers single videos and creator profiles, not LIVE.<br>**Community:** Firebase Auth + Firestore, a report button, and admin moderation. Moderation stays a human job, so this needs someone assigned to do it. | Owner |
| 2 | Community quoted Quran as typed text | **Fixed.** Posts, questions and answers attach verses by key (`surah:ayah`) plus an optional highlighted word range, chosen from the mushaf. The full verse is shown in a verse card. Typing «…» Quran text in a post is not offered. The rule is shown at the top of the screen. | Design |
| 3 | Two different Quran texts | **One look everywhere: Uthmani (Hafs).**<br>The mushaf uses the printed-page fonts. Every card uses the KFGQPC HAFS Uthmanic Script font with the Uthmani Hafs text of the same source.<br>Quotes and highlights are no longer tied to one text. Each now carries a **word range** (`words: [start, end]`, 1-based, see README), so it can be placed on any edition. | Owner + Design |
| 4 | Printed pages vs flowing text | **Printed Madani pages (604)** for the Mushaf *and* the Hizb screen.<br>The Hizb screen opens the printed page where the hizb starts. The ayahs before the start, and after the end on the last page, are dimmed. A «۞ الحزب n» marker sits at the start. The header shows the page range (hizb 2 = pages 11–21). | Owner |
| 5 | Licence trap | **Do not reuse quran_android code** (GPL-3.0). Use data and fonts only, and record each source's licence in «مصادر النص والصوت» (Settings).<br>**Tanzil:** the text must stay unmodified, with attribution.<br>**KFGQPC fonts and QPC page data:** check their terms.<br>**Recitation audio:** check each reciter's permission. | Design → engineering checks |
| 6 | Lunar day and location | **Astronomical, on the device.** Night 1 is the first evening after the conjunction (new moon) on which the moon sets after the sun at the user's location. Moonrise and moonset use the device location (permission asked). Without permission, the user picks a city. Nothing leaves the device. | Owner |
| 7 | Language and theme | **Arabic only.** **Themes:** عادي / ليلي / حسب الجهاز, from Settings, default «حسب الجهاز». The dark palette is in `tokens.json → color.dark`. | Owner |
| 8 | Code rules vs Sojourn's setup | **Recommended: approve Sojourn's machinery.** `core:designsystem`, the token generator, detekt rules and Lucide icons converted to vectors meet the same intent. `DESIGN.md §0` now states the intent, not the mechanism. | Waiting for owner sign-off |
| 9 | Missing screens | **Designed:** Settings, and the Mushaf index (السور / الأحزاب / الأجزاء). The Home settings button and the الذكر gear now open Settings.<br>**Still open:** onboarding, and الفرقان (waits for its content). | Design |
| 10 | Tree counts in station 6 | The design uses النخل ٢٠ and الأعناب ١١ (verse counts checked against the text). The source file says ١٩ and ١٠. **The content owners must confirm.** | Waiting for owner |


# Round 2 (26 Nov 2026), after the second review

| # | Point | Decision | By |
|---|---|---|---|
| 11 | Word ranges missing on الحكمة | **Fixed.** All 28 quotes in the الحكمة passages (opens, closes, steps, verdict) now have `words`. Every one of the 115 door quotes has a range and was checked. | Design |
| 12 | The card font needs its own text | **Cards show the King Fahd Complex Hafs text** in its KFGQPC HAFS Uthmanic Script font, not Tanzil Uthmani.<br>**Tanzil simple** is only the base that content is written and matched against. Credit both in «مصادر النص والصوت». | Design (reviewer's proposal) |
| 13 | Tanzil "unmodified" condition | **Ship Tanzil's file untouched.**<br>Splitting off the basmala, dropping ۞ and dropping pause-mark tokens happen **when the app reads the file**, not in the file.<br>The `verses` maps inside `data/*.json` are test fixtures that hold that read-time output. They are not shipped as the Quran text. | Design (reviewer's proposal) |
| 14 | Page fonts are heavy | **Bundle all 604 page fonts and the page layout data in the APK.** The mushaf works offline from the first launch. Settings says so. | Owner |
| 15 | Community sign-in | **Google account only.** Use the GitLive Firebase SDK for Kotlin Multiplatform, since Firebase has no official one. The rest of the app needs no account. | Owner |
| 16 | Firestore rules | The repo is public, so the security rules are the only protection. Users write only their own posts and replies. Live entries and moderation are writable only by accounts with the `moderator` claim. Every post must reference a valid verse key. | Design → engineering |
| 17 | Account deletion | Settings → «حسابي في المجتمع» → «حذف حسابي ومشاركاتي» deletes the account and all its posts (Play Store requirement). | Design |
| 18 | Who manages Live and reports | **Inside the app, for moderator accounts.** A hidden «الإدارة» screen (Settings, moderators only) adds or edits streams and handles reports. Moderators must be named before release. | Owner |
| 19 | No tracking | **Superseded by #23.** ~~Never add Analytics, Crashlytics or any SDK that sends usage data.~~ Only Community and Live talk to the network. Settings states this. | Design |
| 20 | ±1 day lunar adjustment | **No adjustment.** The astronomical rule only, explained in Settings. The month will often start a day before Umm al-Qura; that is accepted. | Owner |
| 21 | Audio scope | Recitation downloads **surah by surah** («تنزيل التلاوة»).<br>«استمع للكلمة» needs a word-by-word audio source with a licence that allows it. If none is cleared, **hide that action**. Do not substitute. | Design |
| 22 | Separator dot | «·» next to Arabic-Indic digits reads as ٠ (zero). It is **replaced by «،»** in every screen, and it is banned in copy (DESIGN §8). | Design |

Still open: the content of «من نحن وكيف نفهم ديننا», onboarding, الفرقان, the station-6 tree counts, the logo choice, and DECISIONS #8 sign-off.


# Round 3 (26 Nov 2026), design v4, answering «Requests to the designer»

| # | Point | Decision | By |
|---|---|---|---|
| 23 | Analytics | **Opt-in** analytics and crash reports (Firebase). Off until the user says yes in a one-time prompt with equal-weight buttons. The Settings note reads «نجمع بيانات استخدام مجهولة لتحسين التطبيق، فقط إن وافقت. لا نبيعها ولا نشاركها». Location, reading content and names are never collected. | Owner (ADR 0008) |
| 24 | Lives and videos | Lives are **TikTok only**, on weekly schedules or as one-offs. Videos are **YouTube**, watched in-app. The hub strip shows lives only. | Owner |
| 25 | «دليل اليوم» | Renamed **«تذكرة»**. It changes on every visit and shows the full verse under its pull-quote. On Home it opens the Mushaf at that verse. | Owner |
| 26 | Sharing | Plain text only: the full verse, then its reference. No image, no app name, no link. | Owner |
| 27 | Lunar term | **«الليلة»** everywhere (Home and Moon). | Design |
| 28 | «لماذا تقرأ القرآن» | Hidden in the APK until its content exists. | Design (engineering proposal) |
| 29 | Notification permission | Asked in-app first, at the first moment a notification is needed, never at first launch. | Design |
| 30 | «أوقات التسبيح» | **Open.** It waits for `specs/screens.md → Home`. Only its Settings part (reminders tied to sunrise and sunset with an offset) is done. | Waiting for spec |
