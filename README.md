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

### What `core:data` contains

| Package        | Purpose                                                                              |
|----------------|--------------------------------------------------------------------------------------|
| `mapper/`      | Extension functions — entity → domain model conversion                               |
| `repository/`  | Implementations of every repository interface defined in `core:domain`               |
| `di/`          | Hilt module — binds interfaces to implementations                                    |

---

## Architecture decisions

### Weather caching

Weather is fetched once per day and cached in Room using the date as the primary key. This is
intentional — weather serves as context on the Daily Intention screen, not as a real-time feed. Once
per day is sufficient for the use case and avoids unnecessary network calls. If multiple fetches per
day are needed in the future, the cache can be extended with a time period indicator (
morning/afternoon) as a composite primary key.

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

### `core:data` uses `android.library` not `kotlin.jvm`

`core:domain` is pure Kotlin and uses the `kotlin.jvm` plugin. `core:data` cannot
do the same because it uses Hilt for dependency injection. Hilt requires the Android
runtime, so any module that uses it must be an `android.library` module.

### Shared weather code mapping

The mapping from Open-Meteo weather codes to `WeatherCondition` enum values was
extracted from `WeatherResponseDto.toWeather()` into a standalone
`mapWeatherCodeToCondition(code: Int)` function in `core:network`. Both the network
mapper and the database cache mapper in `core:data` need the same logic — extracting
it avoids duplication without creating a new shared module or violating the existing
dependency direction.

### Analytics computed entirely in memory

`AnalyticsRepositoryImpl` fetches all sessions and check-ins and filters them in
memory per week rather than adding date-range queries to the DAOs. The dataset is
local-only and bounded — a user will never accumulate enough data to make this a
performance problem. Keeping the DAO layer simple and pushing computation into the
repository keeps the database layer clean and avoids over-engineering.

### `saveEveningReflection` fetches before upserting

`IntentionDao` uses `@Upsert` — saving a reflection for a date that already has
priorities would wipe those priorities if the entity were reconstructed from scratch.
`saveEveningReflection` fetches the existing entity first via a one-shot suspend
query, copies it with the new reflection, and then upserts. If no intention exists
for that date the operation is a no-op — there is nothing to reflect on.

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

| Module               | Status          |
|----------------------|-----------------|
| `:core:domain`       | ✅ Complete      |
| `:core:database`     | ✅ Complete      |
| `:core:network`      | ✅ Complete      |
| `:core:data`         | 🚧 In progress  |
| `:core:ui`           | ⬜ Not started   |
| `:feature:session`   | ⬜ Not started   |
| `:feature:habits`    | ⬜ Not started   |
| `:feature:analytics` | ⬜ Not started   |

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
