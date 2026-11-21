# Proposal

## Why

During a break the day face shows «Короткая практика» above the dial, next to Finish and Continue. That crowds the clock. Mission principle 7: quiet chrome. The day-look panel already holds view and appearance; the short-practice offer belongs there.

## What Changes

- The day face no longer shows a short-practice button.
- When `BuiltInCourse.offer` is true, the day side menu shows «Короткая практика» and opens the same course screen.
- An open work interval still hides the offer. Settings «Курс» is unchanged.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `learning-library`: the course offer lives in the day side menu, not on the face.

## Impact

- `DayScreen.kt`, `DaySideMenu.kt`.
