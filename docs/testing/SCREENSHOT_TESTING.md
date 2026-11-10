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

Baselines must be generated on **Linux** to match the CI environment (`ubuntu-latest`).

**Recommended: Use the CI workflow**

1. Push your UI changes to a branch.
2. Manually trigger the **"Update Screenshot Baselines"** workflow from the GitHub Actions tab.
3. The workflow generates baselines on Linux and commits them back to your branch.
4. Review the committed baseline changes in the resulting commit.

**Alternative: Local Linux environment**

If you have a Linux machine or container:

```bash
./gradlew :app:updateDevDebugScreenshotTest :app:updateProductionDebugScreenshotTest
```

This renders every `@PreviewTest` composable and saves the PNGs to:
```
app/src/screenshotTestDevDebug/reference/
app/src/screenshotTestProductionDebug/reference/
```

### Validate Against Baselines

```bash
./gradlew :app:validateDevDebugScreenshotTest :app:validateProductionDebugScreenshotTest
```

This re-renders every preview and compares pixel-by-pixel against the baseline. On mismatch, it fails the build and generates an HTML report.

### View the Report

On validation failure, open:
```
app/build/reports/screenshotTest/preview/debug/dev/index.html
app/build/reports/screenshotTest/preview/debug/production/index.html
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

2. Push your changes and trigger the **"Update Screenshot Baselines"** workflow on GitHub Actions.

3. Review the generated PNGs in `app/src/screenshotTestDevDebug/reference/` and `app/src/screenshotTestProductionDebug/reference/`.

4. Commit both the `.kt` file and the baseline PNGs (committed by the workflow).

### Updating a Baseline After an Intentional UI Change

1. Make the UI change in production code.
2. Run validation locally to see what changed:
```bash
./gradlew :app:validateDevDebugScreenshotTest :app:validateProductionDebugScreenshotTest
```
3. Open the HTML report and review the diff.
4. If the change is intentional and correct, push your changes and trigger the **"Update Screenshot Baselines"** workflow.
5. **Review the new baselines** in the committed changes. Never blindly accept all changes.
6. The workflow commits the updated baseline PNGs automatically.

### What NOT to Do

- **Never** run screenshot update tasks in the main CI build. Only validation runs in the regular `build.yml` workflow.
- **Never** accept all baseline changes without reviewing the diff. A visual change must be reviewable before its baseline is updated.
- **Never** use non-deterministic data (timestamps, random values) in preview composables.
- **Never** generate baselines on macOS and expect them to pass on Linux CI. Always use the CI workflow or a Linux environment.

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

Screenshot validation runs in the `build.yml` workflow after compilation, targeting both flavors:

```yaml
- name: Validate screenshot baselines (dev and production)
  run: ./gradlew :app:validateDevDebugScreenshotTest :app:validateProductionDebugScreenshotTest
```

If validation fails, the build fails and the HTML report is uploaded as an artifact.

### Updating Baselines in CI

Baselines are regenerated using the separate `update-screenshot-baselines.yml` workflow, which is triggered manually (`workflow_dispatch`). This workflow:

1. Runs on `ubuntu-latest` (Linux) to match the validation environment.
2. Generates baselines for both `dev` and `production` flavors.
3. Commits the updated baselines back to the branch.

Never trigger this workflow from the main `build.yml` — baseline updates must be explicit and reviewable.

## Limitations

1. **Host-side rendering only.** LayoutLib rendering may differ slightly from device rendering (font metrics, anti-aliasing). This is acceptable for regression detection but not for pixel-perfect device validation.
2. **Baselines must match the CI OS.** Baselines are generated on Linux (`ubuntu-latest`) via the `update-screenshot-baselines.yml` workflow. macOS-generated baselines will not match Linux CI validation due to font rendering, anti-aliasing, and color rendering differences. Always use the CI workflow to regenerate baselines after UI changes.
3. **No interaction testing.** Screenshot tests capture static renders. They cannot test animations, gestures, or state transitions. Use Compose UI behavior tests for that.
4. **TestDataFactory duplication.** Each test source set maintains its own copy of `TestDataFactory`. The `screenshotTest` source set is variant-specific and cannot share code via Gradle's `sourceSets` API. Changes to test data must be applied to all copies.
5. **Flavor-specific baselines.** Each product flavor (`dev`, `production`) has its own baseline directory. Both must be updated when UI changes affect screenshot tests.

## Future Improvements (Phase 2)

- Add font scale 1.5x variants for accessibility coverage.
- Add dark theme variants if the app supports dark mode.
- Add RTL layout variants.
- Refactor screens to accept `UiState` parameters so previews can render the actual production composables.
- Consider Dropshots for device-based screenshot validation.
