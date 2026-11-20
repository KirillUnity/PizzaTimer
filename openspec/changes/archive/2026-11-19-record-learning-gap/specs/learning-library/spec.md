# Spec Delta

## Purpose

Record that the tutorial course from the mission is not part of the current build, so it cannot be treated as already shipped.

## ADDED Requirements

### Requirement: No tutorial library in this build
The system MUST NOT offer a tutorial course, repeatable lesson passes, user-authored tutorials, publishing, download, or deletion of tutorials.

#### Scenario: Primary navigation has no practice library
- **WHEN** the user moves between the top-level destinations
- **THEN** the destinations are the day, the task list, reports, and settings, and none of them is a tutorial library

### Requirement: Tutorial course arrives only through a later change
A tutorial library MUST be introduced by a new change that updates this capability. Design mockups are not that library.

#### Scenario: Mockups are not a destination
- **WHEN** a design frame shows a practice hub
- **THEN** the running app still has no tutorial library until a change archives an update to this spec
