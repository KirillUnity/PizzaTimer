# Stitch: дашборд обучения

Канон циферблата: [TZ.md](TZ.md). Кит: [horological_paper_craft/DESIGN.md](../stitch_timedial_planner_ui_kit/horological_paper_craft/DESIGN.md). Промпт потока: [STITCH_FLOW_PROMPT.md](STITCH_FLOW_PROMPT.md). Этот промпт не меняет `:app`.

Вставляйте блок **Prompt** целиком в [stitch.withgoogle.com](https://stitch.withgoogle.com). Если проект кита уже открыт: «Продолжай набор Horological Paper Craft, не создавай новый бренд».

Это не LMS и не Duolingo. Дашборд показывает, какие **приёмы инструмента** человек уже опробовал на живом дне: циферблат, leftover 5:3:2, старт в центре, фиксация потока, закрытие дня. Прогресс — спокойные часы и «опробовано / ещё нет», без серий, очков и конфетти.

Онбординг из четырёх текстовых страниц остаётся введением. Дашборд не заменяет его и не живёт пятой вкладкой: вход с **Настроек**, нижняя навигация как сейчас.

HTML в `:app` не копировать.

---

## Prompt

```
Continue the Horological Paper Craft kit. Do not invent a new brand.

Design a mobile Android UI increment for TimeDial: a learning dashboard for the instrument, not a course catalog and not a gamified school. Keep the system exactly: warm paper `#FBF9F5`, iron-gall ink `#1C1B18`, terracotta `#E0533C` only for now-needle, live session, and primary commit, sand `#EADBCE` for unallocated time, emerald `#2E7D52` for constructive progress, warm gray `#8E8B82` for completed or not-yet-tried, ochre `#E59819` and cobalt `#2563EB` only as category inks on miniature dials. Domine for durations and titles, tabular figures. Plus Jakarta Sans for UI. Uppercase tracked micro-labels. 8dp grid, 48dp targets, tonal paper elevation, no blue shadows, no neon, no glassmorphism.

Product truth: TimeDial is a clock-face day planner. A day is two 12-hour faces. Timed work sits on wall-clock hours. Untimed tasks share leftover minutes, importance HIGH:MEDIUM:LOW = 5:3:2. Start appends a new interval; Pause closes it; the past is immutable. A checkpoint on pause is one line «где остановился». There is no pomodoro, no streaks, no points, no XP, no badges, no leaderboard, no confetti.

Goal of this increment: show calm mastery of the five crafts, and a weekly reading of closed minutes. Learning is «I tried this on a real day», not a quiz score. Do not add a fifth bottom-nav tab. The dashboard opens from Settings; Settings stays the active nav item.

Draw six phone screens, light mode, Russian UI copy, Sunday 8 Nov sample. Bottom nav on every screen: День, Задачи, Отчёты, Настройки — Настройки active in terracotta.

Screen 1 — Hub, practiced. Title «Практика». Eyebrow «приёмы инструмента». One progress line, not a chart wall: label «ОПРОБОВАНО» and «3 из 5». Thin 4dp track, emerald fill 60%, sand remainder. Five paper skill rows (56dp+, 16px radius, 4dp is not enough). Each row: 36dp round mark, title, one-line status, trailing micro-label.
  1. «День — циферблат» — «опробовано сегодня» — emerald mark with a check.
  2. «Доли leftover 5:3:2» — «опробовано» — check.
  3. «Старт в центре» — «опробовано, 42 мин» — check.
  4. «Фиксация потока» — «ещё нет» — sand mark, no check.
  5. «Закрытие дня» — «ещё нет».
Under the list a week card: «7 дней» and «4 ч 05 мин в закрытых отрезках», text button «Неделя». Helper body-sm: «Это не серия дней. Пустой день тоже в порядке.» Ghost row at the bottom: «Показать введение» (the existing four-page onboarding).

Screen 2 — Lesson: dial. Back chevron + title «День — циферблат». A 180dp miniature 12-hour ring (not a second live clock): sand leftover, one gray done sector, one ochre live sector, terracotta needle. Body: «Цветные секторы — задачи, не строки списка. Две половины: AM и PM.» Primary «Опробовать на дне» terracotta. Ghost «Понятно». No pager dots competing with the dial.

Screen 3 — Lesson: leftover. Title «Доли leftover». A 5:3:2 bar of three paper blocks labeled «высокая 5», «средняя 3», «низкая 2» in emerald / ochre / cobalt, not a pie that copies pizza mode. Body: «Задачи без часов делят свободные минуты половины. Одинаковая важность — равные доли. Отдых рисуется только если минуты ещё остались.» Primary «Опробовать на дне».

Screen 4 — Lesson: flow. Title «Старт и фиксация». Miniature dial with terracotta play in the hole. Two short steps as paper cards: «1. Выберите сектор — Старт добавляет новый отрезок.» «2. Пауза закрывает его. Одна строка: где остановился.» Helper: «Прошедший отрезок не меняется.» Primary «К живому дню».

Screen 5 — Week reading. Title «Неделя». One number in Domine: «4 ч 05 мин». Caption «в закрытых отрезках, 2–8 ноя». Seven vertical paper ticks Mon–Sun, emerald fill proportional to closed minutes that day (Sun tallest, Wed empty sand). No line chart, no percentages in red. Two quiet stats: «3 захода» and «1 фиксация». Empty-day note: «Ср без закрытых отрезков — это не ошибка.» Text button «В отчёты».

Screen 6 — Hub, empty beginner. Same layout as screen 1, but «ОПРОБОВАНО» is «0 из 5», track fully sand. All five rows «ещё нет». Week card: «Закрытых отрезков нет» in the same calm type, no error red. Primary CTA «К плану дня». The introduction link remains.

Do not draw: dark theme, tablets, pizza/petals/list as full modes, plan-verdict sheets, settings chrome (palette, backgrounds), notifications, calendar sync, flashcards, quizzes, hearts, streaks, certificates. One coherent set of six frames, same inks, skill rows never cramped under 48dp.
```

---

## Куда класть кадры

| Экран | Папка |
| --- | --- |
| 1. Хаб, уже пробовали | `stitch_timedial_planner_ui_kit/learn_hub/` |
| 2. Урок: циферблат | `stitch_timedial_planner_ui_kit/learn_lesson_dial/` |
| 3. Урок: leftover | `stitch_timedial_planner_ui_kit/learn_lesson_leftover/` |
| 4. Урок: старт и фиксация | `stitch_timedial_planner_ui_kit/learn_lesson_flow/` |
| 5. Неделя | `stitch_timedial_planner_ui_kit/learn_week/` |
| 6. Хаб, пустой | `stitch_timedial_planner_ui_kit/learn_empty/` |

В каждой папке — `screen.png` и `code.html`. HTML в `:app` не копировать.
