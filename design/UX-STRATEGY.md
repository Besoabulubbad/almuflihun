# UX strategy: design now, keep improving after release

The v7 design was not tested with users before handoff, by the owner's choice. It is the design team's best judgement, checked against all 79 v5 screens and the reading and contrast audit. **After release, we keep improving it with real use.** This file is that plan, and the code must be built to support it (see §4).

## 1. Who it is for

| | New user | Serious student |
|---|---|---|
| Wants | Read, listen, a daily verse, ask a simple question | Search phrases and roots, keep collections, go deeper |
| Fears | Clutter, getting lost | Shallow tools, slow paths, losing work |
| Gets | A calm surface: search, continue, the hub | The same surface, plus options one level down |

**Principle:** simple surface, deep layers. Nothing for the expert sits on the first screen; everything for the expert is one tap away.

## 2. Core tasks and targets

Counted from the app opening on الذكر. These are the targets to measure after release.

| # | Task | Path in v7 | Target taps |
|---|---|---|---|
| 1 | Do today's تلاوة | الذكر → «أكمل من الصفحة n» | 1 |
| 2 | Find a verse by a phrase («يا ايها الذين امنوا») | الذكر search → type → result | 2 |
| 3 | Open a known ayah (٢:٢١٩) | Search → type → result | 2 |
| 4 | Listen to a surah | القرآن → play on the surah row | 2 |
| 5 | Pause or skip the recitation | Mini player (any screen), widget or notification | 1 |
| 6 | Save an ayah | Tap ayah → احفظ → tick | 2–3 |
| 7 | Ask about an ayah | Tap ayah → اسأل عنها → write → send | 3 (+ sign-in once) |
| 8 | See how a word is written across the Quran | Search the word → spelling forms | 2 |
| 9 | Find the next live and set a reminder | المجتمع → banner or البثوث → ذكّرني | 2–3 |
| 10 | Message the moderators | المجتمع → المشرفون → write → send | 3 |

## 3. Where we are least sure

These were decided by judgement and are built as swappable variants (§4).

| Place | v7 choice | Alternative to try later |
|---|---|---|
| الذكر card order | Search → تلاوة → week → moon → تذكرة → tiles | تذكرة before the week; week folded into the تلاوة card |
| AyahSheet | استمع filled, then من هنا, احفظ, شارك, انسخ | احفظ second; «اسأل» as a sixth tile |
| Search results | Spelling forms, then grouped by surah | One flat list in Mushaf order, forms as a filter only |
| المجتمع default | Opens on النقاش | Opens on البثوث when a live is on now |
| Search placement | A field on الذكر and القرآن | A search icon in every tab's top bar |

## 4. What the code must support from day one

1. **Opt-in analytics** (DECISIONS #23: off until the user agrees; never location, reading content or names). Events only, no text:
   - `search_submitted` (query length, mode, result count bucket: 0 / 1–10 / 11–100 / 100+), `search_result_opened`, `search_options_opened`
   - `ayah_sheet_opened`, `ayah_action` (which action)
   - `player_action` (play, pause, next, previous, repeat, speed)
   - `tab_opened` (which tab), `dhikr_card_tapped` (which card), `section_opened`
   - `tilawa_page_read`, `tilawa_finished`, `tilawa_reminder_opened`
   - `spelling_form_tapped`
   - `widget_tapped` (which widget), `notification_opened` (which channel), `media_control` (source: app, notification, lock screen, headset)
   - `moderator_message_sent`, `sign_in_prompt_shown`, `sign_in_completed`
   - `live_reminder_set`, `live_opened`, `video_played`
   - `collection_saved`, `post_started`, `post_sent`
   - `task_time` for tasks 1, 2, 3 and 6 (start to finish, in seconds)
2. **Zero-result searches.** The most useful signal for search. Owner decision needed: DECISIONS #23 forbids collecting reading content. Proposal: with consent, send only the normalised text of queries that returned **zero results**, never linked to a user. If the owner says no, keep only the count.
3. **Remote config + feature flags** for the variants in §3, the الذكر card order, each section tile and each المجتمع segment.
4. **A/B assignment** by a random install id (not the account), 50/50, one experiment at a time.
5. **In-app feedback**: «أرسل ملاحظة» in Settings (exists), plus a one-question prompt after a task, at most once a month: «كم كان هذا سهلًا؟» 1–7.

## 5. The improvement loop after release

**Every month (one hour):**
- Look at the funnel for each core task: started → finished, and time.
- Look at the zero-result searches (or their count).
- Look at what is never tapped in الذكر.
- Read every feedback message.
- Pick at most **one** change for the next release.

**Every quarter, a short usability round:**
- Five people from the community (TikTok audience), mixed: new and serious, women and men, at least one reader over 50.
- The 10 tasks in §2, think aloud, recorded with consent. The owner runs it in Arabic; the design team prepares the script and analyses the notes.
- Fix what three or more people struggled with.

**Experiments** (when there are enough users, about 1,000 a week): run one of the §3 variants for two weeks. Keep the variant only if task success or time improves clearly and nothing else gets worse.

**When we call a task "good"**
- At least 80% finish it without help.
- The median taps meet §2.
- It scores 6 or more out of 7 on the ease question.
- Returning users still do it a week later without searching for it.

## 6. Rules for changing the design

- Change the design system first (`tokens.json`, `DESIGN-SYSTEM.md`), then screens.
- Every change has a reason from the data or the sessions, written in the decision log.
- Prefer removing over adding. A new feature has to beat an existing one in use to earn a place on the first screen.
- Nothing beyond what users need: new study tools go one level down, in ادرس or the search options.
