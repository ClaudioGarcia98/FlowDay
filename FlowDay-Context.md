# FlowDay — Full Project Context

## Developer

- **Name:** Cláudio
- **GitHub:** ClaudioGarcia98
- **Background:** 2 years .NET MAUI + 6 months backend
- **Goal:** Mid-to-senior native Android developer job in 2026

---

## App Description

FlowDay is a local-first personal productivity app built around three daily questions:

- **Morning:** what are my 1–3 priorities today?
- **During the day:** am I actually focusing?
- **Evening:** did I do what I said I would?

### Features

**Focus sessions** — foreground timer that runs in background, label sessions, persistent notification with pause/stop actions.

**Daily intention** — morning priorities (max 3), evening reflection closes the loop. Shows current weather context (temperature + condition) fetched once per day via Open-Meteo and cached locally in Room.

**Habits** — daily check-ins, streak tracking, visual history grid, WorkManager daily reminder notification.

**Analytics** — weekly dashboard with Compose Canvas charts. No third-party chart library.

### Design Principles

- Local-first, private by design
- Network is additive — app works fully offline; weather is context, not a dependency
- No cloud sync · No social features · No gamification · No AI · No calendar integration

---

## Tech Stack

| Area | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.3.21 |
| UI | Jetpack Compose + Material 3 | BOM 2026.05.00 |
| Architecture | Clean Architecture + MVI + Multi-module | — |
| DI | Hilt | 2.59.2 |
| Async | Coroutines + Flow | 1.11.0 |
| Database | Room | 2.8.4 |
| Background | ForegroundService + WorkManager | — |
| Widget | Jetpack Glance | — |
| Network | Retrofit + OkHttp + Kotlin Serialization | Retrofit 3.0.0 / OkHttp 4.12.0 / KotlinX Serialization 1.11.0 |
| Testing | JUnit4 + MockK + Turbine + MockWebServer | — |
| CI | GitHub Actions | — |
| AGP | Android Gradle Plugin | 9.2.1 |
| KSP | Kotlin Symbol Processing | 2.3.7 |
| Min SDK | — | 26 |
| Compile SDK | — | 36 |
| Java | — | VERSION_17 |

---

## Module Structure

```
FlowDay/
├── app/                          Entry point · MainActivity · NavHost
├── core/
│   ├── domain/                   Pure Kotlin — zero Android dependency
│   ├── database/                 Room — entities · DAOs · TypeConverters · Hilt module
│   ├── network/                  Retrofit — Weather API · DTOs · caching · Hilt module
│   ├── data/                     Repository implementations · mappers · Hilt bindings
│   └── ui/                       Design system · shared Compose components
└── feature/
    ├── session/                  Focus timer screen
    ├── habits/                   Habits screen
    └── analytics/                Analytics dashboard screen
```

---

## Module Status

| Module | Status |
|---|---|
| `:core:domain` | ✅ Complete |
| `:core:database` | ✅ Complete |
| `:core:network` | ✅ Complete |
| `:core:data` | 🚧 In progress |
| `:core:ui` | ⬜ Not started |
| `:feature:session` | ⬜ Not started |
| `:feature:habits` | ⬜ Not started |
| `:feature:analytics` | ⬜ Not started |

---

## core:domain — Complete

**Package:** `dev.flowday.core.domain`
**Plugin:** `id("org.jetbrains.kotlin.jvm")` — pure Kotlin, zero Android imports

### Models

```kotlin
FocusSession        // id, startedAt (Instant), endedAt (Instant?), durationSeconds, label
                    // computed: isActive = endedAt == null

Habit               // id, name, iconKey, currentStreak, longestStreak
                    // NO streak fields stored — computed from HabitCheckIn at runtime

HabitCheckIn        // id, habitId, date (LocalDate), completedAt

DailyIntention      // id, date, priorities (List<String> max 3), eveningReflection, createdAt
                    // computed: hasEveningReview = eveningReflection.isNotBlank()

WeeklyStats         // weekStart, totalFocusSeconds, sessionCount, habitCompletionRate (Float), bestFocusDay (LocalDate?)
                    // NEVER stored — computed at runtime from sessions + check-ins

Weather             // temperature (Double), condition (WeatherCondition)
                    // NEVER stored in domain — cached in core:database as WeatherCacheEntity

WeatherCondition    // enum: CLEAR_SKY, PARTLY_CLOUDY, FOG, RAIN, SNOW, SHOWERS, THUNDERSTORM, UNKNOWN
```

### Repository Interfaces

```kotlin
SessionRepository
    fun getSessionsStream(): Flow<List<FocusSession>>
    fun getTodaySessionsStream(): Flow<List<FocusSession>>
    suspend fun getActiveSession(): FocusSession?
    suspend fun startSession(label: String = ""): Long
    suspend fun endSession(sessionId: Long)
    suspend fun deleteSession(sessionId: Long)

HabitRepository
    fun getHabitsStream(): Flow<List<Habit>>
    fun getCheckInsForDate(date: LocalDate): Flow<List<HabitCheckIn>>
    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>
    suspend fun createHabit(name: String, iconKey: String): Long
    suspend fun checkIn(habitId: Long, date: LocalDate)
    suspend fun undoCheckIn(habitId: Long, date: LocalDate)
    suspend fun deleteHabit(habitId: Long)

IntentionRepository
    fun getIntentionForDate(date: LocalDate): Flow<DailyIntention?>
    suspend fun savePriorities(date: LocalDate, priorities: List<String>)
    suspend fun saveEveningReflection(date: LocalDate, reflection: String)

AnalyticsRepository
    fun getWeeklyStatsStream(weeksBack: Int = 8): Flow<List<WeeklyStats>>
    fun getTotalFocusSecondsStream(): Flow<Long>

WeatherRepository
    fun getWeather(latitude: Double, longitude: Double): Flow<Weather?>
```

### Use Cases

```
Session:
    StartSessionUseCase       — checks no active session exists, returns Result<Long>
    EndSessionUseCase         — wraps endSession in runCatching, returns Result<Unit>
    GetTodayFocusTimeUseCase  — filters completed sessions, sums durationSeconds, returns Flow<Long>
    GetActiveSessionUseCase   — returns FocusSession?

Habit:
    CheckInHabitUseCase       — validates date is not in the future, returns Result<Unit>

Intention:
    SaveIntentionUseCase      — validates 1-3 priorities, no blanks, trims whitespace
```

### Key Decisions

- Use cases return `Result<T>` — failure visible in type signature, not hidden exceptions
- `operator fun invoke()` on all use cases — callable like functions
- Repository interfaces in domain, implementations in `core:data`
- WeeklyStats and streak computed at runtime — never stored in database
- `@Inject constructor` on use cases — `javax.inject:javax.inject:1` dependency

### Tests — All Passing

```
StartSessionUseCaseTest
    fails when a session is already active
    does not call startSession when one is already active
    succeeds and returns session id when no active session

EndSessionUseCaseTest
    succeeds when repository completes without error
    returns failure when repository throws

GetTodayFocusTimeUseCaseTest
    returns total focus time for today
    emits zero when no sessions today
    emits zero when all sessions are active
    emits sum of all sessions when none are active

CheckInHabitUseCaseTest
    fails when date is in the future
    succeeds when habit and date are correct
    succeeds when date is in the past

SaveIntentionUseCaseTest
    fails when priorities list has more than 3 items
    fails when priorities list is empty
    fails when any priority is blank
    succeeds with 1 priority
    succeeds with exactly 3 priorities
    trims whitespace before saving
```

---

## core:database — Complete

**Package:** `dev.flowday.core.database`
**Plugins:** `android.library` + `ksp` + `hilt` + `room`

### Entities

```kotlin
FocusSessionEntity          table: focus_sessions
    id: Long (autoGenerate)
    startedAtEpochSecond: Long
    endedAtEpochSecond: Long?   // null = session still active
    durationSeconds: Long
    label: String

HabitEntity                 table: habits
    id: Long (autoGenerate)
    name: String
    iconKey: String
    createdAtEpochSecond: Long

HabitCheckInEntity          table: habit_check_ins
    id: Long (autoGenerate)
    habitId: Long               // FK → habits.id CASCADE DELETE
    dateIso: String             // "2025-05-12"
    completedAtEpochSecond: Long
    // index on habitId
    // unique index on (habitId, dateIso) — enforces one check-in per habit per day at DB level

DailyIntentionEntity        table: daily_intentions
    id: Long (autoGenerate)
    dateIso: String
    prioritiesJson: String      // JSON array ["A", "B", "C"]
    eveningReflection: String
    createdAtEpochSecond: Long

WeatherCacheEntity          table: weather_cache
    dateIso: String (PrimaryKey) // "2025-05-15" — one entry per day
    temperature: Double
    weatherCode: Int
```

### Database Version

Current version: **4**

Version 4 added `WeatherCacheEntity` for weather caching.

`fallbackToDestructiveMigration()` is used in `DatabaseModule` during development — acceptable while there are no real users. Must be replaced with proper `Migration` objects before any public release to avoid wiping user data on schema changes.

### DAOs

```kotlin
SessionDao
    getSessionsStream(): Flow<List<FocusSessionEntity>>
    getTodaySessionStream(startOfDayEpoch: Long): Flow<List<FocusSessionEntity>>
    getSessionsInRangeStream(startEpoch: Long, endEpoch: Long): Flow<List<FocusSessionEntity>>
    getActiveSession(): FocusSessionEntity?
    insertSession(session: FocusSessionEntity): Long
    endSession(sessionId: Long, endTime: Long)
    deleteSession(sessionId: Long)

HabitDao
    getHabitsStream(): Flow<List<HabitEntity>>
    getCheckInsForDate(date: String): Flow<List<HabitCheckInEntity>>
    getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckInEntity>>
    getAllCheckInsStream(): Flow<List<HabitCheckInEntity>>   // added for analytics computation
    insertHabit(habit: HabitEntity): Long
    insertCheckIn(checkIn: HabitCheckInEntity)
    undoCheckIn(habitId: Long, date: String)
    deleteHabit(habitId: Long)

IntentionDao
    getIntentionForDate(date: String): Flow<DailyIntentionEntity?>
    getIntentionForDateOnce(date: String): DailyIntentionEntity?   // added for saveEveningReflection
    getIntentionsInRangeStream(startDateIso: String, endDateIso: String): Flow<List<DailyIntentionEntity>>
    upsertIntention(intention: DailyIntentionEntity)
    deleteIntention(intention: DailyIntentionEntity)

WeatherDao
    getWeatherCache(): Flow<WeatherCacheEntity?>
    insertWeather(weather: WeatherCacheEntity)   // @Upsert
```

### TypeConverters

```kotlin
// util/TypeConverters.kt
// Uses java.time.Instant — requires minSdk 26
@TypeConverter fun convertLongToInstant(value: Long?): Instant?
@TypeConverter fun convertInstantToLong(value: Instant?): Long?
```

### FlowDayDatabase

```kotlin
@Database(entities = [...all five...], version = 4)
@TypeConverters(TypeConverters::class)
abstract class FlowDayDatabase : RoomDatabase()
    abstract fun sessionDao(): SessionDao
    abstract fun habitDao(): HabitDao
    abstract fun intentionDao(): IntentionDao
```

### DatabaseModule (Hilt)

```kotlin
// di/DatabaseModule.kt
@Module @InstallIn(SingletonComponent::class)
object DatabaseModule
    @Provides @Singleton provideDatabase(context): FlowDayDatabase
    @Provides provideSessionDao(db): SessionDao
    @Provides provideHabitDao(db): HabitDao
    @Provides provideIntentionDao(db): IntentionDao
```

### Key Decisions

- Timestamps as epoch seconds (Long) — TypeConverter handles Instant conversion
- Dates as ISO strings ("2025-05-12") — avoids timezone complexity
- Priorities as JSON string — acceptable for max 3 short strings, documented in README
- Foreign key + CASCADE on HabitCheckIn — orphaned check-ins impossible
- Index on habitId — prevents full table scan on habit deletion
- Schema export enabled — stored at `database/schemas/.../1.json`, committed to git
- minSdk raised to 26 — required for java.time.Instant without desugaring
- `getAllCheckInsStream()` added to HabitDao — needed by AnalyticsRepositoryImpl to compute weekly habit completion rate across all habits without a per-habit query loop
- `getIntentionForDateOnce()` added to IntentionDao — needed by `saveEveningReflection` to fetch the existing entity as a one-shot suspend call so priorities can be preserved during an upsert; the existing Flow query cannot be used inside a suspend function without collecting

### DAO Tests — In Progress

```
SessionDaoTest      — in-memory Room database · insert/query/delete/active session
HabitDaoTest        — habits + check-ins · cascade delete · undo check-in
IntentionDaoTest    — upsert behaviour · query by date
```

---

## core:network — Complete

**Package:** `dev.flowday.core.network`
**Plugins:** `android.library` + `ksp` + `hilt` + `kotlin-serialization`

### Purpose

Fetches current weather from Open-Meteo and surfaces it as context on the Daily Intention screen. Demonstrates a full production-grade network layer for portfolio and interviews.

**Why weather?** Fits the local-first philosophy — network is additive, not required. App works fully offline; weather is a nice-to-have context layer that forces real implementation of every network pattern interviewers look for.

**Why Open-Meteo?** Free, no API key, no account required. Real REST API with structured JSON responses.

### Structure

```
core/network/
├── api/
│   └── WeatherApiService.kt        // Retrofit interface
├── dto/
│   └── WeatherResponseDto.kt       // WeatherResponseDto + WeatherCurrentDto
├── mapper/
│   └── WeatherMapper.kt            // WeatherResponseDto.toWeather() + mapWeatherCodeToCondition()
├── di/
│   └── NetworkModule.kt            // Hilt — provides OkHttpClient, Retrofit, WeatherApiService
└── NetworkResult.kt                // sealed class: Loading / Success<T> / Error
```

### DTOs

```kotlin
@Serializable
data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    val current: WeatherCurrentDto
)

@Serializable
data class WeatherCurrentDto(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weathercode") val weatherCode: Int
)
```

### NetworkResult

```kotlin
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}
```

### Mapper

```kotlin
fun WeatherResponseDto.toWeather(): Weather
// delegates condition mapping to mapWeatherCodeToCondition()

fun mapWeatherCodeToCondition(code: Int): WeatherCondition
// 0 → CLEAR_SKY, 1-3 → PARTLY_CLOUDY, 45-48 → FOG,
// 51-67 → RAIN, 71-77 → SNOW, 80-82 → SHOWERS,
// 95-99 → THUNDERSTORM, else → UNKNOWN
// extracted as standalone function — shared with core:data WeatherMapper
```

### NetworkModule

```kotlin
@Module @InstallIn(SingletonComponent::class)
object NetworkModule
    @Provides @Singleton provideOkHttpClient(): OkHttpClient   // HttpLoggingInterceptor.Level.BODY
    @Provides @Singleton provideRetrofit(okHttpClient): Retrofit  // base URL: api.open-meteo.com
    @Provides provideWeatherApiService(retrofit): WeatherApiService
```

### Caching strategy

- Weather fetched once per day maximum
- Cached in Room (`WeatherCacheEntity` with `dateIso` as primary key)
- On app open: serve cache if fetched today, otherwise fetch fresh
- If fetch fails and cache exists: serve stale cache silently
- If fetch fails and no cache: emit null, app continues normally

### Key Decisions

- `mapWeatherCodeToCondition()` extracted from `WeatherResponseDto.toWeather()` into a standalone package-level function — `core:data` needs the same mapping logic for `WeatherCacheEntity → Weather` conversion; extracting avoids duplication without creating a shared module outside the existing architecture

### Tests — All Passing

```
WeatherMapperTest
    maps temperature correctly
    maps condition CLEAR_SKY correctly
    maps condition PARTLY_CLOUDY correctly
    maps condition FOG correctly
    maps condition RAIN correctly
    maps condition SNOW correctly
    maps condition SHOWERS correctly
    maps condition THUNDERSTORM correctly
    maps condition UNKNOWN correctly

WeatherApiServiceTest
    returns correct data in a 200 status code
    returns error on 500 response
    returns UNKNOWN condition on malformed JSON
```

---

## core:data — In Progress

**Package:** `dev.flowday.core.data`
**Plugins:** `android.library` + `ksp` + `hilt` + `kotlin-serialization`

### Purpose

Implements the repository interfaces defined in `core:domain`. Sits between the domain layer and the data sources (`core:database`, `core:network`). Contains mappers to convert entities and DTOs to domain models.

### Dependencies

- `:core:domain` — repository interfaces and domain models
- `:core:database` — DAOs and entities
- `:core:network` — WeatherApiService and mapWeatherCodeToCondition

### Structure

```
core/data/
├── di/
│   └── DataModule.kt               // Hilt — binds repository interfaces to implementations
├── mapper/
│   ├── SessionMapper.kt            // FocusSessionEntity → FocusSession
│   ├── HabitMapper.kt              // HabitEntity → Habit · HabitCheckInEntity → HabitCheckIn
│   ├── IntentionMapper.kt          // DailyIntentionEntity → DailyIntention
│   └── WeatherMapper.kt            // WeatherCacheEntity → Weather
└── repository/
    ├── SessionRepositoryImpl.kt
    ├── HabitRepositoryImpl.kt
    ├── IntentionRepositoryImpl.kt
    ├── AnalyticsRepositoryImpl.kt
    └── WeatherRepositoryImpl.kt
```

### Mappers

```kotlin
// SessionMapper.kt
fun FocusSessionEntity.toFocusSession(): FocusSession
// Long → Instant via Instant.ofEpochSecond()
// endedAt nullable — uses ?.let { Instant.ofEpochSecond(it) }

// HabitMapper.kt
fun HabitEntity.toHabit(): Habit
// createdAtEpochSecond dropped — not in domain model
// currentStreak and longestStreak hardcoded to 0 — computed at runtime, never stored

fun HabitCheckInEntity.toHabitCheckIn(): HabitCheckIn
// dateIso String → LocalDate via LocalDate.parse()
// completedAtEpochSecond Long → Instant via Instant.ofEpochSecond()

// IntentionMapper.kt
fun DailyIntentionEntity.toDailyIntention(): DailyIntention
// id hardcoded to 0L — dateIso is the entity primary key, domain model has id: Long = 0
// prioritiesJson String → List<String> via Json.decodeFromString()
// dateIso String → LocalDate via LocalDate.parse()

// WeatherMapper.kt
fun WeatherCacheEntity.toWeather(): Weather
// weatherCode Int → WeatherCondition via mapWeatherCodeToCondition() from core:network
```

### Repository Implementations

```kotlin
SessionRepositoryImpl(private val sessionDao: SessionDao)
// getSessionsStream — Flow map + toFocusSession()
// getTodaySessionStream — calculates startOfDayEpoch via LocalDate.now().atStartOfDay(ZoneOffset.UTC)
// getActiveSession — nullable entity mapped with ?.toFocusSession()
// startSession — builds FocusSessionEntity with Instant.now().epochSecond, inserts via DAO
// endSession — calls DAO with Instant.now().epochSecond as endTime
// deleteSession — delegates directly to DAO

HabitRepositoryImpl(private val habitDao: HabitDao)
// LocalDate → String via date.toString() for all DAO calls
// createHabit — builds HabitEntity with Instant.now().epochSecond
// checkIn — builds HabitCheckInEntity with date.toString() and Instant.now().epochSecond
// undoCheckIn — delegates to DAO with date.toString()

IntentionRepositoryImpl(private val intentionDao: IntentionDao)
// savePriorities — serializes List<String> to JSON via Json.encodeToString()
// saveEveningReflection — fetches existing entity via getIntentionForDateOnce(),
//   copies with new reflection via .copy(), returns early if no existing intention

AnalyticsRepositoryImpl(private val sessionDao: SessionDao, private val habitDao: HabitDao)
// getWeeklyStatsStream — combines three Flows: sessions + checkIns + habits
//   weekStarts computed outside combine — (0 until weeksBack).map { today.minusWeeks(it).with(MONDAY) }
//   sessions and checkIns filtered per week by epoch range and LocalDate range respectively
//   habitCompletionRate = weekCheckIns.size / (habits.size * 7), guarded against empty habits
//   bestFocusDay — groupBy DayOfWeek, sumOf durationSeconds, maxByOrNull, weekStart.with(bestDayOfWeek)
// getTotalFocusSecondsStream — filters completed sessions, sumOf durationSeconds

WeatherRepositoryImpl(private val weatherDao: WeatherDao, private val weatherApiService: WeatherApiService)
// serves cache if dateIso == today
// on miss: fetches from network, caches result, returns domain model
// on network failure: returns stale cache or null
```

### Key Decisions

- `android.library` plugin instead of `kotlin.jvm` — Hilt requires the Android runtime; any module using Hilt must be an Android library module
- No Room, Retrofit, or Serialization direct dependencies in `build.gradle.kts` — those are encapsulated in `core:database` and `core:network`; `core:data` only depends on the module, not the libraries directly. Serialization added as an exception because `IntentionRepositoryImpl` serializes priorities to JSON directly
- `LocalDate.toString()` for date → String conversion — produces ISO format `"2025-05-12"` by default, matching the format stored in the database
- `Instant.now().epochSecond` for timestamps — consistent with how entities store time throughout the project
- Streak fields hardcoded to 0 in `HabitMapper` — streaks are computed at runtime from check-ins, never stored; the mapper has no access to check-in history
- `id = 0L` in `IntentionMapper` — `DailyIntentionEntity` uses `dateIso` as primary key; the domain model has `id: Long = 0` as a default, so 0L is the correct value to pass
- `combine` with three Flows in `AnalyticsRepositoryImpl` — analytics computation requires sessions, check-ins, and habit count simultaneously; `combine` re-emits whenever any source changes, keeping the dashboard reactive
- All check-ins fetched in memory for analytics rather than adding a range query to `HabitDao` — dataset is local-only and bounded; acceptable for this use case, avoids over-engineering the DAO layer
- `saveEveningReflection` fetches existing entity before upserting — preserves priorities written earlier in the day; without this, upserting with empty priorities would wipe the morning intention
- `result ?: return` early exit in `saveEveningReflection` — if no intention exists for the date, there is nothing to add a reflection to; silently doing nothing is the correct behaviour

---

## Production Readiness

Required before the app is considered portfolio-complete.

### ProGuard / R8

`proguard-rules.pro` must include rules for:
- Retrofit — keep service interfaces
- Kotlin Serialization — keep serializable classes
- Hilt — handled by plugin, verify on release build

### Baseline Profiles

- Improves startup time by pre-compiling critical code paths
- Added in `:app` module using `androidx.profileinstaller`
- Simple to add, strong signal to interviewers that you think about performance

### Accessibility

- Content descriptions on all icon buttons and image components
- Minimum touch target — 48dp enforced via Compose semantics
- Tested with TalkBack on device

### README

The README is what interviewers read before they read any code. Must include:
- App description and screenshots
- Architecture diagram (module graph)
- Key technical decisions and why
- How to build and run
- Known limitations / future improvements

---

## Gradle Setup

### libs.versions.toml — key entries

```toml
[versions]
agp = "9.2.1"
kotlin = "2.3.21"
ksp = "2.3.7"
room = "2.8.4"
hilt = "2.59.2"
kotlinxCoroutines = "1.11.0"
mockk = "1.14.9"
turbine = "1.2.1"
composeBom = "2026.05.00"
retrofit = "3.0.0"
okhttp = "4.12.0"
kotlinxSerializationJson = "1.11.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
jetbrains-kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### Critical Gradle Notes

- AGP 9.0+ has Kotlin built in — `kotlin-android` plugin causes conflicts, do NOT add it
- Root `build.gradle.kts` declares all plugins with `apply false`
- Submodules apply plugins without version
- `ksp()` used for Room compiler and Hilt compiler — not `implementation()`
- `core:domain` uses `kotlin.jvm` plugin — NOT `android.library`
- `core:data` uses `android.library` — required by Hilt

### gradle.properties — performance settings

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.daemon=true
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=50
```

---

## Git Workflow

**Repository:** github.com/ClaudioGarcia98/FlowDay

### Branch strategy

```
master    ← production · protected · PRs only · no direct push
develop   ← pre-production · protected · PRs only
feature/claudio/[name]  ← all work happens here
```

### Commit convention

```
feat:      new feature or module
fix:       bug fix
test:      adding or fixing tests
docs:      README or documentation
chore:     dependencies, config, maintenance
refactor:  code restructure without behaviour change
```

### Completed branches / PRs

```
feature/claudio/domain-tests   → merged to develop
feature/claudio/core-database  → merged to develop
feature/claudio/core-network   → merged to develop
```

### Current branch

```
feature/claudio/core-data   ← in progress
```

---

## Testing Approach

- MockK for mocking repository interfaces — never mock concrete classes
- Turbine for testing Flow emissions — `awaitItem()` + `awaitComplete()`
- `runTest` for all coroutine tests
- `every` for regular functions returning Flow
- `coEvery` for suspend functions
- `coVerify` to verify specific arguments were passed
- In-memory Room database for DAO tests — no mocking, real SQL queries
- MockWebServer for network tests — real HTTP responses, no mocking Retrofit
- Pattern: Arrange → Act → Assert with blank lines between
- Failure tests need no `coEvery` — business rule fires before repository called
- Success tests need `coEvery` — execution reaches the repository

---

## Architecture Principles

- Domain layer = pure Kotlin, no Android, testable on JVM
- Repository interfaces in domain, implementations in `core:data`
- DAOs return entities, never domain models
- Mappers in `core:data` convert entity ↔ domain model
- Use cases contain decisions — if no decision, call repository directly from ViewModel
- `Result<T>` for operations that can fail — failure visible in type
- `NetworkResult<T>` for network operations — Loading / Success / Error
- `Flow` for reactive streams, `suspend` for one-shot operations
- Computed values never stored — derive at runtime
- Each module has one job and one job only

---

## Teaching Approach

- Questions before every new concept
- Cláudio answers in plain English before writing any code
- Cláudio writes all code himself — no copy-paste
- Mistakes corrected with explanation of why, not just what
- README updated alongside code
- Each module committed and PR'd before moving to next
- Understanding verified before moving forward
