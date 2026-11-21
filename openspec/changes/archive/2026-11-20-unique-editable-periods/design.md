# Design

## Context
Pause writes a tagged break task. `visibleTasks` lists every row, so `#break` appears. `RemoveBlock` exists but `IntervalRow` never calls it. Closed blocks stay immutable.

## Goals / Non-Goals
- Goal: one civil period per slot on a day; unclosed periods editable and removable.
- Non-goals: rewriting closed `TimeBlock`s, showing breaks as real tasks.

## Decisions
1. **Filter `BreakSession.isBreak` from `visibleTasks` and `#break` from tag chips.**
2. **`TimeBlock.overlaps`** uses half-open civil ranges; overnight splits at midnight. Zero-length occupies one minute.
3. **`TaskDraftValidator`** takes other blocks on that date and returns `OverlappingPeriod`.
4. **Interval row:** tap the range → start picker; end ±15; Remove on unclosed blocks. Time picker overlay applies `ChangeBlockStart` / `ChangeBlockEnd`.

## Risks / Trade-offs
- [Leftover break rows remain in Room] → Hidden from the list; day dial still uses the open wedge.

## Migration Plan
None.

## Open Questions
None.
