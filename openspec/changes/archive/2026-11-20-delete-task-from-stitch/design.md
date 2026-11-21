# Design

## Decisions

1. The day screen uses its own string resources so the task-editor dialog keeps its current copy.
2. «Оставить» is a filled button tinted with the terracotta token. «Удалить» is a `TextButton` and does not use the error color as a fill.
3. Confirm deletes the task through the repository (intervals go with it) and deletes every report for that task, including the completion record. Dismiss only clears `pendingDelete`.
4. Neighbor tasks are not rewritten.

## Risks

- Room already cascades reports when the task row goes away. The explicit report delete keeps the in-memory test store honest.
