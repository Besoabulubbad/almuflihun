# الضياء, station 6: how the trees are counted

- **Status:** verified 2026-11-27 against the shipped Tanzil text; checked on every build by `StationSixCountsTest` (`core:quran`).
- **Where it is shown:** «الضياء», station 6, the `count_bars` block ("verses that name the tree or its fruit").

## The definition

A verse is counted for a tree when **one of its words is the tree's name**:

| Bar | Names counted |
|---|---|
| النخل | نخل، نخلة، نخيل |
| الأعناب | عنب، أعناب |
| الزيتون | زيتون، زيتونة |
| الرمان | رمان |
| التين | تين |

- A name may carry the attached letters و، ف (and), ب، ل، ك (prepositions) and the article ال, and may end in tanween alef or ة (والنخل، بنخل، ونخلا، النخلة). Nothing else is attached.
- Words are compared by their letters only (vowel marks set aside), in the shipped Tanzil text without pause marks.
- A verse is counted **once** per tree, however many times it names it.
- **Not counted:** words for parts or products of the tree that are not its name: زيت (oil, 24:35), طلع (spathe), قنوان (clusters), لينة (59:5). Deciding that one of these means the tree would be interpretation, which the app does not do (DESIGN §1).
- **Why the fruit is not counted separately:** the only candidate, رطب, means "moist" in 6:59 («ولا رطب ولا يابس») and dates in 19:25. Telling the two apart needs interpretation; and 19:25 is already counted, because it names «النخلة».

## The counts and the verses

| Bar | Verses | Where |
|---|---|---|
| النخل | **20** | 2:266, 6:99, 6:141, 13:4, 16:11, 16:67, 17:91, 18:32, 19:23, 19:25, 20:71, 23:19, 26:148, 36:34, 50:10, 54:20, 55:11, 55:68, 69:7, 80:29 |
| الأعناب | **11** | 2:266, 6:99, 13:4, 16:11, 16:67, 17:91, 18:32, 23:19, 36:34, 78:32, 80:28 |
| الزيتون | **6** | 6:99, 6:141, 16:11, 24:35, 80:29, 95:1 |
| الرمان | **3** | 6:99, 6:141, 55:68 |
| التين | **1** | 95:1 |

These match the numbers in `design/data/diyaa-food.json`. The file also notes 19 and 10 for the palm and the grapes "in the source file" (`differs_from_source_file`); those came from an earlier count and are not used.
