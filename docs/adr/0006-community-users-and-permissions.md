# 0006 — Community: review by default, trust granted by moderators, roles as data

- **Status:** accepted
- **Date:** 2026-11-26
- **Deciders:** Mohammad Abu Lubad, Claude
- **Tags:** community, security, moderation

## Context and problem statement

Community lets people share verses, ask, answer, reflect and comment, with full threads. The owner wants anyone to be able to take part, but nothing to appear unchecked; a public points history so moderators can judge whom to trust; grades shown publicly; and staff roles with distinct powers, with the owner's account above all. Every change to the APK means handing it out again, so changing who may do what should not need a release. The full rules are in [the spec](../specs/community-users.md).

## Decision drivers

- Nothing written by an unknown person is public before a human accepts it.
- Trust is a human decision, per person and per content type, informed by history.
- Enforcement is server-side: Firestore rules and Cloud Functions, never the client.
- Roles and point rules change without an APK release.

## Options considered

1. **Roles and point rules as data**, permissions fixed in code, per-person trust grants, everything enforced through custom claims and rules.
2. **Fixed roles in code** (member, moderator, admin, owner), with grades auto-unlocking permissions.
3. **Per-person permission toggles only**, no roles.

## Decision outcome

**Chosen: Option 1.**

- A fixed list of permission codes in code (spec §2). Roles are Firestore documents mapping a name to permission codes, editable by the owner in Admin. Starting roles: عضو، مراجع، مشرف، مدير، المالك.
- Every item from a member is **pending** until a reviewer accepts it. Moderators grant `direct.<type>` per person to publish that type without review. Edits re-enter review unless the person has `direct` for that type; the last accepted version stays visible meanwhile.
- Rejections carry a reason chosen from a list, written freely, or both; the author is notified and keeps seeing the rejected item.
- Reports: each new one pushes to everyone with `reports.handle`; three reports from different users hide an item until a moderator decides.
- Points are awarded only by Cloud Functions from events, logged per person, never below zero. Grades are public labels from points (جديد، مشارك، مشارك فاعل، متمرّس، من أهل المجتمع) and unlock nothing by themselves.
- «أفضل رد» is chosen by the question's author; a moderator can change it.
- Full threads; the display stops indenting at a designed depth and shows «ردّاً على [اسم]» instead.
- The owner account is set once by a maintainer script, and cannot be demoted, banned or deleted by others. Every later role is assigned in the app.
- Effective permissions = role + personal grants − writes if banned, computed by a function into custom claims and refreshed on every change.

### Consequences

- Good: no unreviewed content from unknown people; trust grows person by person.
- Good: new roles or new point values need no APK.
- Bad: moderators carry a heavy review load, especially comments. Mitigated by granting `direct.comment` early and by the grouped hourly review-queue push.
- Bad: custom claims refresh only when the sign-in token refreshes. Mitigated: the function forces a token refresh, and the app reloads the token when it is told the person's permissions changed.
- Bad: much new UI to design (spec §11).

### Confirmation

- Emulator tests cover every rule: a member's item is created pending; a member cannot publish directly, edit others' items, set points or set roles; a banned user cannot write.
- Function tests cover points, grades, reports auto-hide and claim computation.

## Links

- [Spec: users, review, points, grades and permissions](../specs/community-users.md)
- ADR [0005](0005-firebase-blaze-push-notifications.md) (push), `design/DECISIONS.md` #1–2, #15–18
