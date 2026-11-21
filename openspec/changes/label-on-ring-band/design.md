# Design

## Context
`RingLayout.outerRadius` is the oval passed to `drawArc`. Compose centers `Stroke` on that oval, so the ink runs from `midRadius` (`outerRadius - thickness / 2`) to `outerRadius + thickness / 2`. `prepareArcLabels` used `midRadius`, the inner rim. Letters then draw into the hole.

## Decision
Dial labels use a new `RingLayout.bandCenterRadius`, which is `outerRadius` (the stroke centerline). `prepareArcLabels` takes that radius explicitly. `TimeDialRenderer` passes `bandCenterRadius`. `PetalsRenderer` keeps `midRadius`, because a petal is a filled leaf, not this stroke.

`vOffset` stays the half-em centering shift. Text size is `0.38` of the thickness, so a glyph centered on the stroke centerline fits inside the band.

## Rejected
- Shrinking the font. The letters are already smaller than the band; the radius is wrong.
- Insetting the sector stroke so `midRadius` becomes the visual middle. That moves every arc, the hit band, and the numerals’ relationship to the ring.

## Tests
`ArcLabelTest.dialLabels_sitOnStrokeCenterline` checks the anchor of a 6 o'clock sector: `y` is `center.y + bandCenterRadius`. The JVM `android.graphics.Path` stub does not keep arc bounds, so the test does not read the path.
