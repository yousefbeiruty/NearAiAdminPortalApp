# NearAI — Android Naming Audit Report

**Date:** 2026-11-15
**Auditor:** Junie (Automated Audit)
**Scope:** NearAiAdminPortal (Admin Android) — full production code; NearAiDescovery (Customer Android) — referenced from prior audit
**Status:** Complete

---

## 1. Executive Summary

This report presents the findings of a comprehensive naming consistency and code readability audit focused on the two NearAI Android projects. The audit examined production source code for naming anti-patterns, Kotlin convention compliance, and cross-platform business terminology consistency with the Backend and Web Admin projects.

**Overall Assessment:** The Admin Android codebase demonstrates **strong naming discipline**. Kotlin conventions are followed correctly, domain terminology is clear, and the clean architecture pattern is well-maintained. The most significant findings are **type-safety gaps** (String where enums should be used) and **one instance-level UPPER_SNAKE_CASE val anti-pattern**. No CRITICAL naming defects remain open — the `carrier`/`courier_name` API contract mismatch was resolved in a prior workstream.

**Key Findings at a Glance:**

| Category | Count | Severity |
|----------|-------|----------|
| Naming convention defects | 4 | 2 MEDIUM, 2 LOW |
| Type-safety issues (String where enum should be) | 2 | 1 MEDIUM, 1 LOW |
| Domain-modeling issues | 1 | MEDIUM |
| Cross-platform terminology + semantic mismatch | 1 | HIGH |
| Compatibility-sensitive changes | 1 | HIGH |

**Total Findings:** 9 (0 CRITICAL open, 2 HIGH, 4 MEDIUM, 3 LOW)

**Final Verdict:** `APPROVED WITH CONDITIONS`

**Previously Resolved:**
- The `carrier`/`courier_name` API contract mismatch (CRITICAL) was **fixed** in Workstream A. The Android `ShipmentDto` now correctly uses `@SerialName("courier_name")`. See `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md` for evidence.

---

## 2. Project Inspection Confirmation

| # | Project | Path | Language | Production Files Inspected | Status |
|---|---------|------|----------|---------------------------|--------|
| 1 | Admin Android | `NearAiAdminPortal` | Kotlin | 102 production `.kt` files (full scan) | ✓ Inspected |
| 2 | Customer Android | `NearAiDescovery` | Kotlin | Not accessible from this workspace | ✗ Not accessible — findings referenced from prior audit |

**Important:** The Customer Android repository (`NearAiDescovery`) is located outside the current Junie workspace. Its 4 Kotlin files were audited in the prior cross-project audit (`NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`). Findings for Customer Android in this report are referenced from that audit, not from fresh inspection.

---

## 3. Audit Coverage by Project and Module

### 3.1 Admin Android (`NearAiAdminPortal`) — Full Inspection

**Total production Kotlin files:** 102

**Scanned packages:**
- `com.nearai.admin/` — `MainActivity.kt`, `NearAiAdminApplication.kt`
- `com.nearai.admin.core.common/` — `Result.kt`, `AppError.kt`
- `com.nearai.admin.core.designsystem/` — `components/`, `theme/`
- `com.nearai.admin.core.network/` — `NetworkModule.kt`, `CoilModule.kt`
- `com.nearai.admin.core.security/` — `SessionManager.kt`, `TokenRefreshManager.kt`, `TokenStorage.kt`
- `com.nearai.admin.feature.aiusage/` — data, domain, presentation (3 files)
- `com.nearai.admin.feature.auditlogs/` — data, domain, presentation (6 files)
- `com.nearai.admin.feature.auth/` — data, domain, presentation (7 files)
- `com.nearai.admin.feature.catalog/` — data, domain, presentation (10 files)
- `com.nearai.admin.feature.dashboard/` — data, domain, presentation (6 files)
- `com.nearai.admin.feature.discovery/` — data, domain, presentation (6 files)
- `com.nearai.admin.feature.inventory/` — data, domain, presentation (5 files)
- `com.nearai.admin.feature.more/` — presentation (1 file)
- `com.nearai.admin.feature.orders/` — data, domain, presentation (12 files)
- `com.nearai.admin.feature.settings/` — presentation (1 file)
- `com.nearai.admin.feature.systemhealth/` — data, domain, presentation (7 files)
- `com.nearai.admin.feature.users/` — data, domain, presentation (7 files)
- `com.nearai.admin.navigation/` — `AppNavHost.kt`, `AppDestination.kt`

**Searches performed:**
- UPPER_SNAKE_CASE instance vals: 2 findings (ProductDetailsViewModel)
- Generic identifiers (`data`, `value`, `result`, `item`, `temp`, `obj`): 2 findings (QuotaSafeguard, ProductKpi)
- Ambiguous abbreviations (`qty`, `amt`, `desc`, `addr`, `cnt`, `idx`): 0 findings
- Ambiguous booleans: all clear and contextual
- String-typed status fields where enums should exist: 3 findings
- `@SerialName` annotations vs backend API: all match (carrier fix verified)
- Key domain files reviewed: `OrderStatus.kt`, `OrderRepository.kt`, `CatalogStatus.kt`, `DashboardRepository.kt`, `BusinessAnalyticsRepository.kt`, `AiUsageRepository.kt`, `SystemHealthRepository.kt`, `UsersRepository.kt`, `AuditLogsRepository.kt`
- Key mappers reviewed: `OrderMapper.kt`, `CatalogMapper.kt`, `DashboardMapper.kt`, `BusinessAnalyticsMapper.kt`, `AuditLogsMapper.kt`, `UsersMapper.kt`, `SystemHealthMapper.kt`
- All ViewModels reviewed for naming patterns

### 3.2 Customer Android (`NearAiDescovery`) — Referenced from Prior Audit

**Files:** 4 Kotlin files (skeleton project)
**Package:** `com.yousef.nearaidescovery`
**Findings:** Package naming includes developer name and contains typo (see Finding C-1)
**Source:** `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`, Finding C-1

---

## 4. Findings by Project

### 4.1 Admin Android — Findings

#### Finding AA-1: UPPER_SNAKE_CASE Instance Val `MAX_IMAGE_FILE_SIZE`

| Field | Value |
|-------|-------|
| **Priority** | MEDIUM |
| **File** | `feature/catalog/presentation/ProductDetailsScreen.kt:305` |
| **Context** | Inside `ProductDetailsViewModel` class (instance-level) |
| **Current** | `private val MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L` |
| **Proposed** | Move to `companion object` as `const val MAX_IMAGE_FILE_SIZE = 10_240_000L` (preferred) or rename to `maxImageFileSize` |
| **Reason** | UPPER_SNAKE_CASE implies a compile-time constant (`const val` or `companion object` val). This is an instance-level `private val` inside a ViewModel. Kotlin convention dictates lowerCamelCase for instance properties. The value IS conceptually constant, so moving it to a `companion object` would make UPPER_SNAKE_CASE appropriate without renaming. The previous cross-project audit incorrectly stated "no instance-level UPPER_SNAKE_CASE vals" — this finding corrects that. |
| **Contract Risk** | None — private property, only referenced within the same class |

#### Finding AA-2: UPPER_SNAKE_CASE Instance Val `ALLOWED_IMAGE_MIME_TYPES`

| Field | Value |
|-------|-------|
| **Priority** | MEDIUM |
| **File** | `feature/catalog/presentation/ProductDetailsScreen.kt:310` |
| **Context** | Inside `ProductDetailsViewModel` class (instance-level) |
| **Current** | `private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")` |
| **Proposed** | Move to `companion object` as `val ALLOWED_IMAGE_MIME_TYPES = setOf(...)` (preferred) or rename to `allowedImageMimeTypes` |
| **Reason** | Same issue as AA-1. UPPER_SNAKE_CASE for an instance-level `private val` violates Kotlin naming conventions. `setOf()` cannot be a `const val`, but placing it in a `companion object` would make UPPER_SNAKE_CASE conventionally appropriate as a class-level constant. |
| **Contract Risk** | None — private property |

#### Finding AA-3: `PaymentInfo.status` — Type-Safety Issue (String Instead of Enum)

| Field | Value |
|-------|-------|
| **Priority** | MEDIUM |
| **Category** | **Type-Safety Issue** (not a naming defect) |
| **File** | `feature/orders/domain/OrderRepository.kt:92` |
| **Current** | `val status: String` |
| **Proposed** | `val status: PaymentStatus` (create `PaymentStatus` enum) |
| **Reason** | This is not a naming problem — the property name `status` is correct. The issue is that the type is `String` when a `PaymentStatus` enum already exists in the backend with 6 values (PENDING, COMPLETED, COLLECTED, FAILED, CANCELLED, REFUNDED). Using String loses compile-time type safety and allows invalid values. |
| **Contract Risk** | None — internal domain model, mapper handles conversion |

#### Finding AA-4: `StatusHistoryEntry.statusType` — Domain-Modeling Issue (String Instead of Enum)

| Field | Value |
|-------|-------|
| **Priority** | MEDIUM |
| **Category** | **Domain-Modeling Issue** (not a naming defect) |
| **File** | `feature/orders/domain/OrderRepository.kt:77` |
| **Current** | `val statusType: String` (comment: `"order" or "fulfillment"`) |
| **Proposed** | `val statusType: StatusHistoryType` (create enum: `ORDER`, `FULFILLMENT`) |
| **Reason** | This is not a naming problem — the property name `statusType` is correct. The issue is that only two valid values exist (`"order"` or `"fulfillment"`), yet both backend and Android use a plain String. The backend validates via `__post_init__`. Android should create an enum for type safety even though the backend does not have one. |
| **Contract Risk** | None — internal domain model |

#### Finding AA-5: Generic `value` Property in `QuotaSafeguard` (Deferrable)

| Field | Value |
|-------|-------|
| **Priority** | LOW |
| **File** | `feature/aiusage/domain/AiUsageRepository.kt:50` |
| **Current** | `val value: String` |
| **Proposed** | `val displayValue: String` or `val quotaValue: String` |
| **Reason** | `value` is too generic — does not convey what the value represents in a quota safeguard context. **Note:** This is a placeholder model for an unimplemented API — the field may change when the real API is available. Renaming can be deferred until the real data model is known. |
| **Contract Risk** | None — internal domain model; feature not yet implemented |

#### Finding AA-6: Generic `value` Property in `ProductKpi`

| Field | Value |
|-------|-------|
| **Priority** | LOW |
| **File** | `feature/inventory/presentation/InventoryScreen.kt:75` |
| **Current** | `val value: String` |
| **Proposed** | `val displayValue: String` or `val metricValue: String` |
| **Reason** | `value` is too generic in a KPI context — does not convey what metric is displayed |
| **Contract Risk** | None — internal UI model |

#### Finding AA-7: `RecentOrder.status` — Type-Safety Issue (String Instead of Enum)

| Field | Value |
|-------|-------|
| **Priority** | LOW |
| **Category** | **Type-Safety Issue** (not a naming defect) |
| **File** | `feature/dashboard/domain/DashboardRepository.kt:20` |
| **Current** | `val status: String` |
| **Proposed** | `val status: OrderStatus` |
| **Reason** | This is not a naming problem — the property name `status` is correct. The issue is that `OrderStatus` enum already exists in `feature/orders/domain/OrderStatus.kt` but `RecentOrder` uses `String`, losing type safety for a dashboard summary. The dashboard is a lower-priority consumer than the orders feature, so this is LOW. |
| **Contract Risk** | None — internal domain model |

**Admin Android Convention Assessment:**

| Convention | Status | Notes |
|-----------|--------|-------|
| Classes: PascalCase | ✓ Compliant | All classes and interfaces follow PascalCase |
| Properties: lowerCamelCase | ⚠ 2 exceptions | `MAX_IMAGE_FILE_SIZE`, `ALLOWED_IMAGE_MIME_TYPES` in ProductDetailsViewModel |
| Enum entries: UPPER_SNAKE_CASE | ✓ Compliant | All enum entries use UPPER_SNAKE_CASE |
| Compile-time constants: UPPER_SNAKE_CASE | ✓ Compliant | Object-level and companion object constants are correct |
| Booleans: `is` prefix | ✓ Compliant | All boolean properties are clear (e.g., `isActive`, `isLoading`) |
| Collections: plural names | ✓ Compliant | `items`, `orders`, `categories`, `logs`, etc. |
| Currency: `*Amount` suffix | ✓ Compliant | Consistent across all money fields |
| `@SerialName` annotations | ✓ Compliant | All match backend API field names (carrier fix verified) |
| No ambiguous abbreviations | ✓ Compliant | No `qty`, `amt`, `desc`, `addr`, `cnt`, `idx` found |
| DTO/domain separation | ✓ Compliant | DTOs never used in presentation layer |
| Mapper pattern | ✓ Compliant | Each feature has its own mapper |

### 4.2 Customer Android — Findings (Referenced from Prior Audit)

#### Finding C-1: Package Name Includes Developer Name and Contains Typo

| Field | Value |
|-------|-------|
| **Priority** | HIGH |
| **File** | All files — package declaration |
| **Current** | `com.yousef.nearaidescovery` |
| **Proposed** | `com.nearai.discovery` |
| **Reason** | Three issues: (1) includes developer name `yousef`, (2) contains typo `nearaidescovery` (missing `i` in "Discovery"), (3) inconsistent with Admin Android's `com.nearai.admin` |
| **Contract Risk** | Medium — skeleton project with minimal code |
| **Source** | `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`, Finding C-1 |

---

## 5. Cross-Platform Terminology Comparison

The following comparison references the existing glossary in `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`. Only Android-relevant discrepancies are listed.

### 5.1 Order Lifecycle

| Business Concept | Admin Android | FastAPI Backend | Web Admin | Consistent? |
|-----------------|---------------|-----------------|-----------|-------------|
| Order Status | `OrderStatus` enum | `OrderStatus` enum | `status: string` | ✓ Name consistent |
| Fulfillment Status | `FulfillmentStatus` | `OrderFulfillmentStatus` | `fulfillment_status: string` | ✗ Name mismatch |
| Approval Status | `ApprovalStatus` enum | String in handler | `approval_status: string` | ✓ (backend has no enum) |
| Payment Status | `String` (no enum) | `PaymentStatus` enum | `status: string` | ⚠ Android lacks enum |

### 5.2 Shipment and Delivery

| Business Concept | Admin Android | FastAPI Backend | Web Admin | Consistent? |
|-----------------|---------------|-----------------|-----------|-------------|
| Courier Name | `courierName` (fixed) | `courier_name` | `courier_name` | ✓ Fixed |
| Tracking Number | `trackingNumber` | `tracking_number` | `tracking_number` | ✓ |
| Delivery Notes | `deliveryNotes` | `delivery_notes` | `delivery_notes` | ✓ |

### 5.3 Catalog and Inventory

| Business Concept | Admin Android | FastAPI Backend | Web Admin | Consistent? |
|-----------------|---------------|-----------------|-----------|-------------|
| Product | `Product` | `Product` | `ProductResponse` | ✓ |
| Variant | `ProductVariant` | `ProductVariant` | `ProductVariantResponse` | ✓ |
| Stock Balance | `StockBalance` | `StockBalance` | `StockBalanceResponse` | ✓ |
| Movement Type | `StockMovementType` enum | String | `movement_type: string` | ⚠ Backend lacks enum |

### 5.4 Finding Cross-1: `FulfillmentStatus` vs `OrderFulfillmentStatus` — Naming + Semantic Mismatch

| Field | Value |
|-------|-------|
| **Priority** | HIGH |
| **Category** | **Cross-Platform Terminology + Semantic Mismatch** |
| **Projects** | Admin Android vs FastAPI Backend |
| **Current** | Admin Android: `FulfillmentStatus` (6 entries), Backend: `OrderFulfillmentStatus` (5 entries) |
| **Semantic mismatch** | Android includes `UNFULFILLED` which the backend does NOT have. The backend starts orders at `AWAITING_CONFIRMATION`. Android's `UNFULFILLED` appears to be a client-side default/fallback. |
| **Proposed** | (1) Align enum names to one canonical form; (2) Decide whether `UNFULFILLED` should exist in the backend or be removed from Android |
| **Reason** | Two issues: (1) naming mismatch — `FulfillmentStatus` vs `OrderFulfillmentStatus`; (2) semantic mismatch — Android has 6 entries, backend has 5. The `UNFULFILLED` entry has no backend equivalent. |
| **Contract Risk** | Medium — cross-platform change affecting API contract interpretation |

---

## 6. Contract-Sensitive Identifiers

The following identifiers must NOT be renamed without coordinated changes. Full details are in `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`, Section 8.

| # | API Field | Admin Android Usage | Must Remain Unchanged |
|---|----------|---------------------|----------------------|
| 1 | `fulfillment_status` | `@SerialName("fulfillment_status")` | ✓ Yes |
| 2 | `status` (order) | `@SerialName("status")` | ✓ Yes |
| 3 | `approval_status` | `@SerialName("approval_status")` | ✓ Yes |
| 4 | `payment_method` | `@SerialName("payment_method")` | ✓ Yes |
| 5 | `courier_name` | `@SerialName("courier_name")` ✓ Fixed | ✓ Yes |
| 6 | `tracking_number` | `@SerialName("tracking_number")` | ✓ Yes |
| 7 | `delivery_notes` | `@SerialName("delivery_notes")` | ✓ Yes |
| 8 | `customer_delivery_note` | `@SerialName("customer_delivery_note")` | ✓ Yes |
| 9 | `*_amount` (all currency) | Multiple DTOs | ✓ Yes |
| 10 | `status_type` | `@SerialName("status_type")` | ✓ Yes |
| 11 | `tracking_url` | `@SerialName("tracking_url")` | ✓ Yes |
| 12 | `estimated_delivery_date` | `@SerialName("estimated_delivery_date")` | ✓ Yes |
| 13 | `dispatched_at` | `@SerialName("dispatched_at")` | ✓ Yes |
| 14 | `approval_decision` | `@SerialName("approval_decision")` | ✓ Yes |
| 15 | `variant_status` | `@SerialName("status")` | ✓ Yes |
| 16 | `movement_type` | N/A (not in Android yet) | ✓ Yes |

---

## 7. Prioritized Remediation Phases

### Phase 0: Structural Fixes (Naming Convention)

**AA-1 + AA-2:** Move `MAX_IMAGE_FILE_SIZE` and `ALLOWED_IMAGE_MIME_TYPES` from instance-level to `companion object` in `ProductDetailsViewModel`.

- This preserves UPPER_SNAKE_CASE naming (which is semantically correct for constants)
- Eliminates the naming convention violation
- No renaming needed

**Effort:** ~15 minutes
**Risk:** Low

### Phase 1: Type-Safety Improvements

**AA-3:** Create `PaymentStatus` enum; update `PaymentInfo.status` type; update mapper.
**AA-7:** Update `RecentOrder.status` to use `OrderStatus`; update dashboard mapper.

**Effort:** ~1 hour
**Risk:** Low — internal type changes only

### Phase 2: Domain-Modeling Improvements

**AA-4:** Create `StatusHistoryType` enum; update `StatusHistoryEntry.statusType` type; update mapper.

**Effort:** ~30 minutes
**Risk:** Low

### Phase 3: Cosmetic Naming Cleanup

**AA-5:** Rename `QuotaSafeguard.value` to `displayValue` (defer until AI API is implemented).
**AA-6:** Rename `ProductKpi.value` to `displayValue`.

**Effort:** ~15 minutes
**Risk:** Negligible

### Phase 4: Cross-Project Alignment

**Cross-1:** Resolve `FulfillmentStatus` vs `OrderFulfillmentStatus` naming AND semantic mismatch (`UNFULFILLED`).

**Effort:** ~1 hour
**Risk:** Medium — cross-platform coordination required

### Phase 5: Customer Android Package Migration

**C-1:** Rename package from `com.yousef.nearaidescovery` to `com.nearai.discovery`.

**Effort:** ~1 hour
**Risk:** Medium — skeleton project, but namespace changes have ripple effects

---

## 8. Shared Glossary Updates

No new glossary entries are needed. The existing glossary in `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`, Section 6, remains accurate. The `courierName` fix has already been reflected in the cross-project terminology table.

---

## 9. Specialist Reviews

### Android Developer Perspective

The Admin Android codebase follows Kotlin naming conventions correctly with two exceptions: the UPPER_SNAKE_CASE instance vals in `ProductDetailsViewModel`. These are legitimate constants conceptually (file size limit, MIME type whitelist). The preferred fix is structural — move them to a `companion object` — rather than renaming, which preserves the semantically correct UPPER_SNAKE_CASE naming while satisfying Kotlin convention.

The String-typed status fields (`PaymentInfo.status`, `StatusHistoryEntry.statusType`, `RecentOrder.status`) represent missed opportunities for type safety. These are type-safety and domain-modeling issues, not naming defects — the property names are correct. Enums already exist for `OrderStatus` and `FulfillmentStatus` in the orders module, but `PaymentStatus` and `StatusHistoryType` need to be created. These are low-risk internal changes since the mapper layer handles conversion.

**Important finding:** Android's `FulfillmentStatus` enum includes `UNFULFILLED`, which the backend does not have. This semantic mismatch must be resolved alongside the naming alignment.

**Recommendation:** The generic `value` properties in `QuotaSafeguard` and `ProductKpi` are low-priority. `QuotaSafeguard` is a placeholder for an unimplemented API — defer until the real data model is known. `ProductKpi.value` is defensible given the surrounding context (`title` already describes the metric).

### Software Architect Perspective

The platform demonstrates strong adherence to clean architecture. The separation between DTO, domain, and presentation layers is well-maintained. The `@SerialName` annotation pattern is the correct approach for mapping between `snake_case` API fields and `lowerCamelCase` Kotlin properties. The carrier/courier_name fix validated this pattern — the annotation was wrong, not the architecture.

**Recommendation:** Consider adding a CI check that compares `@SerialName` annotations against backend Pydantic schemas to catch future contract mismatches early.

### QA Engineer Perspective

The `carrier`/`courier_name` mismatch was a silent data loss bug caught by the naming audit. The 3 regression tests added to `OrderMapperTest.kt` provide good coverage for the shipment mapping. Similar serialization tests should be added for other DTOs to prevent future contract mismatches.

**Recommendation:** Add DTO serialization tests for all features that verify `@SerialName` annotations match actual backend JSON payloads.

---

## 10. EGM Compliance Assessment

**Elephant-Goldfish Model Review:** Not available via subagent in the current session. The analysis followed the issue's phased approach and covered all required categories.

**Self-Assessment against EGM Principles:**

| Principle | Compliance | Notes |
|-----------|-----------|-------|
| Evidence-based findings | ✓ | All findings include file paths and line numbers |
| Distinguish defects from preferences | ✓ | Only flagged actual issues, not style preferences |
| Contract protection | ✓ | All 16 contract-sensitive identifiers documented |
| Cross-project consistency | ✓ | Full terminology comparison with Backend and Web Admin |
| Prioritized remediation | ✓ | Findings classified CRITICAL/HIGH/MEDIUM/LOW |
| No code modification | ✓ | Audit-only, no source code changed |
| Honest coverage reporting | ✓ | Customer Android explicitly marked as not accessible |

---

## 11. Final Verdict

### APPROVED WITH CONDITIONS

The Admin Android codebase demonstrates strong naming discipline. Kotlin conventions are followed correctly, domain terminology is clear, and the clean architecture pattern is well-maintained. The Customer Android project could not be inspected directly from this workspace; its findings are referenced from the prior cross-project audit.

**Critical Issues:** None open. The `carrier`/`courier_name` API contract mismatch was resolved in a prior workstream.

**Remaining Conditions for Full Approval:**

1. **Fix UPPER_SNAKE_CASE instance vals** — Move `MAX_IMAGE_FILE_SIZE` and `ALLOWED_IMAGE_MIME_TYPES` to a `companion object` in `ProductDetailsViewModel` (preferred) or rename to lowerCamelCase.
2. **Add type safety for status fields** — Create `PaymentStatus` and `StatusHistoryType` enums; update `RecentOrder.status` to use `OrderStatus`.
3. **Align fulfillment status naming and semantics** — Choose one canonical name across Admin Android and Backend; resolve the `UNFULFILLED` semantic mismatch.
4. **Fix Customer Android package** — Rename from `com.yousef.nearaidescovery` to `com.nearai.discovery`.

**Strengths:**
- Consistent `*Amount` suffix for currency fields
- Proper use of `@SerialName` for API field mapping
- Clear separation of order status, fulfillment status, and approval status
- Well-structured domain models with appropriate validation
- Consistent pagination pattern
- No ambiguous abbreviations found
- No generic identifiers beyond the 2 flagged `value` properties
- All boolean properties have clear `is` prefix

**Finding Summary:** 9 findings total (0 CRITICAL open, 2 HIGH, 4 MEDIUM, 3 LOW). Findings include 4 naming convention defects, 2 type-safety issues, 1 domain-modeling issue, 1 cross-platform terminology and semantic mismatch, and 1 compatibility-sensitive change. All remaining findings are remediable without architectural changes or API contract modifications.

**Validation Note:** This report was validated on 2026-11-15. See `NEARAI_ANDROID_NAMING_FINDINGS_VALIDATION.md` for the detailed validation report, including finding-by-finding verification, classification corrections, and the revised implementation roadmap.

---

*This report is a supplement to `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`. It deepens the Admin Android audit with full module coverage and corrects the previous audit's incorrect claim that no instance-level UPPER_SNAKE_CASE vals exist. The Customer Android project is not accessible from this workspace; its findings are referenced from the prior audit.*
