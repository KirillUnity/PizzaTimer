# Proposal: Fix stacked arc titles

## Why
On the dial, titles such as «Отдых» and «Deep» draw every letter on top of the same point. `Canvas.drawTextOnPath` treats `Paint.Align.CENTER` as centering each glyph at `hOffset`, not the whole string.

## What Changes
- Path labels use left alignment and `hOffset = (arcLength - textWidth) / 2`.
- Straight pizza helper labels may keep `Align.CENTER`.
- `ArcLabelTest.prepareArcLabels_centersOnArc` asserts the new offset.

## Capabilities
- `day-dial`: sector titles stay readable along the arc.

## Impact
DIAL and PETALS labels. Pizza wedges stay unlabeled.
