# Spec Delta

## MODIFIED Requirements

### Requirement: No tutorial library in this build
The system MUST offer exactly three built-in practices as a course: Heartfulness (3–5 min), yoga pause (2–3 min), and micro-qigong (1–2 min). Each MUST have steps with a checkpoint, a note that it is a trial and not treatment, and the research named in mission §7.2. The system MUST NOT offer user-authored lessons, publishing, download, a feed, likes, or ratings. Top-level navigation MUST stay the day, the task list, reports, and settings. `BuiltInCourseTest.catalog_isThreePractices_andNavigationStaysFour` locks the catalog and the tab count.

#### Scenario: Primary navigation has no practice library
- **WHEN** the user moves between the top-level destinations
- **THEN** the destinations are the day, the task list, reports, and settings, and none of them is a tutorial library

#### Scenario: Each practice names its research
- **WHEN** the user opens one of the three practices
- **THEN** the screen shows steps, a checkpoint, a trial-not-treatment note, and the §7.2 citation

### Requirement: Tutorial course arrives only through a later change
User-authored tutorials, publishing, and download MUST stay out of this build. The instrument practice hub MUST remain the skill list, not this course. The built-in course is the three practices above.

#### Scenario: Practice hub is not the course
- **WHEN** the user opens Practice from Settings
- **THEN** the screen records which instrument skills were tried on a real day and does not offer authoring, download, publishing, or a repeatable lesson pass

#### Scenario: Course mockups are not a destination
- **WHEN** a design frame shows a social tutorial library
- **THEN** the running app still has no authoring, download, or feed

## ADDED Requirements

### Requirement: A repeat appends history
Finishing a practice MUST append one history row on the device. A second finish of the same practice MUST keep the earlier row. `BuiltInCourseTest.secondPass_appends` locks this.

#### Scenario: Two passes of one practice
- **WHEN** the user finishes Heartfulness twice
- **THEN** the history has two Heartfulness rows

### Requirement: An open work interval hides the offer
The day MUST offer the course during a break, or before a task the user tagged as needing concentration. It MUST NOT offer the course while a work interval is open. `BuiltInCourseTest.openWorkInterval_hidesOffer` locks this.

#### Scenario: Break offers the course
- **WHEN** a break is open and no work interval is open
- **THEN** the day offers the course

#### Scenario: Open work hides the course
- **WHEN** a work interval is open
- **THEN** the day does not offer the course
