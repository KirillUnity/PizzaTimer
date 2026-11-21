# Stitch: кадры под миссию

Миссия 0.5: [../spec/mission.md](../spec/mission.md) (§2.1 очередь, §6.4 переживание вместо кнута). Канон дня: [TZ.md](TZ.md). Кит: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md). Эти промпты не меняют `:app`.

Один блок **Prompt** — один чат на [stitch.withgoogle.com](https://stitch.withgoogle.com) и один кадр телефона. Первую фразу не сокращайте. HTML в приложение не копировать. PNG кладите в папку под промптом.

Круг остаётся главным. Меняется то, что человек читает в отверстии и на листах: одно текущее дело, одна строка «что дальше», тихий шаг, отход, одно следующее, вечер после дел.

Старые кадры, которые миссии противоречат, в сборку не брать:

| Было на кадре | Почему не брать | Чем заменить |
| --- | --- | --- |
| Бейдж «В ПОТОКЕ» в дырке | Приложение не объявляет, что человек в потоке | `mission_sitting` — имя дела и «ещё N мин» |
| Полоска «СДЕЛАНО · часы из часов» на лице дня | Сводка живёт вечером, не поверх шага | `mission_front` и `mission_evening` |
| Лист «Где остановился» сразу после паузы | Пауза лист не открывает | `mission_break` и `mission_thread` |
| Слово «Продолжить» на лице перерыва | Возврат — иконка Play | `mission_break` |
| Красное кольцо, процент, «пора», серия | Это кнут | ни один кадр ниже |

Общее для всех кадров: светлая тема, русский текст, дата «Вс, 8 ноя», низ День / Задачи / Отчёты / Настройки. 12 сверху, по часовой. Сектора одного дня: серая «Глубокая работа» 09:00–11:30, серая «Встреча» 13:00–14:00, охра «Черновик отчёта», песок свободного времени, один маленький сливовый сектор. Игла «сейчас» — терракота. Подписи половин — «ДО» и «ПО», не AM и PM.

---

## 1. Сейчас — один фронт

Папка: `stitch_timedial_planner_ui_kit/mission_front/`

День открыт, заход не идёт. В отверстии одно дело и одна строка, с чего его продолжить. Остальные сектора тихие.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the play control, sand #EADBCE for unallocated time, emerald #2E7D52 for a selected mark, warm gray #8E8B82 for completed sectors. Ochre #E59819 and cobalt #2563EB only as sector inks. Domine for the clock time and dial numerals, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

The day is a 12-hour face. Labels are «ДО» and «ПО», ПО selected. Date «Вс, 8 ноя». 12 is up, clockwise. Now needle terracotta. Sample sectors stay on the ring and none of them is raised except the front: gray done «Глубокая работа», gray done «Встреча», ochre «Черновик отчёта» with a quiet 1.5dp inner line meaning it is the front, sand leftover, one small plum slice. No view-mode chip row. No «Скрыть готовые». A small menu icon at the trailing edge.

This screen is the moment before work. The hole names ONE current action. Micro-label «СЕЙЧАС». Task name «Черновик отчёта» in Domine, not a second clock. One ink line «со второго абзаца». A 56dp terracotta circle with a play triangle and NO word on the button. Spoken name «Старт». No day total, no «сделано из», no percent, no red ring, no «пора», no streak, no badge «В ПОТОКЕ». Bottom nav: День, Задачи, Отчёты, Настройки. День is active.
```

---

## 2. Шаг — тихий заход

Папка: `stitch_timedial_planner_ui_kit/mission_sitting/`

Идёт один заход. В отверстии имя и сколько этого шага осталось. Бейджа потока нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the pause control, sand #EADBCE for unallocated time, emerald #2E7D52 unused on this frame, warm gray #8E8B82 for completed sectors. Ochre #E59819 for the live sector. Domine for durations, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

A sitting is open on «Черновик отчёта». The ochre sector has a 1.5dp inner highlight. Closed sectors stay gray and do not move. 12 is up. Now needle terracotta. Date «Вс, 8 ноя». «ДО» | «ПО» and the menu icon are outline-only. No view-mode row. No day total. No chart. No course button on the dial.

Center hole only: micro-label «ЭТОТ ШАГ», task name «Черновик отчёта», and one quiet ink line «ещё 18 мин». That line is not red, not a countdown ring, not a percent. Do NOT write «В ПОТОКЕ», «этот заход» as a badge, or any claim that the person is in flow. Primary control is a 56dp terracotta circle with a pause icon (two bars) and NO word on the button. Spoken name «Пауза». Bottom nav stays but is visually secondary: День, Задачи, Отчёты, Настройки.
```

---

## 3. Отход — перерыв

Папка: `stitch_timedial_planner_ui_kit/mission_break/`

Пауза закрыла работу. Можно отойти. Лист не открыт. Возврат — иконка Play.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the play control, sand #EADBCE for the break wedge, emerald #2E7D52 unused here, warm gray #8E8B82 for the closed sitting. Ochre may remain as a thin closed memory of «Черновик отчёта». Domine for durations. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Pause has happened. The closed work interval stays where it was. From now, a sand wedge is labeled «Перерыв». The dial is dimmed one step. Date «Вс, 8 ноя». No view-mode row. No sheet. No field «где остановился». No mood chips. No practice button on the ring.

Center: micro-label «ПЕРЕРЫВ». Task name «Черновик отчёта» in warm gray. One quiet ink line «можно отойти». Do not write «пора вернуться», «ты отстал», or a step count. Primary control in the hole is a 56dp terracotta circle with a play icon and NO word on the button. Spoken name «Старт». Below the dial, one filled paper button «Завершить задачу» with a terracotta label, centered. Do NOT draw a second button «Продолжить». Bottom nav: День, Задачи, Отчёты, Настройки.
```

---

## 4. Живая нить в покое

Папка: `stitch_timedial_planner_ui_kit/mission_thread/`

Дело выбрано, заход не идёт. Одну строку «что дальше» правят здесь, не на паузе.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the play control, sand #EADBCE for unallocated time, emerald #2E7D52 for the selected mark, warm gray #8E8B82 for completed sectors. Ochre #E59819 for «Черновик отчёта». Domine for the clock time. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Nothing is running. «Черновик отчёта» is selected: a small paper callout near the ochre sector, title only, no essay. The dial stays large. Date «Вс, 8 ноя». «ДО» | «ПО», ПО selected. No view-mode row.

Under the hole, one paper chip, 16dp corners, not a modal and not a new page. Micro-label «ЧТО ДАЛЬШЕ». One outlined field containing «со второго абзаца». Helper in body-sm, ink not red: «Снимется, когда шаг начнётся». Hole: time «15:40», micro-label «СЕЙЧАС», and a play icon with no word. Do not open a pause slip. Do not show mood chips. Do not show a day total. Bottom nav: День, Задачи, Отчёты, Настройки. День active.
```

---

## 5. Следующее — лист после шага

Папка: `stitch_timedial_planner_ui_kit/mission_finish/`

Круг уже не в работе. Лист называет переживание этого шага и одно следующее дело.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the primary commit, sand #EADBCE unused on the sheet, emerald #2E7D52 for the selected numeral, warm gray #8E8B82 for secondary type. Ochre sector dimmed behind the sheet. Domine for the sheet title. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

The sitting is over. The dial is dimmed, still the Day tab, not a new destination. A level-4 paper sheet, 24dp corners, above the nav. Title «Завершить задачу». Read-only line «Черновик отчёта».

The sequence is the point of the sheet. First outlined field, visually first: label «Следующее дело», text «Кому отдать черновик». Helper «День предложит его дальше».

Then three compact 1–10 rows. Each row shows one selected numeral in an emerald oval and the others as ink outlines. «Вовлечённость» 7, «Навык» 6 with one short line «Что стало легче», «Состояние после» 8. Each row carries the micro-label «САМООТЧЁТ». Chips «Вызов» | «Помеха», «Помеха» selected. One optional field «Как сделать это вызовом», empty is fine. Primary button «Сохранить», terracotta label.

Do not draw a checkbox «я в потоке», a diagnosis, a score called «балл потока», a red error, or advice that this treats anxiety. The dial behind stays dim and shows no day total.
```

---

## 6. Вечер — ряды, не балл

Папка: `stitch_timedial_planner_ui_kit/mission_evening/`

Вкладка отчётов. Числа по отдельности. На круге этой сводки нет: круга на этом кадре нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only as a quiet tick on the active nav item, sand #EADBCE for empty tracks if any, emerald #2E7D52 for a single calm mark, warm gray #8E8B82 for secondary lines. Domine for the screen title. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

This is the Reports tab after the work, not the dial. Title «Отчёты». Date line «Вс, 8 ноя». Micro-label under the title «САМООТЧЁТ, НЕ ДИАГНОЗ».

Four paper rows, separate, never summed into one flow score:
1. «Состояния» — «7 и 8».
2. «Вызовы и помехи» — «1 вызов, 1 помеха» and one quiet line of the reframe «отдать черновик утром».
3. «Навык» — «6».
4. «Начато» — «2 дела».

No ring chart, no percent, no «балл потока», no red, no «мало успел», no streak. Empty would be the same type with zeros and the line «Закрытых отрезков нет», but draw the filled calm rows on this one frame. Bottom nav: День, Задачи, Отчёты, Настройки. Отчёты is active.
```

---

## 7. Порог — практика в меню перерыва

Папка: `stitch_timedial_planner_ui_kit/mission_practice/`

Перерыв на круге. Практики только в боковой панели. На дуге кнопки нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle, sand #EADBCE for the break wedge, emerald #2E7D52 for the selected practice row mark, warm gray #8E8B82 for closed work. Plus Jakarta Sans for UI. Domine for the panel title. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

A break is open. The dial is visible, dimmed, with a sand wedge «Перерыв» and the hole line «можно отойти». The side panel is OPEN from the trailing edge, about 320dp, 24dp corners. It is not a new page. Title «День».

Section micro-label «ПРАКТИКА». Helper «Проба, не лечение». Three quiet rows, no photos, no timers shouting: «Очистка ума» 3–5 мин, «Йога-пауза» 2–3 мин, «Микро-цигун» 1–2 мин. No row is a loud card. No download, no likes, no feed.

On the dimmed dial there is NO practice button and NO course chip. Do not draw a streak, a step counter, or «пора». Bottom nav remains under the scrim: День active.
```

---

## 8. Край умения — одно предложение сузить

Папка: `stitch_timedial_planner_ui_kit/mission_nudge/`

Одна бумажная карточка. Пропуск такой же спокойный, как согласие. Наград нет.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for one text button, sand #EADBCE for leftover on the dial, emerald #2E7D52 unused, warm gray #8E8B82 for the skip. Ochre sector «Черновик отчёта» closed and gray-quiet behind the card. Plus Jakarta Sans for UI. Domine for the card title. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

The dial is the Day tab, idle, dimmed one step. A single paper card above the nav, 24dp corners. Title «Чуть уже». Body «Похожие шаги уже даются легко. Сузить следующий черновик?» Two buttons on one row, same size: «Сузить» with a terracotta label on paper, and «Пропустить» in ink, not red, not disabled-looking. No stars, no points, no confetti, no “you should”, no countdown. The closed sectors behind the card do not move and show no edited times. Bottom nav: День active.
```

---

## 9. Вечер на дне — закрытие без замка

Папка: `stitch_timedial_planner_ui_kit/mission_day_close/`

Карточка говорит, сколько минут уже записано. Play по-прежнему в отверстии: день не заперт.

### Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design ONE mobile Android phone screen, light mode, Russian UI, for TimeDial. Keep the system exactly: warm paper #FBF9F5, iron-gall ink #1C1B18, terracotta #E0533C only for the now-needle and the play control, sand #EADBCE for leftover, emerald #2E7D52 unused, warm gray #8E8B82 for helper type. Domine for the card title and the duration. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Still the Day tab. The dial remains visible and usable: hole shows «15:40» and a play icon, so the day is not locked. Closed sectors stay gray. A paper card under the dial, not a blocking dialog: title «День закрыт», duration «4 ч 05 мин уже записано», text button «К отчётам». If the minutes were zero the same card would say «Закрытых отрезков нет» in the same calm type; draw the 4 ч 05 мин version. No warning stripe, no «мало», no lock icon, no notification bell, no percent. Bottom nav: День, Задачи, Отчёты, Настройки. День active.
```
