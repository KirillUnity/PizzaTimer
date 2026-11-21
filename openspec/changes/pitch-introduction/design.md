# Design

## Context
`OnboardingScreen` is the root of `setContent` while the introduction is showing. It is outside `NavigationSuiteScaffold`, so it does not inherit that scaffold’s insets. `padding(24.dp)` is shorter than the 3-button navigation bar, which is why «Дальше» is clipped. The pager column pins text to the top and leaves the rest blank.

## Decision
The column uses `safeDrawingPadding()` so the status bar and the navigation bar stay outside the controls.

Four pages, fixed order:

1. Queue — what to do now and what comes next, without a whip (§2, §6.4).
2. Place — the dial is the one place; one front is in the work (§6.3).
3. Step — one quiet session, one sound at the edge, the ring does not turn red (§2.1, §6.4).
4. Thread — the finish names the experience and one next line; evening rows come after the work (§2.1).

Figures are Compose, in the paper tokens, not Stitch HTML and not bitmaps. They fill the flexible middle of the page. Petal and practice sketches stay where they are.

## Rejected
- Teaching leftover shares on page two. That is layout, not the bet.
- A fifth page about retention or a market. The person opening the app should see the product, then the day.

## Tests
No unit test locks the old copy. `ShortFlowScaleTest` still locks that skipping the introduction does not ask the dispositional form. Verify the inset and the four figures by opening the introduction on a phone with a 3-button bar.
