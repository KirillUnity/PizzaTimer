# Proposal: Restore the break caption after Pause

## Why
After Pause the hole is often smaller than the session-detail threshold, so the word «Перерыв» never appears. The user still needs that caption when a break starts.

## What Changes
- While a break is open, the hole MUST show the break title even on a compact ring.
- Play/Pause stay icon-only. Closed intervals stay unchanged.

## Capabilities
- `flow-session`: break caption after Pause is always visible.

## Impact
`RunningSessionHole`, compact pizza/petals overlay.
