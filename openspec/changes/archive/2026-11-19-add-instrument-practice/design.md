# Design

## Context

See proposal.md for why. Settings is one `NavigationSuiteScaffold` destination (`AppDestinations` has four entries). Pause text, mood, and day-close already persist in `FlowMemoryStore`. Sector selection is `DayUiIntent.SelectTask` while `viewMode` is `DIAL`. Untimed save is `TasksViewModel.save` when `blocks` is empty. Start succeeds only after `TaskTimer.canStart` and the repository upsert. Commit already trims and ignores a blank note. Day close is `DayUiIntent.ShowDayClose`.

## Goals / Non-Goals

**Goals:**

- One MVI screen hosted inside Settings, with hub and lesson pages.
- Five boolean flags in a new preferences DataStore, set only, never cleared.
- A pure function for the seven weekday columns.

**Non-Goals:**

- A separate week destination, session counts, or a reports shortcut on that card.
- Recomputing leftover angles, a new `DialRenderer`, or a fifth nav item.
- Clearing a flag, streaks, XP, confetti, Room migrations.

## Decisions

1. **New `PracticeProgressStore`, not more keys on flow memory.** Flow memory is per task and per date and is cleared on Start. Practice flags are installation-scoped and sticky. Alternative: reuse `FlowMemoryStore`. Rejected because Start must keep clearing the checkpoint without clearing «tried».
2. **Flags are an enum set.** `markTried` only adds. There is no clear API. In-memory and DataStore implementations share that contract.
3. **Week math lives in `kit.core`.** `WeekClosedMinutes.columns(today, tasks)` returns Monday–Sunday of the ISO week that contains `today`. Closed duration uses `TimeMath.durationMinutes`. Open blocks (`endMinute == null`) are skipped. The UI draws column heights from those integers and does not call layout.
4. **Hooks stay at the successful action.**
   - Dial: `SelectTask` when `viewMode == DIAL` (select or the tap that clears the same sector). List, pizza, and petals do not mark it.
   - Untimed: after a successful editor upsert whose saved task has no blocks. Duplicate and move do not mark it.
   - Start: after `taskRepository.upsert` returns for that start. A rejected start does not mark it.
   - Checkpoint: in the same branch that calls `saveCheckpoint` with non-empty trimmed text.
   - Day close: in `showDayClose` next to `markDayClosed`. Dismiss does not mark or clear it.
5. **Navigation.** `SettingsDestinationContent` swaps between the settings list and Practice. `onOpenDay` sets the suite destination to Day. Onboarding reuses the existing replay callback. Settings stays selected until that handoff.
6. **Lessons are static Compose.** The dial lesson draws a fixed ring with named sweep constants. The leftover lesson is a row of three weights 5, 3, and 2 with labels. Copy comes from `docs/STITCH_LEARNING_PROMPT.md` via `values` and `values-ru`. No Stitch HTML.
7. **Progress phrase.** The hub binds `triedCount` into `practice_progress` (`ОПРОБОВАНО · %1$d из 5` / `TRIED · %1$d of 5`). The unit test locks the count and the Russian count fragment `3 из 5` through a tiny formatter used only to keep that phrase next to the resource, not as a second live string source in the composable. The composable uses the resource.

## Risks / Trade-offs

- [Start flag waits on upsert] → a crash between tap and write can miss the flag. Acceptable: the next successful Start sets it, and nothing is cleared.
- [SelectTask also fires for pizza and petals] → gate on `ViewMode.DIAL` so those modes do not mark the dial skill.
- [Parallel edits to DayViewModel] → add one constructor parameter with a default in-memory store so existing tests keep compiling.

## Migration Plan

No Room version change. First launch reads missing preferences as an empty set. Rollback is removing the Settings row; leftover preference keys are harmless.

## Open Questions

None.
