# 0007 — Recitation audio and timings from the Quran Foundation API, through our backend

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** quran, audio, backend, licensing

## Context and problem statement

Listening needs audio files and, for the playing-word and playing-ayah states, timings per ayah and per word (`design/DESIGN.md` §6, MushafPlay). The owner chose two reciters, علي جابر and محمد أيوب. Research (2026-11-26) found:

- QUL has word timings for علي جابر (resource 404) but none for محمد أيوب; QUL licence pages say "no copyright information".
- quran.com (Quran Foundation) has علي جابر (chapter reciter 158, marked «تجريبي»); whether his files return word `segments` is not documented. محمد أيوب is not on quran.com.
- The Content API uses OAuth2 `client_credentials`. Its quickstart says a mobile app must not embed the `client_secret` and must call its own backend.
- The terms allow user-requested offline downloads of recitation audio kept indefinitely in the app's private storage, with credit to Quran Foundation and the reciter. An offline copy of recitation records (audio URLs and timings) must come through **Content Sync**, refreshed at least every 7 days when connected. Audio on `download.quranicaudio.com` and the other QF hosts is QF content under these terms and must not be rehosted.

## Decision drivers

- Clear, written terms for audio and timings.
- No secret in the APK.
- Offline listening with highlighting after a user downloads a surah.

## Options considered

1. **Mix:** quranicaudio files + QUL 404 timings for علي جابر; everyayah per-ayah files for محمد أيوب.
2. **Quran Foundation API through our backend** (Cloud Functions, ADR 0005).
3. **Replace محمد أيوب** with a quran.com reciter that has timings.

## Decision outcome

**Chosen: Option 2, starting with علي جابر only** (owner). محمد أيوب is deferred; adding him needs a source outside Quran Foundation and a new ADR.

- **Credentials.** The owner registers the app in the Quran Foundation Developer Console. The client ID and secret live in Firebase Secret Manager, read only by Cloud Functions. The APK never sees them.
- **Proxy.** A Cloud Function exposes only what the app needs: the chapter-recitation list and per-chapter timings (`segments=true`) for allow-listed reciter IDs, and Content Sync for the `chapter_recitations` group. It caches the 1-hour access token, re-requests on 401, backs off on 429, and rate-limits callers.
- **Offline.** The app keeps recitation records (audio URLs and timings) from Content Sync through the proxy and re-syncs at least every 7 days when online. When a record is removed or its URL changes, the app deletes or replaces the downloaded file.
- **Audio.** Streamed from, or downloaded on the user's request from, the `audio_url` the API returns (QF hosts), surah by surah, into the app's private storage. Never rehosted. Deleted when the user removes it or access ends.
- **Credit.** Settings → «مصادر النص والصوت» credits Quran Foundation and the reciter.
- **Playback.** One file per surah; «استمع للآية», «استمع من هنا», the playing ayah and the playing word all come from the timestamps (`timestamp_from`, `timestamp_to`, `segments` as `[word_position, start_ms, end_ms]`). `duration` is ignored because legacy data can be negative.

### Consequences

- Good: written terms cover audio, offline downloads and timings.
- Good: more reciters later are a configuration change if they are on quran.com.
- Bad: listening depends on our backend and on QF's API being up; downloaded surahs still play offline.
- Bad: the backend must avoid becoming an open proxy. Mitigated: allow-listed endpoints and reciter IDs, rate limits, the budget alert (ADR 0005). Firebase App Check is added once the app is distributed through Play (Play Integrity does not vouch for sideloaded APKs).
- Risk: علي جابر is marked beta and his word `segments` are unconfirmed. **The first audio spike checks this.** If segments are missing, the fallback is QUL 404's timings for the same file, verified by comparing durations; if those do not match, playing-word marking is hidden and only the playing ayah is shown.
- Neutral: the mushaf fonts and layout stay as in ADR 0004 (QUL, committed). This ADR covers audio only.

### Confirmation

- Function tests (emulator) cover the allow-list, token refresh and rate limits.
- A secret-scanning check fails CI if a QF client secret pattern appears in the repo or the APK.
- The credits screen lists Quran Foundation and علي جابر.

## Links

- [Quran Foundation quickstart](https://api-docs.quran.foundation/docs/quickstart/), [Developer terms](https://api-docs.quran.foundation/legal/developer-terms/), [Chapter reciter audio file](https://api-docs.quran.foundation/docs/content_apis_versioned/4.0.0/chapter-reciter-audio-file/), [Content Sync](https://api-docs.quran.foundation/docs/tutorials/content-sync/getting-started/)
- ADR [0004](0004-quran-assets-in-repo.md), [0005](0005-firebase-blaze-push-notifications.md); `design/DECISIONS.md` #5, #21
