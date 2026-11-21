# Design

## Decisions

1. The catalog is a fixed enum: Heartfulness, yoga pause, micro-qigong. Each lesson lists step ids, a minute range, a trial note, and a citation taken from mission §7.2. Yoga has no single author in that section, so the citation names the effects the mission lists (N200, MoCA/MMSE, HRV) and the years 2023–2024, without inventing a paper title.
2. A pass is a row `(practice, epoch millis)`. `CourseHistory.append` always adds. DataStore stores one line per pass. Nothing uploads.
3. The concentration mark is the existing task tag `concentration`. The user adds it. The offer is visible when no work interval is open and either a break is open or the selected task has that tag. `BuiltInCourse.offer` returns false while a work interval is open.
4. The course screen lives under Settings, beside the instrument practice hub. `AppDestinations` stays four values. The day shows a card, not a new tab and not a layer on top of an open work interval.
5. Finish is enabled only after every step checkpoint is checked. Finish appends one row.

## Risks

- The instrument practice hub still tracks dial skills. It is not this course. The two screens stay separate so the old hub requirement remains true.
