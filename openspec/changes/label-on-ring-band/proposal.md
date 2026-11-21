# Proposal: Keep sector titles on the ring

## Why
Task titles such as «Отдых» fall into the white hole. `drawArc` centers the stroke on the oval, so the painted band’s middle is `outerRadius`. Labels were placed on `midRadius`, which is the inner rim of that stroke. Glyphs then hang inward, off the dial. This serves mission §6: the day is read on the clock face, and a title that leaves the ring is not on the face.

## What Changes
- Dial titles follow the stroke centerline (`bandCenterRadius`), not `midRadius`.
- Arc length and the straight-label anchor use that same radius.
- Petal labels stay on `midRadius`. Pizza stays unlabeled.
- `ArcLabelTest.dialLabels_sitOnStrokeCenterline` locks the dial radius.

## Capabilities
- `day-dial`: a sector title sits on the painted ring.

## Impact
Dial labels only. Hit-testing, reflow, the hole (live chrono, play, break caption), and petal layout stay as they are.

## Preservation
Task list, flow session, and the rest of the day dial stay intact. Closed blocks stay immutable. No Room migration.
