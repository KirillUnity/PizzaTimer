# Spec Delta

## MODIFIED Requirements

### Requirement: Tutorial course arrives only through a later change
A tutorial library (mission §7: repeatable lesson passes, user-authored tutorials, publishing, and download) MUST be introduced by a later change that updates this capability. The instrument practice hub is not that library. Design mockups of the tutorial course are not that library.

#### Scenario: Practice hub is not the course
- **WHEN** the user opens Practice from Settings
- **THEN** the screen records which instrument skills were tried on a real day and does not offer authoring, download, publishing, or a repeatable lesson pass

#### Scenario: Course mockups are not a destination
- **WHEN** a design frame shows the mission §7 tutorial course
- **THEN** the running app still has no tutorial library
