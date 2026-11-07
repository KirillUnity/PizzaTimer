# TimeDial Planner

Android day planner drawn as a **clock face**, not a list. One day is two 12-hour faces (AM / PM). Tasks are colored sectors; tap a sector for the description.

Current version: **1.0.0** (`v1.0-android`). Product spec: [docs/TZ.md](docs/TZ.md).

---

## What v1.0-android can do

- **DIAL**, **PIZZA**, **PETALS**, and **LIST** for the same day
- Untimed tasks fill leftover minutes (importance **5 : 3 : 2**)
- Projects, importance, move/duplicate, and daily or weekday recurrence
- **Start / Pause** in the center of the dial (new interval only; closed blocks stay)
- Text reports with a **local photo or file** copied into the app files directory
- Dial underlays (none, solid color, three presets). A user photo is a stub
- Settings stored in **DataStore**: default view, accent, completed color, background
- Short onboarding, **English and Russian** from the system locale, TalkBack labels

## Not in v1.0-android (v2)

- Kotlin Multiplatform, iOS, Compose Multiplatform
- Google Calendar / Apple Calendar
- Cloud sync, widgets, publishing the kit

---

## Local database

Room schema changes use `version++` and `fallbackToDestructiveMigration()` only. **There is no `Migration` class.** Installing a build whose schema version changed **wipes the local database** (tasks, blocks, reports). Uninstall or clear app storage. Attachment files under the app files directory are not a Room migration; clearing storage removes those too.

Preferences (view mode, colors, background, onboarding) live in DataStore and are not wiped by a Room schema bump.

---

## Build

Needs **JDK 17+** and an **Android SDK**.

```bat
gradlew.bat :app:assembleDebug
gradlew.bat :app:testDebugUnitTest
```

Debug APK: `app\build\outputs\apk\debug\app-debug.apk` (or `dist\TimeDial-debug.apk` via `build-apk.bat` when that script is present). minSdk 29. First launch can seed a sample day.

Docs: [docs/TZ.md](docs/TZ.md) · [docs/ROADMAP.md](docs/ROADMAP.md) · [docs/CHANGELOG.md](docs/CHANGELOG.md) · [docs/PLAY_CHECKLIST.md](docs/PLAY_CHECKLIST.md) · [AGENTS.md](AGENTS.md)

---

## License

Source is published as-is for this repository; no SPDX license file yet.
