## MODIFIED Requirements

### Requirement: An open work interval hides the offer
The day MUST offer the course from the day side menu during a break, or before a task the user tagged as needing concentration. It MUST NOT show a short-practice control on the day face above the dial. It MUST NOT offer the course while a work interval is open. `BuiltInCourseTest.openWorkInterval_hidesOffer` locks the gate.

#### Scenario: Break offers the course
- **WHEN** a break is open and no work interval is open
- **THEN** the day offers the course

#### Scenario: Open work hides the course
- **WHEN** a work interval is open
- **THEN** the day does not offer the course

#### Scenario: Face has no practice button
- **WHEN** a break is open
- **THEN** Finish and Continue stay on the day face and the short-practice control is only in the side menu
