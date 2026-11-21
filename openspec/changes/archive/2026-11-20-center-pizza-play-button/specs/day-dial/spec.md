## ADDED Requirements

### Requirement: Pizza hub control is centered
On pizza and petals, the Start (or Pause) control MUST sit at the geometric center of the figure. LIVE CHRONO MAY sit above that control. It MUST NOT shift the control off center. `OverlayHoleFractionTest.pizzaHub_isSmallerThanDialHole` locks the hub size used for those modes.

#### Scenario: Idle pizza centers Start
- **WHEN** the day is in pizza view with no running session
- **THEN** the Start control is at the center of the pie and the live-chrono labels sit above it
