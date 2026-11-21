# Proposal: Name field on create-today

## Why
The chooser has no title field. Create-for-today either feels broken or saves an unnamed task. The user asked for a name field on that first screen.

## What Changes
- The chooser shows a title field.
- Create-for-today saves that title, or the default name if it is still blank.
- Configure still starts at the name stage with the same title.

## Capabilities
- `task-list`: create chooser includes a name field.

## Impact
`CreateChooser` and `createTodayQuick_*` tests.
