# Proposal

## Why

View chips and “Hide done” sit on the day face, including while a session is running. Mission principle 7 (section 9) keeps the immersion moment to the work in front of the user and moves appearance aside. The look (view, accent, completed color, underlay) already lives in settings and DataStore. This change moves those controls into a side paper panel so the face stays the clock.

## What Changes

- Remove the view-mode chips and the hide-completed chip from the day face. Before-noon / after-noon and reset-rotation stay.
- A side panel titled “День” / “Day” offers view (dial, pizza, petals, list), accent, completed color, underlay, and hide-completed.
- The panel and Settings write the same `Appearance` through `AppearanceRepository`. A change is visible at once and still there after restart.
- Hide-completed still reflows remaining work on canvas views and only filters the list. Weights stay 5:3:2. No new store.
- During a running session the hole still shows the task name and this session’s time. The view row is not on the face.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `day-dial`: Where view and hide-completed are offered. Geometry, reflow, leftover shares, and rotation that does not edit times stay as specified.

## Impact

- `DayScreen`, `DayChromeRow`, a new side panel composable, `DayUiIntent.SetLook`.
- Strings in `values` and `values-ru`.
- A unit test that a look chosen on the day is the appearance Settings reads back.
- `ReflowTest` and `VisibleListTasksTest` stay.
- No Room change, no Hilt, no dial geometry change.
