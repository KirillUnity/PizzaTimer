## MODIFIED Requirements

### Requirement: Dispositional form is asked once
The app MUST NOT show the 9-item short dispositional form after the introduction or at any other time. Finishing or skipping the introduction MUST open the day. The introduction pages themselves stay. The optional 9-item short state form on the completion sheet MUST stay available and skippable. `ShortFlowScaleTest.skippedForm_keepsState` still locks that skip.

#### Scenario: A finished form stays finished
- **WHEN** the user has already completed or skipped the dispositional form
- **THEN** onboarding does not ask for it again

#### Scenario: Intro opens the day
- **WHEN** the user finishes or skips the introduction
- **THEN** the next screen is the day face, not a dispositional questionnaire
