# day-stats Specification

## Purpose
TBD - created by archiving change day-state-stats. Update Purpose after archive.

## Requirements

### Requirement: Day stats stay separate
The reports screen MUST show, for one day and not as a single number: the 1–10 state of each completed task, the hindrance count, the challenge count, the thought lines, the mean skill, the “what got easier” lines, and how many tasks were started. Each row MUST be labeled as a self-report. The text MUST stay on the device. `DayStateStatsTest.twoCompletions_keepTheirNumbers` locks the numbers.

#### Scenario: Two completions keep their numbers
- **WHEN** two completed tasks on the same day have different state, skill, and demand marks
- **THEN** the report lists both states, the matching hindrance and challenge counts, the mean skill, and the started-task count

### Requirement: Empty day is quiet
An empty day MUST show zero counts, a zero mean skill, and empty lines. It MUST NOT show a warning.

#### Scenario: Nothing was logged
- **WHEN** the day has no tasks and no completions
- **THEN** every count is zero and the text lines are empty

### Requirement: Open session hides the summary on the dial
While a work session is open, this summary MUST NOT appear on the dial. `DayStateStatsTest.summary_staysOffTheOpenDial` locks that.

#### Scenario: Session is open
- **WHEN** a task interval is still open
- **THEN** the dial does not show the day-stats summary

### Requirement: The nudge reads proxies
The difficulty offer MUST read the stored skill and engagement proxies. It MUST NOT rename those proxies to a flow score.

#### Scenario: Proxies stay proxies
- **WHEN** the offer is computed from saved completions
- **THEN** the day-stats rows still call the numbers a self-report

### Requirement: Form profile stays apart from proxies
Reports MUST show the short-form profile separately from the 1–10 means. The profile MUST NOT be added into the mean skill. `ShortFlowScaleTest.profile_isSeparateFromProxyMean` locks this.

#### Scenario: Two sources stay separate
- **WHEN** a completed task has both a skill proxy and a short-form profile
- **THEN** the report shows the skill mean and the form profile as different rows
