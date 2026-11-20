# visible-day Specification

## Purpose

Choose which local date the day screen shows, and advance that date at midnight only while the screen is still on today.

## Requirements

### Requirement: Open today follows the local date
While the visible date is the local today, the day screen MUST replace that date with the new local date when the local calendar day changes. The screen MUST show tasks for the new date, MUST materialize recurrence for that visible date only, and MUST select the AM or PM half from the new local time. The sample day MUST NOT be inserted again because of this advance. Locked by `DayViewModelTest.midnight_followsOpenToday`.

#### Scenario: Today at 23:59 then the next local date
- **WHEN** the visible date is local today at 23:59 and the next clock tick is the next local date
- **THEN** the visible date equals that new local date and the half matches the new time

#### Scenario: Sample day is not inserted again
- **WHEN** the visible date advances because local today changed
- **THEN** the sample day is not seeded onto the new date by that advance

### Requirement: A chosen other date stays across midnight
A date the user opened with the day arrows or the calendar MUST stay selected when the local date changes, including when that chosen date is yesterday. Returning the visible date to local today MUST follow the clock again. Locked by `DayViewModelTest.midnight_followsOpenToday`.

#### Scenario: Yesterday does not move
- **WHEN** the visible date is yesterday and the clock ticks from 23:59 onto the next local date
- **THEN** the visible date stays yesterday

#### Scenario: Returning to today follows again
- **WHEN** the user selects local today after having viewed another date
- **THEN** a later local date change advances the visible date again

### Requirement: An open timer is not rewritten at midnight
This change MUST NOT close an open interval and MUST NOT append minutes after midnight onto the interval that was already open. Closed intervals stay unchanged.

#### Scenario: Open block survives the date change
- **WHEN** an interval is open as local today becomes the next date
- **THEN** that interval stays open with the same start and gains no end minute from the date change
