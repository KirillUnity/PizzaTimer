# Spec Delta

## ADDED Requirements

### Requirement: Play asks for a 15-minute length
Before an interval opens, the system MUST ask for a length. The step MUST be 15 minutes. When the task already has an end on the dial and 40 minutes remain, the offer MUST be 30 minutes. When the task has no end, the offer MUST be 25 minutes. The user MUST be able to change the length before the interval opens. The offer MUST be whole minutes. `SessionDurationTest` locks the 40-minute and untimed offers.

#### Scenario: Forty minutes offers thirty
- **WHEN** the planned end is 40 minutes ahead
- **THEN** the offered length is 30 minutes

#### Scenario: No end offers twenty-five
- **WHEN** the task has no end on the dial
- **THEN** the offered length is 25 minutes

### Requirement: The session hole shows minutes left
While an interval is open, the hole MUST show the task name and one quiet line of minutes left. It MUST NOT show a percent, a red ring, ratings, or the day’s done total.

#### Scenario: Open session hides the day total
- **WHEN** a task has an open interval
- **THEN** the day face does not show the done-today line and the hole shows minutes left

### Requirement: The chime closes the interval once
When the chosen minute arrives, the system MUST close the open interval on that minute and play one short in-app sound. A later tick MUST NOT play the sound again and MUST NOT append another interval. Already ended intervals MUST stay unchanged. The timer MUST NOT change its own length. No notification channel is created. `DayViewModelTest.timedStart_closesOnEndMinuteOnce` locks the close.

#### Scenario: Clock reaches the end minute
- **WHEN** a 15-minute session reaches its end minute
- **THEN** the open interval ends on that minute, older intervals are unchanged, and a second interval is not added

#### Scenario: A later tick does not chime again
- **WHEN** time moves past the end minute after the interval has closed
- **THEN** the sound is not requested again
