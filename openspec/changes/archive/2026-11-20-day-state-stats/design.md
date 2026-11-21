# Design

## Decisions

1. `DayStateStats.of` reads tasks for one date and completion records already stored by `TaskCompletionCodec`. Break wedges are not counted as started tasks. A started task is one with at least one interval.
2. State values, thought lines, and “what got easier” lines come only from completions whose task is on that day and whose `done` flag is true. Mean skill is the average of those skill proxies, or 0 when there are none.
3. The reports screen renders the rows. `DayStateStats.showOnDial` is false whenever a session is open, and the day screen does not render the summary.
4. Completion records stay out of the free-text note list so the codec is not shown as a note. They are still on the device.

## Risks

- Mean skill uses a double. The test compares with a small tolerance.
