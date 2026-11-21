# Design

## Decisions

1. Scores are a `TaskCompletion` value encoded into one `TaskReport` whose id is `completion:<taskId>`. The 1–10 fields are documented as self-report proxies, not a clinical scale and not FSS-2. A new Room column would wipe the local database; the existing report text column already stores device-only notes.
2. Finish closes the open break, then sets the sheet open for the work task id stored on that break. The dial stays underneath a scrim.
3. Submit builds `TaskCompletion` only when state, engagement, and skill are integers 1…10 and exactly one demand mark is set. Otherwise the sheet stays open and nothing is written. An untitled task also needs a non-blank name.
4. A non-blank next-task name inserts one task on the same day with no blocks. An empty name inserts nothing. Closed blocks of the finished task are copied through unchanged. Done sets `TaskStatus.DONE`. Not-done keeps the previous status and stores the progress line on the completion.
5. The task note list skips reports whose text is a completion record, so the codec is not edited as a free-text note.

## Risks

- A user report that happens to start with the codec prefix would be hidden. The prefix includes a version token that normal notes do not use.
