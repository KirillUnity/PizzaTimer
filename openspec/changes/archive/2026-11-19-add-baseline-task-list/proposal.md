# Proposal

## Why

The task list is already how the day is planned (mission §5.4, step «а» in part). A later change must not silently rewrite closed intervals or how untimed work shares leftover minutes. This change records that shipped behavior as a contract.

## What Changes

- Add a preservation spec for the task list. No Kotlin, schema, or navigation change.

## Capabilities

### New Capabilities

- `task-list`: Planning and editing the day's tasks, including append-only start and pause and leftover shares for untimed work.

### Modified Capabilities

## Impact

- Locked by existing tests: `TasksViewModelTest`, `RoomTaskRepositoryTest`, `TaskDraftValidatorTest`, `LeftoverLayoutTest`, `TaskTimerTest`.
- Shipped dial, flow session, and reports stay as they are.
