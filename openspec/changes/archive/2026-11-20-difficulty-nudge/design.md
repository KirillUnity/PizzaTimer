# Design

## Decisions

1. Similar tasks share a trimmed title. The last three completions of that title are the window. Order is the report timestamp.
2. The window qualifies only when every skill is at least 8 and every engagement is at most 4. The key is the three task ids. A skipped key is not offered again.
3. Agreeing appends a `nudge:<kind>` line to the next task description when that next task exists. Blocks and the completion record stay as saved. There is no points field.
4. The offer is a card on the day after a qualifying save. It is not drawn as a score on the dial.

## Risks

- A renamed task starts a new window. That matches “similar” as the same title.
