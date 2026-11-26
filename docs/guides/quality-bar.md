# Quality bar: what "done" means for a screen

This is a production app. A screen is merged only when every item below holds. The PR template repeats this list; the automated review checks it.

## Behaviour

- [ ] Matches `design/` at the token level: no invented sizes, colours, text styles or durations (ADR 0001).
- [ ] Every state is implemented and has a screenshot: content, loading, empty, error, offline, no-location (where relevant), signed-out (where relevant), and each permission variant (member, reviewer, moderator, admin, owner) where the screen changes with it.
- [ ] Every action gives visible feedback within 100 ms (press state, then result or progress).
- [ ] Nothing the user typed is ever lost: drafts survive rotation, process death and leaving the screen.
- [ ] Works offline as far as the feature allows, and says clearly what needs the internet.
- [ ] Back (including Android predictive back) always goes where the user expects; deep links and notifications land on the right item with a sensible back stack.

## Motion and feel

- [ ] Transitions, sheets, paging and highlights use the motion tokens only.
- [ ] Reduced motion is honoured (DESIGN §9).
- [ ] Haptics only where the design lists them.
- [ ] No layout jump when content loads (reserved space or skeletons).

## Performance budgets (mid-range Android, release build)

| Measure | Budget |
|---|---|
| Cold start to Home drawn | ≤ 1.5 s |
| Any screen transition | no dropped frames (60 fps; 120 fps devices smooth) |
| Mushaf page turn, including its font | ≤ 100 ms to fully drawn after the first visit; no blank page |
| Playing-word highlight vs audio | within 50 ms of the timing data |
| Search results (offline, whole Quran) | ≤ 300 ms |
| APK size | measured on every release; growth over 1 MB needs a note in the PR |

Measured with Macrobenchmark and baseline profiles on Android; regressions fail CI once the baselines exist.

## Accessibility

- [ ] TalkBack reads everything in order, in Arabic; icon-only buttons have Arabic labels; ﴿n﴾ reads as «آية n»; highlights are announced (DESIGN §9).
- [ ] No clipping at 130% font size (screenshot test).
- [ ] Touch targets ≥ 44 × 44.
- [ ] Contrast checked by the token contrast test in both themes.

## Correctness of Quran content

- [ ] Every verse shown comes from the verse data; every quote and highlight is inside its verse (build-time test).
- [ ] No verse is cut or line-clamped.

## Privacy

- [ ] Analytics: only events in the [tracking plan](../analytics/tracking-plan.md), reviewed by the analytics-analyst agent.
- [ ] No personal data in logs.
