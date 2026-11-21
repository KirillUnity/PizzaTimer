## ADDED Requirements

### Requirement: Hole play and pause swap
The hole control MUST show Pause while a non-break work interval is open and MUST show Play when idle or when a break is open. Pressing Pause MUST pause. Pressing Play MUST start or resume. `HoleTimerControlTest.openWork_isPause_breakOrIdle_isPlay` locks the pairing.

#### Scenario: Start then pause
- **WHEN** the user starts a session from Play
- **THEN** the hole control is Pause

#### Scenario: Pause then play
- **WHEN** the work interval is closed and a break is open
- **THEN** the hole control is Play
