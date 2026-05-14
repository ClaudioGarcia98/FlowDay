# FlowDay

> Most productivity apps track what you did.  
> FlowDay starts with what you *intend* to do — then helps you follow through.
---

## What is FlowDay

Most productivity apps track what you did. FlowDay starts with what you *intend*
to do — then helps you follow through and shows you whether you actually did it
over time.

---

## Features

### Focus sessions

Start a timer when you sit down to do deep work. The timer keeps running even when
the app is closed or the phone is locked. Label each session. At the end of the day
see exactly how much time you actually focused.

### Daily intention

Every morning write your 1 to 3 priorities for the day. Not a todo list — intentions.
What actually matters today. In the evening do a quick review — did you follow through?
One line of reflection closes the loop between what you planned and what you did.

### Habits

A small set of daily habits you want to maintain. One tap to check in each day.
Streak tracking and a visual history grid show your consistency over weeks at a glance.
A daily notification reminds you to check in.

### Analytics

A weekly dashboard showing focus hours per day, habit completion consistency, your
best focus day, and your streaks. Drawn natively with Compose Canvas — no third-party
chart library.

---

## What it intentionally does NOT do

No cloud sync · No social features · No gamification · No AI · No calendar integration

Everything stays on your device. Private by design.

---

## Architecture

FlowDay follows Clean Architecture with strict unidirectional data flow.  
Dependencies only point inward — features depend on core, never the reverse.

### Why `core:domain` is pure Kotlin

`core:domain` has zero Android dependencies. This means every business rule —
use cases, validation logic — can be tested on the JVM in under 3 seconds without
an emulator.

The domain layer defines what the app needs through repository interfaces.  
The implementation of those interfaces lives in `core:data` — domain never knows
how data is fetched, only that it can be.

### What `core:domain` contains

| Package       | Purpose                                                                             |
|---------------|-------------------------------------------------------------------------------------|
| `model/`      | Pure Kotlin data classes — `FocusSession`, `Habit`, `DailyIntention`, `WeeklyStats` |
| `repository/` | Interfaces — contracts for data access, no implementation                           |
| `usecase/`    | Business rules — one class, one decision, one responsibility                        |

---

## Architecture decisions

### JSON storage for priorities

`DailyIntention` stores its `priorities` field as a JSON string in Room via a
TypeConverter. This is acceptable here because priorities are capped at 3 short
strings — the serialization overhead is negligible for this volume.

For larger or more complex datasets this approach would be reconsidered in favor
of a separate relational table to avoid performance and querying limitations of
JSON string storage.

### Database migrations

The app currently uses `fallbackToDestructiveMigration()` in the Room database builder.
This means if the schema changes and no migration is provided, Room drops and recreates
the database instead of crashing.

This is intentional during development — there are no real users and no data worth
preserving. Before any public release this will be replaced with explicit `Migration`
objects that preserve user data across schema versions.

---

## Testing approach

Every use case has unit tests covering all decision paths — success cases, failure
cases, and edge cases.

Mocks replace real implementations so tests run on the JVM with no Android dependency,
completing in under 5 seconds.

Each test covers one decision. Failure tests need no repository scripting — the
business rule fires before the repository is ever called.

The CI pipeline runs the full test suite on every push, making it impossible to merge
code that breaks existing behaviour.

> This approach doesn't eliminate all bugs — UI and integration issues still require
> manual testing — but it makes logic bugs in the domain layer practically impossible
> to ship.

## Testing Decisions

### DAO Tests — AndroidTest

**In-memory database**
DAO tests use `Room.inMemoryDatabaseBuilder` instead of a real database.
A real database persists to disk, meaning data from one test can leak into another
and cause false failures or false passes. In-memory database lives in RAM and is
destroyed after each test, so every test starts with a clean slate.

**`Flow.first()` instead of `collect()`**
DAO methods that return `Flow` are tested using `.first()` to collect a single emission.
`collect()` suspends forever — it keeps listening until the Flow is cancelled, which
would cause tests to hang indefinitely. `first()` collects one emission, cancels the
Flow, and returns immediately.

**`runTest` instead of `runBlocking`**
Coroutine-based tests use `runTest` from `kotlinx-coroutines-test`.
`runTest` handles virtual time and propagates uncaught exceptions correctly,
making it the right choice for testing suspend functions and Flows.

---

## Project status

| Module               | Status         |
|----------------------|----------------|
| `:core:domain`       | ✅ Complete     |
| `:core:database`     | ✅ Complete     |
| `:core:network`      | 🚧 In progress |
| `:core:data`         | ⬜ Not started  |
| `:core:ui`           | ⬜ Not started  |
| `:feature:session`   | ⬜ Not started  |
| `:feature:habits`    | ⬜ Not started  |
| `:feature:analytics` | ⬜ Not started  |

---

## Running locally

```bash
git clone https://github.com/ClaudioGarcia98/FlowDay.git
cd FlowDay
./gradlew :core:domain:test
```

**Requirements:** Android Studio Meerkat · JDK 17 · Android API 26+

---

## License

MIT