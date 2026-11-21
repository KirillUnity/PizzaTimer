## MODIFIED Requirements

### Requirement: No tutorial library in this build
The system MUST offer exactly three built-in practices as a course: Heartfulness (3–5 min), yoga pause (2–3 min), and micro-qigong (1–2 min). Each MUST have steps with a checkpoint and the research named in mission §7.2. The course list MUST NOT show a trial-not-treatment line. The list MUST offer two buttons: add a personal course, and refresh the list. A personal course is a local title only. The system MUST NOT offer publishing, a feed, likes, or ratings. Top-level navigation MUST stay the day, the task list, reports, and settings. `BuiltInCourseTest.catalog_isThreePractices_andNavigationStaysFour` locks the built-in catalog and the tab count. `CustomCourseStoreTest.add_thenRefresh_keepsTitle` locks local add.

#### Scenario: Primary navigation has no practice library
- **WHEN** the user moves between the top-level destinations
- **THEN** the destinations are the day, the task list, reports, and settings, and none of them is a tutorial library

#### Scenario: Each practice names its research
- **WHEN** the user opens one of the three practices
- **THEN** the screen shows steps, a checkpoint, and the §7.2 citation

### Requirement: Tutorial course arrives only through a later change
Publishing, a social feed, and remote download MUST stay out of this build. The instrument practice hub MUST remain the skill list, not this course. The built-in course is the three practices above. A personal course MAY be added as a local title. Refresh MUST reload the on-device list.

#### Scenario: Practice hub is not the course
- **WHEN** the user opens Practice from Settings
- **THEN** the screen records which instrument skills were tried on a real day and does not offer authoring, download, publishing, or a repeatable lesson pass

#### Scenario: Course mockups are not a destination
- **WHEN** a design frame shows a social tutorial library
- **THEN** the running app still has no publishing or feed
