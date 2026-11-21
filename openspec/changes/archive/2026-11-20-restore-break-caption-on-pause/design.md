## Context
`RunningSessionHole` gates the break caption on `holeDiameter >= 160.dp`. Phone holes are smaller, so Pause only swaps the icon to Play.

## Decision
Show `break_title` whenever the work task is paused, independent of hole size. Keep the task title and «этот заход» behind the large-hole gate. `TimerHoleOverlay` shows the same caption when `sessionPaused` is true so pizza/petals match.

## Risks
A small hole with minutes-left plus «Перерыв» plus Play may wrap. Use one caption line, no extra chrome.
