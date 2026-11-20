# Proposal

## Why

A pause note, its mood, and the day-close card live only in `DayViewModel`. Process death, a new launch, or leaving the day drops them, so the tester cannot see where they stopped. Mission §5.4 (track immersion) and §6 (flow as a session, not a lost frame) need that note to belong to the task and that day-close to belong to the local date.

## What Changes

- After Commit, store the trimmed non-blank note and the selected mood (Calm, Deep, Stuck) in DataStore, keyed by task id. A blank note does not replace a saved note or its mood.
- Skip closes the slip and does not write the store.
- The resume chip reads that record when the sector is selected and the task has no open interval. The chip shows the same mood words as the slip. Start deletes that task's record and appends a new interval. Closed blocks stay unchanged.
- Changing the visible day hides the slip and the day-close card and does not delete task notes.
- Day close is remembered for the local date the user closed. Entering that date shows the card until it is dismissed. The next local date does not show it. Dismissal lasts until the user closes that date again.
- The card does not block Start, edit, or new tasks, and it does not create a `TaskReport`. "To reports" still only opens the reports tab. Zero closed minutes stay the calm "no closed intervals" line.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `flow-session`: Persist the stop note and mood per task, show them on the resume chip after a new ViewModel, and remember day-close visibility per local date without locking the day or writing a report.

## Impact

- `DayViewModel`, day-flow chip and day-close card, a new DataStore store and Koin binding. No Room schema bump, no Migration, no new Gradle module. `kit.render` stays free of DataStore. Shipped pause/start interval rules, leftover layout, and the reports tab stay intact.
