# Tasks

## 1. Copy and header

- [x] 1.1 Set `dial_am` and `dial_pm` to AM and PM in `values/strings.xml` and `values-ru/strings.xml`. Keep `cd_half_am` / `cd_half_pm` as “Before noon” / “After noon” and «до полудня» / «после полудня». Verify `HalfLabelStringsTest` matches.
- [x] 1.2 Put `AmPmRow` at the leading edge of the day header `Row` when `canvasMode` is true, and the day-look menu `IconButton` at the trailing edge. Stop drawing half chips in `DayChromeRow`. Verify the list view still omits the half switch.
- [x] 1.3 Update `AmPmRow` KDoc and `HalfLabelStringsTest` so they lock AM/PM visible copy. Run `gradlew.bat :app:testDebugUnitTest` and keep it green.

## Workflow follow-up

- Archive with `openspec archive am-pm-left-menu-right --yes` only after `:app:testDebugUnitTest` passes.
- Do not archive `icon-only-timer-buttons`.
