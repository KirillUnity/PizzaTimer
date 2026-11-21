# Design

## Context

See proposal.md. `TaskCompletion.orNull` returns null without three proxies and a mark.

## Goals / Non-Goals

- Goal: Save always stores; the sheet can reopen with the stored values.
- Non-goal: changing the 1–10 model or allowing a missing mark in the codec.

## Decisions

1. **Defaults on submit.** 5 for each missing proxy; challenge if the mark is unset.
2. **Same `completion:{taskId}` report.** Upsert keeps one editable record.
3. **Open from the task sheet.** `OpenCompletion` loads decode into `CompletionDraft`.

## Risks / Trade-offs

- [Unset fields become 5] → The sheet shows 5 on reopen; the user can change them.

## Migration Plan

None.

## Open Questions

None.
