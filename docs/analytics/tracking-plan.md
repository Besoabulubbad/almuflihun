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
