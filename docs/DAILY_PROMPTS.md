# TimeDial Planner — daily technical prompts

Copy one prompt into Cursor Agent per day. Do not merge two days unless the previous day’s DoD is already in the repo.

- [How to use / Как пользоваться](#how-to-use--как-пользоваться)
- [Shared template / Общий шаблон](#shared-template--общий-шаблон)
- [Week 1 — v0.1](#week-1--v01)
- [Week 2 — v0.2](#week-2--v02)
- [Week 3 — v0.3](#week-3--v03)
- [Week 4 — v0.4](#week-4--v04)
- [Week 5 — v0.5](#week-5--v05)
- [Week 6 — v1.0-android](#week-6--v10-android)

Product spec: [TZ.md](TZ.md). Roadmap: [ROADMAP.md](ROADMAP.md). Agent rules: [AGENTS.md](../AGENTS.md).

---

## How to use / Как пользоваться

**RU.** Один день = один чат (или явно напишите «продолжи день N»). Вставляйте блок **Prompt (RU)** целиком. После ответа проверьте Acceptance. Пояснение **Why this prompt** не копируйте в агент — оно для вас.

**EN.** One day = one chat. Paste **Prompt (EN)** in full. Do not paste the Why section into the agent.

---

## Shared template / Общий шаблон

Каждый промпт ниже следует одной схеме:

| Block | Why it works for an implementation agent |
|-------|------------------------------------------|
| **Role** | Cuts generic “write an app” answers. Forces Compose Canvas / Room / MVI, not Flutter or XML Views. |
| **Context** | Names the real package `com.example.clockplannerproject` and broken files, so the agent edits this repo instead of scaffolding a new KMP project from the Word TZ. |
| **Goal / Non-goals** | One increment. Explicit bans (KMP, Hilt, calendar, extra Gradle modules) stop the TZ from leaking into day 1. |
| **Constraints** | Encodes architecture: `TimeMath` once, Strategy renderers, 0° = 12 o’clock, gray DONE, Koin only. |
| **Acceptance** | Testable DoD. Agents ship more complete work when success is a checklist, not a vibe. |
| **Output** | File list + tests. Reduces drive-by refactors of unrelated packages. |

Per-day **Why this prompt** adds the *task-specific* reason (Canvas vs leftover vs destructive Room). The shared table is the *format* reason. From week 3, follow [TZ.md](TZ.md): no Room `Migration` class.

---

## Week 1 — v0.1

### Day 1 — Compile + domain + navigation

#### Prompt (RU)

```
Роль: старший Android-разработчик Jetpack Compose (Kotlin 2, Material 3). Ты правишь существующий репозиторий ClockPlanner, не создаёшь новый проект.

Контекст:
- namespace com.example.clockplannerproject, minSdk 29, один модуль :app.
- Сейчас НЕ компилируется: data/ClockSegmentData.kt (Dioganal, пустой ClockData, сломанный ClockSegment), ui/clock/DrawClock.kt и DrawClockSegment.kt (нет импортов Compose, неверный when).
- MainActivity.kt: NavigationSuiteScaffold с Home/Favorites/Profile и Greeting.

Цель дня:
1. Починить сборку debug.
2. Заменить черновые модели на домен kit.core: Task, TaskStatus (TODO/IN_PROGRESS/DONE), TaskId, минуты от полуночи startMinute/endMinute.
3. Навигация: День / Задачи / Настройки (enum + иконки). Экран День — заглушка «dial coming», не ломай Scaffold.
4. Удалить или заменить некомпилируемый Canvas-код минимальной заглушкой DrawClock(tasks) которая пока рисует пустой Canvas или Text, но СОБИРАЕТСЯ.

Non-goals: KMP, iOS, новые Gradle-модули, Hilt, Google Calendar, Room (это день 2), настоящий циферблат (день 3).

Ограничения:
- Код и KDoc на английском. Отвечай по-русски.
- Не подключай Hilt. DI пока не обязателен.
- Не оставляй опечаток Dioganal / orientation на несуществующих полях.

Acceptance:
- ./gradlew :app:assembleDebug успешен.
- Нет ссылок на сломанные типы.
- Три пункта навигации переключаются и переживают rotation через rememberSaveable.

Output: список изменённых файлов, что проверить в эмуляторе, что НЕ сделано специально.
```

#### Prompt (EN)

```
Role: senior Android Jetpack Compose engineer (Kotlin 2, Material 3). You are patching the existing ClockPlanner repo, not creating a new project.

Context:
- namespace com.example.clockplannerproject, minSdk 29, single :app module.
- Does not compile: data/ClockSegmentData.kt (Dioganal, empty ClockData, broken ClockSegment), ui/clock/DrawClock.kt and DrawClockSegment.kt (missing Compose imports, invalid when).
- MainActivity.kt: NavigationSuiteScaffold with Home/Favorites/Profile and Greeting.

Goal today:
1. Make debug assemble succeed.
2. Replace draft models with kit.core domain: Task, TaskStatus (TODO/IN_PROGRESS/DONE), TaskId, startMinute/endMinute from midnight.
3. Navigation: Day / Tasks / Settings. Day screen may stub “dial coming”. Keep NavigationSuiteScaffold.
4. Replace non-compiling Canvas code with a compiling stub DrawClock (empty Canvas or Text).

Non-goals: KMP, iOS, extra Gradle modules, Hilt, Google Calendar, Room (day 2), real dial drawing (day 3).

Constraints: KDoc/code in English. No Hilt. No leftover Dioganal typos.

Acceptance: :app:assembleDebug succeeds; three destinations survive rotation via rememberSaveable.

Output: files changed, emulator checks, explicitly deferred work.
```

#### Why this prompt

**RU/EN.** День 1 специально **узкий и запрещающий**: ТЗ начинается с KMP-каркаса из 12 модулей, а репозиторий даже не собирается. Формат Goal + жёсткий Non-goals не даёт агенту «выполнить ТЗ целиком». Упоминание конкретных сломанных файлов и опечатки `Dioganal` — якорь на реальный diff, а не на зелёный с нуля проект. Acceptance через `assembleDebug` — единственный честный DoD для дня расчистки.

---

### Day 2 — MVI + Room + Koin + repositories

#### Prompt (RU)

```
Роль: Android-инженер, MVI + Room + Koin.

Контекст: после дня 1 домен Task в kit.core, навигация День/Задачи/Настройки, сборка зелёная. Room ещё нет.

Цель:
- Интерфейсы kit.core: TaskRepository (observeTasks(LocalDate): Flow<List<Task>>, upsert, delete), TimeProvider (now(), observeTime(): Flow).
- Реализации в data/: Room entity/DAO/DB, RoomTaskRepository; SystemTimeProvider на kotlinx-datetime.
- Koin: single<TaskRepository>, single<TimeProvider>. Application-класс + AndroidManifest.
- MVI-база: UiState / UiIntent / UiEffect; DayViewModel подписан на observeTasks(today) + observeTime.
- Экран День показывает количество задач текстом (ещё без Canvas-логики углов).

Non-goals: Canvas-сектора, жесты, настройки внешнего вида, календарь, Hilt.

Ограничения: kotlinx-datetime в kit.core, не java.time. DAO на Dispatchers.IO. Не создавать :core:database модуль — пакет data внутри :app. Добавь зависимости в gradle/libs.versions.toml + app/build.gradle.kts (Room, Koin, serialization если нужно).

Acceptance: unit-тест in-memory Room (upsert → observe). Приложение стартует без crash. ViewModel не держит Context.

Output: файлы, как прогнать тест, схема таблиц.
```

#### Prompt (EN)

```
Role: Android engineer, MVI + Room + Koin.

Context: Day 1 left Task in kit.core, Day/Tasks/Settings nav, green build. No Room yet.

Goal: TaskRepository + TimeProvider in kit.core; Room + RoomTaskRepository + SystemTimeProvider in data/; Koin Application; MVI DayViewModel observing today + time; Day screen shows task count as text.

Non-goals: Canvas sectors, gestures, appearance settings, calendar, Hilt.

Constraints: kotlinx-datetime in kit.core; DAO on Dispatchers.IO; keep everything in :app; add Room/Koin via libs.versions.toml.

Acceptance: in-memory Room unit test; app starts; ViewModel has no Context.

Output: files, how to run the test, table schema.
```

#### Why this prompt

Отделяем **данные от рисования**. Если смешать Room и Canvas в одном дне, агент обычно ломает и DI, и draw. Явные интерфейсы в `kit.core` готовят DIP из ТЗ без Gradle-модулей. Запрет `java.time` и Hilt закрывает типовой дрейф стека. Acceptance «in-memory Room» проверяет контракт репозитория, а не UI.

---

### Day 3 — Canvas ring dial + colored sectors + labels

#### Prompt (RU)

```
Роль: инженер Compose Canvas. Рисуешь кольцевой 24-часовой циферблат.

Контекст: Task приходит из DayViewModel. DrawClock — заглушка. Углы считать только в kit.core.time.TimeMath.

Цель:
- TimeMath.minuteToAngle(minuteOfDay: Int): Float — 0 минут = 0° (12 часов), по часовой, 1440 минут = 360°.
- TimeMath.durationToSweep(start, end) с переходом через полночь.
- kit.render.TimeDialRenderer: фон кольца, сектора задач цветом task.color, DONE → серый config.colors.completedColor, подпись названия (укоротить если не влезает).
- kit.compose.TimeDial / DialView: Canvas + drawWithCache для статичного кольца.
- Preview с 3–4 фейковыми задачами.
- Пока БЕЗ автоповорота «сейчас сверху» (день 4) и БЕЗ hit-test (день 5).

Non-goals: pizza/petals, жесты, Room-изменения, KMP.

Ограничения:
- Никаких new Path()/Paint()/Color(hex) внутри DrawScope.draw цикла — remember / распарсить цвет заранее.
- Магические числа — именованные константы в geometry.
- Функции < 50 строк, SRP: renderBackground, renderTaskSectors, renderLabels.

Acceptance: Preview показывает сектора пропорционально длительности. Unit-тесты TimeMath (0→0f, 720→180f, 1440→360f или 0). assembleDebug зелёный.

Output: файлы render/compose, тесты TimeMath, скриншот-описание Preview.
```

#### Prompt (EN)

```
Role: Compose Canvas engineer. Draw a 24-hour ring dial.

Context: Tasks flow from DayViewModel. DrawClock is a stub. Angles only via kit.core.time.TimeMath.

Goal: TimeMath.minuteToAngle / durationToSweep (midnight wrap); TimeDialRenderer ring + colored sectors + truncated labels; DONE uses completedColor; TimeDial/DialView with drawWithCache; Preview with 3–4 fake tasks. No now-at-top (day 4), no hit-test (day 5).

Non-goals: pizza/petals, gestures, Room changes, KMP.

Constraints: no Path/Paint/Color(hex) allocations in the draw loop; named geometry constants; functions < 50 lines.

Acceptance: Preview sectors scale with duration; TimeMath tests (0→0°, 720→180°); assembleDebug green.
```

#### Why this prompt

Role «Canvas engineer» удерживает агента в `DrawScope`, а не в XML Custom View. Вынос `TimeMath` до рисования — DRY из ТЗ: иначе углы размножатся в pizza/petals. Запрет аллокаций в draw — единственный способ не получить jank, который ТЗ требует, но агенты часто игнорируют без явного Non-allocation. Hit-test и автоповорот вынесены: они ломают геометрию, если появляются в тот же день, что и первый `drawArc`.

---

### Day 4 — Real-time sync, current task at 12 o’clock

#### Prompt (RU)

```
Роль: Compose + время. Нужен живой циферблат, где «сейчас» всегда наверху.

Контекст: сектора уже рисуются в абсолютных углах от полуночи. TimeProvider.observeTime() тикает.

Цель:
- ClockConfig / поле rotation: visualAngle = TimeMath.minuteToAngle(minute) - TimeMath.minuteToAngle(nowMinute). После этого now всегда на 12 часах.
- Маркер текущего времени (стрелка/линия на 12).
- Подпись текущего локального времени в центре или сверху.
- Анимация поворота: animateFloatAsState или ручной lerp не чаще 1 Hz (не 60 пересборок Path в секунду без нужды).
- derivedStateOf для nowMinute.

Non-goals: пользовательский жест вращения (день 10), уведомления, смена даты в полночь кроме обновления списка задач.

Ограничения: автоповорот — отдельное поле от userRotationOffset (оставь offset = 0). Не крутить сами данные Task, только матрицу/угол сцены.

Acceptance: задача, пересекающая «сейчас», визуально у 12 часов. Через 1–2 минуты на эмуляторе сектор уезжает по часовой. Unit: visualAngle(now)=0.

Output: формулы, файлы, как проверить вручную.
```

#### Prompt (EN)

```
Role: Compose + time. Live dial with “now” always at the top.

Context: sectors use absolute midnight-based angles. TimeProvider.observeTime() ticks.

Goal: visualAngle = minuteToAngle(minute) - minuteToAngle(now); now marker at 12; current time label; animate at ~1 Hz; derivedStateOf for nowMinute. Keep userRotationOffset = 0 for later.

Non-goals: user rotate gesture (later week 2), notifications.

Constraints: rotate the scene, not Task data.

Acceptance: current task sits at 12 o’clock; after 1–2 minutes it moves clockwise; unit test visualAngle(now)==0.
```

#### Why this prompt

ТЗ формулирует UX расплывчато («диаграмма вращается»). Промпт даёт **формулу** `visualAngle = task - now`, иначе агенты двигают стрелку, а сектора оставляют привязанными к полуночи. Разделение `userRotationOffset` заранее предотвращает войну жестов с автосинхронизацией на неделе 2. Лимит 1 Hz — защита от лишних рекомпозиций Canvas.

---

### Day 5 — Hit-test + bottom sheet stub

#### Prompt (RU)

```
Роль: Canvas hit-testing + Material 3 ModalBottomSheet.

Контекст: сектора с visualAngle. Нужен тап по задаче.

Цель:
- kit.render.hitTestTask(offset, tasks, config, geometry, nowMinute, userRotationOffset): Task? — перевести touch в угол и радиус, попасть в кольцо, выбрать задачу с минимальным sweep среди пересечений.
- TimeDial(onTaskClick).
- ui.day: при клике UiIntent.SelectTask → sheet с названием, временем, описанием (если пусто — placeholder), статусом.
- Закрытие sheet сбрасывает выбор.

Non-goals: редактирование CRUD (неделя 2), long-press, жесты масштаба.

Ограничения: hit-test чистая функция, покрыть unit-тестами (центр — miss, точка на секторе — hit). Не класть бизнес-логику в Composable сверх маппинга intent.

Acceptance: тап по сектору открывает sheet этой задачи; тап в дырку кольца не открывает. Тесты hit-test зелёные.

Output: алгоритм в 5 шагах, файлы, тесты.
```

#### Prompt (EN)

```
Role: Canvas hit-testing + Material 3 ModalBottomSheet.

Goal: pure hitTestTask(offset, …) mapping touch to angle/radius; smallest-sweep wins on overlap; TimeDial(onTaskClick); Day screen sheet with title, time, description, status.

Non-goals: CRUD edit, long-press, pinch.

Acceptance: tap sector opens that task; tap hole does nothing; unit tests for miss/hit.
```

#### Why this prompt

Hit-test как **чистая функция** (не внутри `pointerInput` без тестов) — единственный способ не отлаживать геометрию пальцем. «Smallest sweep wins» закрывает дыру ТЗ про перекрытия. Sheet в день 5 только read-only: если сразу дать CRUD, агент уйдёт в формы и бросит попадание.

---

### Day 6 — Sample day + empty/error states + Previews

#### Prompt (RU)

```
Роль: продуктовый Android UI.

Контекст: циферблат, время, sheet. Данных мало, UX дырявый.

Цель:
- Сидер / debug sample: типичный день (сон, работа, обед, спорт) только в debug через BuildConfig или отдельный SampleTasks.
- Пустой день: иллюстрация + CTA «Добавить задачу» (кнопка может вести на заглушку Tasks).
- Ошибка репозитория: UiEffect / snackbar, не белый экран.
- @Preview: пустой, полный, все DONE, одна задача через полночь.
- contentDescription у навигации и маркера «сейчас».

Non-goals: настоящий CRUD save, жесты, статистика.

Acceptance: четыре Preview без crash. Пустой и заполненный состояния различимы. assembleDebug зелёный.
```

#### Prompt (EN)

```
Role: product Android UI.

Goal: debug sample day; empty state + CTA; repository error snackbar; Previews (empty, full, all DONE, overnight task); contentDescriptions on nav and now-marker.

Non-goals: real CRUD save, gestures, stats.

Acceptance: four Previews; empty vs filled distinguishable; assembleDebug green.
```

#### Why this prompt

Отдельный день на **состояния** снижает риск, что неделя 1 закончится только happy-path Canvas. Sample data в debug не засоряет Room-прод. Previews в Acceptance заставляют агента оставить визуальные регрессионные точки до жестов.

---

### Day 7 — Polish, tests, tag v0.1

#### Prompt (RU)

```
Роль: tech lead, закрываешь v0.1. Не добавляй фичи.

Контекст: дни 1–6. Нужен Definition of Done из docs/ROADMAP.md для v0.1.

Цель:
- Починить compile warnings, мёртвый код Greeting, неиспользуемые импорты.
- Добить unit-тесты TimeMath + hitTest + repository, если дыры.
- Краткий docs/CHANGELOG.md секция v0.1.
- README: как собрать, что умеет v0.1, чего нет.
- Если git доступен — Conventional Commit и инструкция тега v0.1 (не пушь и не тегай, если пользователь не просил git; просто подготовь сообщение).

Non-goals: любая фича недели 2+.

Acceptance: чеклист v0.1 из ROADMAP все пункты true. assembleDebug + unit test task зелёные.
```

#### Prompt (EN)

```
Role: tech lead closing v0.1. No new features.

Goal: kill dead Greeting/unused imports; fill TimeMath/hitTest/repository tests; CHANGELOG v0.1; README run instructions. Prepare Conventional Commit message for tag v0.1; do not push/tag unless asked.

Non-goals: week 2+ features.

Acceptance: ROADMAP v0.1 DoD; assembleDebug + unit tests green.
```

#### Why this prompt

Явный **«не добавляй фичи»** обязателен: в полировочные дни агенты начинают pizza-режим. Role tech lead смещает фокус на DoD и тесты. Запрет самовольного git tag уважает user rule про коммиты.

---

## Week 2 — v0.2

### Day 8 — Task CRUD (create / update / delete)

#### Prompt (RU)

```
Роль: Android формы + Room + MVI.

Контекст: v0.1 показывает задачи. Редактирования нет.

Цель:
- Экран Задачи: список на выбранную дату, FAB «создать».
- Форма: название, описание, цвет (ограниченная палитра), start/end TimePicker, статус.
- Валидация: end ≠ start; название не пустое; корректный overnight.
- Intents: Create, Update, Delete, ConfirmDelete.
- После save Flow обновляет циферблат без ручного refresh.

Non-goals: жесты на Canvas, настройки темы, статистика, календарь.

Ограничения: ViewModel тестируемый; цвет хранить как ARGB Long/Int, парсить вне draw().

Acceptance: создать → сектор появляется; изменить время → сектор едет; удалить → сектор исчезает. Unit-тест валидации.
```

#### Prompt (EN)

```
Role: Android forms + Room + MVI.

Goal: Tasks screen + FAB; form (title, description, palette color, start/end, status); validation; Create/Update/Delete; dial refreshes via Flow.

Non-goals: canvas gestures, theme settings, stats, calendar.

Acceptance: create/update/delete visible on dial; validation unit test.
```

#### Why this prompt

CRUD отделён от жестов: указатели и формы в одном дне почти всегда дают полурабочие оба. Валидация overnight названа явно — типичный баг 24h-планировщика. Цвет как число «вне draw» продолжает контракт дня 3.

---

### Day 9 — Full task card + complete toggle

#### Prompt (RU)

```
Роль: UX карточки задачи.

Контекст: sheet уже показывает поля. Нужен полноценный просмотр и DONE.

Цель:
- Bottom sheet: описание полностью, теги если есть в модели (добавь List<String> tags только если это маленькое поле; иначе пропусти теги).
- Кнопки: Выполнить / Вернуть в работу, Изменить, Удалить (confirm dialog).
- DONE → сектор сразу серый через тот же renderer.
- Не закрывать циферблат под sheet (ModalBottomSheet standard).

Non-goals: hide-completed reflow (день 12), жесты размера.

Acceptance: toggle DONE меняет цвет без перезапуска процесса. Edit из sheet открывает форму дня 8.
```

#### Prompt (EN)

```
Role: task card UX.

Goal: full description in sheet; Complete/Reopen, Edit, Delete with confirm; DONE sectors go gray immediately; Edit opens day-8 form.

Non-goals: hide-completed reflow, resize gestures.

Acceptance: DONE color updates without process restart.
```

#### Why this prompt

ТЗ требует серый DONE и полное описание по нажатию — это **продуктовое ядро**, его нельзя растворить в «жестах». Toggle статуса отдельно от hide-completed: иначе агент склеит «скрыть» и «выполнить».

---

### Day 10 — Rotate clock gesture

#### Prompt (RU)

```
Роль: Compose pointerInput / detectTransformGestures.

Контекст: автоповорот держит now на 12. Нужен ЖЕСТ вращения пользователя.

Цель:
- InteractionConfig + userRotationOffset (градусы), суммируется с autoNowRotation.
- Один палец вокруг центра: изменение offset. Не ломать скролл NavigationSuite.
- Кнопка «сбросить поворот» (offset = 0) на экране День.
- Документируй: автосинхронизация now остаётся; жест добавляет смещение сцены.

Non-goals: pinch-zoom размера задачи (день 11), hide completed.

Ограничения: hit-test должен учитывать тот же итоговый угол, что и renderer. Тест: given offset 90, hit at visual position.

Acceptance: можно повернуть день, now-маркер остаётся логически «сейчас»; Reset возвращает ТЗ-поведение «сейчас сверху».
```

#### Prompt (EN)

```
Role: Compose pointerInput / detectTransformGestures.

Goal: userRotationOffset added to autoNowRotation; one-finger rotate around center; Reset control; hit-test uses the same final angle as renderer.

Non-goals: pinch-resize task, hide completed.

Acceptance: user can rotate; Reset restores now-at-top; hit-test stays aligned.
```

#### Why this prompt

Без явного **сложения** auto + user агент затирает ТЗ-требование «сейчас сверху». Упоминание hit-test в том же дне обязательно: иначе клики разъедутся с картинкой. Reset — приёмочный якорь, что жест не ломает спецификацию навсегда.

---

### Day 11 — Resize task gesture

#### Prompt (RU)

```
Роль: жесты изменения длительности.

Контекст: поворот сцены есть. Нужно «увеличивать размер задачи» из ТЗ.

Цель:
- Выбранная задача (после tap) или long-press: жест от центра / pinch по сектору меняет endMinute (или duration), snap к 5 минутам.
- Минимум длительности 5 минут, максимум не вытеснять соседей без правила: если overlap — разрешить overlap (z-order как в hit-test), не делать сложный auto-shift соседей.
- UiIntent.ResizeTask(id, newEndMinute) → upsert.
- Visual feedback: пока жест, preview sweep без записи каждую кадр-секунду в Room (debounce upsert 300–500 ms или commit on finger up).

Non-goals: hide completed, новые режимы pizza.

Acceptance: жест увеличивает сектор; после отпускания значение в форме совпадает. Unit: snap-to-5min.

Output: какой жест выбран (pinch vs drag edge) и почему в коде KDoc.
```

#### Prompt (EN)

```
Role: duration-resize gestures.

Goal: selected or long-pressed task; pinch/drag changes endMinute; snap 5 minutes; min 5; overlaps allowed; debounce or commit on finger up; ResizeTask intent.

Non-goals: hide completed, pizza mode.

Acceptance: sector grows; form matches after release; snap unit test. Document chosen gesture in KDoc.
```

#### Why this prompt

ТЗ говорит «увеличивать размер» без механики. Промпт **фиксирует правило overlap** (разрешить, не auto-shift): иначе день уйдёт в планировщик-календарь. Debounce Room — чтобы жест не убил БД. Просьба задокументировать выбранный жест в KDoc снимает двусмысленность pinch vs край сектора, но не блокирует старт.

---

### Day 12 — Hide completed + sector reflow

#### Prompt (RU)

```
Роль: доменная геометрия дня.

Контекст: ТЗ: скрыть выполненные, чтобы увеличить пространство для задач в работе.

Цель:
- Флаг hideCompleted в состоянии дня (switch на экране).
- НЕ просто filter() для отрисовки в абсолютных часах. Нужна стратегия reflow:
  1) Visible tasks = not DONE (или все, если флаг выкл).
  2) Функция kit.core.layout.Reflow.layout(tasks, mode): для режима CompactRemaining распределить visible tasks пропорционально их duration в полные 360°, сохранив порядок по startMinute.
  3) Когда флаг выкл — layout = реальные часы (v0.1 поведение).
- Подпись на секторе + tooltip/legend что в compact режиме углы ≠ часы стены (показать реальные start–end текстом на секторе или в легенде).
- Unit-тесты: два TODO + один DONE → при hide два сектора суммарно 360°.

Non-goals: pizza renderer, статистика.

Acceptance: выкл hide — как часы; вкл hide — DONE исчезают, оставшиеся крупнее. Now-at-top всё ещё осмыслен в реальном режиме; в compact now-marker можно оставить на 12 как «сейчас» без привязки к секторам — опиши в KDoc.
```

#### Prompt (EN)

```
Role: day-geometry domain.

Goal: hideCompleted switch; Reflow.layout CompactRemaining spreads visible tasks over 360° proportional to duration, order by startMinute; off = wall-clock layout; labels still show real start–end; unit test two TODO + one DONE → 360°.

Non-goals: pizza renderer, stats.

Acceptance: toggle changes layout as specified; document now-marker behavior in compact mode in KDoc.
```

#### Why this prompt

Это **главный продуктовый риск ТЗ**. Без слова «reflow / 360°» агент сделает `filter` и сектора останутся тонкими. Тест «сумма sweep = 360» — числовой DoD. KDoc про now-marker обязателен, потому что в compact режиме «часы стены» и «доля дня» расходятся — это нужно назвать, а не спрятать.

---

### Day 13 — Overlap z-order + accessibility on dial

#### Prompt (RU)

```
Роль: качество взаимодействия циферблата.

Контекст: жесты и reflow есть, крайние случаи сырые.

Цель:
- Стабильный z-order: меньше duration выше (уже в hit-test) + одинаковая сортировка в renderer.
- TalkBack: custom semantics на DialView — список задач с временем, clickable.
- Отмена жеста (за границами) не портит offset.
- Overnight задача рисуется двумя дугами или одной с wrap — единообразно с TimeMath.

Non-goals: новые view modes.

Acceptance: две перекрывающие задачи кликабельны предсказуемо. Overnight виден до и после 00:00. assembleDebug + существующие unit зелёные.
```

#### Prompt (EN)

```
Role: dial interaction quality.

Goal: renderer z-order matches hit-test (shorter duration on top); TalkBack semantics listing tasks; gesture cancel does not corrupt offset; overnight as two arcs or one wrap, consistent with TimeMath.

Non-goals: new view modes.

Acceptance: overlapping taps are predictable; overnight visible across midnight.
```

#### Why this prompt

День «краёв» перед тегом v0.2. Semantics здесь, потому что Canvas по умолчанию немой для TalkBack — если отложить всё на неделю 6, hit-test придётся переписывать. Overnight назван отдельно: это классический баг `drawArc`.

---

### Day 14 — Polish, tests, tag v0.2

#### Prompt (RU)

```
Роль: tech lead v0.2. Без фич недели 3.

Цель: регрессия CRUD, sheet, rotate, resize, hide+reflow; добить тесты Reflow и snap; CHANGELOG v0.2; убрать отладочные Log.

Acceptance: DoD v0.2 из ROADMAP. Не начинать PizzaRenderer.
```

#### Prompt (EN)

```
Role: tech lead v0.2. No week-3 features.

Goal: regression CRUD/sheet/rotate/resize/reflow; Reflow+snap tests; CHANGELOG v0.2; remove debug Logs.

Acceptance: ROADMAP v0.2 DoD. Do not start PizzaRenderer.
```

#### Why this prompt

Повтор явного бана следующей фичи. Короче, чем день 7: формат «tech lead + DoD» уже известен агенту, лишняя вода снижает следование чеклисту.

---


## Week 3 — v0.3

View modes DIAL/PIZZA/PETALS/LIST already live in `:app`. Week 3 does **not** reimplement pizza. Spec: [TZ.md](TZ.md). **No Room `Migration` class** — `version++` and `fallbackToDestructiveMigration()` only.

### Day 15 — Domain + destructive schema

#### Prompt (RU)

```
Роль: домен гибкого времени. Ты правишь ClockPlannerProject, не новый проект.

Контекст: namespace com.example.clockplannerproject. DIAL/PIZZA/PETALS/LIST уже есть. Канон: docs/TZ.md.

Цель:
- Task: список TimeBlock(startMinute, endMinute), Importance (HIGH/MEDIUM/LOW), tags: List<String>.
- Пустой список блоков = задача без времени. Несколько блоков = несколько секторов, один TaskId.
- Убрать startMinute/endMinute на Task как единственный источник истины (углы только из блоков).
- Room: новые поля/таблицы, version++, ТОЛЬКО fallbackToDestructiveMigration(). Не писать class Migration.
- Редактор: можно не указывать время (сохранение без блоков валидно, если title не пустой).
- Тесты маппера Task ↔ Entity.

Non-goals: leftover-раскладка, веса 5:3:2 в UI, теги-фильтр, таймер, повтор, отчёты, pizza с нуля.

Acceptance: assembleDebug; задача без времени сохраняется; wipe при смене version задокументирован в KDoc Database. Нет Migration.
```

#### Prompt (EN)

```
Role: flexible-time domain.

Goal: TimeBlock list, Importance, tags on Task; empty blocks = untimed; Room version++ with fallbackToDestructiveMigration only; editor allows no time; mapper tests.

Non-goals: leftover layout, 5:3:2 UI, tag filter, timer, recurrence, reports, pizza from scratch.

Acceptance: untimed task saves; no Migration class; KDoc that schema wipes.
```

#### Why this prompt

Схема до leftover. Запрет Migration — агент иначе напишет 1→2 migrate и сломает «переустановка».

---

### Day 16 — Leftover layout (equal shares)

#### Prompt (RU)

```
Роль: геометрия leftover вместо Rest.

Контекст: блоки есть. Важность в модели может быть, но СЕГОДНЯ доли равные.

Цель:
- kit.core.layout: timed → civil clip на половине; leftover половины = пул; untimed делят пул ПОРОВНУ.
- «Отдых» только если leftover > 0 и нет untimed (или после равных долей ещё остаток — сегодня остатка не должно быть, если есть хотя бы один untimed: они забирают весь пул).
- Если leftover половины = 0, untimed на этой половине не рисуются.
- Unit: две безвременные + пустой AM → два сектора суммарно 360°. Timed 9–12 + одна untimed → untimed только в gaps.
- Тот же layout для PIZZA (другой fill, те же минуты). UI не считает доли.

Non-goals: веса 5:3:2, теги UI, таймер.

Acceptance: тесты leftover; Rest не перекрывает untimed; DIAL и PIZZA используют один layout API.
```

#### Prompt (EN)

```
Role: leftover geometry instead of Rest.

Goal: timed civil clip; leftover pool on the 12h face; untimed split equally; Rest only if leftover remains without untimed; untimed hidden if leftover is 0; unit two untimed → 360°; same layout minutes for PIZZA.

Non-goals: 5:3:2, tag UI, timer.

Acceptance: tests; UI does not compute shares.
```

#### Why this prompt

Равные доли до весов — иначе агент смешает 50/30/20 с багом packing.

---

### Day 17 — Importance weights 5:3:2

#### Prompt (RU)

```
Роль: важность leftover.

Цель:
- HIGH:MEDIUM:LOW = 5:3:2. Доля w_i / sum(w) от leftover половины.
- Равная важность → равные доли (две HIGH, пустой leftover → 50/50).
- Три разные на пустом leftover → 50% / 30% / 20% (целочисленные минуты, последний кусок добивает сумму).
- Timed по-прежнему civil; untimed только leftover.
- Редактор: три уровня важности.
- Unit-тесты весов + смесь с timed 9–12.

Non-goals: теги UI, повтор, таймер.

Acceptance: тест 50/30/20; UI не хардкодит проценты.
```

#### Prompt (EN)

```
Role: leftover importance.

Goal: weights 5:3:2; equal importance equal shares; three distinct → 50/30/20 with integer remainder on last slice; editor three levels; tests with timed mix.

Non-goals: tag UI, recurrence, timer.

Acceptance: 50/30/20 unit test; no percents in UI.
```

#### Why this prompt

Числа 50/30/20 в Acceptance, чтобы не выдумали «приоритет сортировки» вместо долей.

---

### Day 18 — Tags

#### Prompt (RU)

```
Роль: теги задачи.

Цель:
- Несколько тегов на Task; чипсы в форме, LIST и sheet.
- Фильтр по тегу на экране Задачи.
- Не отдельный CRUD категорий.

Non-goals: облако, календарь, Start/Pause.

Acceptance: сохранить задачу с 2+ тегами; фильтр сужает список; wipe DB ок.
```

#### Prompt (EN)

```
Role: task tags.

Goal: multiple tags; chips in form, LIST, sheet; filter on Tasks screen; no category CRUD.

Non-goals: cloud, calendar, timer.

Acceptance: 2+ tags persist; filter works.
```

#### Why this prompt

Теги отдельно от leftover, иначе форма раздувается.

---

### Day 19 — Multi-block draw + editor

#### Prompt (RU)

```
Роль: несколько отрезков одной задачи.

Цель:
- Несколько TimeBlock → несколько секторов, один TaskId (hit-test/sheet как overnight).
- Форма: список интервалов; добавить/удалить только незакрытые/будущие.
- Если endMinute блока <= now — read-only (не resize, не править поля).
- Спорт 8–9 и 12–13 видны дважды на диаграмме.

Non-goals: Start/Pause в центре, recurrence.

Acceptance: два блока одной задачи кликабельны в один sheet; прошедший блок не меняется из формы.
```

#### Prompt (EN)

```
Role: several intervals per task.

Goal: one TaskId, many sectors; editor list of blocks; closed blocks (end <= now) read-only; sport 8–9 and 12–13 draws twice.

Non-goals: center timer, recurrence.

Acceptance: both sectors open the same sheet; past block immutable in the form.
```

#### Why this prompt

Append-only таймера нет, но модель блоков должна уже рисоваться, иначе день 26 ломает layout.

---

### Day 20 — Mixed diagram QA + hideCompleted

#### Prompt (RU)

```
Роль: QA смеси timed/untimed.

Цель:
- CompactRemaining: leftover и веса после исключения DONE.
- Overnight + несколько блоков.
- Previews: только timed; только untimed 50/50; смесь; три важности.
- LIST: hideCompleted фильтр, без leftover 360 (KDoc).

Non-goals: recurrence, Start/Pause.

Acceptance: unit compact + leftover; Preview смесь; pizza не дублирует формулы.
```

#### Prompt (EN)

```
Role: mixed timed/untimed QA.

Goal: compact leftover after dropping DONE; overnight + multi-block; previews; LIST filter KDoc.

Non-goals: recurrence, timer.

Acceptance: compact+leftover test; mixed preview.
```

#### Why this prompt

День перед тегом: явный compact vs leftover.

---

### Day 21 — Polish, tests, tag v0.3

#### Prompt (RU)

```
Роль: tech lead v0.3. Без недели 4.

Цель: регрессия блоков, leftover 5:3:2, теги; CHANGELOG v0.3; README: схема сбрасывается, обновление = переустановка. Не начинать таймер и повтор.

Acceptance: DoD v0.3 из ROADMAP. Нет class Migration.
```

#### Prompt (EN)

```
Role: tech lead v0.3.

Goal: regression blocks/leftover/tags; CHANGELOG v0.3; README wipe/reinstall. Do not start timer or recurrence.

Acceptance: ROADMAP v0.3 DoD. No Migration class.
```

#### Why this prompt

Явный бан таймера, чтобы не утащить неделю 4.

---

## Week 4 — v0.4

### Day 22 — Move task to another day

#### Prompt (RU)

```
Роль: перенос экземпляра.

Цель: действие «перенести на дату» меняет date задачи; блоки и теги сохраняются. Не генерировать серию.

Non-goals: дубликат, recurrence, таймер.

Acceptance: задача исчезает с исходного дня и появляется на новом в observeByDate.
```

#### Prompt (EN)

```
Role: move instance.

Goal: change task date; keep blocks and tags; no series generation.

Non-goals: duplicate, recurrence, timer.

Acceptance: observeByDate moves the row.
```

#### Why this prompt

Перенос ≠ дубликат. Развести в разные дни.

---

### Day 23 — Duplicate task

#### Prompt (RU)

```
Роль: дубликат.

Цель: новая TaskId, копия title/description/color/importance/tags/блоков/даты (или выбранный день). Прошедшие блоки копируются как данные, не «оживают».

Non-goals: recurrence rule, Start/Pause.

Acceptance: два независимых id; правка копии не меняет оригинал.
```

#### Prompt (EN)

```
Role: duplicate.

Goal: new id, copy fields and blocks; edits do not affect original.

Non-goals: recurrence rule, timer.

Acceptance: two ids in repository.
```

#### Why this prompt

Копия до шаблона повтора — проще материализация.

---

### Day 24 — Recurrence daily / weekdays

#### Prompt (RU)

```
Роль: повтор без Google Calendar.

Цель:
- Правило: ежедневно ИЛИ набор дней недели.
- Материализация экземпляра на видимый день (today / выбранная дата Tasks), если ещё нет.
- Не плодить бесконечный горизонт в БД.

Non-goals: таймер, облако, календарный sync.

Acceptance: daily задача видна завтра после материализации; weekdays пропускает невыбранный день. Unit на правило.
```

#### Prompt (EN)

```
Role: recurrence without Google Calendar.

Goal: daily or weekday set; materialize instance for the visible day only if missing; no infinite horizon.

Non-goals: timer, cloud, calendar sync.

Acceptance: unit for rule; visible day gets an instance.
```

#### Why this prompt

Только видимый день — иначе Room забьётся экземплярами.

---

### Day 25 — Tap raises sector + description cloud

#### Prompt (RU)

```
Роль: выбор на циферблате.

Цель:
- Выбранная задача рисуется поверх (z-order last).
- Облако/callout с описанием у сектора (не только BottomSheet).
- Работает на DIAL и PIZZA.

Non-goals: Start/Pause, новые режимы.

Acceptance: тап поднимает сектор; облако показывает description; второй тап/dismiss закрывает.
```

#### Prompt (EN)

```
Role: dial selection.

Goal: selected task paints on top; callout with description; DIAL and PIZZA.

Non-goals: Start/Pause, new modes.

Acceptance: tap raises; cloud shows description.
```

#### Why this prompt

Z-order до таймера: кнопка в центре не должна теряться под секторами.

---

### Day 26 — Start / Pause in the hole

#### Prompt (RU)

```
Роль: таймер append-only.

Цель:
- Оверлей в центре кольца (и на пицце): Старт / Пауза при выбранной задаче.
- Старт: новый TimeBlock startMinute = now, end открыт; статус IN_PROGRESS.
- Пауза: endMinute = now. Старые блоки не менять.
- Нельзя Старт, если уже есть открытый блок этой задачи.

Non-goals: отчёты, recurrence UI.

Acceptance: unit append; прошедший блок не меняется после Pause.
```

#### Prompt (EN)

```
Role: append-only timer.

Goal: center overlay Start/Pause; Start appends block at now; Pause closes end; no edit of old blocks; one open block max.

Non-goals: reports, recurrence UI.

Acceptance: append unit test; past block unchanged.
```

#### Why this prompt

Центр, не sheet: ТЗ явно «по середине круга».

---

### Day 27 — Timer + diagram (two sport sectors)

#### Prompt (RU)

```
Роль: связка таймера и отрисовки.

Цель: сценарий ТЗ — блок 8:00–9:00 уже есть; в 12:00 Старт, через час Пауза → два сектора спорта, первый не изменился.
- Идущий блок (без end) рисуется от start до now.
- Hit-test оба сектора → одна задача.

Non-goals: медиа-отчёты.

Acceptance: Preview/тест двух блоков; live-сектор растёт с now (не чаще layout API).
```

#### Prompt (EN)

```
Role: timer meets renderer.

Goal: existing 8–9 plus Start at 12 and Pause at 13 → two sectors, first immutable; open block start→now; same TaskId.

Non-goals: media reports.

Acceptance: two-block test; live slice uses now via layout.
```

#### Why this prompt

Числовой сценарий из ТЗ — DoD таймера.

---

### Day 28 — Polish, tests, tag v0.4

#### Prompt (RU)

```
Роль: tech lead v0.4. Без отчётов недели 5.

Цель: регрессия move/duplicate/recurrence/cloud/Start-Pause; CHANGELOG v0.4.

Acceptance: DoD v0.4 ROADMAP.
```

#### Prompt (EN)

```
Role: tech lead v0.4.

Goal: regression move/duplicate/recurrence/callout/timer; CHANGELOG v0.4. No week-5 reports.

Acceptance: ROADMAP v0.4 DoD.
```

#### Why this prompt

Бан отчётов.

---

## Week 5 — v0.5

### Day 29 — Report entity

#### Prompt (RU)

```
Роль: отчёт как сущность.

Цель: TaskReport (id, taskId, createdAt, text). Room, destructive version bump, без Migration. Репозиторий observeReports(taskId).

Non-goals: файлы, UI редактора текста.

Acceptance: upsert/observe unit; wipe ok.
```

#### Prompt (EN)

```
Role: report entity.

Goal: TaskReport; Room destructive bump; observeReports(taskId).

Non-goals: files, text editor UI.

Acceptance: repository test.
```

#### Why this prompt

Сущность до UI и медиа.

---

### Day 30 — Report text UI

#### Prompt (RU)

```
Роль: текст отчёта.

Цель: с карточки задачи — создать/править текст отчёта. Список отчётов задачи (дата + превью).

Non-goals: picker файлов.

Acceptance: текст переживает процесс (Room).
```

#### Prompt (EN)

```
Role: report text UI.

Goal: create/edit report text from the task card; list reports.

Non-goals: file picker.

Acceptance: text survives process restart.
```

#### Why this prompt

Текст отдельно от permissions медиа.

---

### Day 31 — Report media attachments

#### Prompt (RU)

```
Роль: вложения локально.

Цель: прикрепить фото/файл; копия в filesDir приложения; URI/path в Room. Без облака. Permissions минимальные. Destructive DB ок.

Non-goals: видеоредактор, sync.

Acceptance: вложение открывается после перезапуска; нет Migration.
```

#### Prompt (EN)

```
Role: local attachments.

Goal: pick image/file; copy to app filesDir; store path in Room; no cloud.

Non-goals: video editor, sync.

Acceptance: attachment opens after process restart; no Migration.
```

#### Why this prompt

filesDir, не MediaStore как единственное хранилище — проще wipe.

---

### Day 32 — View reports on the task

#### Prompt (RU)

```
Роль: просмотр отчётов.

Цель: sheet/экран: текст + превью вложений; удаление отчёта. Связь с тем же TaskId.

Non-goals: статистика дня.

Acceptance: Preview; TalkBack на кнопках вложений.
```

#### Prompt (EN)

```
Role: view reports.

Goal: text + attachment preview; delete report; same TaskId.

Non-goals: day stats.

Acceptance: Preview; contentDescription on attach actions.
```

#### Why this prompt

Закрыть UX отчёта до цифр.

---

### Day 33 — Day statistics (secondary)

#### Prompt (RU)

```
Роль: статистика дня, вторично.

Цель: часы timed-блоков (стена часов); untimed не считать как civil duration, либо отдельно KDoc. Не дублировать leftover-формулы.

Non-goals: PNG export.

Acceptance: unit; hideCompleted не врёт в цифрах (stats = wall-clock truth).
```

#### Prompt (EN)

```
Role: day stats, secondary.

Goal: sum timed blocks wall-clock; document untimed; no leftover formula copy.

Non-goals: PNG.

Acceptance: unit; stats ignore hideCompleted compact.
```

#### Why this prompt

Связка hideCompleted и stats — стена часов.

---

### Day 34 — Export or week rollup (thin)

#### Prompt (RU)

```
Роль: тонкий экспорт или неделя.

Цель: ЛИБО экспорт дня PNG как раньше, ЛИБО сводка недели по timed-минутам. Одно из двух, не оба, если не успеваешь. Без новых Gradle-модулей.

Non-goals: магазины, календарь.

Acceptance: одна фича работает; CHANGELOG черновик.
```

#### Prompt (EN)

```
Role: thin export or week rollup.

Goal: either PNG day export or week timed-minutes — one feature.

Non-goals: stores, calendar.

Acceptance: one shippable feature.
```

#### Why this prompt

Не раздувать неделю 5: отчёты главные.

---

### Day 35 — Polish, tests, tag v0.5

#### Prompt (RU)

```
Роль: tech lead v0.5. Без подложек недели 6.

Цель: регрессия отчётов+медиа; CHANGELOG v0.5.

Acceptance: DoD v0.5 ROADMAP.
```

#### Prompt (EN)

```
Role: tech lead v0.5.

Goal: reports+media regression; CHANGELOG v0.5. No week-6 backgrounds.

Acceptance: ROADMAP v0.5 DoD.
```

#### Why this prompt

Бан подложек.

---

## Week 6 — v1.0-android

### Day 36 — Backgrounds / underlays

#### Prompt (RU)

```
Роль: подложки циферблата.

Цель:
- BackgroundConfig: None, Color, Preset(name) из drawable; UserImage — API + заглушка «coming».
- 3 пресета; scrim для читаемости.
- Видно под DIAL и PIZZA.

Non-goals: полный photo picker.

Acceptance: смена пресета видна; текст читается.
```

#### Prompt (EN)

```
Role: dial underlays.

Goal: None/Color/Preset; three drawables; scrim; UserImage stub.

Non-goals: full photo picker.

Acceptance: preset under DIAL and PIZZA; text readable.
```

#### Why this prompt

Бывший день 19: перенесён, чтобы leftover успел в v0.3.

---

### Day 37 — Settings + DataStore (mode, colors, background)

#### Prompt (RU)

```
Роль: настройки + DataStore.

Цель: default ViewMode, акценты, completedColor, background preset. AppearanceRepository. TimeDialConfig из DataStore, не хардкод. Нет Hilt. Старые ключи не роняют приложение (prefs, не Room Migration).

Non-goals: 5 раскладок текста.

Acceptance: перезапуск сохраняет режим и подложку.
```

#### Prompt (EN)

```
Role: settings + DataStore.

Goal: default ViewMode, accents, completedColor, background; AppearanceRepository; TimeDialConfig from DataStore.

Non-goals: five text layouts.

Acceptance: process restart restores mode and background. No Hilt.
```

#### Why this prompt

Prefs ≠ Room. Не писать Migration.

---

### Day 38 — Onboarding + empty-state journey

#### Prompt (RU)

```
Роль: онбординг.

Цель: 3–5 экранов зачем циферблат, leftover без времени, Старт в центре. Не блокировать повторный вход флагом без DataStore. Пустой день ведёт к созданию задачи.

Non-goals: магазины.

Acceptance: rememberSaveable/DataStore «seen»; Skip работает.
```

#### Prompt (EN)

```
Role: onboarding.

Goal: 3–5 screens (clock, untimed leftover, center Start); persist seen; skip; empty day CTA.

Non-goals: stores.

Acceptance: skip works; seen persisted.
```

#### Why this prompt

Онбординг знает новый TZ, не только кольцо v0.1.

---

### Day 39 — Localization ru / en

#### Prompt (RU)

```
Роль: l10n.

Цель: все user-facing strings в values / values-ru, включая leftover legend, важность, Старт/Пауза, отчёты. Никакого хардкода в Composable кроме Preview.

Acceptance: смена языка системы меняет UI.
```

#### Prompt (EN)

```
Role: l10n.

Goal: all user-facing strings including leftover, importance, Start/Pause, reports.

Acceptance: system locale switches UI.
```

#### Why this prompt

Новые строки недели 3–5 иначе останутся английскими.

---

### Day 40 — Accessibility

#### Prompt (RU)

```
Роль: TalkBack.

Цель: Старт/Пауза, облако, теги, отчёты, переключатель режимов — contentDescription. Не ломать custom actions циферблата.

Acceptance: чеклист ключевых экранов без жестов-only.
```

#### Prompt (EN)

```
Role: TalkBack.

Goal: Start/Pause, callout, tags, reports, view-mode switcher labeled; keep dial custom actions.

Acceptance: no gesture-only primary actions.
```

#### Why this prompt

Таймер в центре легко забыть для a11y.

---

### Day 41 — Icon, splash, tests, README

#### Prompt (RU)

```
Роль: оболочка релиза.

Цель: иконка/splash; assembleDebug + testDebugUnitTest зелёные; README: wipe схемы, как собрать, что умеет v1.0-android vs v2 (KMP/календарь).

Non-goals: публикация в Play.

Acceptance: README указывает TZ.md и что обновление уничтожает локальную БД до явной миграции (её нет).
```

#### Prompt (EN)

```
Role: release shell.

Goal: icon/splash; tests green; README wipe policy, TZ.md, v2 bans.

Non-goals: Play upload.

Acceptance: README states destructive schema.
```

#### Why this prompt

Политика wipe должна быть в README, не только в KDoc.

---

### Day 42 — Play-ready checklist, tag v1.0-android

#### Prompt (RU)

```
Роль: tech lead v1.0-android.

Цель: чеклист Play без обязательной публикации; CHANGELOG; тег только если пользователь просит git. Не начинать KMP.

Acceptance: DoD недели 6 ROADMAP.
```

#### Prompt (EN)

```
Role: tech lead v1.0-android.

Goal: Play checklist without mandatory publish; CHANGELOG; tag only if user asks. No KMP.

Acceptance: week-6 ROADMAP DoD.
```

#### Why this prompt

Финал трека: бан KMP явным.
