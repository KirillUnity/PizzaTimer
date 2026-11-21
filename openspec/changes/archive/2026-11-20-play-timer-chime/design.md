# Design

## Context

See proposal.md for why. `TaskTimer.start` appends an open block. `TaskTimer.pause` closes it. `DayUiIntent.StartTimer` still starts at once for existing tests. The hole already shows a title and an icon-only pause control. `DayDoneLine` is the day total above the dial.

## Goals / Non-Goals

**Goals:**

- Offer a length, then start with `StartFor`.
- Remember the end minute in the ViewModel and close on that minute when the clock reaches it.
- Play `res/raw/session_chime.wav` once from the day route.

**Non-Goals:**

- Shortening the timer from a flow sign, a notification channel, a red ring, or a completion sheet. Screen-off delivery is not promised.

## Decisions

1. **Floor the remainder to 15, with a floor of 15.** Forty minutes becomes 30. Under 15 minutes of remainder still offers 15 so the step stays legal. No end offers 25, which the user can then step by 15. Alternative considered: nearest 15, which would turn 40 into 45 and overshoot the planned end.

2. **Keep `StartTimer` without a budget.** Existing tests start immediately and move the clock. Only `StartFor` records an end minute, and the day screen sends `StartFor` after the sheet. A manual pause clears the budget so the chime does not fire.

3. **Close at the budget minute, not at a later now.** If the clock jumps past the end, `TaskTimer.pause` uses the budget minute. The chime effect is sent once per start.

4. **Sound stays in the composition.** The ViewModel sends `PlayChime`. The route plays the raw resource and releases the player. No channel.

## Risks / Trade-offs

- [The budget lives in the ViewModel] → a killed process does not chime. That matches the dark-screen limit. The open interval remains until the next start or pause.
- [25 is not on the 15 grid] → plus and minus move by 15 and clamp to 15…720.

## Migration Plan

No data migration. Rollback removes the sheet and the watcher.

## Open Questions

None.
