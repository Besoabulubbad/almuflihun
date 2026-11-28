# Tracking plan

The only list of what «مجتمع القرآن» collects (ADR [0008](../adr/0008-analytics-and-crash-reporting.md), amended by ADR [0020](../adr/0020-analytics-v7.md)). **Every event answers a product question. No question, no event.** Owned by the analysis agent (`.claude/agents/analytics-analyst.md`); changed only through a reviewed PR.

- **Status:** v1, 2026-11-28, rewritten for design v7 (ADR [0018](../adr/0018-design-v7.md)) under ADR [0020](../adr/0020-analytics-v7.md). Replaces v0 (2026-11-26) and its study-engine section; the differences are listed in "Changes from v0" at the end.

## Rules for every event

1. **Consent first.** Nothing is collected before the user agrees (F1 step 2 or Settings «ما نجمعه»). Turning the switch off stops collection at once and drops anything queued. We do not count declines; the consent rate is unknown by design.
2. **No identity.** No user ID (even when signed in), name, email, Google or Firebase Auth ID, profile or post author, precise location, contacts or advertising ID. No random install id of our own.
3. **No free text, with one exception.** Post, comment, moderator-message and feedback text, and search queries, are never sent. **The single exception:** the normalised words of a Quran search that returned **zero results**, sent under the conditions in "Zero-result searches" below, through a channel that carries no user or install link. Every other search is counts and codes only.
4. **Quran position** is allowed only at page, hizb, surah, door or station level. Never verse text, never a verse key (surah:ayah), never which word, root or lemma.
5. **Names** are `snake_case`, at most 40 characters. Parameters and their values come only from "Parameter values" below; values are short codes, small integers or buckets.
6. **Screen views** are sent manually from the navigation layer as `screen_view` with the destination's `screen_name` code (list below), never automatically, and **never with the destination's arguments** (no page, verse key, door entry, post id, user id or video id in `screen_name`).
7. **Measure without nudging.** Nothing here is used to build streaks, "days in a row", goals or reminders that push. وقتي مع القرآن and the WeekStrip stay on the device and are never sent.

## Product questions and the events that answer them

| # | Question | Events (parameters) |
|---|---|---|
| Q1 | Which areas do people use, how often, and for how long? | `screen_view` (`screen_name`); engagement time is automatic |
| Q2 | Do people read the Mushaf, how much, and where do they stop? | `mushaf_page` (`page`), at most once per page per visit; `reading_end` (`surface`, `pages`, `minutes_bucket`, `last_page`) |
| Q3 | Do people keep their «تلاوة» (the hizb reading), and do they continue or stop when one is finished? | `hizb_open` (`hizb`); `hizb_finish` (`hizb`) when C4 «أتممت» is reached; `hizb_next` (`hizb`) for «تابع إلى الحزب n الآن». «يكفي اليوم» is a `hizb_finish` with no `hizb_next`. Reminders: `notification_open` and `reminder_set` |
| Q4 | Is listening used, from where, and does it fail? | `audio_play` (`mode`, `reciter`) when a new recitation starts; `audio_download` (`surah`); `audio_error` (`kind`) |
| Q5 | Which doors interest people? Does «تذكرة» lead anywhere? | `door_open` (`door_id`); the تذكرة card is `dhikr_card_open` with `card` `tadhkira_door` or `tadhkira_mushaf` (Q19); the تذكرة widget is `widget_open` (Q22) |
| Q6 | Do people travel through الضياء? Where do they stop? | `station_open` (`station`); `journey_finish` |
| Q7 | Do people find what they search for, from which field, and what do they look for and not find? | `search` (`kind`, `results_bucket`, `terms`, `ops`, `scoped`, `forms`, `from`), once per submitted query, never per keystroke; `search_open_result` (`kind`, `rank`); zero-result words: `search_zero` (`query`, `kind`), see its own section |
| Q8 | Do people save, and what? | `save_add` (`kind`, `from`, `in_other`) on the tick, not on «احفظ»; `save_remove` (`kind`) |
| Q9 | Where do people drop out of posting in المجتمع, and where do posts start? | `compose_start` (`type`, `from`); `compose_submit` (`type`); `compose_cancel` (`type`, `step`). Acceptance and rejection rates come from Firestore, not Analytics |
| Q10 | Are lives and videos watched, and do live details and «ذكّرني» help? | `live_open_tiktok` (`show_id`, `from`); `remind_toggle` (`show_id`, `on`, `from`); `video_play` (`video_id`); live details (B3) views are `screen_view` `live_details` |
| Q11 | Which notifications help, and which annoy? | `notification_open` (`category`); `notification_setting` (`category`, `on`) |
| Q12 | Which settings do people change? | `theme_set` (`mode`); `location_source` (`source`); `reminder_set` (`which`, `on`) |
| Q13 | Do people update the APK? | `update_seen` (`version`); `update_open` (`version`) |
| Q14 | Who leaves? | `app_remove` (automatic, Android); FCM unregistered tokens |
| Q15 | What breaks? | Crashlytics crashes and non-fatals; non-fatal `content_mismatch` if a quote cannot be placed at runtime |
| Q16 | Which study tools are used, from where, and do they lead back to reading? | `study_open` (`tool`, `from`); `study_follow` (`tool`, `target`). **Entry points pending:** word tap is off and the AyahSheet has no study action (ADR 0018), so today only `from` = `search`, `collection`, `panel` and `index` can occur; the others stay defined for when entry points are decided |
| Q17 | Is advanced search understood, and does it find something? | the `search` parameters `kind`, `terms`, `ops`, `scoped`; `search_options_open` when C7 «خيارات البحث» is opened; found-rate from `results_bucket` and `search_open_result` |
| Q18 | Do people organise verses into collections, and use them? | `collection_create` (`count`: collections held after creating); `save_add` (`from`, `in_other`); `collection_share` (`target`); `note_add`; listening to a collection is `audio_play` `mode` `collection` |
| Q19 | Do the four tabs and الذكر's cards work: which are used, in which position, and what is never tapped? | `tab_open` (`tab`) on a TabBar tap; `dhikr_card_open` (`card`, `position`); views of الذكر from `screen_view` `dhikr` are the denominator |
| Q20 | Which AyahSheet actions are used, is their order right, and how often is the sheet closed with no action? | `ayah_sheet_open` (`from`); `ayah_action` (`action`, `position`) |
| Q21 | Is recitation controlled from the mini player or from outside the app, and are play scope and the sleep timer found? | `media_control` (`action`, `source`) for every control on a recitation already loaded; `player_option` (`option`, `value`) |
| Q22 | Are the home-screen widgets used, and which? | `widget_open` (`widget`, `link`); the quick bar's play button is `media_control` `source` `widget` |
| Q23 | Does «حسب الرسم» (spelling forms) help people find the right verses? | `spelling_form_open` (`rank`, `form_count`); whether forms were shown is the `search` parameter `forms` |
| Q24 | Is the direct line to the moderators (المشرفون) used, and for what kind of message? | `moderator_message_send` (`kind`, `with_verse`). Volume, reply time and outcome come from Firestore, not Analytics |
| Q25 | Does asking signed-out people to sign in lose them, and at which action? | `sign_in_prompt` (`reason`) when B7 opens; `sign_in_result` (`reason`, `result`) |
| Q26 | Do the core tasks (UX-STRATEGY §2, tasks 1, 2, 3 and 6) meet their tap targets, and how long do they take? | `task_time` (`task`, `seconds`, `taps`, `outcome`), definitions below |
| Q27 | How easy do people find the core tasks? | `ease_answer` (`task`, `score`), from the «كم كان هذا سهلًا؟» prompt, at most once a month per device |

### Task times (Q26)

Counted on the device, sent once per task as one `task_time`, bucketed. Taps are taps on the app's own controls between start and finish (keyboard keys are not taps).

| `task` | §2 | Start | Finish (`outcome` `done`) | `abandoned` |
|---|---|---|---|---|
| `tilawa` | 1 | a visit starts on الذكر | the Reader opens at the تلاوة page | not sent (a visit is not an intention) |
| `find_verse` | 2 | the search field is focused | a result of a text `kind` is opened | a query was submitted and the search was left with no result opened |
| `open_ayah` | 3 | the search field is focused | a result of `kind` `jump` is opened | as above, when the last query was a `jump` |
| `save_ayah` | 6 | «احفظ» is tapped in the AyahSheet | the tick saves (`save_add`) | the picker is closed without saving |

A visit starts at launch or on returning after 30 minutes or more (docs/specs/screens.md §1). Task success rates "without help" come from the quarterly usability rounds, not from these events.

### The ease prompt (Q27)

One question, «كم كان هذا سهلًا؟» 1–7, shown after a `done` task from the table above, at most once a month per device (the limit is kept on the device), always dismissible with no follow-up. Dismissing sends `score` `skip`. It never asks for text; written opinions stay in «أرسل ملاحظة» (Firestore).

## Zero-result searches

The words of a Quran search that found nothing are the clearest sign of what readers look for and cannot find (ADR 0020 §3). They are sent **only** when all of these hold:

- Consent is on (rule 1); turning it off deletes anything not yet sent.
- The search was submitted and returned **zero** results.
- `kind` is `letters`, `whole_word` or `phrase`. **Not** `root` or `lemma`: these are picked letters, not natural phrasing, a zero there means "not a Quranic root" which the count already shows, and a root chosen for a personal concern (illness, debt, divorce) is the most sensitive thing a person types. **Not** `exact`: the normalised form erases the tashkeel, the only thing exact mode compares, so the words would mislead. **Not** `mixed`, `surah` or `jump`: operators and references, counts are enough.
- The raw query has no digit (Arabic-Indic or Latin), no Latin letter and no `@`. If it has any, **nothing** is sent (we do not strip and send the rest), so phone numbers, emails and handles never leave the device.
- After the app's forgiving normalisation (diacritics removed, letter forms unified, the same as search, #91) it is 2 to 50 characters of Arabic letters and spaces.
- The same normalised query is sent at most once per device per day.

**What protects it.** `search_zero` is **not a Firebase Analytics event**, because Analytics attaches an app-instance id to every event, which would be the install link ADR 0020 forbids. It goes through the same `Analytics` wrapper and consent gate to a separate sink: **a callable Cloud Function** (owner, 2026-11-28) that stores a record no client can read, list or change, holding exactly `query`, `kind` and the month (`2026-11`), with no auth uid, installation id, device data, timestamp finer than the month, or request log of the payload. It cannot be joined to `search` events or to anyone. The function also limits the rate and drops repeats on the server, and logs nothing of the payload. Records are deleted after **6 months** (owner, 2026-11-28); the analyst reads them only in aggregate (the monthly review lists queries with their counts) and never copies them into issues or docs with anything that could identify the person who typed them.

## Remote settings (Firebase Remote Config)

ADR 0020 §1. The app **only downloads** a small settings file; it sends nothing about the user, and it sets no user property for the active settings. Defaults are bundled, so the app works offline and before the first fetch, and keeps the last fetched file when offline.

What it may switch, and nothing else without a plan change:

- the order of the الذكر cards, and showing or hiding each card;
- each section tile (ادرس، الضياء، الفرقان) shown or hidden;
- each المجتمع segment (النقاش، البثوث، الفيديوهات، المشرفون) shown or hidden, and the default segment;
- the UX-STRATEGY §3 variants: the AyahSheet order, search results (spelling forms first, or one flat list with forms as a filter), the المجتمع default (النقاش, or البثوث when a live is on), and the search placement.

Rules:

- **Everyone gets the same settings at the same time.** Conditions may use only app version and platform; never percentages, random percentiles, Analytics audiences or user properties.
- **Effects are read before and after a change**, by date, from the change log below. `position` on `dhikr_card_open` and `ayah_action` records the order the person actually saw, so a reorder is readable without knowing which file was active. That is why no `config` user property is needed.
- Remote Config fetches carry the Firebase installation ID as Firebase requires; we never read, store or export it.
- **The fetch may happen before consent** (owner, 2026-11-28), like the update check, so everyone gets the same layout from the first launch. «ما نجمعه» says so in one line: the app downloads its layout settings and sends nothing about you.

| Date | Setting | From → to | Question it tests |
|---|---|---|---|
| (none yet) | | | |

## No A/B tests

There are no experiments: no random install id, no 50/50 assignment, no Firebase A/B Testing, no percentage conditions in Remote Config (ADR 0020 §4). A §3 variant is tried by switching it for everyone and comparing periods. A later ADR can revisit this at about 1,000 users a week.

## User properties (no identity)

| Property | Values |
|---|---|
| `theme` | light, dark, system |
| `signed_in` | yes, no |
| `staff` | yes, no (never which role); lets staff use be excluded from community numbers |
| `location_source` | gps, city, none |

## Parameter values

Any value not listed here is a plan change.

| Parameter | Events | Values |
|---|---|---|
| `screen_name` | `screen_view` | `dhikr` (A1), `moon` (A2), `study` (A3), `door` (A4), `door_all`, `hikma`, `diyaa` (A5), `station` (A6), `community_discussion` (B1), `post`, `profile`, `live` (B2), `live_details` (B3), `videos` (B4), `video`, `moderators` (B5), `compose` (B6), `quran` (C1), `mushaf` and `hizb` (C2), `tilawa_done` (C4), `search` (C5, C6), `collections` (C8), `collection` (C9), `verse_picker`, `settings` (D1), `tilawa_settings` (D2), `my_time`, `reciter`, `offline_audio`, `appearance`, `notifications`, `location`, `city_picker`, `about`, `what_we_collect`, `feedback`, `sources`, `update`, `onboarding` (F1), `admin`, `admin_inbox`, `word_study`, `ayah_study`, `surah_overview`. Sheets (AyahSheet, C7, B7) are not screen views; they have their own events |
| `tab` | `tab_open` | dhikr, community, quran, settings |
| `card` | `dhikr_card_open` | search, tilawa, week, moon, tadhkira_door, tadhkira_mushaf, tile_study, tile_diyaa, tile_furqan |
| `position` | `dhikr_card_open`, `ayah_action` | 1–9, the place on screen as shown, counted from the top or the start |
| `page`, `last_page` | `mushaf_page`, `reading_end` | 1–604 |
| `pages` | `reading_end` | pages read in that reading, an integer |
| `surface` | `reading_end` | mushaf, hizb (the «تلاوة» reading) |
| `minutes_bucket` | `reading_end` | 0-1, 1-5, 5-15, 15+ |
| `hizb` | `hizb_open`, `hizb_finish`, `hizb_next` | 1–60 |
| `mode` | `audio_play` | ayah, from_here, hizb, surah (index row play), collection |
| `mode` | `theme_set` | light, dark, system |
| `reciter` | `audio_play` | the reciter's code (ali_jaber) |
| `surah` | `audio_download` | 1–114 |
| `kind` | `audio_error` | network, source, timing, storage, other |
| `door_id` | `door_open` | the door ids in `abwab-4.json` |
| `station` | `station_open` | 1–11 |
| `kind` | `search`, `search_open_result`, `search_zero` | surah, jump, letters (default, forgiving), whole_word, phrase, root, lemma, exact, mixed (more than one term with مع / أو / بدون); `search_zero` only letters, whole_word, phrase |
| `results_bucket` | `search` | 0, 1-10, 11-100, 100+ |
| `terms` | `search` | 1, 2, 3+ |
| `ops`, `scoped`, `forms` | `search` | yes, no (مع / أو / بدون used; a range set; spelling forms shown) |
| `from` | `search` | dhikr, quran, widget, link |
| `rank` | `search_open_result`, `spelling_form_open` | 1, 2-5, 6+ |
| `query` | `search_zero` only | the normalised words, 2–50 Arabic letters and spaces |
| `form_count` | `spelling_form_open` | 2, 3-5, 6+ (forms shown for that search) |
| `kind` | `save_add`, `save_remove` | ayah, quote |
| `from` | `save_add` | ayah_sheet, panel, word, search_results |
| `in_other` | `save_add` | yes, no |
| `count` | `collection_create` | 1, 2-5, 6+ |
| `target` | `collection_share` | text, community |
| `type` | `compose_start`, `compose_submit`, `compose_cancel` | verse, question, reflection, answer, comment |
| `from` | `compose_start` | fab, ayah_sheet, live, collection, thread, widget |
| `step` | `compose_cancel` | type, text, verse, send |
| `show_id`, `video_id` | Q10 events | ids of shows and videos in Firestore |
| `on` | `remind_toggle`, `notification_setting`, `reminder_set` | yes, no |
| `from` | `live_open_tiktok`, `remind_toggle` | banner, list, details, notification, widget |
| `category` | `notification_open`, `notification_setting` | reminder_morning, reminder_evening, live_reminder, live_now, answers, moderator_replies, new_posts, update, announcement, playback (one code per channel or push category) |
| `source` | `location_source` | gps, city, none |
| `which` | `reminder_set` | morning («قبل طلوع الشمس»), evening («قبل الغروب») |
| `version` | `update_seen`, `update_open` | the app's version name |
| `tool` | `study_open`, `study_follow` | panel, word, related_phrase, related_root, overview |
| `from` | `study_open` | ayah_sheet, mushaf, search, collection, panel, index |
| `target` | `study_follow` | mushaf, door |
| `from` | `ayah_sheet_open` | mushaf, hizb, search, door, station, collection, community, dhikr |
| `action` | `ayah_action` | listen, from_here, save, share, copy, ask, door |
| `action` | `media_control` | play, pause, next, previous, repeat, speed, stop |
| `source` | `media_control` | app (the mini player or any in-app control), notification, lock_screen (a media control while the device is locked), headset (any media button: headset, car, Bluetooth), widget |
| `option`, `value` | `player_option` | `scope`: surah, hizb, quarter, ayah, off; `sleep`: 15, 30, 60, end (end of the current scope), off. New timer choices are a plan change |
| `widget` | `widget_open` | quick_bar, tilawa, moon, tadhkira, community |
| `link` | `widget_open` | search, page, moon, ayah, community, compose |
| `kind` | `moderator_message_send` | question, report, suggestion |
| `with_verse` | `moderator_message_send` | yes, no (a verse attached) |
| `reason` | `sign_in_prompt`, `sign_in_result` | ask, answer, send, moderators, settings |
| `result` | `sign_in_result` | done, cancel, error |
| `task` | `task_time`, `ease_answer` | tilawa, find_verse, open_ayah, save_ayah |
| `seconds` | `task_time` | 0-5, 5-10, 10-30, 30-60, 60+ |
| `taps` | `task_time` | 1, 2, 3, 4-5, 6+ |
| `outcome` | `task_time` | done, abandoned |
| `score` | `ease_answer` | 1–7, skip |

## v7's event list, mapped (UX-STRATEGY §4)

| v7 name | In this plan |
|---|---|
| `search_submitted` | `search`; the query length is not sent (no question needs it; `terms` covers phrase length) |
| `search_result_opened` | `search_open_result` |
| `search_options_opened` | `search_options_open` |
| `ayah_sheet_opened`, `ayah_action` | `ayah_sheet_open`, `ayah_action` |
| `player_action` | `media_control` with `source` `app`; starting a new recitation is `audio_play` |
| `tab_opened`, `dhikr_card_tapped` | `tab_open`, `dhikr_card_open` |
| `section_opened` | `screen_view` |
| `tilawa_page_read`, `tilawa_finished` | `mushaf_page` with `reading_end` `surface` `hizb`; `hizb_finish` |
| `tilawa_reminder_opened`, `notification_opened` | `notification_open` |
| `spelling_form_tapped` | `spelling_form_open` |
| `widget_tapped` | `widget_open` |
| `media_control` | `media_control` |
| `moderator_message_sent` | `moderator_message_send` |
| `sign_in_prompt_shown`, `sign_in_completed` | `sign_in_prompt`, `sign_in_result` |
| `live_reminder_set`, `live_opened`, `video_played` | `remind_toggle`, `live_open_tiktok`, `video_play` |
| `collection_saved` | `save_add` |
| `post_started`, `post_sent` | `compose_start`, `compose_submit` |
| `task_time` | `task_time` |
| A/B assignment | not adopted (ADR 0020) |

## Where each event is sent

| Code point | Events |
|---|---|
| Navigation layer (every destination change) | `screen_view` |
| TabBar | `tab_open` |
| الذكر (A1) cards and tiles | `dhikr_card_open` |
| Reader (C2), Mushaf and تلاوة | `mushaf_page`, `reading_end`, `hizb_open`, `hizb_finish` and `hizb_next` (C4) |
| AyahSheet, wherever it opens | `ayah_sheet_open`, `ayah_action` |
| Player service (Media3 session; iOS remote commands) | `audio_play`, `audio_error`, `media_control` (all sources), `player_option` |
| Offline audio | `audio_download` |
| Search (C5, C6, C7) | `search`, `search_open_result`, `search_options_open`, `spelling_form_open`, `search_zero` (separate sink) |
| Collections (C8, C9) and the save picker | `save_add`, `save_remove`, `collection_create`, `collection_share`, `note_add` |
| Doors (A3, A4), الضياء (A5, A6) | `door_open`, `station_open`, `journey_finish` |
| المجتمع: Compose (B6), B2, B3, B4, B5, B7 | `compose_*`, `live_open_tiktok`, `remind_toggle`, `video_play`, `moderator_message_send`, `sign_in_prompt`, `sign_in_result` |
| Widgets (Glance, WidgetKit) on opening a deep link | `widget_open` |
| Notification tap handling; Settings الإشعارات | `notification_open`, `notification_setting` |
| Settings and onboarding | `theme_set`, `location_source`, `reminder_set`, user properties |
| Update check | `update_seen`, `update_open` |
| Task timer (one small on-device tracker) and the ease prompt | `task_time`, `ease_answer` |
| Quote placement at runtime | `content_mismatch` |
| Study engine screens (entry points pending) | `study_open`, `study_follow` |

The wrapper's allow-list and its test must include every event, parameter and value above; `search_zero` must be refused by the Firebase Analytics sink.

## What we deliberately do not collect

- Verse text, verse keys (surah:ayah), the word, root or lemma searched or studied, and which verse a sheet or study tool was opened on.
- Search queries, **except** the normalised words of zero-result searches under the conditions above, which are unlinked to any user or install.
- Post, comment, moderator-message, note and feedback text; collection names; the number of verses or notes a person holds.
- Names, emails, account, Auth, Google or installation IDs, who wrote a post or whose profile was opened.
- Exact location, contacts, the advertising ID.
- Which audience filter a person picks in البثوث (للأخوات and the others would reveal something personal; owner, 2026-11-28); interest by audience is read from `show_id`, joined to the show's data in the analysis only.
- وقتي مع القرآن, the WeekStrip's days, any "days in a row".
- Anything for experiments: no random install id, no variant assignment.

## Not measured (no question yet)

Reordering collections, local search history, P6 recent pages, the تذكرة swipe, the audience filter, post search in النقاش, which widgets are installed but never tapped, the reading amount in D2 (the unit stays the hizb, ADR 0018). Add a question first if one is needed.

## Changes from v0

No call sites exist yet outside `core/analytics`, so renames cost only the wrapper and its tests.

**Kept unchanged (name and parameters):** `screen_view`, `mushaf_page`, `reading_end`, `hizb_open`, `hizb_finish`, `hizb_next`, `audio_download`, `audio_error`, `door_open`, `station_open`, `journey_finish`, `save_add`, `save_remove`, `collection_create`, `collection_share`, `note_add`, `compose_submit`, `compose_cancel`, `video_play`, `notification_setting`, `theme_set`, `location_source`, `reminder_set`, `update_seen`, `update_open`, `content_mismatch`, `study_open`, `study_follow`; user properties unchanged.

**Removed:**
- `tadhkira_open` (`target`): merged into `dhikr_card_open`, `card` `tadhkira_door` / `tadhkira_mushaf`. The `Target` enum stays for `study_follow`.

**Changed values:**
- `search` `kind`: `words` renamed `letters`; added `whole_word`, `exact`. The same `SearchKind` serves `search_open_result` and `search_zero`.
- `search` `results_bucket`: `0, 1-5, 6-50, 50+` becomes `0, 1-10, 11-100, 100+` (v7's buckets).
- `audio_play` `mode`: added `surah`, `collection`.
- `audio_error` `kind`, `compose_cancel` `step`, `notification_*` `category`, `reminder_set` `which`, `theme_set` `mode`, `reciter`: were free strings, now fixed codes (make them enums).

**Added parameters to existing events:**
- `search`: `forms` (yes, no), `from` (dhikr, quran, widget, link).
- `search_open_result`: `rank`.
- `compose_start`: `from`.
- `live_open_tiktok`: `from`. `remind_toggle`: `from`.

**Added events:** `tab_open`, `dhikr_card_open`, `ayah_sheet_open`, `ayah_action`, `media_control`, `player_option`, `widget_open`, `search_options_open`, `spelling_form_open`, `moderator_message_send`, `sign_in_prompt`, `sign_in_result`, `task_time`, `ease_answer`, and `search_zero` (wrapper-gated, **not** sent to Firebase Analytics).

**Added sections:** zero-result searches, remote settings and its change log, no A/B tests, parameter values, v7 mapping, code points.
