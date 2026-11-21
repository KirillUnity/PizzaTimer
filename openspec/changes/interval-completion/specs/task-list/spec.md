# Spec Delta

## MODIFIED Requirements

### Requirement: Completion lives on the interval
A saved completion MUST attach to one interval of the task and MUST NOT be stored inside the interval’s minutes. A second interval of the same task MUST store a separate completion. Saving MUST leave the task status unchanged and MUST leave already-ended intervals unchanged.

#### Scenario: Two intervals keep separate scores
- **WHEN** the user scores one interval and then scores another interval of the same task
- **THEN** both scores are stored, and the task status stays as it was

#### Scenario: Closed intervals stay put
- **WHEN** the user saves a completion
- **THEN** every already-ended interval on that task is unchanged
