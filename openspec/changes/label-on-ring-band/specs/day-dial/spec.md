## MODIFIED Requirements

### Requirement: Sector titles follow the ring
On the clock-face ring, a task title MUST be drawn along the sector arc at the stroke centerline (`RingLayout.bandCenterRadius`), not on the inner rim and not as a straight chord. Glyphs MUST stay on the painted band. Glyphs MUST NOT stack at a single point. Glyphs on the lower half MUST stay upright. Thin slices below `minSweepForLabelDeg` stay unlabeled. `ArcLabelTest.labelArc_reversesOnLowerHalf`, `ArcLabelTest.prepareArcLabels_centersOnArc`, and `ArcLabelTest.dialLabels_sitOnStrokeCenterline` lock this.

#### Scenario: Title sits on the arc
- **WHEN** a leftover or timed sector is wide enough for a label
- **THEN** the title is laid out on a circular path at the center of the painted ring
- **AND** letters follow the arc in reading order instead of overlapping
- **AND** the letters stay on the ring, not in the hole
