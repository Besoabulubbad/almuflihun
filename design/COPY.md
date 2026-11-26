# Push and in-app copy (v4)

The rules come from DESIGN §8:
- Plain Arabic, with no exclamation marks and no emoji.
- «» only around Quran words.
- No guilt, and no urgency words like «لا تفوّت» or «سارع».
- The **title** is at most 30 characters and the **body** at most 90.

In the tables:
- `{name}` is the user's display name.
- `{title}` is the stream or post title.
- `{type}` is آية / سؤال / تأمّل.
- `{n}` is a count, written in Arabic-Indic digits.
- `{time}` is local time.

## Notification categories (Settings → الإشعارات)

| Category key | Title | Body | Opens |
|---|---|---|---|
| `live` | بدأ البث | «{title}» على تيك توك الآن. | Live card → TikTok |
| `live` (reminder, 10 min before) | بعد ١٠ دقائق | «{title}» يبدأ {time}. | Live |
| `posts.new` | في المجتمع | {name} شارك {type} جديدة. | The post |
| `replies.posts` | رد على منشورك | {name} ردّ على {type}ك. | The thread at the reply |
| `replies.comments` | رد على تعليقك | {name} ردّ على تعليقك. | The thread at the reply |
| `updates` | تحديث متوفر | النسخة {version} جاهزة للتحميل. | Update sheet |
| `announcements` | {admin title} | {admin body} | Home |
| `staff.review` | للمراجعة | {n} منشورات بانتظار القبول. | Admin → المراجعة |
| `staff.reports` | بلاغ جديد | بلاغ على {type} من {name}. | Admin → البلاغات |
| `moon.moments` (local, off by default) | ليلة الهلال | الهلال يُرى الليلة بعد الغروب في {city}. | Moon |
| `moon.moments` | خسوف القمر | يبدأ الخسوف {time} ويُرى من موقعك. | Moon |

## Result of a post (always sent, not a category)

| Case | Title | Body |
|---|---|---|
| Accepted | نُشر منشورك | قُبل {type}ك وصار ظاهراً للجميع. |
| Rejected | لم يُنشر منشورك | السبب: {reason}. افتحه لتعدّله وترسله من جديد. |
| Chosen as best answer | ردّك أفضل رد | اختار السائل ردّك على سؤاله. |

## Reminders (Settings → التذكير, local)

| Reminder | Title | Body |
|---|---|---|
| قبل طلوع الشمس | حزبك | الحزب {n} في انتظارك. |
| قبل الغروب | حزبك | الحزب {n} في انتظارك. |

No streaks, no «لم تقرأ اليوم», no counts of missed days.

## In-app strings introduced in v4

| Where | Text |
|---|---|
| Home card action | افتح في المصحف |
| الأبواب, door visible | افتح الباب / افتح في المصحف |
| الأبواب, door hidden | افتح في المصحف |
| Rise/set unknown | لا نعرف، plus the button «حدّد مدينتك لنعرف أوقات القمر» |
| الفرقان portal | قريباً |
| Live offline | لا إنترنت. هذه القائمة من آخر تحديث، {time}، وقد تكون تغيّرت. |
| Time empty state | هنا يظهر وقتك مع القرآن يوماً بيوم. يُحسب حين تقرأ في المصحف أو تستمع، ويبقى على جهازك. |
| Saved sync note | محفوظاتك تُحفظ في جهازك، وتُزامَن مع حسابك إن سجّلت الدخول. |
| Privacy note | نجمع بيانات استخدام مجهولة لتحسين التطبيق، فقط إن وافقت. لا نبيعها ولا نشاركها. |
| Pending item | بانتظار المراجعة. لا يراه غيرك بعد. |
| Answer without verse | كل رد يحتاج آية دليلاً (the send button is disabled) |
| Hizb end | وصلت إلى آخر الحزب {n}. Button: ابدأ الحزب {n+1}. Note: لا يتقدّم الحزب إلا حين تتقدّم أنت. |


## v5 strings

| Where | Text |
|---|---|
| Word hint (Mushaf) | اضغط على كلمة لدراستها |
| AyahSheet | استمع، من هنا، تدبّر، مجموعة، شارك، انسخ |
| Collection note label | تأمّلي (خاص بي) |
| Add-to sheet | أضف {ref} إلى، with the note «الآية الواحدة قد تكون في أكثر من مجموعة.» |
| Search summary | {n} آية في {m} سور |
| Too many results | {n} آية لـ«{q}». نعرض أول ٢٠٠ مجمّعة حسب السورة. ضيّق بالنطاق أو أضف شرطاً. |
| Word study count | الجذر ورد {n} مرة في {m} آية |
| Update row | المثبّت {a}، والمتوفر {b}، or «لديك أحدث نسخة، {a}» |
| Required update | يلزم تحديث التطبيق, then «هذه النسخة ({a}) لم تعد تعمل مع المجتمع والبثوث. نزّل النسخة {b} لتكمل.» |
| Verify failed | لم نستطع التحقق من الملف, then «الملف المنزّل لا يطابق توقيع المفلحون، فلن نثبّته. حذفناه. نزّله من جديد.» |
| Failed post | لم يُرسل سؤالك, then «لا إنترنت الآن. حفظنا ما كتبته والآية المرفقة كمسودة.» |
| Service down | الخدمة غير متاحة الآن, then «…المصحف والأبواب والضياء تعمل كما هي من الجهاز.» |
| Onboarding | المفلحون: «القرآن وحده. تقرأه وتستمع إليه، وتدرس كلماته، وتعيش مع آياته في يومك.» |
