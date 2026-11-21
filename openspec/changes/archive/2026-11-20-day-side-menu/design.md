# Design

## Context

See proposal.md for why. `DayChromeRow` draws half chips, every `ViewMode`, and hide-completed on the day face. `Appearance` (view, accent, completed color, background, dark theme) is already in DataStore via `AppearanceRepository`. `DayViewModel` observes it and `SetViewMode` writes `viewMode` back. Hide-completed is only `DayUiState.hideCompleted`. `Reflow` and `visibleListTasks` already implement canvas reflow versus list filter. `RunningSessionHole` already shows the title and session elapsed.

## Goals / Non-Goals

**Goals:**

- Drop view and hide-completed chips from `DayChromeRow`.
- Add a paper side panel that writes the same `Appearance` fields Settings writes, and toggles the existing hide-completed flag.
- Keep dark theme when the panel saves a look.

**Non-Goals:**

- A new preference, dial geometry, Play without a selection, a break, or stats. Photo import stays on Settings; the panel can select none, a solid color, and the existing presets, and it keeps a photo that Settings already stored.

## Decisions

1. **`SetLook` merges onto the stored appearance.** The panel does not carry `darkTheme`. The ViewModel copies view, accent, completed color, and background onto `latestAppearance` and saves that. Alternative considered: a second DataStore for the panel. That would split the look the spec says is shared.

2. **Menu open state stays in composition.** The day clock rebuilds `DayUiState` on each tick. A `menuOpen` flag there would need to be copied in that rebuild. `remember` in `DayScreen` avoids that.

3. **Reuse `AppearancePalette` and the existing background kinds.** The Stitch frame shows extra swatches. The app already offers three accents and three completed colors. The panel uses those so Settings and the panel cannot disagree.

4. **Leave `Reflow` and `visibleListTasks` alone.** Hide-completed still calls `ToggleHideCompleted`. Canvas modes already pass compact reflow when the flag is set.

## Risks / Trade-offs

- [Panel and Settings duplicate swatch widgets] → both call `AppearancePalette` and `BackgroundConfig`, and a unit test checks the repository value.
- [A photo underlay cannot be imported from the panel] → Settings keeps the importer; the panel does not clear a stored photo when it saves another field.

## Migration Plan

No data migration. Rollback restores `DayChromeRow` chips.

## Open Questions

None.
