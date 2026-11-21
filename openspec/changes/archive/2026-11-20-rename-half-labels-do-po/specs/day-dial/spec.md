# Spec Delta

## ADDED Requirements

### Requirement: Half labels say before noon and after noon
The day face SHALL label 00:00–12:00 as ДО in Russian and “Before noon” in English, and 12:00–24:00 as ПО in Russian and “After noon” in English. Russian TalkBack MUST read «до полудня» and «после полудня». English content descriptions MUST use those English phrases. Visible labels and content descriptions MUST NOT contain the abbreviations AM or PM.

#### Scenario: Russian labels and descriptions
- **WHEN** the Russian string catalog is shown
- **THEN** the half controls read ДО and ПО and their content descriptions are «до полудня» and «после полудня»

#### Scenario: English labels avoid AM and PM
- **WHEN** the default English string catalog is read
- **THEN** the half labels are “Before noon” and “After noon” and the content descriptions do not contain AM or PM

### Requirement: Switching the half does not move stored minutes
Selecting the other half MUST NOT change stored task minutes. A sector placed at 9:00 stays on minute 540. Clock geometry stays 0 degrees at 12 o’clock, clockwise, with 1440 minutes per full turn.

#### Scenario: Nine o’clock stays put
- **WHEN** a sector starts at 9:00 and the afternoon face is selected
- **THEN** the stored start minute is still 540 and the morning clip still begins at 540
- **AND** `DialHalfTest.workBlock_isAmOnly` locks the 9:00 morning slice
