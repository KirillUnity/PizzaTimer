# Proposal

## Why

The finish sheet still reads as a clinical disclaimer: «самоотчёт, не клиническая шкала», a next-task field, and a long caption about paraphrased items and diagnosis. The user asked to drop those lines and to label the nine ratings as a 5-point scale.

## What Changes

- Remove the self-report caption under the title.
- Remove the «Следующее дело» field from the sheet. A blank next name still creates nothing.
- Replace the short-form caption with «Оцените по 5-балльной шкале» (English: “Rate on a 5-point scale”).
- Keep the 1–10 proxies, demand mark, optional nine items, and FSS-2 wording off the sheet.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `flow-session`: finish-sheet copy; short-form caption is a 5-point prompt.
- `task-list`: the sheet does not show a next-task field.

## Impact

- `CompletionSheet.kt`, `completion_proxy_caption` / `flow_form_caption` strings.
