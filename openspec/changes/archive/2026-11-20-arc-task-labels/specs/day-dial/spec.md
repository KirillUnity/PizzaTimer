## ADDED Requirements

### Requirement: Sector titles follow the ring
On the clock-face ring, a task title MUST be drawn along the sector’s mid-radius arc, not as a straight chord. Glyphs on the lower half MUST stay upright. Thin slices below `minSweepForLabelDeg` stay unlabeled. `ArcLabelTest.labelArc_reversesOnLowerHalf` and `ArcLabelTest.prepareArcLabels_centersOnArc` lock this.

#### Scenario: Title sits on the arc
- **WHEN** a leftover or timed sector is wide enough for a label
- **THEN** the title is laid out on a circular path at mid-radius of that sector
