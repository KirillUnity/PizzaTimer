# Design

## Context
`RunningSessionHole` already routes pause vs resume, but the glyph is always pause bars. Compact mode passes a null task into the hole, so the idle Play stays on screen after Start.

## Goals / Non-Goals
- Goal: the visible icon matches Start vs Pause.
- Non-goals: bringing Continue as a labeled button, changing break wedges.

## Decisions
1. **`holeTimerControl(workOpen)`** returns Pause only while a non-break interval is open.
2. **`RunningSessionHole` uses PlayArrow when `paused`.**
3. **Hole task falls back to the open work task** so compact and a cleared selection still show Pause.

## Risks / Trade-offs
- [Pause icon while untitled Play runs] → That is a real session; Pause is correct.

## Migration Plan
None.

## Open Questions
None.
