## ADDED Requirements

### Requirement: Pause shows the break title in the hole
After Pause, the hole MUST show the break title («Перерыв») even when the inner diameter is below the session-detail threshold. Play and Pause MUST stay icon-only. `HoleTimerControlTest.pause_showsBreakCaption` locks the caption.

#### Scenario: Compact hole after Pause
- **WHEN** the user pauses a running work interval
- **THEN** the hole shows the break title and a Play icon
