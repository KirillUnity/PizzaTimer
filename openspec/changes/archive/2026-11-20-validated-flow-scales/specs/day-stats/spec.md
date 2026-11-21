# Spec Delta

## ADDED Requirements

### Requirement: Form profile stays apart from proxies
Reports MUST show the short-form profile separately from the 1–10 means. The profile MUST NOT be added into the mean skill. `ShortFlowScaleTest.profile_isSeparateFromProxyMean` locks this.

#### Scenario: Two sources stay separate
- **WHEN** a completed task has both a skill proxy and a short-form profile
- **THEN** the report shows the skill mean and the form profile as different rows
