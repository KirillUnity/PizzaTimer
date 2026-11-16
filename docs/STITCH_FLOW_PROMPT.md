# Stitch: поток, прогресс, фиксация

Канон циферблата: [TZ.md](TZ.md). Визуальный кит: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md). Этот промпт не меняет `:app`.

Вставляйте блок **Prompt** целиком в [stitch.withgoogle.com](https://stitch.withgoogle.com). Если проект кита уже открыт, первой фразой добавьте: «Продолжай набор Horological Paper Craft, не создавай новый бренд».

Локальные кадры по этому промпту уже лежат в папках ниже (`code.html` + `screen.png`). Их собрали в ките Horological Paper Craft, потому что Stitch открывается во iframe и без входа в Google промпт туда не вставить. HTML в `:app` не копировать.

Что получится: шесть мобильных кадров одного дня — простое, выбранный сектор, поток, листок паузы, возврат, закрытие дня. Тон спокойный, без серий, очков, конфетти и помодоро.

Код Compose, новые режимы циферблата, облако отчётов и правка закрытых блоков в эту поставку не входят. PNG складывать в `stitch_timedial_planner_ui_kit/` по именам экранов ниже.

---

## Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design a mobile Android UI increment for TimeDial, an existing clock-face day planner. Keep the Horological Paper Craft system exactly: warm paper `#FBF9F5`, iron-gall ink `#1C1B18`, terracotta `#E0533C` only for now-needle, live session, and primary commit, sand `#EADBCE` for unallocated time, emerald `#2E7D52` for constructive progress, warm gray `#8E8B82` for completed sectors. Domine for dial numerals and durations, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Product truth, do not redesign away: the day is a 12-hour clock face (AM or PM), 12 o’clock is up, clockwise. Timed work sits on wall-clock hours. Untimed work shares leftover minutes. Completed sectors are gray. The past is immutable. Start only appends a new interval; Pause closes it at now. There is no pomodoro, no streaks, no points, no confetti, no countdown anxiety.

Goal of this increment: show engagement as calm progress, and capture flow state at the moment work pauses. The dial stays the hero. Add a quiet progress reading that is separate from the clock angles, a visible in-flow session, a one-line checkpoint on pause, a resume hint, and a short end-of-day card.

Draw six phone screens, light mode, Russian UI copy, realistic afternoon sample (about 15:40). Sample sectors: «Глубокая работа» emerald 09:00–11:30 done gray; «Встреча» cobalt 13:00–14:00 done gray; «Черновик отчёта» ochre running or selected; sand leftover; one small plum personal slice. Now needle terracotta at 12 because the scene keeps now at the top only if that is already the kit rule — otherwise keep a normal clock with now at the true hour and a terracotta needle. Do not invent a second clock.

Screen 1 — Idle day. Top: date «Вс, 8 ноя» and a one-line progress, not a chart dashboard: label «СДЕЛАНО» and «3 ч 20 мин из 6 ч». Under it a thin 4dp rounded track, emerald fill, sand remainder. Dial dominates. Center hole: micro-label «ЖИВОЙ ХРОНО» in terracotta and time «15:40» in Domine. No play button until a sector is chosen. Bottom nav unchanged in weight: День, Задачи, Отчёты, Настройки.

Screen 2 — Sector selected, ready. Ochre sector slightly raised. Small paper callout near the sector: title «Черновик отчёта», one line of description, text button «Подробнее». Center becomes a 56dp terracotta circle with a play icon and the word «Старт». Above the button, task name in title-sm, not a second clock. The day-progress line stays.

Screen 3 — In flow. Same dial, ochre sector has a 1.5dp inner highlight. Chrome quiets: AM/PM and view-mode chips fade to outline-only; bottom nav stays but is visually secondary. Center: micro-label «В ПОТОКЕ», Domine tabular elapsed «00:42», task name «Черновик отчёта», and a short sand-to-terracotta arc meaning this session only, caption «этот заход». Primary control is a terracotta pause circle labeled «Пауза». No percent shouting, no red urgency.

Screen 4 — Pause slip. Dial dimmed one step, not a new page. A level-4 paper sheet, 24dp corners, anchored above the nav: title «Где остановился», one outlined field with placeholder «Следующий абзац — цифры за четверг», optional chips «Спокойно», «Глубоко», «Застрял» (one selected, emerald). Primary button «Зафиксировать» terracotta. Ghost button «Пропустить». Helper in body-sm: «Прошедший отрезок не меняется».

Screen 5 — Resume. After the slip closes, the ochre sector shows a paper chip: «Продолжить с: цифры за четверг» and a small play affordance. Center returns to «Старт», with the last checkpoint as body-sm under the task name. Day progress unchanged until a new interval is closed.

Screen 6 — Day close, still on the Day tab, not the Reports archive. A paper card under the dial: «День закрыт», «4 ч 05 мин в закрытых отрезках», one saved line from the last checkpoint, text button «В отчёты». Empty success is allowed: if nothing was closed, the card says «Закрытых отрезков нет» in the same calm type, no warning color.

Do not draw: settings, onboarding, plan-verdict sheets, pizza or petals or list modes, dark theme, tablets, streaks, leaderboards, notifications, calendar sync, or a full report editor. One coherent set of six frames, same sample day, same inks, enough spacing that the dial is never cropped by the progress line or the slip.
```

---

## Куда класть кадры

| Экран | Папка |
| --- | --- |
| 1. День в простое | `stitch_timedial_planner_ui_kit/day_flow_idle/` |
| 2. Сектор выбран | `stitch_timedial_planner_ui_kit/day_flow_selected/` |
| 3. В потоке | `stitch_timedial_planner_ui_kit/day_flow_running/` |
| 4. Листок паузы | `stitch_timedial_planner_ui_kit/day_flow_pause_slip/` |
| 5. Возврат | `stitch_timedial_planner_ui_kit/day_flow_resume/` |
| 6. Закрытие дня | `stitch_timedial_planner_ui_kit/day_flow_close/` |

В каждой папке — `screen.png` (и `code.html`, если Stitch его отдаёт). HTML в `:app` не копировать.
