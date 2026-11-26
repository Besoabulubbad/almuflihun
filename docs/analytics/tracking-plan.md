# Tracking plan

The only list of what the app collects (ADR [0008](../adr/0008-analytics-and-crash-reporting.md)). **Every event answers a product question. No question, no event.** Owned by the analysis agent (`.claude/agents/analytics-analyst.md`); changed only through a reviewed PR.

- **Status:** v0, 2026-11-26, a first draft for the owner and the agent to refine.

## Rules for every event

1. Collected only after consent; nothing before.
2. No identity: no user ID, name, email, Google ID or precise location.
3. No free text: no post content, search queries or feedback text. Counts and categories only.
4. Quran position is allowed at page, hizb, surah, door or station level. Never verse text.
5. Names are `snake_case`, at most 40 characters; parameters come from the lists below; values are short codes.
6. Screen views are sent manually from the navigation layer with the destination's key name (`screen_name`), not automatically.

## Product questions and the events that answer them

| # | Question | Events (parameters) |
|---|---|---|
| Q1 | Which areas do people use, how often, and for how long? | `screen_view` (`screen_name`); engagement time is automatic |
| Q2 | Do people read the Mushaf, how much, and where do they stop? | `mushaf_page` (`page`), at most once per page per visit; `reading_end` (`surface`: mushaf, hizb; `pages`; `minutes_bucket`; `last_page`) |
| Q3 | Do people follow their hizb? | `hizb_open` (`hizb`); `hizb_finish` (`hizb`); `hizb_next` (`hizb`) |
| Q4 | Is listening used, in which mode, and does it fail? | `audio_play` (`mode`: ayah, from_here, hizb; `reciter`); `audio_download` (`surah`); `audio_error` (`kind`) |
| Q5 | Which doors interest people? Does «تذكرة» lead anywhere? | `door_open` (`door_id`); `tadhkira_open` (`target`: door, mushaf) |
| Q6 | Do people travel through الضياء? Where do they stop? | `station_open` (`station`); `journey_finish` |
| Q7 | Do people find what they search for? | `search` (`kind`: surah, jump, words; `results_bucket`: 0, 1-5, 6-50, 50+); `search_open_result` (`kind`) |
| Q8 | Do people save, and what? | `save_add` (`kind`: ayah, quote); `save_remove` (`kind`) |
| Q9 | Where do people drop out of posting in Community? | `compose_start` (`type`); `compose_submit` (`type`); `compose_cancel` (`type`, `step`). Acceptance and rejection rates come from Firestore, not Analytics. |
| Q10 | Are lives and videos watched? | `live_open_tiktok` (`show_id`); `video_play` (`video_id`); `remind_toggle` (`show_id`, `on`) |
| Q11 | Which notifications help, and which annoy? | `notification_open` (`category`); `notification_setting` (`category`, `on`) |
| Q12 | Which settings do people change? | `theme_set` (`mode`); `location_source` (`source`: gps, city, none); `reminder_set` (`which`, `on`) |
| Q13 | Do people update the APK? | `update_seen` (`version`); `update_open` (`version`) |
| Q14 | Who leaves? | `app_remove` (automatic, Android); FCM unregistered tokens |
| Q15 | What breaks? | Crashlytics crashes and non-fatals; non-fatal `content_mismatch` if a quote cannot be placed at runtime |

## User properties (no identity)

| Property | Values |
|---|---|
| `theme` | light, dark, system |
| `signed_in` | yes, no |
| `staff` | yes, no (never which role) |
| `location_source` | gps, city, none |

## What we deliberately do not collect

Verse text, post or feedback text, search terms, names, emails, account IDs, exact location, contacts, the advertising ID.

## Study engine (ADR 0012), reviewed 2026-11-26

| # | Question | Events (parameters) |
|---|---|---|
| Q16 | Which study tools are used, from where, and do they lead back to reading? | `study_open` (`tool`: panel, word, related_phrase, related_root, overview; `from`: ayah_sheet, mushaf, search, collection, panel, index); `study_follow` (`tool`; `target`: mushaf, door) when a verse or door is opened from a study tool |
| Q17 | Is advanced search understood, and does it find something? | `search` gains `kind` values root, lemma, phrase, mixed, plus `terms` (1, 2, 3+), `ops` (yes, no: and/or/not used) and `scoped` (yes, no); found-rate from the existing `results_bucket` and `search_open_result` (`kind`). Sent once per completed query, never per keystroke. |
| Q18 | Do people organise verses into collections, and use them? | `collection_create` (`count`: 1, 2-5, 6+, collections held after creating); `save_add` gains `from` (ayah_sheet, panel, word, search_results) and `in_other` (yes, no: the verse is already in another collection); `collection_share` (`target`: text, community); `note_add` |

Review notes:

- **Never collected:** the query, the word, root or lemma searched or studied, which verse or surah a study tool was opened on, collection names, note text, the number of verses or notes per user. A root or lemma is chosen by the user's own concern (illness, debt, divorce, sin) and, joined to the app instance, would build a profile of religious interest; rule 3 covers it as free text. Surah-level position is allowed by rule 4 but no question needs it here.
- **Reuse:** four separate open events became one `study_open` with `tool`; saving to a collection, including «save results», reuses `save_add` instead of `collection_add` and `search_save_results`, so Q8 and Q18 read from one event.
- **Added to answer the questions:** `study_follow` (Q16's "lead to reading" had no event), `mixed`, `ops` (Q17's and/or/not), `count` and `in_other` (Q18: several collections, a verse in several).
- **Not measured, no question yet:** reordering, search history, P2 copy, P5 play scope, P6 recent pages, P8 sleep timer. Add a question first if needed.
- The wrapper's allow-list and its test must include every event, parameter and value above.
