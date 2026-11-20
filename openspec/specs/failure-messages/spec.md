# failure-messages Specification

## Purpose

System-language copy when the day, the task list, or reports cannot finish a repository action, so a Russian locale does not see an English fallback.

## Requirements

### Requirement: Repository failures use a system-language resource
When saving, loading, copying, moving, resizing, deleting, timing, placing a plan, or collecting a report fails in the repository, the snackbar MUST show a string from resources (`values` and `values-ru`). The effect MUST carry a string resource id. The screen MUST resolve that id. The snackbar MUST NOT show `Exception.message`.

#### Scenario: Repository error emits a resource id
- **WHEN** an intent ends in a repository exception
- **THEN** the effect carries a string resource id and does not carry an English sentence
- **AND** `DayViewModelTest` locks this for a failed task update

#### Scenario: Load failure banner matches the resource
- **WHEN** loading the day or the task list fails in the repository
- **THEN** the on-screen error line uses the same load resource as the snackbar, not the exception text

### Requirement: Covered fallback copy
The system MUST provide a system-language string for each of these failures: day load, sample day, resize, task update, duplicate, move, timer, delete; task note, task load, task save, task delete, task duplicate, task move, move onto the diagram, place the plan; report collect, report note save.

#### Scenario: Each fallback exists in both locales
- **WHEN** the default and Russian string catalogs are read
- **THEN** each failure listed above has a named string in both catalogs

### Requirement: Planner refusals stay on their localized copy
Already localized planner refusals (empty goal, a day that has passed, an invalid work window, and service unavailable) MUST stay as they are. The system MUST NOT replace them with an English fallback.

#### Scenario: Offline collect still speaks the localized refusal
- **WHEN** collecting a report is refused because the planner service is unavailable
- **THEN** the snackbar shows the existing localized refusal
- **AND** `ReportsViewModelTest.collectToday_offlineTitle_keepsNotesAndDoesNotDeleteTasks` still expects that refusal

### Requirement: No in-app language picker
The system MUST choose the failure copy from the system locale. It MUST NOT add a language picker.

#### Scenario: Locale follows the system
- **WHEN** the device language is Russian
- **THEN** the failure snackbar uses the Russian catalog and settings do not offer a language switch
