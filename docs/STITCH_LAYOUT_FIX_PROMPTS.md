# Stitch: правки вёрстки UI-кита

Канон продукта: [TZ.md](TZ.md). Токены: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md). Эталон более спокойного хрома: `stitch_timedial_planner_ui_kit/_planner/chrome.css` и кадры `plan_sheet_*`, `reports_collect`, `tasks_plan_entry`. Поток: [STITCH_FLOW_PROMPT.md](STITCH_FLOW_PROMPT.md). Обучение: [STITCH_LEARNING_PROMPT.md](STITCH_LEARNING_PROMPT.md).

Один промпт — один чат в [stitch.withgoogle.com](https://stitch.withgoogle.com). Вставляйте блок **Prompt** целиком. Если проект кита открыт, первой фразой: «Продолжай набор Horological Paper Craft, не создавай новый бренд». Круг 1: **A**, потом B–F. После русского дампа `_5`–`_25` берите круг 2: [STITCH_UI_REFINE_PROMPTS.md](STITCH_UI_REFINE_PROMPTS.md) (**A2** только если ещё аватар/24H, иначе **B2**→**F2**). HTML в `:app` не копировать.

Эти промпты не меняют Compose. Они чинят макеты в `stitch_timedial_planner_ui_kit/`.

---

## Что сломано (аудит)

В папке два несовместимых набора кадров.

1. **Хвост Stitch на Tailwind** (`day_dial_view_*`, `dial_*`, `day_pizza_view`, `day_petals_view`, `list_two_intervals`, `tasks_list_inventory`, `tasks_task_editor_multi_interval`, `reports_week_local_notes`, `settings_appearance_onboarding`): английский язык, шапка «HOROLOGY MERIDIAN · 24H», аватар, баннеры режима, 24-часовая калибровка, primary из YAML `#ab2d1b`.
2. **Спокойный хром** (`_planner/chrome.css`, `plan_sheet_*`, `reports_collect`, `tasks_plan_entry`, `day_flow_*`, `learn_*`): русский язык, бумага `#FBF9F5`, терракота `#E0533C`, без аватара.

Продукт в `:app` ближе ко второму набору: дата между стрелками, календарь справа, AM/PM, Dial|Pizza|Petals|List, циферблат занимает основную высоту, Старт в отверстии, облако у сектора.

### Конкретные дыры вёрстки

- **Циферблат не герой.** На `day_dial_view_am` под кольцом карточка сегмента, leftover 62.5%, Rest, огромный FAB. DESIGN.md: на compact dial — верхние ~55% экрана. Баннер «DIAL MODE • LEFTOVERS…» обрезан.
- **Время дублируется.** 10:42 в шапке и снова в отверстии.
- **Облако закрывает инструмент.** `day_dial_view_selected_sector_callout`: широкая карточка *над* кольцом, а не маленькое облако у сектора. В отверстии «START TIMER 00:00:00 2h Target» — помодоро, которого нет в ТЗ.
- **12 ч против 24 ч.** ТЗ: две 12-часовые половины. Кадры пишут 24H, на кольце то 10/2/3/9, то 12/03/06/09.
- **Pizza / Petals.** Те же баннеры («VIEW B: PIZZA MODE…»). Под фигурой список клиньев и блок «Slice Geometry». На пицце подписи SLEEP / TRIAGE повёрнуты и наезжают. Petals: легенда + timeline дублируют лепестки.
- **LIST.** Жаргон «Tick 19», «360° geometry locked», «5H 30M LOGGED». Чипы тегов обрезаны. FAB наезжает на последнюю карточку.
- **Задачи.** Карточки выше 56dp, заголовки с многоточием, проценты leftover, live-бар. `tasks_plan_entry` уже спокойнее — к нему тянуть inventory.
- **Редактор.** Длинный scroll, «50% Priority Allocation», 24h dial, Save в шапке плюс куча секций. Закрытый блок должен быть нередактируемым — это уже есть, оставить, убрать шум.
- **Отчёты.** `reports_week_local_notes`: пирог 88% CAP, On Track, ZIP-экспорт. В ТЗ — текст на устройстве и «Собрать за сегодня / неделю». Эталон: `reports_collect`.
- **Настройки.** Локализация, SQLite dump, TalkBack-маркетинг, 24H, dark как равный столбец. В приложении: вид по умолчанию, цвет, подложка, «Показать введение».
- **Поток (`day_flow_*`).** Нет стрелок даты и Hide done. Ряд AM/PM + Dial|Pizza|Petals|List тесный. Облако наезжает на цифры. В потоке Пауза еле влезает в отверстие. Чип «Продолжить с» перекрывает сектор. Игла now на 15:40, хотя в продукте now можно держать у 12.
- **Практика (`learn_*`).** Хаб почти впритык к nav. На неделе подписи дней *над* столбиками. Кнопки «Понятно» / «В отчёты» висят в пустоте без ритма 8dp.
- **Кнопки и цели.** Primary в DESIGN.md: высота 48dp, радиус 8px, `#E0533C`. В `_planner` радиус 12px. Чипы режимов ниже 48dp. Обрезанные `#Engineering (3)`.

Эталон, который **не ломать:** `plan_sheet_form` / вердикты, `reports_collect`, `tasks_plan_entry`. Их хром — цель для остальных.

---

## Общие токены (вставлять в каждый промпт как уже согласованные)

- Canvas `#FBF9F5`, ink `#1C1B18`, terracotta `#E0533C` только now / live / primary commit, sand `#EADBCE` leftover, emerald `#2E7D52` конструктивный прогресс, completed `#8E8B82`.
- Domine — заголовки и длительности (tabular). Plus Jakarta Sans — UI. Micro-label uppercase, tracking 0.05–0.08em.
- Сетка 8dp, поля 16dp, nav 64dp с 48dp hit target, primary 48×100% ширина листа, радиус 8px.
- Compact телефон 390×844. Циферблат — квадрат, не меньше ~55% высоты контента над nav.
- Язык UI: русский. Никакого «24H», «Horology Meridian», аватара, пирога CAP, серий, XP, помодоро.

Шапка дня как в приложении: стрелка · дата (serif) · стрелка · календарь. Под ней одна строка AM|PM и Dial|Pizza|Petals|List (hit ≥ 48dp по высоте ряда, не два баннера). Hide done — тихий текст/иконка, не третья пилюля времени.

Нижняя навигация: День, Задачи, Отчёты, Настройки.

---

## A. Общая оболочка

Чинит шапку, nav, цвет, язык на **всех** кадрах, не рисуя заново пиццу.

### Prompt

```
Continue Horological Paper Craft. Do not invent a new brand.

Fix the shared mobile chrome of the existing TimeDial kit. Do not redesign the product. Unify every screen to one shell.

Tokens: paper `#FBF9F5`, ink `#1C1B18`, terracotta `#E0533C` only for the now needle, live session, and primary buttons. Sand `#EADBCE` leftover. Emerald `#2E7D52` constructive progress. Completed gray `#8E8B82`. Domine for titles and durations (tabular lining figures). Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 16dp side margins, 48dp minimum tap targets, 64dp bottom nav. Primary buttons: 48dp height, 8px radius, terracotta, white label. No blue shadows, no glass, no neon.

Language: Russian on every chrome label. Remove «HOROLOGY MERIDIAN · 24H», avatars, English Day/Tasks/Reports/Settings. Bottom nav: День, Задачи, Отчёты, Настройки. Active item terracotta, inactive sand-ink.

App bar (Day and Tasks): previous-day chevron, date in Domine (example «Вс, 8 ноя»), next-day chevron, calendar icon 48dp. No profile circle. No 24H subtitle. Settings and Reports use a simple title + one eyebrow, no day pager unless the screen is a day.

Delete truncated mode banners («DIAL MODE • LEFTOVERS…», «VIEW B: PIZZA MODE…», «VIEW C: PETALS…», «MULTI-INTERVALS SUPPORTED…»).

Product clock is 12-hour AM or PM, not a 24-hour marketing caliber. Do not write 24H anywhere. Numerals on a 12-hour face are 12, 3, 6, 9 (or 12, 1…11), never a mix of 10/2/3/9 and 00/06/12/18 on the same ring.

Redraw the shell on a phone 390×844, light mode, and show it applied to four existing destinations without changing their jobs: Day (dial visible), Tasks, Reports, Settings. Keep content recognizable; only the chrome and spacing change.

Do not add streaks, dark theme frames, tablets, or a fifth tab.
```

---

## B. День: кольцо, пицца, лепестки

Циферблат снова главный. Старт в отверстии. Облако маленькое.

### Prompt

```
Continue Horological Paper Craft after the shared chrome prompt. Russian UI. 12-hour AM/PM face. Terracotta `#E0533C`. Paper `#FBF9F5`.

Fix Day canvas layouts. Compact phone 390×844. The analog figure (ring, pizza, or petals) occupies about 55% of the height above the bottom nav. Do not park large inventory cards under the figure on canvas modes. At most one quiet selected-task name under the dial, 56dp, 16px radius, 4dp left ink.

Idle DIAL: hole shows micro-label «ЖИВОЙ ХРОНО» and wall time in Domine tabular. No play button until a sector is selected. One terracotta now needle. Optional Hide done control in the chrome row, not a second clock. Do not repeat the time in the app bar if it is in the hole.

Selected DIAL: raise the chosen sector slightly. A small paper callout (max width 180dp, 16px radius) sits beside the sector, not a full-width card covering the ring. Callout: title, one line of description, text «Подробнее». Hole: 56dp terracotta play and the word «Старт». No countdown, no «2h Target», no pomodoro.

Running DIAL: hole «В ПОТОКЕ», elapsed tabular, task name, 56dp «Пауза». Chrome (AM/PM and view modes) goes outline-only. Bottom nav stays but quieter.

PIZZA: solid wedges from the center, same day geometry, Start/Pause in the hub. No rotated SLEEP/TRIAGE labels colliding. No «Slice Geometry» essay. No duplicate wedge inventory list.

PETALS: organic petal lengths, same geometry. No color legend plus a four-row timeline that repeats every petal. Center may show elapsed if running, otherwise live time.

Same sample Sunday afternoon, Russian labels. Draw six frames: DIAL idle, DIAL selected, DIAL running, PIZZA idle, PETALS idle, DIAL with hide-done (completed sectors gray, remaining work still on the face).

Do not draw LIST, settings, reports, streaks, 24H, or English chrome.
```

---

## C. Список дня, задачи, редактор

### Prompt

```
Continue Horological Paper Craft. Russian UI. Same shell as the chrome prompt.

Fix three list surfaces so they match DESIGN.md cards: 56dp row height as the default, 16px radius, 4dp left fountain-pen edge in the task ink, trailing duration in tabular figures. Titles must not ellipsize in the middle of a word if two lines fit; prefer two lines over «Engine Architec…».

LIST day (view mode List, still the Day tab): one task can show two interval rows (08:00–09:00 and 12:00–13:00). Hide done is a filter, not a 360° reflow. Remove «Tick 19», «360° geometry locked», «5H 30M LOGGED», leftover percents like 62.5%. Tag chips 32dp height, 8px radius; they must not clip the last chip. FAB «+ Задача» 56dp, terracotta, does not cover the last row — add bottom padding above the nav.

Tasks tab: keep the plan-entry strip from tasks_plan_entry («Спланировать цель») at the top. Inventory cards: status chip, title, civil time or «без часов», importance as High/Medium/Low not «Weight 8 total». No live progress bar of leftover share. FAB same as List.

Task editor (sheet or full page): title field, description, color dots, importance HIGH/MEDIUM/LOW, status TODO/IN_PROGRESS/DONE, list of time blocks. A closed past block is visible and locked (no steppers). Open/future block can change end. «+ Добавить отрезок». Recurrence daily/weekdays. Move / duplicate / delete. Remove «50% Priority Allocation», «24h dial», «Adjust Crown», dual Save in a cramped header. Primary «Сохранить» 48dp at the bottom of the sheet above nav or as the sheet’s commit. 48dp targets on steppers.

Draw four frames, light phone: List with two intervals; Tasks inventory with plan entry; editor with one closed locked block and one editable block; editor empty-untimed (no blocks, leftover hint only).

Do not invent cloud sync or calendar.
```

---

## D. Отчёты и настройки

### Prompt

```
Continue Horological Paper Craft. Russian UI. Same shell.

Reports must match the quiet collect screen, not a fitness pie. Title «Отчёты», eyebrow «Заметки живут на устройстве». Two actions: primary «Собрать за сегодня», secondary sand «Собрать за неделю». Then a list of paper notes: title, local datetime, body text, «Править» / «Удалить», optional local photo/file chips. Empty success copy is calm: «Закрытых отрезков нет» is not an error. Delete the 88% CAP ring, On Track, week ZIP export, «38h 45m Tracked» dashboard. Attachments stay local; show a thumbnail row, not a media player skin.

Settings: title «Настройки». Default view mode row (Dial/Pizza/Petals/List). Accent color dots. Completed gray swatch. Dial underlay presets. One text button «Показать введение». One text button «Практика» that will open the learning hub (do not draw the hub here). Remove SQLite dump, language marketing cards, TalkBack manifesto, 24H caliber, export ZIP, dark/light as a second product. Dark mode may stay a single quiet switch if already in the kit, not a hero.

Draw three frames: Reports with one note; Reports empty week collect success; Settings appearance.

Do not add a fifth tab.
```

---

## E. Поток: прогресс и фиксация (правка кадров)

Накладывается на уже существующие `day_flow_*`.

### Prompt

```
Continue Horological Paper Craft. Russian UI. You already have six flow frames (idle, selected, running, pause slip, resume, day close). Fix their layout. Do not add gamification.

Use the unified Day shell: date pager + calendar, AM/PM, Dial|Pizza|Petals|List with 48dp row height, optional Hide done. Progress line «СДЕЛАНО · 3 ч 20 мин из 6 ч» with a 4dp emerald/sand track sits under the date, not instead of the date pager. The ring still ~55% of the content column. 16dp margins. Nothing clipped by the nav.

Idle hole: «ЖИВОЙ ХРОНО» + 15:40. No play until selected.

Selected: 180dp max callout beside the ochre sector, not covering numerals 12/3. Hole 56dp Старт and the task name. Callout must leave 8dp gap to the hole.

Running: the hole is 156dp max. Content that must fit without clipping: «В ПОТОКЕ», 00:42, task name, caption «этот заход», 56dp Пауза. If it does not fit, drop the inner arc, keep elapsed and Pause. Quiet chrome.

Pause slip: level-4 paper sheet, 24px corners, above the nav. Title «Где остановился». One field. Chips Спокойно/Глубоко/Застрял (32dp, 8px). Primary «Зафиксировать» 48dp. Ghost «Пропустить». Helper «Прошедший отрезок не меняется.» Dial behind is dimmed one step, still recognizable, not a new route.

Resume: chip «Продолжить с: цифры за четверг» docks to the sector without covering the hole or the plum slice. 48dp play on the chip.

Day close: card under a slightly smaller dial, never overlapping nav. «День закрыт», hours, last checkpoint, «В отчёты».

Same Sunday sample. Six corrected frames. No 24H, no English, no second clock in the app bar.
```

---

## F. Дашборд обучения (правка кадров)

Накладывается на `learn_*`.

### Prompt

```
Continue Horological Paper Craft. Russian UI. Learning hub opens from Settings; Settings remains the active tab. No fifth tab. No streaks, XP, quizzes.

Fix the six practice frames (hub practiced, lesson dial, lesson leftover, lesson flow, week, hub empty).

Hub: title «Практика», eyebrow «приёмы инструмента». Progress «ОПРОБОВАНО · 3 из 5» (or 0 из 5 on empty). Five skill rows, each min-height 56dp, 16px radius, 8dp gap. Week card compact: one Domine duration, text «Неделя». «Показать введение» is a 48dp ghost that sits 16dp above the nav with no overlap. If the five rows plus week plus CTA do not fit 844px, make the week card one line (duration + chevron) instead of a tall block. Empty hub: track 0, all «ещё нет», primary «К плану дня», same spacing rules.

Lesson pages: back 48dp, title, one illustration ≤180dp, body, primary 48dp terracotta, ghost 48dp. Do not leave a huge empty band; group the buttons 16dp above nav. Leftover 5:3:2 bars must be visually 5:3:2 width, labeled высокая / средняя / низкая.

Week: Domine total, seven ticks with weekday labels *under* the columns, empty Wednesday sand and a calm caption. Stats row 3 columns. «В отчёты» 48dp above nav.

Draw the six corrected frames, light 390×844, same inks.
```

---

## Куда класть новые PNG

Не затирать эталонные `plan_sheet_*` и `reports_collect`, пока кадр не стал явно лучше. Новые исправленные экраны дня класть поверх одноимённых папок (`day_dial_view_am`, `day_pizza_view`, …) или в суффикс `_fix`, если нужно сравнить. `code.html` + `screen.png`. Tailwind-хвост после приёмки можно не хранить.

## Что сознательно не просим

Новые режимы циферблата, KMP, тёмная тема как отдельный набор, планшеты, облако, календарь, правки `:app`.
