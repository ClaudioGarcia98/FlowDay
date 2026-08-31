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

**Daily intention** — priorities (max 3 per day), evening reflection closes the loop. Accessed from the Home screen.

**Habits** — daily check-ins, streak tracking, visual history grid, WorkManager daily reminder notification.

**Analytics** — weekly dashboard with Compose Canvas charts. No third-party chart library.

### Design Principles

- Local-first, private by design
- Network is additive — app works fully offline; weather is context, not a dependency
- No cloud sync · No social features · No gamification · No AI · No calendar integration

---

## Tech Stack

| Area         | Technology                              | Version                                              |
|--------------|-----------------------------------------|------------------------------------------------------|
| Language     | Kotlin                                  | 2.3.21                                               |
| UI           | Jetpack Compose + Material 3            | BOM 2026.05.00                                       |
| Architecture | Clean Architecture + MVI + Multi-module | —                                                    |
| DI           | Hilt                                    | 2.59.2                                               |
| Async        | Coroutines + Flow                       | 1.11.0                                               |
| Database     | Room                                    | 2.8.4                                                |
| Background   | ForegroundService + WorkManager         | —                                                    |
| Widget       | Jetpack Glance                          | —                                                    |
| Network      | Retrofit + OkHttp + Kotlin Serialization| Retrofit 3.0.0 / OkHttp 4.12.0 / KSerialization 1.11.0 |
| Testing      | JUnit4 + MockK + Turbine + MockWebServer| —                                                    |
| CI           | GitHub Actions                          | —                                                    |
| AGP          | Android Gradle Plugin                   | 9.2.1                                                |
| KSP          | Kotlin Symbol Processing                | 2.3.7                                                |
| Min SDK      | —                                       | 26                                                   |
| Compile SDK  | —                                       | 36                                                   |
| Java         | —                                       | VERSION_17                                           |

---

## Module Structure

```
FlowDay/
├── app/               Entry point · MainActivity · NavHost
├── domain/            Pure Kotlin — zero Android dependency
├── database/          Room — entities · DAOs · TypeConverters · Hilt module
├── network/           Retrofit — Weather API · DTOs · caching · Hilt module
├── data/              Repository implementations · mappers · Hilt bindings
├── ui/                Design system · shared Compose components
└── feature/
    ├── session/        Focus timer screen
    ├── habits/         Habits screen
    └── analytics/      Analytics dashboard screen
```

### Gradle module paths

```
:app
:domain
:database
:network
:data
:ui
:feature:session
:feature:habits
:feature:analytics
```

### Dependency direction

```
:app → :feature:* → :ui
:app → :feature:* → :domain
:data → :domain + :database + :network
```

`:ui` depends on nothing. Features depend on `:ui` and `:domain`. `:app` wires everything together. Nothing depends on `:app`.

---

## Module Status

| Module               | Status          |
|----------------------|-----------------|
| `:domain`            | ✅ Complete      |
| `:database`          | ✅ Complete      |
| `:network`           | ✅ Complete      |
| `:data`              | ✅ Complete      |
| CI (GitHub Actions)  | ✅ Complete      |
| `:ui`                | 🔄 In progress  |
| `:feature:session`   | ⬜ Not started   |
| `:feature:habits`    | ⬜ Not started   |
| `:feature:analytics` | ⬜ Not started   |

---

## Screens & Navigation

### Bottom navigation destinations (4)

- **Home** — today's overview
- **Session** — focus timer
- **Habits** — daily check-ins + streaks
- **Analytics** — weekly dashboard

### Adaptive navigation

| Screen size | Component |
|-------------|-----------|
| Compact (phone) | `NavigationBar` (bottom) |
| Medium (tablet portrait / foldable) | `NavigationRail` (side) |
| Expanded (tablet landscape) | `NavigationDrawer` (side panel) |

Implemented using `WindowSizeClass`. Lives in `:ui/component/` as a shared component.

### Home screen

The home screen adapts to what the user has actually done — not the time of day:

- **No intentions set** → show weather + prompt to set intentions + quick start session + habit progress
- **Intentions set, no reflection** → show weather + intentions + prompt for evening reflection + quick start session + habit progress
- **Intentions set + reflection done** → show weather + intentions + reflection + focus summary + habit progress

Intention detail and evening reflection are accessed from the Home screen, not a separate nav destination.

### Intention design decision

Priorities are capped at 3 per day — enforced in `SaveIntentionUseCase`. This is intentional: the constraint forces the user to decide what actually matters, not dump everything into a list. FlowDay is not a todo app.

**v2 feature (planned):** When all 3 intentions are completed, the app unlocks an additional slot of 3. Rewards follow-through without removing the discipline of the limit.

---


### Session screen

- Large timer display (elapsed time)
- Session label input field — "What are you working on?"
- Three controls: reset (secondary), play/pause (primary blue), stop (secondary)
- Today's focus summary below — total time + session count

### Habits screen

- Today's progress bar at top — "X of Y habits done"
- Habit list — each card has emoji icon, name, streak, tap to check in
  - Checked state: filled blue circle with checkmark
  - Unchecked state: empty circle with border
- Monthly calendar section below the list
  - Checked days: filled blue circle
  - Partial days (some habits done): lighter blue circle
  - Today: outlined in blue
  - Navigation arrows to go between months

### Analytics screen

- Week selector with back/forward navigation
- Bar chart — focus hours per day of the week (Mon–Sun), best day highlighted in darker blue
- Two summary cards side by side — total focus time this week + best day
- Habit consistency ring — percentage of habits completed this week
- Streak list — each habit with current streak (🔥) and personal best

---

## :ui — In Progress

**Package:** `dev.flowday.ui`
**Plugin:** `android.library` + `kotlin.compose`

### Structure

```
dev.flowday.ui/
├── theme/
│   ├── Color.kt       — FlowDay color palette
│   ├── Type.kt        — Inter font family + typography scale
│   └── Theme.kt       — FlowDayTheme (dark + light, no dynamic color)
└── component/         — shared Compose components (adaptive nav, etc.)
```

### Font

Inter — static `.ttf` files, 4 weights:
- `inter_regular.ttf` — FontWeight.Normal (400)
- `inter_medium.ttf` — FontWeight.Medium (500)
- `inter_semibold.ttf` — FontWeight.SemiBold (600)
- `inter_bold.ttf` — FontWeight.Bold (700)

### Color palette

```kotlin
// Brand — updated to match app logo (richer, deeper blue)
Blue700 = Color(0xFF1A3FD4)   // dark primary container
Blue600 = Color(0xFF1E4FE8)   // light primary
Blue500 = Color(0xFF3B7DE8)   // mid accent
Blue400 = Color(0xFF60A5FA)   // dark primary
Blue200 = Color(0xFF93C5FD)   // lightest accent

// Neutrals
Neutral950 = Color(0xFF0A0A0F)
Neutral900 = Color(0xFF111118)
Neutral800 = Color(0xFF1C1C27)
Neutral700 = Color(0xFF2A2A38)
Neutral300 = Color(0xFFB0B0C8)
Neutral200 = Color(0xFFD4D4E8)
Neutral100 = Color(0xFFF0F0F8)
Neutral50  = Color(0xFFF8F8FC)

// Semantic
Success = Color(0xFF22C55E)
Warning = Color(0xFFF59E0B)
Error   = Color(0xFFEF4444)
```

### Theme

`FlowDayTheme` wraps `MaterialTheme` with custom color schemes for dark and light mode. Dynamic color disabled — FlowDay has a deliberate visual identity. Applied at the root in `MainActivity`.

Light scheme: `primary = Blue600`, `onPrimary = Neutral50`, `primaryContainer = Blue200`
Dark scheme: `primary = Blue400`, `onPrimary = Neutral950`, `primaryContainer = Blue700`

### Splash screen

- Library: `androidx.core:core-splashscreen`
- Icon: `ic_splash_logo` (Gemini-generated PNG, AI-generated original asset)
- Background: `#F8F8FC` (light) / `#0A0A0F` (dark) — adapts to device theme via `values/themes.xml` + `values-night/themes.xml`
- `installSplashScreen()` called before `super.onCreate()` in `MainActivity`
- Two XML themes: `Theme.FlowDay` (app theme) + `Theme.FlowDay.Splash` (splash only)
- Activity uses `Theme.FlowDay.Splash` in `AndroidManifest.xml`

### App icon

- Gemini-generated logo — flowing loops + upward arrow + sun burst, FlowDay blue palette
- Background removed via remove.bg for transparent PNG
- Generated all density variants via Android Studio Image Asset tool
- Adaptive icon with `#F8F8FC` background layer

### Key decisions

- No dynamic color — app has intentional visual identity
- Inter chosen for modern, readable feel matching "motivated, clean, relaxed, beautiful" mood
- Blue palette updated to richer, deeper blue to match logo energy and vibrancy
- `:ui` depends on nothing — no `:domain`, no `:data`, no other FlowDay module
- Font preview error in Android Studio is a known limitation with font resources in library modules — the app itself runs correctly
- Habits history uses monthly calendar instead of GitHub-style grid — universally understood without explanation
---

## :domain — Complete

**Package:** `dev.flowday.domain`
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
                    // NEVER stored in domain — cached in :database as WeatherCacheEntity

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
- Repository interfaces in `:domain`, implementations in `:data`
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

## :database — Complete

**Package:** `dev.flowday.database`
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
`fallbackToDestructiveMigration()` used during development — must be replaced with proper `Migration` objects before any public release.

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
    getAllCheckInsStream(): Flow<List<HabitCheckInEntity>>   // for analytics computation
    insertHabit(habit: HabitEntity): Long
    insertCheckIn(checkIn: HabitCheckInEntity)
    undoCheckIn(habitId: Long, date: String)
    deleteHabit(habitId: Long)

IntentionDao
    getIntentionForDate(date: String): Flow<DailyIntentionEntity?>
    getIntentionForDateOnce(date: String): DailyIntentionEntity?   // for saveEveningReflection
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
- Priorities as JSON string — acceptable for max 3 short strings
- Foreign key + CASCADE on HabitCheckIn — orphaned check-ins impossible
- Index on habitId — prevents full table scan on habit deletion
- Schema export enabled — stored at `database/schemas/.../1.json`, committed to git
- minSdk raised to 26 — required for java.time.Instant without desugaring
- `getAllCheckInsStream()` added to HabitDao — needed by AnalyticsRepositoryImpl to compute weekly habit completion rate without a per-habit query loop
- `getIntentionForDateOnce()` added to IntentionDao — needed by `saveEveningReflection` to fetch existing entity as a one-shot suspend call; the existing Flow query cannot be used inside a suspend function without collecting

### DAO Tests — All Passing

```
SessionDaoTest      — in-memory Room database · insert/query/delete/active session
HabitDaoTest        — habits + check-ins · cascade delete · undo check-in
IntentionDaoTest    — upsert behaviour · query by date
```

---

## :network — Complete

**Package:** `dev.flowday.network`
**Plugins:** `android.library` + `ksp` + `hilt` + `kotlin-serialization`

### Purpose

Fetches current weather from Open-Meteo and surfaces it as context on the Home screen. Demonstrates a full production-grade network layer for portfolio and interviews.

**Why weather?** Fits the local-first philosophy — network is additive, not required. The app works fully offline; weather is a nice-to-have context layer that forces real implementation of every network pattern interviewers look for.

**Why Open-Meteo?** Free, no API key, no account required. Real REST API with structured JSON responses.

### Structure

```
network/
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
// extracted as standalone function — shared with :data WeatherMapper
```

### NetworkModule

```kotlin
@Module @InstallIn(SingletonComponent::class)
object NetworkModule
    @Provides @Singleton provideOkHttpClient(): OkHttpClient   // HttpLoggingInterceptor.Level.BODY
    @Provides @Singleton provideRetrofit(okHttpClient): Retrofit  // base URL: api.open-meteo.com
    @Provides @Singleton provideWeatherApiService(retrofit): WeatherApiService
```

### Network Tests — All Passing

```
WeatherApiServiceTest (MockWebServer)
    returns weather response on success
    throws exception on network error
    throws exception on malformed JSON

WeatherMapperTest
    maps clear sky code correctly
    maps unknown code to UNKNOWN
    maps all condition codes correctly
```

---

## :data — Complete

**Package:** `dev.flowday.data`
**Plugins:** `android.library` + `hilt`

### Structure

```
data/
├── mapper/
│   ├── SessionMapper.kt
│   ├── HabitMapper.kt
│   ├── IntentionMapper.kt
│   └── WeatherMapper.kt
├── repository/
│   ├── SessionRepositoryImpl.kt
│   ├── HabitRepositoryImpl.kt
│   ├── IntentionRepositoryImpl.kt
│   ├── AnalyticsRepositoryImpl.kt
│   └── WeatherRepositoryImpl.kt
└── di/
    └── DataModule.kt              // @Binds abstract class
```

### Key Decisions

- `DataModule` uses abstract class with `@Binds` — more efficient than `@Provides` for interface binding
- `AnalyticsRepositoryImpl` uses `combine` across three Flows — sessions, check-ins, habits — computed in memory
- `WeatherRepositoryImpl` uses cache-first strategy — returns cached weather if today's cache exists, otherwise fetches from network
- `IntentionRepositoryImpl` uses Kotlin Serialization for JSON priorities
- `saveEveningReflection` fetches existing entity before upserting — prevents wiping priorities

### Tests — All Passing

```
SessionRepositoryImplTest (5 tests)
HabitRepositoryImplTest (7 tests)
IntentionRepositoryImplTest (5 tests)
WeatherRepositoryImplTest (4 tests)
AnalyticsRepositoryImplTest (6 tests)
```

---

## CI/CD

GitHub Actions runs on every push to `feature/claudio/**` and every PR targeting `develop` or `master`.

### What runs on every push

- Unit tests — `:domain`, `:network`, `:data`
- Debug build — confirms the full project compiles

### What does NOT run on CI

DAO tests (`:database` module) require an Android emulator. Run locally before pushing.

### Workflow file

`.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches:
      - 'feature/claudio/**'
      - develop
      - master
  pull_request:
    branches:
      - develop
      - master

jobs:
  unit-tests:
    name: Unit Tests + Build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle.kts', '**/libs.versions.toml') }}
      - run: chmod +x gradlew
      - run: ./gradlew :domain:test :network:test :data:test --stacktrace
      - run: ./gradlew assembleDebug --stacktrace
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: '**/build/reports/tests/'
```

---

## Production Readiness

Required before the app is considered portfolio-complete.

**ProGuard / R8** — `proguard-rules.pro` must include rules for Retrofit (keep service interfaces), Kotlin Serialization (keep serializable classes), and Hilt (handled by plugin, verify on release build).

**Baseline Profiles** — Improves startup time by pre-compiling critical code paths. Added in `:app` module using `androidx.profileinstaller`. Simple to add, strong signal to interviewers that you think about performance.

**Accessibility** — Content descriptions on all icon buttons and image components. Minimum touch target 48dp enforced via Compose semantics. Tested with TalkBack on device.

**README** — The README is what interviewers read before they read any code. Must include app description and screenshots, architecture diagram (module graph), key technical decisions and why, how to build and run, and known limitations / future improvements.

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
- `:domain` uses `kotlin.jvm` plugin — NOT `android.library`
- `:data` uses `android.library` — required by Hilt
- `:ui` uses `android.library` + `kotlin.compose` — Compose requires Android runtime

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
feature/claudio/domain-tests      → merged to develop
feature/claudio/core-database     → merged to develop
feature/claudio/core-network      → merged to develop
feature/claudio/start-data-module → merged to develop (includes CI setup)
feature/claudio/core-data         → merged to develop
```

### Current branch

```
feature/claudio/ui-module   ← in progress (screens designed, theme complete, splash screen done)
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
- Repository interfaces in `:domain`, implementations in `:data`
- DAOs return entities, never domain models
- Mappers in `:data` convert entity ↔ domain model
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
