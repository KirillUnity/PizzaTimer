## MODIFIED Requirements

### Requirement: Completion sheet requires the three proxies
The completion sheet MUST collect state-after, engagement, and skill as integers from 1 to 10. It MUST NOT show a self-report or clinical-scale caption. It MUST NOT offer an “I am in flow” checkbox and MUST NOT use the words FSS-2. Submitting with any of those three missing MUST leave the sheet open and MUST NOT write a completion. `DayViewModelTest.completion_missingState_staysOpen` locks the refusal.

#### Scenario: Empty state is refused
- **WHEN** the user submits the sheet without a state-after value
- **THEN** the sheet stays open and no completion is stored

### Requirement: Short state form is optional
The completion sheet MUST keep state, engagement, and skill required. It MAY collect the 9-item short flow-state form. The caption for that form MUST be a prompt to rate on a 5-point scale. It MUST NOT mention diagnosis or paraphrased questionnaire items. Skipping that form MUST still save the 1–10 state. The sheet MUST NOT diagnose flow and MUST NOT say the user is in flow. `ShortFlowScaleTest.skippedForm_keepsState` locks the skip.

#### Scenario: Skip keeps the proxy
- **WHEN** the user saves a completion with a state proxy and skips the short form
- **THEN** the stored state proxy remains and the form profile is absent
