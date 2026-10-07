# TimeDial Planner — product specification (TZ)

Canon for weeks 3–6. Visual modes DIAL / PIZZA / PETALS / LIST already exist in `:app` and stay a **render layer** on top of day geometry. Do not re-scaffold pizza from scratch.

Product spec: [ROADMAP.md](ROADMAP.md). Daily prompts: [DAILY_PROMPTS.md](DAILY_PROMPTS.md). Agent rules: [AGENTS.md](../AGENTS.md).

---

## Русский

### Суть продукта

План дня — **циферблат**, не список. Задача может жить без часов, с несколькими отрезками, с тегами и важностью. Таймер только **добавляет** новый отрезок. Прошлое не переписывается.

### Домен

- **Task** — id, title, description, color, status (`TODO` / `IN_PROGRESS` / `DONE`), date экземпляра, **Importance**, **tags**, список **TimeBlock**, опционально правило повтора.
- **TimeBlock** — полуоткрытый интервал `[startMinute, endMinute)` в минутах суток. Пустой список блоков = задача **без времени**. Несколько блоков = несколько секторов с одним `TaskId` (спорт 08:00–09:00 и 12:00–13:00).
- **Importance** — `HIGH` / `MEDIUM` / `LOW`, веса **5 : 3 : 2**.
- **Tag** — несколько строк на задачу; не отдельный CRUD категорий на неделе 3.
- **TaskReport** (неделя 5) — текст + вложения (файлы в app storage, URI в Room). Не облако.

`startMinute`/`endMinute` на корне Task **не** единственный источник истины: углы и длительность считаются по блокам.

### Геометрия дня (timed + untimed)

На **текущей 12-часовой половине** (AM или PM):

1. Задачи **со временем** стоят на часах стены (civil clip, как сейчас).
2. Свободные минуты (бывший Rest) — **пул leftover**.
3. Задачи **без времени** делят leftover: доля `w_i / sum(w)` по весам важности.
4. Равная важность → равные доли (две HIGH на пустом leftover → 50/50). Три разные → **50% / 30% / 20%** leftover (целочисленные минуты; последний кусок добивает сумму).
5. «Отдых» рисуется только если после долей untimed ещё остались свободные минуты.
6. Если leftover половины = 0, безвременные **на этой половине не рисуются**.
7. На AM и PM **одни и те же веса**; размер сектора = доля × leftover **этой** половины.

Hide-completed (compact): leftover и доли считаются **после** исключения `DONE`. LIST: hide-completed — **фильтр**, без 360° reflow.

UI **не** считает доли и углы сам — только `TimeMath` и layout API в `kit.core.layout`.

### Неизменяемость прошлого

Закрытый блок (`endMinute` уже в прошлом относительно now) нельзя resize и нельзя править в форме. Новый отрезок только **append**.

### Повтор и копии (неделя 4)

- Перенос: меняет `date` экземпляра.
- Дубликат: новая задача с копией блоков и тегов.
- Повтор: ежедневно или выбранные дни недели; материализация экземпляра на видимый день. Без Google/Apple Calendar.

### Тап и таймер (неделя 4)

- Тап по сектору: задача **поверх** (z-order); облако с описанием у сектора.
- В **центре** кольца (оверлей и на пицце): Старт / Пауза.
- Старт: новый `TimeBlock` с `startMinute = now`.
- Пауза: `endMinute = now` у открытого блока. Статус `IN_PROGRESS`, пока идёт.
- Пример: уже был спорт 8–9; в 12 Старт, час работы, Пауза → блоки `[8:00–9:00]` и `[12:00–13:00]`. Первый не меняется.

### Хранилище

Room + DataStore. **Миграций Room нет:** `version++`, только `fallbackToDestructiveMigration()`. Обновление приложения на этапе разработки = **переустановка / очистка данных**. Не писать класс `Migration`.

### View modes

DIAL (кольцо), PIZZA (доли от центра), PETALS (лепестки), LIST (Compose, не Canvas). Новый режим = новый `DialRenderer`. LIST не вызывает factory.

### Запрещено в этом треке (v2)

KMP, iOS, Google/Apple Calendar, Maven Central, магазины как обязательный шаг, облачные отчёты.

---

## English (summary)

- Tasks may have **zero or many** `TimeBlock`s; one id, many sectors.
- **Untimed** tasks share each 12h face’s **leftover** (former Rest), weights HIGH:MEDIUM:LOW = **5:3:2** (50/30/20 when all three differ). Equal importance → equal shares. Rest paints only leftover after untimed. Timed tasks stay wall-clock.
- Past blocks are **immutable**; timer **appends** Start/Pause intervals.
- Tags on the task; recurrence/move/duplicate in week 4; reports + local media in week 5.
- **No Room migrations** — destructive version bump; reinstall/wipe until Play-ready.
- UI must not invent leftover math; use `kit.core.layout`.
- DIAL/PIZZA/PETALS/LIST already in the app; week 3 does not reimplement pizza from zero.
