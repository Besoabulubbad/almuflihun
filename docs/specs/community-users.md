# Spec: users, review, points, grades and permissions (Community)

- **Status:** accepted 2026-11-26, recorded in [ADR 0006](../adr/0006-community-users-and-permissions.md). Point values and grade thresholds are tunable data.
- **Date:** 2026-11-26
- **Covers:** Community (posts, questions, answers, reflections, comments), moderation, Live management, announcements.


## 1. The idea in one paragraph

Anyone signed in may write anything in Community, but by default **everything waits for a moderator to accept it**. Every action builds a public **points** history. Moderators read that history and decide, person by person, whom to trust with publishing **without review**, and for which kinds of content. Roles give staff their powers. Grades are public labels computed from points; **they unlock nothing by themselves**, because trust is always a human decision.

## 2. Permissions (fixed in code)

| Code | What it allows | Who has it by default |
|---|---|---|
| `post.verse` | Share a verse (آية) | every signed-in user |
| `post.question` | Ask a question (سؤال) | every signed-in user |
| `post.reflection` | Write a reflection (تأمّل) | every signed-in user |
| `post.answer` | Answer a question (must attach a verse) | every signed-in user |
| `post.comment` | Comment or reply in a thread | every signed-in user |
| `direct.verse` … `direct.comment` | Publish that type **without review** (one permission per type) | granted per person |
| `review.queue` | Accept or reject items waiting for review | reviewer role and above |
| `reports.handle` | See reports; uphold or dismiss them | moderator and above |
| `content.hide` | Hide any published item | moderator and above |
| `user.ban` | Ban or unban a user (read-only while banned) | moderator and above |
| `user.trust` | Grant or remove `direct.*` for a person | moderator and above |
| `streams.manage` | Add, edit and end Live streams | moderator and above |
| `announcements.send` | Send an announcement push to everyone | admin |
| `roles.assign` | Give a person a role | admin |
| `roles.edit` | Create roles and change their permissions | owner only |

A person's effective permissions = their role's permissions + their personal grants, minus everything that writes if they are banned. A Cloud Function computes this and puts it on the person's sign-in token (custom claims); Firestore rules check it on every write. The app only shows or hides controls.

## 3. Roles

Roles are data in Firestore, edited in Admin by the owner (data, not code). Starting set:

| Role | Permissions |
|---|---|
| عضو (member) | `post.*` (all reviewed) |
| مراجع (reviewer) | member + `review.queue` |
| مشرف (moderator) | reviewer + `reports.handle`, `content.hide`, `user.ban`, `user.trust`, `streams.manage` |
| مدير (admin) | moderator + `announcements.send`, `roles.assign` |
| المالك (owner) | everything + `roles.edit`. Exactly one account: the owner's. It cannot be demoted, banned or deleted by anyone else. |

## 4. Review flow

1. A member submits an item. Without the matching `direct.*` it is **pending**: only its author sees it, labelled «بانتظار المراجعة».
2. Reviewers get it in the Admin review queue (oldest first) and either **accept** (published) or **reject** with a reason: pick one from a short list, write one freely, or both.
3. The author is notified either way. A rejected item stays visible to its author with the reason.
4. Items from people with `direct.*` publish immediately and can still be reported.
5. **Editing** a published item sends it back to review, unless the person has `direct.*` for that type. While in review the last accepted version stays visible.
6. Comments are reviewed too, like everything else. This is the heaviest load on moderators, so granting `direct.comment` early matters.

## 5. Reports

- Any signed-in user can report a published item, with a reason.
- Everyone with `reports.handle` gets a push for each new report.
- A moderator **upholds** (the item is hidden; the author loses points) or **dismisses** the report.
- An item reported by 3 different users is hidden automatically until a moderator decides.

## 6. Points (my proposal, changeable later because rules are data)

| Event | Points |
|---|---|
| Your item is accepted: question / answer / verse share / reflection | +10 / +10 / +5 / +5 |
| Your comment is accepted | +2 |
| Your answer is chosen «أفضل رد» | +15 |
| Someone likes your item | +1 (at most 20 a day from likes) |
| Someone saves your item | +2 (at most 20 a day from saves) |
| Your item is rejected | −5 |
| A report against you is upheld | −20 |
| You are banned | −50 |

- Points never go below 0. Points come only from published items and from other people's actions; your own likes and saves on your own items count for nothing.
- Every change is a line in the person's points log (event, item, moderator if any, time). Moderators read this log on the person's profile before granting trust.
- «أفضل رد» is chosen by the question's author; a moderator can change it.

## 7. Grades (my proposal)

Public, computed from total points, unlocking nothing by themselves:

| Grade | From |
|---|---|
| جديد | 0 |
| مشارك | 30 |
| مشارك فاعل | 150 |
| متمرّس | 500 |
| من أهل المجتمع | 1500 |

The owner approved these names (2026-11-26). They describe participation, not piety. The designer decides how they look.

## 8. Notifications added by this spec

| Push | Sent to |
|---|---|
| Your item was accepted / rejected | the author |
| New item in the review queue | people with `review.queue`, grouped: at most one push an hour |
| New report | people with `reports.handle` |
| You were granted / lost trust, or got a new role | that person |

These sit beside ADR 0005's live, new post, reply, update and announcement pushes. All are on by default, and each can be turned off in Settings.

## 9. Threads

- Full threads: replies to replies, without limit in the data.
- On screen, threads indent up to a depth the designer chooses; deeper replies show «ردّاً على [اسم]» instead of more indentation.

## 10. Profiles

Public profile: name and photo from Google (the user can hide the photo), grade, points, published items. Moderators also see the points log, accepted/rejected counts, reports, bans and the history of trust changes.

## 11. Screens the designer must add

- Community: «بانتظار المراجعة» and «مرفوض» states, compose flows per type (with verse attachment), threads, like/save/report actions, «أفضل رد».
- Profile (public) and My profile.
- Admin: review queue, reports (exists), user search and user history, grant trust, ban, role assignment, roles editor (owner), announcement composer (ADR 0005).
- Settings: notification switches per category.
