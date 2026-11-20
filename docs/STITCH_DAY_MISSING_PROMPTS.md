# Stitch: отдельные промпты недостающих кадров

Канон: [TZ.md](TZ.md). Очередь: [ROADMAP_NEXT.md](ROADMAP_NEXT.md). Общий набор: [STITCH_DAY_NEXT_PROMPT.md](STITCH_DAY_NEXT_PROMPT.md). Кит: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md).

Уже есть день с ДО/ПО в папках `utf_8_*`. Ниже только то, чего в наборе нет. Каждый блок **Prompt** вставляется в [stitch.withgoogle.com](https://stitch.withgoogle.com) отдельно. Первую фразу не сокращайте: она держит бренд.

Один промпт — один кадр. HTML в `:app` не копировать. PNG кладите в папку под промптом.

Свежий заход лёг не в `day_next_*`, а в папки `_82`, `_86`, `_88`, `horological_paper_craft_3`, `_89`, `_90`. Для сборки берём их. Рядом есть более шумные дубли: их не переносим на экран.

| Кадр | Брать | Не брать |
| --- | --- | --- |
| Боковая панель | `_82` | `horological_paper_craft_2` — тот же лист, если он обрезан так же |
| Старт без выбора | `_86` | `_87` кладёт «Новое дело» внутрь круга и добавляет легенду; `_79` пишет «Новое дело не выбрано» |
| Этот заход | `_88` | `_80` подписывает кнопку словом «Пауза»; `_85` показывает `42:02` и почти пустое кольцо |
| Перерыв | `horological_paper_craft_3` | `_81` — римские цифры и карточки под кругом |
| Лист завершения | `_89` | `_84` — тот же лист пустыми полями, годится как второй кадр |
| Удаление | `_90` | `_83` добавляет значок в шапку диалога |

На кадрах шапка часто подписана латинским «Den». В приложении остаётся «День». Под кругом нет сводок вроде «Аналитический срез» и «Хронометр № 408».

---

## 1. Боковая панель

Папка: `stitch_timedial_planner_ui_kit/day_next_menu/`

На лице дня нет ряда «Циферблат / Пицца / Лепестки / Список» и нет «Скрыть готовые». Они живут в панели.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the primary commit, sand #EADBCE for unallocated time, emerald #2E7D52 for a selected mark, warm gray #8E8B82 for completed sectors. Ochre #E59819 and cobalt #2563EB only as sector inks. Domine for dial numerals, Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

The day is a 12-hour face. Labels are «ДО» and «ПО», not AM and PM. ПО is selected. Date «Вс, 8 ноя». Time in the hole «15:40», micro-label «ЖИВОЙ ХРОНО». Now needle terracotta. Sample sectors behind the scrim: gray done «Глубокая работа», gray done «Встреча», ochre «Черновик отчёта», sand leftover, one small plum slice. Bottom nav: День, Задачи, Отчёты, Настройки. День is active.

Draw the side panel OPEN. The dial stays visible, dimmed one step. This is not a new page. Panel is a paper sheet from the trailing edge, about 320dp wide, 24dp corners. Title «День».

Section «ВИД»: four quiet rows — Циферблат (selected, emerald mark), Пицца, Лепестки, Список.
Section «ВНЕШНИЙ ВИД»: a row of accent dots, one selected terracotta; a gray swatch «Цвет готового»; three underlay chips, one selected.
Section «ДЕНЬ»: a switch «Скрыть готовое», off.

On the dimmed day itself there is NO view-mode chip row and NO «Скрыть готовые» control. Only the date, ДО | ПО, a small menu icon, and the dial. No settings essay, no export, no dark-theme hero, no charts. Scrim tap would close the panel; do not draw a second state.
```

---

## 2. Старт без выбранного дела

Папка: `stitch_timedial_planner_ui_kit/day_next_play/`

Ни один сектор не выбран. В центре круг с иконкой play и строка «Новое дело». Слова «Старт» на кнопке нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle, the live play control, and primary commit, sand #EADBCE for unallocated time, emerald #2E7D52 for constructive progress, warm gray #8E8B82 for completed sectors. Ochre #E59819 and cobalt #2563EB only as sector inks. Domine for the clock time and dial numerals, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Nothing is selected. Play still starts a new untitled task. The button is an icon only.

Top: date «Вс, 8 ноя». Segmented pair «ДО» | «ПО», ПО selected, ink on paper, not a loud filled pill. A small paper icon button at the trailing edge, spoken name «Меню дня». Do NOT draw the view-mode row and do NOT draw «Скрыть готовые» on this face.

Dial dominates. Sectors sit on the ring, none raised, no callout: gray «Глубокая работа» 09:00–11:30, gray «Встреча» 13:00–14:00, ochre «Черновик отчёта», sand leftover, one small plum slice. 12 is up, clockwise. Now needle terracotta.

Center hole: micro-label «ЖИВОЙ ХРОНО», time «15:40» in Domine, and a 56dp terracotta circle with a play triangle and NO word on the button. Spoken name «Старт». Under the circle one line «Новое дело». No task title. No chart. No percent. Bottom nav: День, Задачи, Отчёты, Настройки. День active.
```

---

## 3. Этот заход и тихий остаток

Папка: `stitch_timedial_planner_ui_kit/day_next_timer/`

Во время захода на круге только этот заход. Остаток — одна строка «ещё 18 мин». Пауза — иконка без слова «Пауза».

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the pause control, sand #EADBCE for unallocated time, emerald #2E7D52 for constructive progress, warm gray #8E8B82 for completed sectors. Ochre #E59819 for the live sector. Domine for durations, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

A sitting is open on «Черновик отчёта». The ochre sector has a 1.5dp inner highlight. Closed sectors stay gray and do not move. 12 is up. Now needle terracotta. Date «Вс, 8 ноя». «ДО» | «ПО» and the menu icon are outline-only, quiet. No view-mode row. No «Скрыть готовые». No day total. No chart. No percent ring.

Center hole only: micro-label «В ПОТОКЕ», Domine elapsed «00:42», task name «Черновик отчёта», caption «этот заход», and one quiet ink line «ещё 18 мин». That line is not red, not a countdown ring, not a second clock. Primary control is a 56dp terracotta circle with a pause icon (two bars) and NO word on the button. Spoken name «Пауза». Bottom nav stays but is visually secondary: День, Задачи, Отчёты, Настройки.
```

---

## 4. Перерыв

Папка: `stitch_timedial_planner_ui_kit/day_next_break/`

Пауза уже закрыла рабочий отрезок. На круге песочный клин «Перерыв». Кнопка «Завершить задачу» и продолжение иконкой play.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the primary label, sand #EADBCE for the break wedge, emerald #2E7D52 for constructive progress, warm gray #8E8B82 for the closed sitting. Ochre #E59819 may remain as a thin memory of «Черновик отчёта», but that work slice is already closed and must not look live. Domine for durations. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Pause has happened. The closed work interval stays where it was. From now, a sand wedge is labeled «Перерыв». The dial is dimmed one step. Date «Вс, 8 ноя». No view-mode row. No mood chips. No field «где остановился». No chart.

Center: micro-label «ПЕРЕРЫВ», the closed sitting «00:42» in warm gray, helper «Прошедший отрезок не меняется». Primary paper button «Завершить задачу» with a terracotta label. Secondary control: a small play icon and the word «Продолжить», for appending a new work interval of the same task. The play icon has no extra word «Старт». Bottom nav: День, Задачи, Отчёты, Настройки.
```

---

## 5. Лист завершения

Папка: `stitch_timedial_planner_ui_kit/day_next_complete/`

Лист поверх приглушённого круга. Шкалы подписаны «САМООТЧЁТ». Галочки «я в потоке» нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the primary commit, sand #EADBCE for unallocated time, emerald #2E7D52 for the selected numeral, warm gray #8E8B82 for unselected marks. Domine for the selected numerals. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, 24dp sheet corners, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

The sitting is over. The dial is dimmed behind a level-4 paper sheet anchored above the bottom nav. This is not a new destination. Title «Завершить задачу». Read-only line «Черновик отчёта». Outlined field «Следующее дело», placeholder «Кому отдать черновик».

Three compact 1–10 rows. Each row shows numerals, one selected inside an emerald oval, the rest ink outlines. Each row carries the micro-label «САМООТЧЁТ»:
- «Вовлечённость» selected 7
- «Навык» selected 6, plus one short outlined line «Что стало легче»
- «Состояние после» selected 8

Chips «Вызов» | «Помеха», «Помеха» selected. Outlined field «Какие мысли мешали». Optional field «Как сделать это вызовом». Chips «Сделано» | «Не до конца», «Не до конца» selected, plus one line «Какой прогресс уже есть». Primary button «Сохранить» terracotta.

Do not draw a checkbox «я в потоке», a diagnosis, a treatment claim, a chart, or FSS-2. The sheet may scroll; keep the dial visible at the top edge. Bottom nav still peeks: День, Задачи, Отчёты, Настройки.
```

---

## 6. Удаление

Папка: `stitch_timedial_planner_ui_kit/day_next_delete/`

Основная кнопка — «Оставить». «Удалить» — спокойный текст, без красной тревоги. Пока этого кадра нет, диалог в приложении не перерисовывают.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the safe primary button and the now-needle, sand #EADBCE for unallocated time, warm gray #8E8B82 for completed sectors, ochre #E59819 for the selected sector. Plus Jakarta Sans for UI. 8dp grid, 48dp targets, 24dp dialog corners, tonal paper elevation, no blue shadows, no neon, no glassmorphism. Do not use error red.

Still on the day, not a new page. Date «Вс, 8 ноя». «ДО» | «ПО», ПО selected. Dial with the ochre sector «Черновик отчёта» selected and a small paper callout with that title. Other sectors: gray done work, sand leftover, one plum slice. 12 is up. Now needle terracotta.

A paper dialog over a one-step dim: title «Удалить это дело?», body ««Черновик отчёта» и его отрезки исчезнут с этого дня». Primary button «Оставить» in terracotta, the strong safe path. Secondary text button «Удалить» in ink, not a filled alarm, not red. No trash illustration, no shake, no warning stripe, no undo toast. Bottom nav: День, Задачи, Отчёты, Настройки.
```
