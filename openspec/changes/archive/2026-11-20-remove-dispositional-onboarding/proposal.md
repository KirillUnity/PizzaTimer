# Proposal

## Why

After the introduction, the app shows a full-screen 9-item short dispositional form titled «Перед началом». The user asked to remove that screen. Mission §8.1 still allows a validated form beside the 1–10 proxies on the completion sheet. The extra gate before the day raises the threshold to start (principle 1).

## What Changes

- After the introduction (or when it is already seen), the next surface is the day, not the dispositional form.
- `DispositionScreen` and the DataStore flag that gated it are removed.
- The optional 9-item short **state** form on the completion sheet stays. The 1–10 proxies stay. The introduction pages stay.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `flow-session`: the dispositional form is no longer asked after onboarding.

## Impact

- `MainActivity` routing, Koin, `ShortFlowScale.shouldAskDisposition`, `ui.onboarding.Disposition*`, `DispositionStore` and its DataStore implementation, `flow_dfs_*` strings, `ShortFlowScaleTest.dispositionalForm_isNotAskedAgain`.
