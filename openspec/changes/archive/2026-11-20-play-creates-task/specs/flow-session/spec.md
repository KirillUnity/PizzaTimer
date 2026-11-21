# Spec Delta

## ADDED Requirements

### Requirement: Untitled Play is a normal open session
An interval opened by Play without a selection MUST follow the same append-only session rules as a start on an existing task. Already ended intervals MUST stay unchanged. The completion sheet MUST NOT open in this step.

#### Scenario: Other closed intervals stay put
- **WHEN** Play creates a new task while other tasks already have ended intervals
- **THEN** those ended intervals are unchanged and the new task has one open interval
