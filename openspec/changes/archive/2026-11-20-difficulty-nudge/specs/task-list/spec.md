# Spec Delta

## ADDED Requirements

### Requirement: A habitual task can be nudged once
When the last three completions of tasks with the same title each have skill at least 8 and engagement at most 4, the system MUST offer once to harden the next similar task by one of: already a step, an explicit requirement, or a narrower result. Two completions MUST NOT offer it. Skipping MUST NOT ask about those same three again. Agreeing MUST NOT change closed intervals or the saved scores. `DifficultyNudgeTest.threshold_and_skip` locks this.

#### Scenario: Two completions stay quiet
- **WHEN** only two similar completions meet the skill and engagement threshold
- **THEN** no offer is shown

#### Scenario: Skip hides the same three
- **WHEN** the user skips the offer for three qualifying completions
- **THEN** those same three do not raise the offer again

#### Scenario: Agree leaves intervals and scores
- **WHEN** the user agrees to one hardening
- **THEN** closed intervals and the 1–10 scores stay as they were
