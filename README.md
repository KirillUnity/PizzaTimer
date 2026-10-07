# PizzaTimer (TimeDial)

Android day planner drawn as a **clock face**, not a list. One day is two 12-hour faces (AM / PM). Tasks are colored sectors; tap a sector for the full card.

GitHub: [KirillUnity/PizzaTimer](https://github.com/KirillUnity/PizzaTimer)

Current version: **v0.3.0**

---

## Description

PizzaTimer (internal name **TimeDial**) lets you plan a day on a dial:

- **DIAL** — ring of sectors  
- **PIZZA** — pie slices from the center  
- **PETALS** — petal layout  
- **LIST** — Compose list (not Canvas)

A task can have **no clock time**, **one interval**, or **several `TimeBlock`s** (same task, several sectors). Untimed work fills leftover minutes on each 12-hour face by importance weights **HIGH : MEDIUM : LOW = 5 : 3 : 2**. Completed tasks use a gray color. Hide-completed **reflows** remaining work on the dial (list mode only filters).

Local storage is Room + DataStore. Schema bumps use `fallbackToDestructiveMigration()` only — **updating a debug build wipes local tasks** until a production migration policy exists.

Not in v0.3: center Start/Pause timer, recurrence, reports, calendar sync, iOS.

---

## Stack

- Kotlin, Jetpack Compose, Material 3  
- minSdk 29, namespace `com.example.clockplannerproject`  
- MVI (`UiState` / `UiIntent` / `UiEffect`), **Koin**  
- Room, DataStore, Coroutines / Flow, kotlinx-datetime  

Docs: [docs/TZ.md](docs/TZ.md) · [docs/ROADMAP.md](docs/ROADMAP.md) · [docs/CHANGELOG.md](docs/CHANGELOG.md) · [AGENTS.md](AGENTS.md)

---

## Build

Needs **JDK 17+** and an **Android SDK**.

```bat
git clone https://github.com/KirillUnity/PizzaTimer.git
cd PizzaTimer
gradlew.bat :app:assembleDebug
gradlew.bat :app:testDebugUnitTest
```

Or:

```bat
build-apk.bat
```

APK: `dist\TimeDial-debug.apk`. Install on an emulator or device (API 29+). First debug launch seeds sample tasks for today.

---

## License

Source is published as-is for this repository; no SPDX license file yet.
