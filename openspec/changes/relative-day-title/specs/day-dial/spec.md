# Spec Delta

## MODIFIED Requirements

### Requirement: Day look opens from a side panel
The day face MUST NOT show view-mode chips or the hide-completed control. Those controls MUST live in a side panel titled Day (Russian: День). The panel MUST offer dial, pizza, petals, and list, plus accent, completed color, underlay, and hide completed. The side panel title stays Day (Russian: День).

#### Scenario: Resting day has no view chips
- **WHEN** the day is at rest
- **THEN** the face does not show view-mode chips or hide-completed above the clock

#### Scenario: Panel edits the shared look
- **WHEN** the user changes view or underlay in the panel
- **THEN** the face updates immediately and Settings shows the same view and underlay after a restart

## ADDED Requirements

### Requirement: Day strip names today, yesterday, and tomorrow
On the day screen, the date-strip title MUST be «Сегодня» when the visible date is the local today, «Вчера» when it is the previous local calendar date, and «Завтра» when it is the next local calendar date. The default English catalog MUST read Today, Yesterday, and Tomorrow for those three dates.

#### Scenario: The open day reads today
- **WHEN** the day screen shows the local today
- **THEN** the date-strip title is «Сегодня» in Russian and Today in the default English catalog

#### Scenario: The previous calendar date reads yesterday
- **WHEN** the day screen shows the local calendar date before today
- **THEN** the date-strip title is «Вчера» in Russian and Yesterday in the default English catalog

#### Scenario: The next calendar date reads tomorrow
- **WHEN** the day screen shows the local calendar date after today
- **THEN** the date-strip title is «Завтра» in Russian and Tomorrow in the default English catalog

### Requirement: Other day-strip titles stay dated
A visible date that is not local today, the previous local date, or the next local date MUST keep the existing short weekday-and-month title (`EEE, d MMM`). The Tasks date strip MUST keep its current title and date line.

#### Scenario: A further day keeps the short date
- **WHEN** the day screen shows a date two or more local days away from today
- **THEN** the date-strip title stays the short weekday-and-month form

#### Scenario: The task list header is unchanged
- **WHEN** the task list shows any date, including local today
- **THEN** its date strip does not use Сегодня, Вчера, or Завтра in place of its current title
