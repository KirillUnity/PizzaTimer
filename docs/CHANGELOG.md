# Changelog

## 0.4.0 — 2026-10-07

Week 4: instances and append-only timer.

### Added
- Move instance to another calendar day (`date` only; blocks and tags kept)
- Duplicate: new `TaskId`, independent copy of fields, tags, and blocks
- Recurrence **daily** or **weekdays**; materialize the visible day only (no infinite horizon, no calendar sync)
- Tap raises the selected sector (paint last); description **callout** on DIAL and PIZZA
- Start / Pause in the ring hole (and pizza center): Start appends an open `TimeBlock`; Pause closes it at now
- Open blocks draw start → now via `Reflow.layout(..., nowMinute)`; several sectors share one `TaskId`
- Room schema **v3** with `fallbackToDestructiveMigration()` — **no `Migration` class**; update wipes local DB

### Not in 0.4
- Reports, media attachments, stats export (week 5)
- Appearance DataStore, onboarding, Play upload
- KMP / iOS, Google/Apple Calendar
- Git tag (do not tag unless asked)

### Suggested git (do not run unless asked)

```
git add -A
git commit -m "feat: ship TimeDial v0.4 instances and timer"

git tag v0.4
```

## 0.3.0 — 2026-10-07

Week 3: flexible time, leftover, importance, tags.

### Added
- View modes: DIAL (ring), PIZZA (pie from center), PETALS (cached bezier leaves), LIST (Compose)
- `DialRenderer` + `RendererFactory`; LIST is not Canvas
- Day-screen switcher; hide-completed reflow on canvas, list filter only
- `TimeBlock` list on `Task`; empty list = untimed leftover; several blocks = several sectors, one id
- `Importance` HIGH/MEDIUM/LOW with leftover weights **5:3:2** (50/30/20 when all three differ)
- Tags (chips + Tasks-screen filter); no category CRUD
- Leftover layout in `kit.core.layout`: civil timed clips, untimed share leftover, Rest only if leftover remains with no untimed
- Editor: save without time; several intervals; closed blocks (`end` already past) are read-only
- Room schema **v2** with `fallbackToDestructiveMigration()` — **no `Migration` class**; update wipes local DB

### Not in 0.3
- Center Start/Pause timer, recurrence, move/duplicate
- Reports, stats export, appearance DataStore
- KMP / iOS, calendar sync

### Suggested git (do not run unless asked)

```
git add -A
git commit -m "feat: ship TimeDial v0.3 leftover, blocks, and tags"

git tag v0.3
```

## 0.2.0 — 2026-10-07

Week 2 interactivity of **TimeDial Planner**.

### Added
- Task CRUD on the Tasks screen (list, FAB, form, palette, TimePicker, status)
- Validation: non-empty title, end ≠ start, overnight ranges allowed
- Full task card: complete/reopen, edit, delete with confirm; DONE sectors stay gray
- Two 12-hour faces (AM/PM); overnight work is two arcs via `TimeMath` / `clipTaskToHalf`
- Two-finger scene rotate (`userRotationOffset`); reset control; now still at 12 unless offset
- Resize duration from the trailing edge (5-minute snap, min 5 minutes)
- Hide completed: CompactRemaining reflow — remaining work fills 360° by duration, not a list filter
- Rest sectors in wall-clock gaps; tangent labels; TalkBack custom actions on the dial
- Overlap z-order: shorter duration paints on top and wins hit-test

### Not in 0.2
- Pizza / petals / list view modes, wallpaper presets
- Settings appearance, notifications, stats export
- KMP / iOS, calendar sync

### Suggested git (do not run unless asked)

```
git add -A
git commit -m "feat: ship TimeDial v0.2 interactivity"

git tag v0.2
```

## 0.1.0 — 2026-10-06

First vertical slice of **TimeDial Planner** (week 1).

### Added
- Domain model (`Task`, minutes from midnight, overnight ranges) and Room persistence
- MVI day screen, Koin, `TimeProvider` synced at 1 Hz
- 24-hour ring Canvas: colored sectors, labels, DONE in gray
- Scene rotation so **now stays at 12 o’clock**, plus a now marker
- Hit-testing and a read-only task bottom sheet
- Debug sample day seeder, empty state, snackbar on repository errors
- Unit tests for `TimeMath`, hit-test, and repository mapping

### Not in 0.1
- Task CRUD UI, rotate/resize gestures, hide-completed reflow
- Pizza / petals / list modes, settings, notifications, KMP / iOS
