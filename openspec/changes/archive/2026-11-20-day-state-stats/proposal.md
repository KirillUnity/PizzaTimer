# Proposal

## Why

The completion sheet stores 1–10 proxies, a challenge or hindrance mark, and short notes. Mission step б and principle 7 (section 9) ask for a quiet evening look at those numbers, separate from each other, and not on the dial while a session is open. They are a self-report, not a flow score.

## What Changes

- The existing reports screen shows, for the selected day: states 1–10 of completed tasks; hindrance and challenge counts plus the thought lines; mean skill and the “what got easier” lines; how many tasks were started.
- Each row is labeled as a self-report.
- An empty day shows zeros and empty lines, with no warning.
- The text stays on the device. The dial does not show this summary while a session is open.

## Capabilities

### New Capabilities

- `day-stats`: separate self-report rows for one day, including the empty day and the rule that an open session hides this summary from the dial.

### Modified Capabilities

- None.

## Impact

- `DayStateStats` in `kit.core`, a block on the reports screen, `DayStateStatsTest`.
- No Room schema change, no chart, no FSS-2.
