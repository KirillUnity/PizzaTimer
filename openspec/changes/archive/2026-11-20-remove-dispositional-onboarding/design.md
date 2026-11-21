# Design

## Context

See proposal.md. `ClockPlannerProjectApp` currently waits for `DispositionViewModel.asked` and, after `onboardingSeen`, shows `DispositionScreen`. `ShortFlowScale.shouldAskDisposition` is true whenever the intro is seen and the form has not been marked asked.

## Goals / Non-Goals

- Goals: never show «Перед началом»; day after intro; keep completion-sheet short form.
- Non-goals: deleting the 1–10 proxies, FSS-2 on the completion sheet, Room, the introduction itself.

## Decisions

1. **Delete the gate, not hide it.** A flag that is always “asked” would still load DataStore on launch. Remove the screen, ViewModel, store, and `shouldAskDisposition`.
2. **Leave leftover DataStore files on device.** They are unused after this change. No schema bump.

## Risks / Trade-offs

- [Users who already answered lose that profile] → The profile was not shown on reports as a separate onboarding row; reports use completion-sheet `shortFlow`. Acceptable.

## Migration Plan

Uninstall is not required. Rollback is restoring the five Kotlin files and the MainActivity branch.

## Open Questions

None.
