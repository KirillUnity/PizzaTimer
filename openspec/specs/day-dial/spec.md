# day-dial Specification

## Purpose
Preserve the clock-face day: geometry, leftover shares, hide-completed reflow, and rotation that does not edit stored times.

## Requirements

### Requirement: Clock geometry
The system SHALL treat 12 o'clock as angle 0, proceed clockwise, and map a full local day of 1440 minutes to a full turn.

#### Scenario: Full day is one turn
- **WHEN** a full local day is mapped onto the dial
- **THEN** 1440 minutes cover 360 degrees starting at 12 o'clock and moving clockwise

### Requirement: Hide completed reflows the dial
On the clock, pizza, and petal views, hiding completed work MUST reflow the remaining work across the face. The list view MUST only filter completed work out. Timed tasks keep their civil hours in wall-clock mode.

#### Scenario: Canvas reflow
- **WHEN** the user hides completed work on a clock-face view
- **THEN** the remaining work is laid out across the face rather than leaving the completed gaps empty

#### Scenario: List filters only
- **WHEN** the user hides completed work in the list
- **THEN** completed rows are omitted and the remaining rows keep their times

### Requirement: Rotation does not edit task times
The system MUST rotate the scene by the user offset plus the automatic now-rotation. Stored task times stay unchanged. With no user offset, now stays at 12 o'clock. Only an interval that has not ended may be resized.

#### Scenario: Now stays at 12
- **WHEN** the user has not applied a rotation offset
- **THEN** the current minute is drawn at 12 o'clock and stored task times are unchanged

#### Scenario: Closed interval cannot be resized
- **WHEN** the user tries to resize an interval that has already ended
- **THEN** that interval stays as stored
