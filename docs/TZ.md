# TimeDial Planner — product specification (TZ)

Canon for weeks 3–6. Visual modes DIAL / PIZZA / PETALS / LIST already exist in `:app` and stay a **render layer** on top of day geometry. Do not re-scaffold pizza from scratch.

Product spec: [ROADMAP.md](ROADMAP.md). Improvements: [ROADMAP_IMPROVE.md](ROADMAP_IMPROVE.md). Daily prompts: [DAILY_PROMPTS.md](DAILY_PROMPTS.md). Agent rules: [AGENTS.md](../AGENTS.md).

---

## Русский

### Суть продукта

План дня — **циферблат**, не список. Задача может жить без часов, с несколькими отрезками, с тегами и важностью. Таймер только **добавляет** новый отрезок. Прошлое не переписывается.

Человек приходит за двумя вещами. Первая — ясный порядок: что делать сейчас и что сразу после. Вторая — чтобы по этому порядку его вёл переживание самого дела. Серии, очки, стыд, «ты отстаёшь», красное кольцо и повторный сигнал в день не входят. Минуты захода только показывают, где этот шаг кончается. Один короткий звук на границе шага остаётся.

Круг может быть полон дел. В работу входит одно. Это ремесло внимания из миссии (§6.3) и очередь из §2.1.

### Внимание и один фронт

- **Фронт дня.** На видимом дне можно отметить одно дело фронтом. Пока заход не идёт, Play и отверстие смотрят на него. Старт другого дела сменяет фронт на это дело. Второго открытого рабочего захода нет.
- **Живая нить.** Между заходами на деле хранится одна строка «что дальше». Она видна, когда это дело выбрано и интервал не открыт. Новый заход строку снимает. Пауза лист с этой строкой не открывает.
- **Перерыв — отход.** После паузы в отверстии «Перерыв» и тихая строка, что можно отойти. Практики остаются в боковом меню. Шаги, сон и пульс день не считает. Отдельного блокнота нет: память дня — круг, текст дела, цели вне захода и эта строка.
- **Край умения.** Если у дела нет конца на круге, заход предлагает 25 минут, шаг 15 минут. Если похожие завершения уже показывают лёгкий навык и низкую вовлечённость, один раз предлагается сузить следующее такое дело. Наград за согласие нет. Закрытые отрезки не переписываются.

Проверка очереди: открыть день и увидеть одно текущее дело, не собирая его из круга. Начать заход — на круге нет сводки, второго дела, красного кольца и слов про отставание. Дойти до края шага — один звук, без повтора. Пауза показывает «Перерыв» и не открывает лист. Завершить и назвать следующее дело — день предлагает его дальше. Поставить строку «что дальше», уйти и вернуться — строка на месте; Play её снимает.

### Пакет разбора без сети

Приложение не вызывает модель. Человек собирает файл сам и относит его агенту сам. Обратно приходит файл предложений, не дамп базы.

- **Наружу.** Человек выбирает дни и поля. По умолчанию: названия, минуты, важность, оценки, пометки «вызов / помеха», названия целей. Свободный текст — только если его включили. Сон, пульс и сырые строки хранилища не входят. В файле есть инструкция агенту: схема ответа, очередь дня, закрытые отрезки не трогать, кнута не предлагать.
- **Обратно.** Каждое предложение — карточка: что замечено, что изменится, галочка. «Применить» пишет только отмеченное. «Отклонить» запоминается. Неизвестная операция, SQL и сдвиг закрытого отрезка отвергаются целиком.
- **Что можно предложить.** Совет на деле без сдвига минут; новые дела как следующие шаги со связью «из какого дела»; заметка о паттерне без правки круга; свой курс названием и шагами текста; фронт будущего дня. Закрытый `TimeBlock` не меняется.

Проверка: выгрузить выбранные дни, ничего не отправив из приложения. Вернуть файл с двумя предложениями, отметить одно, отклонить второе. Отмеченное видно в дне. Отклонённое не появилось. Закрытые минуты те же. Повтор того же отклонённого предложения не встаёт как новое.

### Домен

- **Task** — id, title, description, color, status (`TODO` / `IN_PROGRESS` / `DONE`), date экземпляра, **Importance**, **tags**, список **TimeBlock**, опционально правило повтора.
- **TimeBlock** — полуоткрытый интервал `[startMinute, endMinute)` в минутах суток. Пустой список блоков = задача **без времени**. Несколько блоков = несколько секторов с одним `TaskId` (спорт 08:00–09:00 и 12:00–13:00).
- **Importance** — `HIGH` / `MEDIUM` / `LOW`, веса **5 : 3 : 2**.
- **Tag** — несколько строк на задачу; не отдельный CRUD категорий на неделе 3.
- **TaskReport** — текст на **неделе 5**; вложения (файлы в app storage, URI в Room) на **неделе 6**. Не облако.

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

Закрытый блок (`endMinute` уже в прошлом относительно now) нельзя resize и нельзя править в форме. Новый отрезок только **append**. Незакрытый отрезок в редакторе можно сдвинуть по времени и убрать. На одном календарном дне **civil-периоды разных дел (и двух блоков одного дела) не пересекаются**; безвременные по-прежнему делят leftover. Дубликат с тем же временем на тот же день не сохраняется.

### Повтор и копии (неделя 4)

- Перенос: меняет `date` экземпляра.
- Дубликат: новая задача с копией тегов; блоки копируются только если они не пересекают уже занятые часы этого дня.
- Повтор: ежедневно или выбранные дни недели; материализация экземпляра на видимый день. Без Google/Apple Calendar.

### Тап и таймер (неделя 4)

- Тап по сектору: задача **поверх** (z-order); облако с описанием у сектора. Повторный тап по выбранному сектору **снимает** выбор.
- В **центре** кольца (оверлей и на пицце): иконки Play / Pause, без слов «Старт» и «Пауза».
- Старт: новый `TimeBlock` с `startMinute = now`.
- Пауза: `endMinute = now` у открытого блока и **перерыв** (песочный клин). В отверстии подпись «Перерыв». Лист «где остановился» с этой кнопки не открывается.
- Пример: уже был спорт 8–9; в 12 Старт, час работы, Пауза → блоки `[8:00–9:00]` и `[12:00–13:00]`. Первый не меняется.
- Стартовый пример дня (Sleep / Deep work / Lunch / Gym) **не** вставляется.

### Список задач

- Вкладки «Задачи» не показывает клинья перерыва (`#break`) и не предлагает этот тег как фильтр.
- Долгое нажатие включает выбор нескольких дел; короткое нажатие в этом режиме ставит и снимает отметку. Удаление спрашивает подтверждение и снимает выбранные дела вместе с их отрезками и связанными перерывами.

### Хранилище

Room + DataStore. **Миграций Room нет:** `version++`, только `fallbackToDestructiveMigration()`. Обновление приложения на этапе разработки = **переустановка / очистка данных**. Не писать класс `Migration`.

### View modes

DIAL (кольцо), PIZZA (доли от центра), PETALS (лепестки), LIST (Compose, не Canvas). Новый режим = новый `DialRenderer`. LIST не вызывает factory.

### Запрещено в этом треке (v2)

KMP, iOS, Google/Apple Calendar, Maven Central, магазины как обязательный шаг, облачные отчёты, ключ модели внутри приложения, автоприменение предложений, сырой SQL из файла агента.

---

## English (summary)

- The person gets a sequence: this step, then one next step. The sitting is pulled by the experience of the work. Streaks, points, shame, “you are behind”, a red ring, and a repeated chime stay out. The chosen minutes and one sound mark where the step ends.
- The dial may hold many tasks. One work interval is open at a time. One task may be the day’s front; starting another replaces that front. Finishing names the next action, and the day offers that action next.
- Between sittings a task keeps one “what’s next” line, shown when that task is selected and idle. The next start clears it. Pause does not open that sheet.
- A break is a step away: hole caption «Перерыв» plus a quiet leave-the-screen line. Practices stay in the side menu. No pedometer, sleep, heart rate, or second notebook.
- Review leaves the device as a file the person assembled (days and fields they chose). It returns as typed proposals, not a database dump. Each proposal is a checkbox. Apply writes only the checked ones. Closed blocks stay immutable. Unknown operations, raw SQL, and a model key inside the app stay out.
- A sitting with no dial end offers 25 minutes, step 15. A one-time “narrow the next” nudge may appear when similar completions are easy and flat. Closed blocks stay immutable. No reward for accepting.
- Tasks may have **zero or many** `TimeBlock`s; one id, many sectors.
- **Untimed** tasks share each 12h face’s **leftover** (former Rest), weights HIGH:MEDIUM:LOW = **5:3:2** (50/30/20 when all three differ). Equal importance → equal shares. Rest paints only leftover after untimed. Timed tasks stay wall-clock.
- Past blocks are **immutable**; timer **appends** Start/Pause intervals. Unclosed periods can be edited or removed. Same-day civil periods must not overlap.
- Pause starts a **break** wedge; the hole caption is «Перерыв»; Play/Pause are **icons**.
- The tasks tab hides break rows, allows **multi-select delete**, and does not seed a sample day.
- Tags on the task; recurrence/move/duplicate in week 4; report **text** in week 5; local media in week 6.
- **No Room migrations** — destructive version bump; reinstall/wipe until Play-ready.
- UI must not invent leftover math; use `kit.core.layout`.
- DIAL/PIZZA/PETALS/LIST already in the app; week 3 does not reimplement pizza from zero.
