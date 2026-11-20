# Stitch: день ДО/ПО, меню, перерыв, завершение, удаление

Канон циферблата: [TZ.md](TZ.md). Очередь шагов: [ROADMAP_NEXT.md](ROADMAP_NEXT.md). Визуальный кит: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md). Этот промпт не меняет `:app`.

Вставьте блок **Prompt** целиком в [stitch.withgoogle.com](https://stitch.withgoogle.com). Если набор Horological Paper Craft уже открыт, оставьте первую фразу как есть.

Что получится: шесть кадров одного дня. Круг остаётся главным. Сначала читаются половины суток, боковая панель и старт без выбранного дела, затем заход с одним мягким остатком, перерыв, лист завершения и спокойное удаление. Вечерняя статистика, курс практик и шкала FSS-2 в эти кадры не входят.

HTML из Stitch в `:app` не копировать. PNG кладите в `stitch_timedial_planner_ui_kit/` по именам ниже. Пока кадра удаления нет, диалог удаления в приложении не перерисовывают.

---

## Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design a mobile Android UI increment for TimeDial, an existing clock-face day planner. Keep the Horological Paper Craft system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle, the live session, and the primary commit, sand #EADBCE for unallocated time and for a break, emerald #2E7D52 for constructive progress, warm gray #8E8B82 for completed sectors. Ochre #E59819 and cobalt #2563EB only as category inks on sectors. Domine for dial numerals and durations, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Product truth, do not redesign away: the day is two 12-hour faces. The labels are Russian «ДО» and «ПО», not AM and PM. ДО is midnight to noon. ПО is noon to midnight. 12 o’clock is up, clockwise. Timed work sits on wall-clock hours. Untimed work shares leftover minutes. Completed sectors are gray. Closed intervals never change. Start only appends a new interval. Pause closes the open work interval and begins a break. There is no pomodoro series, no streaks, no points, no confetti, no red countdown, no clinical form, no “I am in flow” checkbox.

Goal of this increment: the dial stays the hero. Chrome that is not this session moves into a side panel. Play can begin a new untitled task. The live session shows only this sitting. Pause becomes a break and reveals “finish the task”. Finishing is a paper sheet after the sitting, with self-report scales marked as self-report. Deleting a task is a calm confirm, not an alarm.

Draw six phone screens, light mode, Russian UI copy, the same afternoon sample (about 15:40), the same day. Sample sectors: «Глубокая работа» emerald 09:00–11:30 done gray; «Встреча» cobalt 13:00–14:00 done gray; «Черновик отчёта» ochre; sand leftover; one small plum personal slice. Now needle terracotta. Bottom nav stays День, Задачи, Отчёты, Настройки. Do not invent a second clock.

Screen 1 — Day, nothing selected, play still available. Top: date «Вс, 8 ноя». Under it a segmented pair «ДО» | «ПО», ПО selected, ink on paper, not a loud toggle. A small paper icon button at the trailing edge opens the side panel; content description «Меню дня». Dial dominates. Center hole: micro-label «ЖИВОЙ ХРОНО», time «15:40» in Domine, and a 56dp terracotta circle with a play icon and no word on the button. The spoken name is «Старт». Under the circle, one line body-sm «Новое дело». No task name yet. No chart.

Screen 2 — Side panel open. The dial is still visible, dimmed one step, not a new page. Panel is a paper sheet from the trailing edge, 24dp corners, width about 320dp. Title «День». Section micro-label «ВИД»: four quiet rows Циферблат, Пицца, Лепестки, Список; Циферблат selected with an emerald mark. Section «ВНЕШНИЙ ВИД»: a row of accent dots, one selected terracotta; a gray swatch labeled «Цвет готового»; three underlay chips, one selected. Section «ДЕНЬ»: a switch «Скрыть готовое», off. No language essay, no export, no dark-theme hero. Scrim tap closes the panel.

Screen 3 — In flow, this sitting only. Ochre sector «Черновик отчёта» has a 1.5dp inner highlight. ДО/ПО and the menu icon fade to outline. Center: micro-label «В ПОТОКЕ», Domine tabular elapsed «00:42», task name «Черновик отчёта», caption «этот заход», and one quiet line «ещё 18 мин» in ink, not red, not a percent, not a ring that shouts. Primary control is a 56dp terracotta circle with a pause icon and no word on the button. The spoken name is «Пауза». No day totals, no scores, no graphs.

Screen 4 — Break. The work slice of «Черновик отчёта» is closed and stays put. From now, a sand wedge is labeled «Перерыв». Dial dimmed one step. Center: micro-label «ПЕРЕРЫВ», the closed sitting «00:42» in warm gray, helper body-sm «Прошедший отрезок не меняется». Primary paper button «Завершить задачу», terracotta label. Secondary text button «Продолжить» with a small play icon, for appending a new work interval of the same task. No mood chips and no “where I stopped” field on this screen.

Screen 5 — Finish sheet, after the sitting. Dial dimmed, not a new destination. Level-4 paper sheet, 24dp corners, above the nav. Title «Завершить задачу». The current task already has a name, shown as a read-only line «Черновик отчёта». Outlined field «Следующее дело», placeholder «Кому отдать черновик». Then three compact 1–10 rows, each a single selected numeral in an emerald oval, the rest ink outlines: «Вовлечённость» selected 7; «Навык» selected 6, with one short outlined line «Что стало легче»; «Состояние после» selected 8. Each row has the micro-label «САМООТЧЁТ». A pair of chips «Вызов» | «Помеха», «Помеха» selected. One outlined field «Какие мысли мешали». One optional field «Как сделать это вызовом». A pair «Сделано» | «Не до конца», «Не до конца» selected, and one line «Какой прогресс уже есть». Primary button «Сохранить» terracotta. No checkbox “я в потоке”. No diagnosis, no advice that this treats anxiety.

Screen 6 — Delete confirm, still on the day. Ochre sector selected, small callout «Черновик отчёта». A paper dialog, 24dp corners: title «Удалить это дело?», body ««Черновик отчёта» и его отрезки исчезнут с этого дня». Primary button «Оставить» in terracotta, so the safe path is the strong one. Secondary text button «Удалить» in ink, not error red, not a filled alarm. No trash illustration, no shake, no warning stripe.

Do not draw: reports or charts, a tutorial course, FSS-2, onboarding, dark theme, tablets, streaks, leaderboards, notifications, calendar sync, pomodoro rings, or a second clock. One coherent set of six frames, same sample day, same inks, enough space that the dial is never cropped by the panel, the break card, or the sheet.
```

---

## Что уже лежит в наборе

Папки `utf_8_*` — свежий день с подписями ДО и ПО. `utf_8_2` не брать: русский текст в кадре сломан. `timedial_1` … `timedial_4` — другие экраны; под кругом там сводка, её на лицо дня не переносим.

| Кадр | Папка | Что сверять |
| --- | --- | --- |
| Пицца, ДО/ПО, «Скрыть готовые» выключено | `stitch_timedial_planner_ui_kit/utf_8_1/` | Дубль по смыслу с `utf_8_10` |
| Заход, «В потоке», «Пауза» | `stitch_timedial_planner_ui_kit/utf_8_3/` | Строки «ещё 18 мин» нет, ДО/ПО всё ещё на лице |
| Лепестки | `stitch_timedial_planner_ui_kit/utf_8_4/` | Вид на лице дня |
| Циферблат, «Скрыть готовые» включено | `stitch_timedial_planner_ui_kit/utf_8_5/` | Стрелка «сейчас» не на 12 |
| Циферблат, готовые скрыты, стрелка к 3 | `stitch_timedial_planner_ui_kit/utf_8_6/` | Тот же хром, что у `utf_8_5` |
| Циферблат, два живых сектора | `stitch_timedial_planner_ui_kit/utf_8_7/` | «Скрыть готовые» выключено |
| Список | `stitch_timedial_planner_ui_kit/utf_8_8/` | Готовые строки остаются в списке |
| Сектор выбран, «Старт» | `stitch_timedial_planner_ui_kit/utf_8_9/` | Старт привязан к выбранному делу |
| Пицца, ДО/ПО | `stitch_timedial_planner_ui_kit/utf_8_10/` | Ближе к покою, чем `utf_8_1` |

## Каких кадров ещё нет

Отдельные промпты и какие папки уже пришли: [STITCH_DAY_MISSING_PROMPTS.md](STITCH_DAY_MISSING_PROMPTS.md). Для сборки: `_82`, `_86`, `_88`, `horological_paper_craft_3`, `_89`, `_90`.

- боковая панель вместо ряда «вид» и «Скрыть готовые» на лице дня;
- «Старт» без выбранного дела, иконка без слова, подпись «Новое дело»;
- тихий остаток «ещё 18 мин» и пауза-иконка;
- перерыв и кнопка «Завершить задачу»;
- лист завершения с самоотчётом;
- спокойное удаление, где основная кнопка — «Оставить».

Пока кадра удаления нет, диалог удаления в приложении не перерисовывают. HTML из этих папок в `:app` не копировать.
