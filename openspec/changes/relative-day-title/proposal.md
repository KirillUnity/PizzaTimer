# Proposal

## Why

The day screen titles the date strip with a short calendar pattern (`EEE, d MMM`), so the day a person is already on still reads as a date. Mission §6.3 keeps one front of the day in view; naming that front «Сегодня», and the adjacent days «Вчера» and «Завтра», makes the front obvious without parsing a date.

## What Changes

- On the day screen, the date-strip title is «Сегодня» when the visible date is local today, «Вчера» when it is the previous local date, and «Завтра» when it is the next local date. The default English catalog uses Today, Yesterday, and Tomorrow.
- Every other visible date keeps the current `EEE, d MMM` title.
- While the screen still follows today, a midnight advance keeps the title «Сегодня» because the visible date moves with local today. A date the user chose stays selected; its title is recomputed against the new local today.
- The Tasks date strip, the side panel titled Day / День, arrows, and the calendar picker stay as they are.

Assumption: the three words are title-cased (Сегодня, Вчера, Завтра), matching the headline style of the strip.

## Capabilities

### New Capabilities

### Modified Capabilities

- `day-dial`: the date-strip title on the day screen uses the relative day word for today, yesterday, and tomorrow. The shipped sentence that the day header title stays Day / День is narrowed to the side panel; the date strip already shows the formatted date, not that word.

## Impact

Day-screen title composition and string catalogs. No new dependency, no Room change, and no change to stored dates or to how the visible date is chosen.

Already shipped and unchanged: day arrows and the calendar, midnight follow-today (`DayViewModelTest.midnight_followsOpenToday`), AM/PM placement, the Day / День side panel, dial geometry, and the Tasks header.
