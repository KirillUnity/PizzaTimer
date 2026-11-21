# Design

## Context

See proposal.md. `TaskCompletionCodec.reportId` was one row per task. `TimeBlock` had no stable id; Room already stored one. Goals are a map of string keys. Notes are `TaskReport` rows.

## Goals / Non-Goals

**Goals:**

- One completion row per interval, keyed by task id and block id.
- Interval goal links and notes use that same block id.
- Saving a completion leaves task status unchanged.

**Non-Goals:**

- Removing the separate complete/reopen control on the task sheet.
- Coloring a sector from the interval’s done flag.

## Decisions

1. **Block id lives on `TimeBlock`.** Start assigns a UUID. Blank ids are filled when the editor opens or when a score sheet opens. Room already has the column.
2. **Scores stay in the report text.** The id is `completion:{taskId}:{blockId}`. The minutes of the block are not rewritten.
3. **Goals reuse `taskLinks`.** The key is `taskId#blockId`. A task with no intervals still uses the task id.
4. **Notes gain `blockId`.** Schema version 8, destructive migration, no `Migration` class. The editor shows notes for the selected interval.

## Risks / Trade-offs

- [An older completion row has no block id] → it is not reused; the next close writes a new interval row.
- [Version 8 wipes the local database] → accepted by the project rule.

## Migration Plan

No data migration. Rollback is restoring the task-level report id and dropping `blockId` from notes.
