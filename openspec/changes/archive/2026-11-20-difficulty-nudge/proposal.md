# Proposal

## Why

When the same kind of task stays easy and flat, mission step д and principle 5 (section 9) ask for one quiet offer to make the next similar task a little harder. Closed intervals stay as they are. Agreement does not award points.

## What Changes

- After three similar completions in a row with skill at least 8 and engagement at most 4, offer once to harden the next similar task: already a step, an explicit requirement, or a narrower result.
- Two completions do not offer it.
- Skip does not ask about those same three again.
- Agreeing records the choice on the next task and does not change scores or closed intervals.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `task-list`: the offer, the skip, and the unchanged intervals.
- `day-stats`: the offer uses the stored self-report proxies and does not rename them to a flow score.

## Impact

- `DifficultyNudge` and a small offer on the day. No Room schema change.
