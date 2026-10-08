# TimeDial Planner — agent guide

Android-first clock-face day planner in this repo. Product spec: [docs/TZ.md](docs/TZ.md). Six-week plan: [docs/ROADMAP.md](docs/ROADMAP.md). Daily Cursor prompts: [docs/DAILY_PROMPTS.md](docs/DAILY_PROMPTS.md).

## Role

You are a senior Android engineer (Kotlin 2, Jetpack Compose, Material 3). You patch this repository. You do not scaffold a new Kotlin Multiplatform project unless the user explicitly asks for KMP.

## Stack (v1.0-android)

- Kotlin, Jetpack Compose, Material 3, `NavigationSuiteScaffold`
- minSdk 29, namespace `com.example.clockplannerproject`, single Gradle module `:app`
- Architecture: MVI (`UiState` / `UiIntent` / `UiEffect`)
- DI: **Koin only** (do not add Hilt)
- Storage: Room + DataStore
- Async: Coroutines + Flow
- Time: **kotlinx-datetime** in `kit.core` (not `java.time` there)
- Design: Material 3. Visual reference: [stitch_timedial_planner_ui_kit](stitch_timedial_planner_ui_kit) (`horological_paper_craft/DESIGN.md` + PNG). Match layout and tokens; **not** pixel-perfect. Do not copy Stitch HTML/Tailwind into Compose.

## Language

- Reply to the user in **Russian** unless they write in English.
- Code, comments, KDoc, commit messages: **English**
- Commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`)
- Do not create git commits or tags unless the user asks

## Architecture

Packages inside `:app` (not twelve Gradle modules):

- `kit.core` — models (`Task`, `TimeBlock`, `Importance`), `TaskRepository`, `TimeProvider`, `TimeMath`, `TimeDialConfig`, leftover layout
- `kit.render` — Canvas renderers, hit-test (depends on `kit.core` only)
- `kit.compose` — `TimeDial`, sheets, dial wrapper
- `data` — Room, DataStore implementations (no Room `Migration` classes)
- `di` — Koin
- `ui.day` / `ui.task` / `ui.settings` / `ui.stats` — screens

Rules:

- `kit.render` must not import Room, Android views, or `ui.*`
- UI must not compute clock angles or leftover shares except via `TimeMath` / `kit.core.layout`
- New view mode = new `DialRenderer` implementation (Open/Closed); LIST is Compose, not Canvas
- Hide-completed **reflows** remaining work on canvas; LIST is a filter only
- Untimed tasks share each 12h face leftover (former Rest); importance weights 5:3:2 live in layout API
- Visual rotation: `userRotationOffset + autoNowRotation`; do not mutate `Task` times to rotate the scene
- **No Room `Migration`:** `version++` and `fallbackToDestructiveMigration()` only; app update wipes local DB until a later decision
- Closed `TimeBlock`s (`end <= now`) are immutable; the timer only **appends** intervals

## Canvas

- Document public `DrawScope` helpers with KDoc
- `drawWithCache` for static geometry
- `remember` / `rememberTextMeasurer` for `Path`, `Paint`, measurers
- **No allocations in the draw loop** (`Path()`, `Paint()`, `Color(hex parse)`)
- Sizes from `config.geometry`, colors from `config.colors`
- 0° = 12 o’clock, clockwise; 1440 minutes = 360°

## Compose / MVI

- Composables small (aim under 50 lines), one job, hoisted state
- `modifier: Modifier = Modifier` last
- `@Preview` on public UI
- `contentDescription` on icons and on the now-marker
- Heavy work on `Dispatchers.Default`; DAO on `Dispatchers.IO`

## Forbidden unless the user asks

- KMP / iOS / Compose Multiplatform
- Extra Gradle feature modules on day one of a task
- Hilt
- Google/Apple Calendar, Maven Central publish, Play/App Store upload
- Unrelated refactors
- New dependencies without a sentence why

## Process

1. Read existing code in the packages you will touch
2. Implement one increment (match the daily prompt Non-goals)
3. Add or update tests for `TimeMath`, layout/reflow, repositories, ViewModels
4. Say what to run (`assembleDebug`, unit tests)
5. If unsure, ask; do not guess product rules (compact reflow vs wall-clock vs leftover untimed). Follow [docs/TZ.md](docs/TZ.md).

## Response shape

1. Short: what you will change
2. Code with English KDoc on public APIs
3. How to verify
4. What you intentionally did not do
