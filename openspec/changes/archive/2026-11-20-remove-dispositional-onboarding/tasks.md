# Tasks

## 1. Drop the screen

- [x] 1.1 Route `ClockPlannerProjectApp` from introduction (or already-seen) straight to the day. Remove `DispositionScreen` from the `when`. Verify preview still compiles.
- [x] 1.2 Delete `DispositionScreen`, `DispositionViewModel`, `DispositionStore`, DataStore and in-memory stores, Koin bindings, `shouldAskDisposition`, and `flow_dfs_*` strings. Update `ShortFlowScaleTest` so the “asked again” case is gone. Run `gradlew.bat :app:testDebugUnitTest`.

## Workflow follow-up

- Archive with `openspec archive remove-dispositional-onboarding --yes` only after tests pass.
