---
sessionId: session-261105-094336-mga7
---

# Testing

## Testing Strategy

### Validation Approach
After each feature batch:
1. **Compile check**: Run `./gradlew :app:compileDebugKotlin` to verify no compilation errors.
2. **Resource reference check**: Verify all `R.string.xxx` references resolve correctly.
3. **Existing tests**: Run `./gradlew :app:testDebugUnitTest` to ensure no behavioral regressions.

### Key Scenarios
- **Auth flow**: Sign in, MFA, error messages display correctly.
- **Dashboard**: All section titles, labels, and status indicators render.
- **Orders**: List, details, status filters, and pagination counts.
- **Catalog**: Product list, create, details, categories.
- **Inventory**: Stock management, product editing.
- **Navigation**: Bottom nav labels, more menu items.
- **Empty/error states**: Loading, empty, and error screens display correctly.

### Edge Cases
- **Format strings**: Verify dynamic content (order IDs, counts, currency) renders correctly.
- **Plurals**: Test singular vs plural forms (1 order vs 5 orders).
- **ViewModel errors**: Verify validation errors still display after extraction.
- **Design system**: Verify shared components (`LoadingScreen`, `EmptyState`, `ErrorState`) still work with resource-backed strings.

### Test Changes
- **No new tests required** for string extraction itself — this is a mechanical refactoring.
- **Update existing tests** if they assert on specific string values (e.g., `AuthViewModelTest` may check error messages).
- **Add resource verification**: Optional — verify `strings.xml` is well-formed after each batch.

# Delivery Steps

### ✓ Step 1: Create string resource files for all features
Create comprehensive string resource files organized by feature, extracting all ~380+ hardcoded strings.
- Create `res/values/strings_common.xml` with shared strings (loading, retry, error, back, cancel, confirm, etc.)
- Create `res/values/strings_auth.xml` with auth feature strings (sign in, email, password, MFA, validation errors)
- Create `res/values/strings_dashboard.xml` with dashboard strings (titles, section headers, metric labels)
- Create `res/values/strings_orders.xml` with orders strings (list title, status labels, detail sections)
- Create `res/values/strings_catalog.xml` with catalog strings (products, categories, create/edit forms)
- Create `res/values/strings_inventory.xml` with inventory strings (stock, variants, movements)
- Create `res/values/strings_users.xml` with users strings (search, filters, actions)
- Create `res/values/strings_aiusage.xml` with AI usage strings (budget, quota, providers)
- Create `res/values/strings_auditlogs.xml` with audit log strings (events, filters, actions)
- Create `res/values/strings_discovery.xml` with discovery strings (analytics, traces, places)
- Create `res/values/strings_systemhealth.xml` with system health strings (services, SLA, controls)
- Create `res/values/strings_settings.xml` with settings strings (account, security, governance)
- Create `res/values/strings_navigation.xml` with navigation labels (bottom nav, more menu)
- Create `res/values/plurals.xml` with plural resources (orders, users, products, stock items)
- Use format arguments (`%1$s`, `%1$d`, `%1$.2f`) for dynamic content

### ✓ Step 2: Extract strings in auth, dashboard, and navigation features
Replace hardcoded strings with `stringResource()` in auth, dashboard, and navigation components.
- Update `AdminSignInScreen.kt`: ~40 strings (labels, placeholders, buttons, messages)
- Update `DashboardScreen.kt`: ~30 strings (titles, section headers, status labels)
- Update `BottomNavigationItem.kt`: 5 navigation labels
- Update `MoreScreen.kt`: ~5 strings
- Update `AuthViewModel.kt`: Change validation error messages to use string IDs or keep as backend-passed messages
- Update `AppError.kt`: Remove default user-facing messages or make them empty
- Verify compilation: `./gradlew :app:compileDebugKotlin`

### ✓ Step 3: Extract strings in orders and catalog features
Replace hardcoded strings with `stringResource()` in orders and catalog screens.
- Update `OrdersListScreen.kt`: ~20 strings (title, filters, status labels, empty states)
- Update `OrderDetailsScreen.kt`: ~30 strings (sections, labels, status, actions)
- Update `ProductsListScreen.kt`: ~15 strings (title, filters, tabs, empty states)
- Update `ProductCreateScreen.kt`: ~20 strings (form labels, placeholders, validation)
- Update `ProductDetailsScreen.kt`: ~30 strings (sections, labels, status, actions)
- Update `CategoriesListScreen.kt`: ~15 strings (title, labels, actions)
- Update `OrderComponents.kt` if it contains hardcoded strings
- Verify compilation: `./gradlew :app:compileDebugKotlin`

### ✓ Step 4: Extract strings in inventory, users, and AI usage features
Replace hardcoded strings with `stringResource()` in inventory, users, and AI usage screens.
- Update `InventoryScreen.kt`: ~30 strings (sections, labels, stock operations)
- Update `ProductEditScreen.kt`: ~20 strings (form labels, placeholders, actions)
- Update `UsersScreen.kt`: ~20 strings (search, filters, actions, detail sheet)
- Update `AiUsageScreen.kt`: ~25 strings (budget, quota, providers, telemetry)
- Verify compilation: `./gradlew :app:compileDebugKotlin`

### ✓ Step 5: Extract strings in remaining features and design system
Replace hardcoded strings in remaining features and shared design system components.
- Update `AuditLogsScreen.kt`: ~20 strings (filters, event types, actions)
- Update `BusinessAnalyticsScreen.kt`: ~15 strings (metrics, labels, sections)
- Update `SystemHealthScreen.kt`: ~15 strings (services, SLA, controls)
- Update `SettingsScreen.kt`: ~10 strings (sections, labels, actions)
- Update `States.kt`: Extract default messages ("Loading...", "Retry")
- Update `Dialogs.kt`: Extract dialog titles, messages, and button labels
- Update `Cards.kt`: Extract card labels and status text
- Update `Statuses.kt`: Extract status pill text
- Update `Inputs.kt`: Extract input labels and placeholders
- Update `Sections.kt`: Extract section headers
- Update `Charts.kt`: Extract chart labels
- Verify compilation: `./gradlew :app:compileDebugKotlin`

### ✓ Step 6: Run tests and produce audit report
Validate all changes, run tests, and produce the final audit report.
- Run full unit tests: `./gradlew :app:testDebugUnitTest`
- Verify no test failures related to string changes
- Update `AuthViewModelTest.kt` if it asserts on specific error message strings
- Verify all `R.string` references resolve correctly
- Produce `ADMIN_CODE_QUALITY_AUDIT.md` with findings, severity, affected files, and proposed fixes
- Produce consolidated `NEARAI_ENGINEERING_QUALITY_REPORT.md` section for Admin Portal