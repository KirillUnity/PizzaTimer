# Proposal

## Why

Mission step в and principles 9 and 10 (section 9) ask for a short course the user can repeat, not a dashboard and not a feed. The learning-library spec still says this build has no course. Three built-in practices from section 7.2 belong on the device, with a history that appends.

## What Changes

- Heartfulness (3–5 min), yoga pause (2–3 min), and micro-qigong (1–2 min) are a course: steps, a checkpoint, a “trial, not treatment” note, and the research named in mission §7.2.
- A second pass appends a history row. It does not replace the first. History stays on the device.
- The day offers the course during a break, or before a task the user tagged as needing concentration. An open work interval hides the offer.
- The course opens from Settings. Top-level navigation stays four destinations.
- No user-authored lessons, publishing, download, feed, likes, or ratings.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `learning-library`: this build includes the three built-in practices and an append-only history. User-authored lessons and exchange stay out.

## Impact

- `BuiltInCourse` and a DataStore history. No Room schema change.
- A Settings screen and a day offer card. `BuiltInCourseTest`.
