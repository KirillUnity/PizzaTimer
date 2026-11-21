# Spec Delta

## ADDED Requirements

### Requirement: One main goal and intermediate goals
The device MUST store one main goal and any number of intermediate goals. Setting the main goal MUST replace the previous main goal rather than add a second one. The goal list MUST be available outside a session. While a session is open the list MUST NOT appear on the dial. `GoalBookTest.oneMain_andListStaysOffTheOpenDial` locks this.

#### Scenario: A second main title replaces the first
- **WHEN** the user sets a main goal and then sets another title
- **THEN** only the latest title is the main goal

#### Scenario: Open session hides the list on the dial
- **WHEN** a work interval is open
- **THEN** the dial does not show the goal list
