# Icons (v7)

**Set:** [Lucide](https://lucide.dev), outline only.
- Stroke 1.75 at 24 dp grid. Sizes `iconS` 16, `iconM` 20, `iconL` 24.
- Colour comes from the component (§6 of DESIGN-SYSTEM.md): `accent` for leading row icons, `iconQuiet` for chevrons and inactive icons, `text` for actions.
- On Android use the Lucide Compose port or exported vector drawables. On iOS use the Lucide SVGs as template images. Never mix in another icon set.
- **RTL:** mirror `arrow-right` (back), `chevron-left` and `list-start`. Never mirror media icons, `search`, `volume-2`, `repeat`.

## Where each icon is used

| Place | Meaning | Lucide name |
|---|---|---|
| TabBar | الذكر | `moon` |
| TabBar | المجتمع | `message-circle` |
| TabBar | القرآن | `book-open` |
| TabBar | الإعدادات | `circle-user` |
| Top bar | Back | `arrow-right` (RTL: points right = back) |
| Top bar | Search (المجتمع posts) | `search` |
| Search field | Search, clear | `search`, `x` |
| Search field (القرآن) | خيارات البحث | `sliders-horizontal` |
| Search suggestions | Recent | `clock` |
| Rows | Open | `chevron-left` |
| MiniPlayer and notification | Repeat (off / ayah / surah) | `repeat`, `repeat-1` (ayah state; surah state = `repeat` in `accent`) |
| MiniPlayer and notification | Previous / next ayah | `skip-forward` / `skip-back` (RTL order, not mirrored) |
| MiniPlayer and notification | Play / pause | `play` / `pause` |
| MiniPlayer | Speed | text «١×», no icon |
| AyahSheet | استمع، من هنا، احفظ، شارك، انسخ | `play`, `list-start`, `bookmark`, `share-2`, `copy` |
| AyahSheet | اسأل عنها في المجتمع | `message-circle-question` |
| WordTooltip | استمع للكلمة، ابحث بجذرها | `volume-2`, `search` |
| TilawaCard | Reminder time | `bell` |
| SectionTiles | ادرس، الضياء، الفرقان | `door-open`, `sun`, `scale` |
| Rows | مجموعاتي، وقتي مع القرآن، التلاوة اليومية | `folder`, `clock`, `book-open` |
| المجتمع | شارك آية، اسأل، تأمّل | `quote`, `message-circle-question`, `lightbulb` |
| المجتمع | Live, reminder set / not set | `radio`, `bell-ring` / `bell` |
| المجتمع | Open in TikTok / YouTube | `external-link` |
| المجتمع | Videos | `video` |
| المشرفون | سؤال، بلاغ، اقتراح | `message-circle-question`, `flag`, `lightbulb` |
| المشرفون and الإدارة | Moderators | `shield-check` |
| Posts | More (report) | `ellipsis` |
| Compose / collections | Attach a verse, new, drag | `quote`, `plus`, `grip-vertical` |
| RuleBox, notes | Info | `info` |
| Done states | Finished | `check` |
| Settings, onboarding | Location | `map-pin` |
| Settings | Notifications, theme, offline audio | `bell`, `sun-moon`, `download` |
| Sign-in | Account | `circle-user` |

## Custom drawings (not icons)

These are drawn in code from the tokens, not taken from Lucide:

| Drawing | Where | Rule |
|---|---|---|
| The mark ق | App icon, splash, notification small icon, store, «عن التطبيق» | DESIGN-SYSTEM §11. Assets in `brand/`. |
| Moon phase | الذكر moon row, Moon page, moon widget | Computed: lit part = terminator ellipse from today's phase; `moon.lit` on `moon.shadow`. |
| Progress ring | TilawaCard, تلاوة widget | Track `accentRing` (or `line`), fill `onAccent` (or `accent`), round caps, starts at 12 o'clock and runs clockwise. |
| Spelling-form underline | Search results | Wavy, 1 dp, `formAlt`, offset 8. |
| Week strip | الذكر | 6 dp bars, `accent` / `accentSoft` / `line`. |
