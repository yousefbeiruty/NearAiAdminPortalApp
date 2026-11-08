# NearAiAdminPortal — Screenshot Testing Guide

## Tool

**Compose Preview Screenshot Testing** (legacy standalone plugin `com.android.compose.screenshot` v0.0.1-alpha16).

This is a host-side screenshot tool that renders Compose `@Preview` functions using Android's LayoutLib — no emulator or device required. It runs fast on CI and locally.

## Why This Tool

- **No emulator needed** — runs on the build host JVM, making CI fast and reliable.
- **Native AGP integration** — dedicated Gradle tasks (`updateDebugScreenshotTest`, `validateDebugScreenshotTest`).
- **Compatible with AGP 9.4.1** — the project's AGP version.
- **Deterministic** — same input always produces the same pixel output on the same OS.
- **HTML diff report** — visual comparison with diff highlighting on failure.

## Quick Start

### Generate Baselines (First Time or After Intentional UI Change)

```bash
./gradlew updateDebugScreenshotTest
```

This renders every `@PreviewTest` composable and saves the PNG to:
```
app/src/screenshotTestDebug/reference/
```

### Validate Against Baselines

```bash
./gradlew validateDebugScreenshotTest
```

This re-renders every preview and compares pixel-by-pixel against the baseline. On mismatch, it fails the build and generates an HTML report.

### View the Report

On validation failure, open:
```
app/build/reports/screenshotTest/preview/debug/index.html
```

The report shows:
- **Reference** — the committed baseline
- **Actual** — the newly rendered screenshot
- **Diff** — pixel differences highlighted

## Workflow

### Adding a New Screenshot Test

1. Create a `@PreviewTest` composable in `app/src/screenshotTest/kotlin/com/nearai/admin/screenshot/`:

```kotlin
@PreviewTest
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "My Screen State")
@Composable
fun MyScreenStatePreview() {
    NearAiAdminTheme(darkTheme = false) {
        // Render the UI state
    }
}
```

2. Generate the baseline:
```bash
./gradlew updateDebugScreenshotTest
```

3. Review the generated PNG in `app/src/screenshotTestDebug/reference/`.

4. Commit both the `.kt` file and the baseline PNG.

### Updating a Baseline After an Intentional UI Change

1. Make the UI change in production code.
2. Run validation to see what changed:
```bash
./gradlew validateDebugScreenshotTest
```
3. Open the HTML report and review the diff.
4. If the change is intentional and correct, update the baseline:
```bash
./gradlew updateDebugScreenshotTest
```
5. **Review the new baseline** before committing. Never blindly accept all changes.
6. Commit the updated baseline PNG(s).

### What NOT to Do

- **Never** run `updateDebugScreenshotTest` in CI. Only `validateDebugScreenshotTest` runs in CI.
- **Never** accept all baseline changes without reviewing the diff. A visual change must be reviewable before its baseline is updated.
- **Never** use non-deterministic data (timestamps, random values) in preview composables.

## Conventions

| Setting | Value | Reason |
|---------|-------|--------|
| Dimensions | 390×844 dp | Representative phone (Pixel 5) |
| Theme | Light only | The app forces light theme |
| Font scale | Default (1.0) | Baseline; add 1.5x variant in Phase 2 |
| Locale | Default (en) | Baseline; add other locales in Phase 2 |
| Background | `showBackground = true` | Ensures consistent background color |

## Current Coverage

| Screen | States Covered |
|--------|---------------|
| Dashboard | Loading, Populated, Error, Empty |
| Orders List | Loading, Populated, Error, Empty |
| Order Details | Loading, Pending+OutForDelivery, Completed+Delivered, Error |

**Total: 12 screenshot baselines**

## CI Integration

Screenshot validation runs in the `build.yml` workflow after compilation:

```yaml
- name: Validate screenshot baselines
  run: ./gradlew validateDebugScreenshotTest
```

If validation fails, the build fails and the HTML report is uploaded as an artifact.

## Limitations

1. **Host-side rendering only.** LayoutLib rendering may differ slightly from device rendering (font metrics, anti-aliasing). This is acceptable for regression detection but not for pixel-perfect device validation.
2. **Cross-platform baseline differences.** Baselines were generated on MacOS. CI runs on `ubuntu-latest` (Linux). LayoutLib can produce pixel differences across operating systems. If CI validation fails on first run, baselines may need regeneration in a Linux environment using `./gradlew updateDebugScreenshotTest` on a Linux machine. No automatic baseline updates are configured in CI.
3. **No interaction testing.** Screenshot tests capture static renders. They cannot test animations, gestures, or state transitions. Use Compose UI behavior tests for that.
4. **TestDataFactory duplication.** Each test source set maintains its own copy of `TestDataFactory`. The `screenshotTest` source set is variant-specific (`screenshotTestDebug`) and cannot share code via Gradle's `sourceSets` API. Changes to test data must be applied to all copies.

## Future Improvements (Phase 2)

- Add font scale 1.5x variants for accessibility coverage.
- Add dark theme variants if the app supports dark mode.
- Add RTL layout variants.
- Refactor screens to accept `UiState` parameters so previews can render the actual production composables.
- Consider Dropshots for device-based screenshot validation.
