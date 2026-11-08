# NearAiAdminPortal — UI Testing Strategy

## Overview

This document defines the testing strategy for the NearAiAdminPortal Android admin application. It covers unit tests, Compose UI tests, and screenshot (visual regression) tests.

## Testing Pyramid

```
         ┌─────────────┐
         │  Screenshot  │  Visual regression — host-side, no emulator
         ├─────────────┤
         │  UI Tests    │  Compose behavior — instrumentation (future)
         ├─────────────┤
         │ Unit Tests   │  ViewModel, Repository, Mapper, DTO — local JVM
         └─────────────┘
```

### Current State (Phase 2C)

| Layer | Count | Location | Framework |
|-------|-------|----------|-----------|
| Unit Tests | 25 | `src/test/java` | JUnit4, MockK, Turbine, coroutines-test |
| Screenshot Tests | 12 previews | `src/screenshotTest/kotlin` | Compose Preview Screenshot Testing |
| UI Behavior Tests | 19 | `src/androidTest/java` | Compose UI Test JUnit4 |

## Test Categories & Commands

### 1. Unit Tests (Local JVM)

**What they test:** ViewModels, Repositories, Mappers, DTO serialization, domain logic.

**Location:** `app/src/test/java/com/nearai/admin/`

**Run all:**
```bash
./gradlew testDebugUnitTest
```

**Run a single test class:**
```bash
./gradlew testDebugUnitTest --tests "com.nearai.admin.feature.dashboard.presentation.DashboardViewModelTest"
```

**View results:**
```
app/build/reports/tests/testDebugUnitTest/index.html
```

**Conventions:**
- Use `MockK` for mocking dependencies (`@RelaxedMockK`, `coEvery`, `coVerify`).
- Use `Turbine` for testing `StateFlow` emissions.
- Use `runTest` with `UnconfinedTestDispatcher` for coroutine testing.
- Set `Dispatchers.setMain(testDispatcher)` in `@Before`.
- Tests mirror the main source structure: `src/test/java/com/nearai/admin/feature/<name>/`

### 2. Screenshot Tests (Host-Side Visual Regression)

**What they test:** Visual rendering of screen states — layout, colors, typography, spacing.

**Location:** `app/src/screenshotTest/kotlin/com/nearai/admin/screenshot/`

**Baselines:** `app/src/screenshotTestDebug/reference/`

**Generate baselines:**
```bash
./gradlew updateDebugScreenshotTest
```

**Validate against baselines:**
```bash
./gradlew validateDebugScreenshotTest
```

**View report on failure:**
```
app/build/reports/screenshotTest/preview/debug/index.html
```

**Conventions:**
- Each `@PreviewTest` composable renders one UI state (loading, populated, empty, error).
- All test data is deterministic — no random or time-based values.
- Screenshots use fixed dimensions: 390×844 dp (representative phone).
- Light theme only (the app forces light theme).
- See [SCREENSHOT_TESTING.md](./SCREENSHOT_TESTING.md) for the full workflow.

### 3. Compose UI Behavior Tests (Instrumentation)

**What they test:** User interactions — clicks, text input, navigation, state transitions.

**Location:** `app/src/androidTest/java/com/nearai/admin/`

**Current tests (19 total):**
- `DashboardScreenTest` — 5 tests (populated KPI, loading, error, retry callback, empty)
- `OrdersListScreenTest` — 6 tests (order card, click callback, loading, error, retry, empty)
- `OrderDetailsScreenTest` — 8 tests (pending+out_for_delivery independence, completed+delivered, loading, error, retry, populated info, cancelled, rejected)

**Infrastructure:**
- `CustomTestRunner` — extends `AndroidJUnitRunner` with `HiltTestApplication`.
- `HiltTestActivity` — empty `@AndroidEntryPoint ComponentActivity` for `createAndroidComposeRule`.
- `hilt-android-testing` dependency wired with `kspAndroidTest`.
- Each test type has its own `TestDataFactory` copy (no source-set wiring).

**Run on emulator or device:**
```bash
./gradlew :app:connectedDebugAndroidTest
```

**View results:**
```
app/build/reports/androidTests/connected/debug/index.html
```

## Dependency Injection in Tests

### Unit Tests

Construct ViewModels directly with mock dependencies:

```kotlin
@RelaxedMockK
private lateinit var repository: DashboardRepository

@Test
fun `example test`() = runTest {
    MockKAnnotations.init(this)
    Dispatchers.setMain(UnconfinedTestDispatcher())
    val viewModel = DashboardViewModel(repository)
    // ...
}
```

### Instrumented Tests

Use `@HiltAndroidTest` with `HiltAndroidRule`:

```kotlin
@HiltAndroidTest
class ExampleScreenTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }
}
```

### Replacing Bindings

Use `@TestInstallIn` to replace a production module for all tests in a source set:

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
abstract class TestNetworkModule {
    @Singleton
    @Binds
    abstract fun bindRepository(fake: FakeRepository): DashboardRepository
}
```

## Test Data

Deterministic test data lives in `TestDataFactory` with dedicated copies per source set:

- **Unit tests:** `app/src/test/java/com/nearai/admin/test/TestDataFactory.kt` (authoritative)
- **Instrumented tests:** `app/src/androidTest/java/com/nearai/admin/test/TestDataFactory.kt`
- **Screenshot tests:** `app/src/screenshotTest/kotlin/com/nearai/admin/test/TestDataFactory.kt`

All three copies are identical. They are maintained separately because the `screenshotTest` source set is variant-specific (e.g., `screenshotTestDebug`) and cannot be wired via Gradle's `sourceSets` API. The `androidTest` copy was initially shared via `sourceSets.getByName("androidTest") { java.srcDir("src/test/java") }`, but this approach was removed during Phase 2C because the `java.srcDir` API is deprecated and proved unreliable after Gradle configuration changes.

All values are fixed constants. Never use `Random`, `System.currentTimeMillis()`, or other non-deterministic sources in test data.

## CI Integration

The GitHub Actions workflow (`.github/workflows/build.yml`) runs:

1. **Gradle Wrapper Validation**
2. **Compile** — `compileDebugKotlin`
3. **Unit Tests** — `testDebugUnitTest`
4. **Screenshot Validation** — `validateDebugScreenshotTest`
5. **Lint** — `lintDebug`

Screenshot baselines are committed to the repository. CI validates against them. To update baselines, run `updateDebugScreenshotTest` locally, review the diff, and commit the new baselines.

## Source Set Layout

```
app/src/
├── main/                          # Production code
├── test/java/                     # Unit tests (local JVM)
│   └── com/nearai/admin/
│       ├── feature/*/             # ViewModel, Repository, Mapper tests
│       └── test/
│           └── TestDataFactory.kt  (authoritative)
├── test/resources/
│   └── robolectric.properties
├── androidTest/java/              # Instrumented tests (requires emulator)
│   └── com/nearai/admin/
│       ├── CustomTestRunner.kt
│       ├── HiltTestActivity.kt
│       ├── feature/*/             # Compose UI behavior tests
│       └── test/
│           └── TestDataFactory.kt  (dedicated copy)
└── screenshotTest/kotlin/         # Screenshot previews
    └── com/nearai/admin/
        ├── screenshot/
        │   ├── DashboardScreenScreenshotPreview.kt
        │   ├── OrdersListScreenScreenshotPreview.kt
        │   └── OrderDetailsScreenScreenshotPreview.kt
        └── test/
            └── TestDataFactory.kt  (dedicated copy)
```

## Phase 2C Audit Findings

### Source-set architecture decision

The initial approach used `sourceSets.getByName("androidTest") { java.srcDir("src/test/java") }` to share TestDataFactory between unit and instrumented tests. During Phase 2C this was evaluated and removed:

- **Problem:** The `java.srcDir` API is deprecated. After a temporary change to narrow the shared directory, the wiring stopped working entirely even after reverting — likely due to Gradle configuration cache or AGP internal changes.
- **Decision:** Each test type now has its own dedicated TestDataFactory copy. No source-set wiring.
- **Trade-off:** Three identical copies must be kept in sync manually. A `test-fixtures` Gradle module would eliminate duplication but is not justified for a single utility class.
- **ScreenshotTest limitation:** The `screenshotTest` source set is variant-specific (`screenshotTestDebug`) and cannot be accessed via `sourceSets.getByName("screenshotTest")` — it always throws `AndroidSourceSet not found`. A dedicated copy is the only working approach.

### Screenshot portability

Baselines were generated on MacOS. CI runs on `ubuntu-latest` (Linux). Compose Preview Screenshot Testing uses LayoutLib rendering, which can produce pixel differences across operating systems (font metrics, anti-aliasing). If CI validation fails on first run, baselines may need regeneration in a Linux environment. No automatic baseline updates are configured in CI.

### CI readiness for instrumented tests

The current CI workflow runs unit tests and screenshot validation but does not run instrumented tests. Adding emulator-based instrumented tests requires:

- **Option A: Android Emulator Runner** — A GitHub Actions runner with pre-booted emulators. Faster but requires a paid runner or self-hosted setup.
- **Option B: Gradle Managed Devices (GMD)** — Gradle provisions, boots, and tears down emulators. Free but adds ~5-10 minutes to CI for emulator cold boot.
- **Blocker:** Neither option is currently configured. The CI workflow would need significant changes (SDK setup, emulator image, ATD system image for headless CI). This is recommended for Phase 3 after the current foundation is stable.

### EGM skill installation status

The EGM skill (`egm-review-loop`) is installed but incomplete:
- **Present:** `SKILL.md`, `agents/openai.yaml`
- **Missing:** All reviewer persona files (`agents/structural.md`, `agents/implementation_readiness.md`, `agents/design_critic.md`, `agents/synthesizer.md`, etc.) and the `resources/document_standards/` directory.
- **Upstream source:** `google/elephant-goldfish-model@7bebca3fcc8203c333863f2bccd054b77bc6a2fa`
- **Resolution:** These files would need to be manually installed from the upstream repository. The skill cannot function without them.

## Known Limitations

1. **Instrumented tests not yet in CI.** The 19 Compose UI behavior tests require an emulator. CI currently runs only unit tests and screenshot validation. Adding instrumented tests to CI requires emulator setup (Gradle Managed Devices or Android Emulator Runner). Recommended for Phase 3.

2. **EGM review loop cannot run.** The EGM skill's reviewer persona files are missing from the installed skill. Only `agents/openai.yaml` exists. These files come from the upstream repository (google/elephant-goldfish-model) and are not installed. Review analysis was conducted inline during implementation.

3. **Screenshot tests are host-side only.** They use LayoutLib rendering, not a real device or emulator. This means they don't catch platform-specific rendering differences (e.g., font rendering on different Android versions). Baselines generated on MacOS may differ from Linux CI rendering.

4. **Screenshot and UI tests cover 3 screens only.** Dashboard, Orders List, and Order Details are covered. 14 additional screens remain without screenshot or UI behavior tests. See [SCREEN_COVERAGE_INVENTORY.md](./SCREEN_COVERAGE_INVENTORY.md) for the full matrix.
