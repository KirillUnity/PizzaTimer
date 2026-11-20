# learning-library Specification

## Purpose
Record that the tutorial course from the mission is not part of the current build, so it cannot be treated as already shipped.

## Requirements

### Requirement: No tutorial library in this build
The system MUST NOT offer a tutorial course, repeatable lesson passes, user-authored tutorials, publishing, download, or deletion of tutorials.

#### Scenario: Primary navigation has no practice library
- **WHEN** the user moves between the top-level destinations
- **THEN** the destinations are the day, the task list, reports, and settings, and none of them is a tutorial library

### Requirement: Tutorial course arrives only through a later change
A tutorial library (mission §7: repeatable lesson passes, user-authored tutorials, publishing, and download) MUST be introduced by a later change that updates this capability. The instrument practice hub is not that library. Design mockups of the tutorial course are not that library.

#### Scenario: Practice hub is not the course
- **WHEN** the user opens Practice from Settings
- **THEN** the screen records which instrument skills were tried on a real day and does not offer authoring, download, publishing, or a repeatable lesson pass

#### Scenario: Course mockups are not a destination
- **WHEN** a design frame shows the mission §7 tutorial course
- **THEN** the running app still has no tutorial library
