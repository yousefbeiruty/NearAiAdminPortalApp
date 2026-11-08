# NearAiAdminPortal — Screen Coverage Inventory

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Covered |
| ⚠️ | Partially covered |
| ❌ | Not covered |

## Screen Coverage

| Screen / Route | Unit Tests (ViewModel) | Screenshot Tests | UI Behavior Tests | Notes |
|---------------|----------------------|-----------------|------------------|-------|
| **Dashboard** (`OverviewRoute`) | ✅ DashboardViewModelTest | ✅ 4 states | ❌ | Loading, Populated, Error, Empty |
| **Orders List** (`OrdersRoute`) | ✅ OrdersListViewModelTest | ✅ 4 states | ❌ | Loading, Populated, Error, Empty |
| **Order Details** (`OrderDetailsRoute`) | ✅ OrderDetailsViewModelTest | ✅ 3 states | ❌ | Loading, Pending+OutForDelivery, Completed+Delivered |
| **Users** (`UsersRoute`) | ✅ UsersViewModelTest | ❌ | ❌ | |
| **Categories** (`CategoriesRoute`) | ❌ | ❌ | ❌ | |
| **Products List** (`ProductsRoute`) | ❌ | ❌ | ❌ | |
| **Product Details** (`ProductDetailsRoute`) | ✅ ProductDetailsViewModelTest | ❌ | ❌ | |
| **Product Create** (`ProductCreateRoute`) | ❌ | ❌ | ❌ | |
| **Inventory** (`InventoryRoute`) | ✅ InventoryViewModelTest | ❌ | ❌ | |
| **Product Edit** (`ProductEditRoute`) | ❌ | ❌ | ❌ | |
| **Discovery / Analytics** (`DiscoveryRoute`) | ✅ BusinessAnalyticsViewModelTest | ❌ | ❌ | |
| **System Health** (`SystemHealthRoute`) | ✅ SystemHealthViewModelTest | ❌ | ❌ | |
| **Audit Logs** (`AuditLogsRoute`) | ✅ AuditLogsViewModelTest | ❌ | ❌ | |
| **AI Usage** (`AiUsageRoute`) | ✅ AiUsageViewModelTest | ❌ | ❌ | |
| **Auth / Sign In** (`LoginRoute`) | ✅ AuthViewModelTest | ❌ | ❌ | |
| **Settings** (`SettingsRoute`) | ❌ | ❌ | ❌ | |
| **More** (`MoreRoute`) | ❌ | ❌ | ❌ | |

## Summary

| Category | Count |
|----------|-------|
| Total screens | 17 |
| Screens with unit tests | 10 (59%) |
| Screens with screenshot tests | 3 (18%) |
| Screens with UI behavior tests | 0 (0%) |
| Screenshot baselines | 11 |

## Important UI States Still Missing

### Dashboard
- Pull-to-refresh state
- Multiple currency revenue display

### Orders List
- Filtered results (by status, fulfillment status)
- Pagination loading state
- Empty state with retry

### Order Details
- Rejected status
- Cancelled status
- Reject dialog open
- Confirmation dialog open
- Action loading state

### Other High-Priority Screens
- **Auth / Sign In** — Loading, Error, Success states
- **Product Details** — Loading, Populated, Error, Out of stock
- **Inventory** — Loading, Populated, Empty, Low stock highlights

## Priority for Future Coverage

### Phase 2 (High Priority)
1. Auth / Sign In — screenshot + behavior tests (login flow is critical)
2. Product Details — screenshot tests (key catalog screen)
3. UI behavior tests for Dashboard, Orders List, Order Details
4. Refactor screens to accept `UiState` parameters for direct testability

### Phase 3 (Medium Priority)
5. Inventory — screenshot tests
6. Discovery / Analytics — screenshot tests
7. System Health — screenshot tests
8. Audit Logs — screenshot tests

### Phase 4 (Lower Priority)
9. Product Create / Edit — screenshot + behavior tests
10. Categories / Products List — screenshot tests
11. Settings / More — screenshot tests
12. State restoration tests for all screens with `rememberSaveable`
