## MODIFIED Requirements

### Requirement: Half labels say before noon and after noon
The day face SHALL label 00:00–12:00 as AM and 12:00–24:00 as PM in both the Russian and default English catalogs. Visible labels MUST be the abbreviations AM and PM. Russian TalkBack MUST still read «до полудня» and «после полудня». English content descriptions MUST read “Before noon” and “After noon”. On a clock-face view, the day-look menu button MUST sit at the leading edge of the day header row and the AM/PM control MUST sit at the trailing edge of that same row. The list view MAY omit the half switch. Switching the half MUST NOT change stored task minutes.

#### Scenario: Russian labels and descriptions
- **WHEN** the Russian string catalog is shown
- **THEN** the half controls read AM and PM and their content descriptions are «до полудня» and «после полудня»

#### Scenario: English labels avoid AM and PM
- **WHEN** the default English string catalog is read
- **THEN** the half labels are AM and PM and the content descriptions are “Before noon” and “After noon”
- **AND** `HalfLabelStringsTest.halfLabels_useAmPmAbbreviations` locks the four string names

#### Scenario: Header places the menu then halves
- **WHEN** the day is shown in a clock-face view
- **THEN** the day-look menu is at the leading edge of the header row and AM and PM are at the trailing edge
- **AND** `HalfLabelStringsTest.headerPlacesMenuThenHalves` locks that order
