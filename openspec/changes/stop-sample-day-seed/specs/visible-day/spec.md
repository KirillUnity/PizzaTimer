## MODIFIED Requirements

### Requirement: Open today follows the local date
While the visible date is the local today, the day screen MUST replace that date with the new local date when the local calendar day changes. The screen MUST show tasks for the new date, MUST materialize recurrence for that visible date only, and MUST select the AM or PM half from the new local time. The sample day MUST NOT be inserted on first launch or because of this advance. Locked by `DayViewModelTest.midnight_followsOpenToday` and `SampleDaySeederTest.emptyStore_staysEmpty`.

#### Scenario: Today at 23:59 then the next local date
- **WHEN** the visible date is local today at 23:59 and the next clock tick is the next local date
- **THEN** the visible date equals that new local date and the half matches the new time

#### Scenario: Sample day is not inserted again
- **WHEN** the visible date advances because local today changed
- **THEN** the sample day is not seeded onto the new date by that advance

## ADDED Requirements

### Requirement: Startup does not plant a sample day
Loading the day MUST NOT insert Sleep, Deep work, Lunch, or Gym. If those fixture ids are already stored, they MUST be deleted on load. Other tasks MUST stay. `SampleDaySeederTest.emptyStore_staysEmpty` and `SampleDaySeederTest.fixtureIds_areRemoved` lock this.

#### Scenario: Empty install
- **WHEN** the task store is empty and the day screen loads
- **THEN** no sample tasks are written

#### Scenario: Leftover fixtures
- **WHEN** the store still has the debug fixture ids
- **THEN** those rows are deleted and other rows remain
