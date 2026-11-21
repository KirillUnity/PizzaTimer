# Proposal

## Why

The course list still leads with «Это проба, не лечение». The user wants that line gone and two buttons: add a personal course, and refresh the list.

## What Changes

- Drop the trial-not-treatment line from the course list.
- Show filled buttons «Добавить свой курс» and «Обновить список курсов».
- Add stores a local title (no publish, feed, or ratings). Refresh reloads the on-device list (built-in plus custom).
- The three built-in practices stay. Tabs stay four.

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `learning-library`: list chrome; local add and refresh; no trial line on the list.

## Impact

- `CourseScreen`, `CourseViewModel`, a small custom-course store, strings, `CustomCourseStoreTest`.
