# Play checklist — TimeDial 1.0.0 (v1.0-android)

Not a store upload. Use this before anyone publishes.

## Identity

- [ ] Application id `com.example.clockplannerproject` is still the debug id. Replace it before a real listing
- [ ] `versionName` 1.0.0, `versionCode` 6
- [ ] Launcher icon and splash use the paper / terracotta / ink palette
- [ ] No Stitch HTML copied into the app

## Data

- [ ] README states that a Room schema bump **wipes the local database** (`fallbackToDestructiveMigration()`, no `Migration` class)
- [ ] Report attachments are files under the app files directory, not a cloud bucket
- [ ] DataStore holds appearance and the onboarding flag. Old or unknown keys must not crash
- [ ] Backup rules: decide whether `filesDir/report-media` and DataStore should be in device backup before production

## Product

- [ ] DIAL and PIZZA show the selected background; labels stay readable
- [ ] Start / Pause, view-mode switch, reports, and attachment actions have `contentDescription` and are buttons, not gesture-only
- [ ] Dial TalkBack custom actions still open a sector
- [ ] System language English and Russian switch the UI (no in-app language picker)
- [ ] Onboarding Skip is persisted; Settings can show the intro again
- [ ] Empty day still offers Add task

## Policy and v2

- [ ] Privacy: notes, tasks, and attachments stay on device. Say that in the listing
- [ ] Permissions: no broad storage permission. Photo and file pickers only
- [ ] Do not ship KMP, iOS, or Google/Apple Calendar in this listing
- [ ] `assembleDebug` and `testDebugUnitTest` are green on the commit you would upload
- [ ] Do not tag `v1.0-android` unless the maintainer asks

## TalkBack pass (key screens)

- [ ] Onboarding: Skip and Next/Done are named
- [ ] Day: view mode, AM/PM, Hide done, Start/Pause, now marker, sector custom actions
- [ ] Tasks: create, delete, importance, project chip, report attach/open/remove
- [ ] Reports: note card, attach photo, attach file, open, remove
- [ ] Settings: view, accent, completed color, background, show introduction
