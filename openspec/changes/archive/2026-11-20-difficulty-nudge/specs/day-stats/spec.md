# Spec Delta

## ADDED Requirements

### Requirement: The nudge reads proxies
The difficulty offer MUST read the stored skill and engagement proxies. It MUST NOT rename those proxies to a flow score.

#### Scenario: Proxies stay proxies
- **WHEN** the offer is computed from saved completions
- **THEN** the day-stats rows still call the numbers a self-report
