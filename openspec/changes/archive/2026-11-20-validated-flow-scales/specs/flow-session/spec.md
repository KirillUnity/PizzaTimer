# Spec Delta

## ADDED Requirements

### Requirement: Short state form is optional
The completion sheet MUST keep state, engagement, and skill required. It MAY collect the 9-item short flow-state form. Skipping that form MUST still save the 1–10 state. The sheet MUST NOT diagnose flow and MUST NOT say the user is in flow. `ShortFlowScaleTest.skippedForm_keepsState` locks the skip.

#### Scenario: Skip keeps the proxy
- **WHEN** the user saves a completion with a state proxy and skips the short form
- **THEN** the stored state proxy remains and the form profile is absent

### Requirement: Dispositional form is asked once
Onboarding MUST offer the 9-item short dispositional form once. After the user submits it or skips it, the form MUST NOT be offered again. `ShortFlowScaleTest.dispositionalForm_isNotAskedAgain` locks this.

#### Scenario: A finished form stays finished
- **WHEN** the user has already completed or skipped the dispositional form
- **THEN** onboarding does not ask for it again
