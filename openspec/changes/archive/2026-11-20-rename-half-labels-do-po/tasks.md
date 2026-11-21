# Tasks

## 1. Half labels

- [x] 1.1 Set `dial_am`, `dial_pm`, `cd_half_am`, and `cd_half_pm` in `values/strings.xml` to “Before noon” and “After noon”, and in `values-ru/strings.xml` to ДО, ПО, «до полудня», and «после полудня». Update `AmPmRow` KDoc and let the English label wrap to two lines. Verify the four names no longer contain AM or PM.

## 2. Tests

- [x] 2.1 Add a unit test that reads both string catalogs and fails if those four content descriptions or labels contain AM or PM, and assert a 9:00 sector keeps minute 540 when the other half is selected. Verify with `gradlew.bat :app:testDebugUnitTest` before archive.

## Workflow follow-up

- Archive with `openspec archive rename-half-labels-do-po --yes` only after `:app:testDebugUnitTest` passes and the boxes above are checked.
