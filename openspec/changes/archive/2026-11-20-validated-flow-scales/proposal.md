# Proposal

## Why

Mission §8.1 and principle 3 ask for a validated self-report beside the 1–10 proxies. The proxies stay. A short flow form and a one-time dispositional form calibrate them without calling the result a flow score or a diagnosis.

## What Changes

- The completion sheet can include the 9-item Short Flow State Scale-2. Skipping it still saves the 1–10 proxies.
- Reports show that profile apart from the 1–10 means.
- Onboarding asks the 9-item Short Dispositional Flow Scale-2 once. After it is answered or skipped, it is not asked again.
- Item sentences are dimension paraphrases. The copyrighted questionnaire text is not copied. KDoc cites Jackson, Martin, and Eklund (2008).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `flow-session`: the short state form is optional; proxies stay required.
- `day-stats`: the form profile is a separate row from the 1–10 means.

## Impact

- `ShortFlowScale` and a DataStore flag for the dispositional form. No Room schema change.
- Completion sheet, reports card, onboarding step. `ShortFlowScaleTest`.
