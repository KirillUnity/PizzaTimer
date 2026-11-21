# Proposal: Stop seeding the sample day

## Why
Debug launches insert Sleep, Deep work, Lunch, and Gym when the store is empty. Those tasks look like a real day and come back after a wipe of today. The user asked to remove them.

## What Changes
- `SampleDaySeeder` MUST NOT insert `SampleTasks.typicalDay`.
- On day load it MUST delete those fixture ids if they are still stored from an earlier install.
- Previews MAY keep `SampleTasks`. User tasks with other ids stay.

## Capabilities
- `visible-day`: no sample day on start; leftover sample ids are removed.

## Impact
Empty first launch. Existing debug databases lose only sleep/work/lunch/gym fixture rows.
