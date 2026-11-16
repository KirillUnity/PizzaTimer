# Доработка UI в Google Stitch (круг 2)

Русский хром на Дне, Задачах, Отчётах и Настройках уже есть. Шапка «Вс, 8 ноя» и нижние вкладки выглядят как в приложении. Этого мало: на Дне циферблат всё ещё теснится карточкой и красной кнопкой «Записать сектор», в центре торчат «осталось 18 мин», на Отчётах снова ZIP и плеер, на потоке обрезается «Скрыть готовые», практика в Stitch не чинилась.

Эти промпты — не новый бренд и не новые вкладки. Они подчищают уже нарисованные экраны, чтобы день читался как часы, а не как лента с кнопкой.

## Как пользоваться

Откройте тот же проект Horological Paper Craft на [stitch.withgoogle.com](https://stitch.withgoogle.com). Один чат — один блок **Prompt** ниже, целиком. Первая фраза чата: «Продолжай набор Horological Paper Craft, не создавай новый бренд». Порядок: **B2 → C2 → D2 → E2 → F2**. Если в чате ещё висят Den/аватар/24H — сначала **A2**.

После каждого чата выгрузите картинки телефона. Не копируйте вёрстку Stitch в приложение: это образец вида, не исходник экранов.

Эталон спокойного вида, который нельзя «улучшать» пирогом и калибром: лист плана цели, сбор отчётов, вердикты «влезает / тесно / невозможно».

## Что должно получиться на экране

На **Дне** человек видит дату между стрелками, календарь справа, ряд ДО|ПО и Циферблат|Пицца|Лепестки|Список. Кольцо занимает большую часть экрана. В покое в отверстии только «ЖИВОЙ ХРОНО» и время. Старт появляется после выбора сектора. Пауза — пока идёт заход. Под кольцом нет огромной красной кнопки и нет «осталось N мин».

На **списке дня** у одной задачи могут быть два отрезка (утро и день). «Скрыть готовые» только прячет строки. Плюс «Задача» не накрывает последнюю карточку.

На **Задачах** сверху тихая строка «Спланировать цель». Карточка: название, часы или «без часов», важность высокая/средняя/низкая. Нет живого бара «идёт учёт» и нет стоп-кадра паузы на списке.

**Редактор:** прошедший отрезок с замком, будущий можно двигать, внизу «Сохранить». Задача без часов — подсказка про свободные минуты, не вторая шапка и не круг 24 часов.

**Отчёты:** заголовок по-русски, «Собрать за сегодня» и «Собрать за неделю», заметки с Править/Удалить. Пустая неделя: «Закрытых отрезков нет» — это не ошибка. Нет ZIP, нет голосового эквалайзера, нет английского Reports.

**Настройки:** вид по умолчанию, цвет, серый для готового, подложка, «Показать введение», «Практика». Без пейджера даты, без «24-часовой круг», без аватара.

**Поток:** тонкая полоска «СДЕЛАНО · 3 ч 20 мин из 6 ч» под датой. Листок паузы называется «Где остановился». Закрытие дня без процентов больше 100.

**Практика** открывается из Настроек, вкладка Настройки остаётся активной. Пять приёмов, «опробовано / ещё нет», без серий и очков.

## Сценарий проверки

1. День в покое: кольцо большое, в центре время, нет Старта, нет красной кнопки под кольцом.
2. Тап по сектору: маленькое облако рядом, в центре Старт, цифры 12 и 3 читаются.
3. Поток: Пауза влезает в отверстие; после паузы листок «Где остановился» не режет дату.
4. Пицца и лепестки: та же геометрия дня, без списка клиньев под фигурой.
5. Список: два интервала у спорта, плюс не наезжает на последнюю строку.
6. Отчёты: только сбор и заметки; настройки без 24H.

Если хоть один шаг падает — этот чат не принимать, повторить тот же промпт, не перескакивать дальше.

---

# Техническое приложение

## Файлы

| Путь | Зачем |
|---|---|
| `docs/TZ.md` | Канон: 12 ч AM/PM, leftover 5:3:2, append Start/Pause, LIST — фильтр |
| `docs/STITCH_LAYOUT_FIX_PROMPTS.md` | Круг 1 (A–F), уже частично отработан |
| `docs/STITCH_FLOW_PROMPT.md` | Исходный поток из шести кадров |
| `docs/STITCH_LEARNING_PROMPT.md` | Исходная практика |
| `stitch_timedial_planner_ui_kit/_planner/chrome.css` | Эталон спокойного хрома |
| `stitch_timedial_planner_ui_kit/plan_sheet_*`, `reports_collect`, `tasks_plan_entry`, `learn_*` | Не затирать, пока кадр Stitch не стал явно тише |

Новые PNG: одноимённые папки или суффикс `_fix`. HTML Tailwind в `:app` не копировать.

## Что чинить в круге 2 (по дампу `_5`–`_25`)

- День `_8`/`_9`/`15_40`: FAB «Записать сектор», leftover в отверстии, карточка под кольцом, время дважды, «Скрыть готовые» обрезано до «Ск».
- Пицца `_12`: дыра смещена, цифры 11–7, клинья не 12/3/6/9.
- Лепестки `_14`: FAB + подпись-инвентарь.
- Список `_15`, задачи `_17`: аватар, фото, FAB на контенте; `_7` — live pause на карточке.
- Редактор `_18`: двойная шапка.
- Отчёты `_5` ZIP/голос; `_19`/`_20` English Reports + аватар.
- Настройки `_21`: «24-часовой круг»; `_6` лучше, но пейджер даты лишний.
- Поток `_22` облако на цифрах; `_23` без заголовка листка; `_25` 104%; метроном / фокус-режим.
- Практика: в этом дампе нет — слать **F2**.

Общие токены в каждый промпт: бумага `#FBF9F5`, чернила `#1C1B18`, терракота `#E0533C` только now / live / primary, песок leftover `#EADBCE`, изумруд `#2E7D52`, готовое `#8E8B82`. Domine + Plus Jakarta Sans. Сетка 8dp, поля 16dp, цели 48dp, nav 64dp. Телефон 390×844, светлая тема. Без 24H, аватара, XP, помодоро, метронома.

## Prompt A2 — добить оболочку

Если ещё есть Reports/Settings по-английски, аватар или 24H.

```
Continue Horological Paper Craft. Do not invent a new brand. Do not redesign the product.

You already drew a Russian shell. Fix remaining chrome only.

Every screen: paper `#FBF9F5`, ink `#1C1B18`, terracotta `#E0533C` only for now needle, live session, and primary buttons. No avatar, no profile circle, no English titles. Bottom nav labels: День, Задачи, Отчёты, Настройки.

Day and Tasks app bar: chevron, Domine date «Вс, 8 ноя», chevron, calendar 48dp. Do not put a day pager on Reports or Settings.

Reports title is «Отчёты». Settings title is «Настройки». One eyebrow each. Never write Reports, Settings, Day, Tasks as the H1. Never write 24H, Horology Meridian, or «24-часовой круг».

Hide done is a full label «Скрыть готовые» or an icon with that content description. Never clip to «Ск».

AM|PM and Циферблат|Пицца|Лепестки|Список are one 48dp-tall row. If they do not fit, wrap to two full-height rows. Do not shrink chips below 48dp tap height.

Show the same four destinations: Day, Tasks, Reports, Settings. Content jobs stay. Chrome only.
```

## Prompt B2 — день: кольцо, пицца, лепестки

```
Continue Horological Paper Craft. Russian UI. 12-hour AM/PM face. Numerals 12, 3, 6, 9 only (or 12–11 evenly). Never mix 11/7 with 12/3 on pizza.

Fix the Day canvas. Phone 390×844. The analog figure occupies about 55% of the height above the 64dp nav.

Delete these from every Day canvas frame:
- the full-width button «Записать сектор» / «+ Записать сектор» / «Интервал» / «Снять выбор» under the dial
- leftover copy in the hole or as a pill: «Осталось 18 мин», «18 min», percents like 62.5%
- large inventory cards, photo cards, countdown to next break, metronome, focus-mode banners
- a second clock in the app bar

Idle DIAL: hole = micro-label «ЖИВОЙ ХРОНО» + wall time in Domine tabular. No play button. One terracotta now needle. Nothing under the ring except optional 8dp of paper.

Selected DIAL: chosen sector slightly raised. Paper callout max width 180dp, 16px radius, 8dp gap from the hole and from numerals 12 and 3. Callout: title, one line, text «Подробнее». Hole: 56dp terracotta «Старт». No pomodoro, no remaining-minutes bar.

Running DIAL: hole «В ПОТОКЕ», elapsed «00:42», task name, 56dp «Пауза». If it does not fit, drop decorations, keep elapsed + Pause. Mode chips outline-only.

PIZZA: solid wedges from a centered hub. Start/Pause in the hub. No rotated labels on wedges. No list of slices under the figure. Hub stays in the geometric center.

PETALS: organic lengths, same day. No legend plus a timeline that repeats every petal. No FAB.

Hide-done DIAL: completed sectors `#8E8B82`, remaining work still on the face (reflow), label «Скрыть готовые» fully visible.

Draw six frames: DIAL idle, DIAL selected, DIAL running, PIZZA idle, PETALS idle, DIAL hide-done. Sunday afternoon sample. No LIST, no settings, no English.
```

## Prompt C2 — список дня, задачи, редактор

```
Continue Horological Paper Craft. Russian UI. Same shell. No avatar.

Fix three list surfaces. Cards: default 56dp, 16px radius, 4dp left ink edge, trailing duration tabular. Two lines of title beat a mid-word ellipsis.

LIST (Day tab, view = Список): Sport shows two interval rows 08:00–09:00 and 12:00–13:00. Hide done is a filter only. Remove live pause buttons on rows, leftover percents, «Tick», logged-hour banners. Tag chips 32dp, not clipped. FAB «+ Задача» 56dp terracotta with padding so it never covers the last card.

Tasks tab: top strip «Спланировать цель» like the quiet plan-entry, no hero photo. Rows: status, title, civil time or «без часов», importance Высокая / Средняя / Низкая. No «идёт учёт» live bar, no pause control on the inventory. Same FAB rules.

Editor: one header. Title, description, color dots, importance, status. Closed past block locked (no steppers). Open/future block may change end. «+ Добавить отрезок». Recurrence. Move / duplicate / delete. Primary «Сохранить» 48dp at the bottom. Untimed editor: leftover hint only, no nested second title bar, no 24h dial.

Four frames: List two intervals; Tasks with plan entry; editor locked+editable block; editor untimed empty.
```

## Prompt D2 — отчёты и настройки

```
Continue Horological Paper Craft. Russian UI. No avatar. No day pager on these two screens.

Reports: H1 «Отчёты», eyebrow «Заметки живут на устройстве». Primary «Собрать за сегодня», secondary sand «Собрать за неделю». Notes: title, local datetime, body, «Править» / «Удалить», optional local file chips. Empty: calm «Закрытых отрезков нет» — not an error, no manifesto cards. Delete ZIP export, Markdown+ZIP, voice equalizer / m4a player, fitness pie, CAP%, English word Reports, version pills.

Settings: H1 «Настройки». Default view: Циферблат / Пицца / Лепестки / Список — caption «12-часовой круг», never 24-hour. Accent dots. Completed gray. Underlay presets. Text rows «Показать введение» and «Практика». Optional one quiet dark-mode switch, not a hero. Remove SQLite, TalkBack manifesto, caliber, ZIP.

Three frames: Reports one note; Reports empty week; Settings appearance.
```

## Prompt E2 — поток

```
Continue Horological Paper Craft. Russian UI. Fix the six flow frames. No gamification, no metronome, no «фокус-режим», no 104% banners.

Shell: date pager + calendar. Under the date, one line «СДЕЛАНО · 3 ч 20 мин из 6 ч» and a 4dp emerald/sand track. No extra percent like 55% if it crowds the label. AM|PM and view modes 48dp; «Скрыть готовые» fully visible, never «Ск». Ring ~55%. No FAB under the dial.

Idle: hole «ЖИВОЙ ХРОНО» + 15:40. No play.

Selected: callout ≤180dp, 8dp gap to hole and to 12/3. Hole 56dp «Старт». No «Снять выбор» / «Интервал» pair under the ring.

Running: hole fits «В ПОТОКЕ», 00:42, task, «этот заход», 56dp «Пауза».

Pause slip: title exactly «Где остановился» visible, not clipped by the mode row. Field, chips Спокойно/Глубоко/Застрял, primary «Зафиксировать», ghost «Пропустить», helper «Прошедший отрезок не меняется.» Dial dimmed one step behind.

Resume: chip «Продолжить с: цифры за четверг» on the sector, not covering the hole. Full words, no ellipsis mid-phrase if two lines fit.

Day close: slightly smaller dial, card «День закрыт», closed hours, last checkpoint, «В отчёты». No 104%, no second progress dashboard.

Six frames, same Sunday. Phone 390×844.
```

## Prompt F2 — практика

```
Continue Horological Paper Craft. Russian UI. Practice opens from Settings; Settings stays the active nav tab. No fifth tab. No streaks, XP, quizzes.

Fix six frames: hub practiced, lesson dial, lesson leftover 5:3:2, lesson flow, week, hub empty.

Hub: «Практика», eyebrow «приёмы инструмента», «ОПРОБОВАНО · 3 из 5». Five rows ≥56dp. Compact week line with duration + «Неделя». Ghost «Показать введение» 48dp, 16dp above nav, no overlap.

Leftover lesson: three bars visually width 5:3:2, labels высокая / средняя / низкая.

Week: weekday labels under columns, empty Wednesday sand, calm caption. «В отчёты» 48dp above nav.

Phone 390×844, same inks.
```

## Сознательно не просим

Новые режимы циферблата, тёмная тема как набор кадров, планшеты, облако, календарь, правки кода `:app` из HTML Stitch.
