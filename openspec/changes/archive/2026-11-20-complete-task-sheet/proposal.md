# Proposal

## Why

Finish task closes the break and stops there. Mission steps а, б, and г, plus §6.1 (flow as a continuum), §6.2 (challenge versus hindrance), and §8.1 (a measured self-report, not a clinical claim) need a short sheet: name, next task, and 1–10 proxies. The proxies are not an FSS-2 score.

## What Changes

- Finish task still closes the break, then opens a sheet over a dimmed dial.
- An untitled task asks for a name. A titled task shows the name read-only.
- «Следующее дело» creates a separate task only when the name is not empty. That task has no open interval.
- Engagement, skill, and state-after are integers 1…10. The sheet says they are a self-report, not a clinical scale. A missing value keeps the sheet open and saves nothing.
- Skill has a short “what got easier” line. One mark is challenge or hindrance, never both. Thoughts can be empty. A hindrance may include how to turn it into a challenge.
- Done or not. If not done, a progress line can be saved without a finale.
- No “I am in flow” checkbox and no FSS-2 wording.
- Scores live on the task, not inside a closed TimeBlock. Closed intervals stay unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `flow-session`: Finish opens the completion sheet. The sheet refuses an empty state proxy.
- `task-list`: Scores attach to the task. The next task is a new row without an open interval.

## Impact

- `TaskCompletion` codec, a completion sheet on the day, scores stored in the existing report row (no Room version bump).
- Tests for a refused empty state, a named next task, and codec round-trip.
