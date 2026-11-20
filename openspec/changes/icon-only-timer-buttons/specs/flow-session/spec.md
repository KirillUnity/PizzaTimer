# Spec Delta

## ADDED Requirements

### Requirement: Start and pause are icons
The start control and the pause control in the dial hole MUST show an icon and MUST NOT show a text label. The control name MUST remain available to accessibility.

#### Scenario: Idle start
- **WHEN** the user can start a sitting from the hole
- **THEN** the control shows a play icon and no word «Старт» on the hole

#### Scenario: Running pause
- **WHEN** a sitting is open
- **THEN** the control shows a pause icon and no word «Пауза» on the hole
