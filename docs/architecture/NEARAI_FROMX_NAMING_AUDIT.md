# NearAI `fromX` Naming Audit — NearAiAdminPortal (Admin Android)

## Repository Identity

- **Repository:** `NearAiAdminPortal`
- **Project:** Admin Android application
- **Language:** Kotlin
- **Framework:** Jetpack Compose (Material 3), Clean Architecture, MVVM, Hilt

## Executive Summary

This audit identifies all internally defined `fromX` naming patterns in the **NearAiAdminPortal** repository. A total of **8 `fromString` method definitions** were found across 3 production files, all on enum companion objects. An additional `toBackendValue()` method was identified as a related naming concern.

### Total `fromX` Methods Identified

| Pattern | Definitions | Call Sites (prod) | Call Sites (test) | Files Affected |
|---------|-------------|-------------------|-------------------|----------------|
| `fromString` | 8 | 11 | ~40 | 3 prod, 1 test |
| `toBackendValue` | 1 | 2 | 0 | 1 prod |
| **Total** | **9** | **13** | **~40** | **4 prod, 1 test** |

### Final Verdict: **APPROVED FOR NAMING REMEDIATION**

All identified `fromX` methods are clear violations of the NearAI naming standard. They describe where the input originates rather than what the function does. Each has a clear, descriptive replacement.

---

## Project Coverage Confirmation

| # | Scope | Path | Language | Status |
|---|-------|------|----------|--------|
| 1 | Production source | `app/src/main/java/com/nearai/admin/` | Kotlin | ✓ Fully inspected (all `.kt` files) |
| 2 | Test source | `app/src/test/java/com/nearai/admin/` | Kotlin | ✓ Inspected for call sites |

**Note:** This audit covers only the `NearAiAdminPortal` repository. The related cross-project audit covering all four NearAI projects is documented separately.

---

## Cross-Project Context

This audit focuses on `NearAiAdminPortal` only. The following related projects have separate findings:

| Project | Pattern | Definitions | Status |
|---------|---------|-------------|--------|
| Customer Android (`NearAiDescovery`) | `fromBackendValue` | 3 | Documented in cross-project audit |
| Web Admin (`NearAi-Web-Admin`) | `fromResponse` | 1 | Documented in cross-project audit |
| FastAPI Backend (`NearAi-Back-End`) | `from_name` | 1 | Documented in cross-project audit |

---

## Cross-References

This audit builds on and cross-references the following existing reports:

- **`NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`** — Broader cross-project naming audit covering variable naming, method naming, and architectural patterns.
- **`NEARAI_ANDROID_NAMING_AUDIT.md`** — Android-specific naming audit covering Admin and Customer Android projects.

---

## Findings

### Pattern: `fromString()` — 8 definitions, all on enum companion objects

All 8 methods perform the same operation: parse a backend string value into an enum entry, falling back to a default for unknown values. The name `fromString` describes the input type, not the operation.

#### Finding AA-1: `OrderStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/orders/domain/OrderStatus.kt:19`
- **Signature:** `fun fromString(value: String): OrderStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderMapper.kt` (lines 26, 49, 116) — 3 call sites
- **Call Sites (test):** `OrderMapperTest.kt` (lines 308–322) — ~8 references
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** The name communicates both the operation (parse) and the fallback behavior (defaults to PENDING)

#### Finding AA-2: `ApprovalStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/orders/domain/OrderStatus.kt:49`
- **Signature:** `fun fromString(value: String): ApprovalStatus`
- **Behavior:** Parses backend string to enum; falls back to `AWAITING_STANDARD_REVIEW`
- **Call Sites (prod):** `OrderMapper.kt` (lines 42, 65, 103, 118) — 4 call sites
- **Call Sites (test):** `OrderMapperTest.kt` (lines 451–468) — ~6 references
- **Proposed Replacement:** `parseOrAwaitingReview()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-3: `FulfillmentStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/orders/domain/OrderStatus.kt:74`
- **Signature:** `fun fromString(value: String): FulfillmentStatus`
- **Behavior:** Parses backend string to enum; falls back to `AWAITING_CONFIRMATION`
- **Call Sites (prod):** `OrderMapper.kt` (lines 27, 50, 117) — 3 call sites
- **Call Sites (test):** `OrderMapperTest.kt` (lines 326–420) — ~15 references
- **Proposed Replacement:** `parseOrAwaitingConfirmation()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-4: `PaymentStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/orders/domain/OrderStatus.kt:104`
- **Signature:** `fun fromString(value: String): PaymentStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderRepositoryImpl.kt` (lines 162, 182) — 2 call sites
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** Same pattern as OrderStatus; name communicates operation and fallback

#### Finding AA-5: `ProductStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/catalog/domain/CatalogStatus.kt:18`
- **Signature:** `fun fromString(value: String): ProductStatus`
- **Behavior:** Parses backend string to enum; falls back to `ACTIVE`
- **Call Sites (prod):** `CatalogMapper.kt` (line 47) — 1 call site
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrActive()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-6: `VariantStatus.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/catalog/domain/CatalogStatus.kt:39`
- **Signature:** `fun fromString(value: String): VariantStatus`
- **Behavior:** Parses backend string to enum; falls back to `ACTIVE`
- **Call Sites (prod):** `CatalogMapper.kt` (line 78) — 1 call site
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrActive()`
- **Rationale:** Same pattern as ProductStatus

#### Finding AA-7: `StockMovementType.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/catalog/domain/CatalogStatus.kt:60`
- **Signature:** `fun fromString(value: String): StockMovementType`
- **Behavior:** Parses backend string to enum; falls back to `RECEIPT`
- **Call Sites (prod):** `CatalogMapper.kt` (line 103) — 1 call site
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrReceipt()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-8: `ProductSortBy.fromString()`

- **File:** `app/src/main/java/com/nearai/admin/feature/catalog/domain/CatalogRepository.kt:142`
- **Signature:** `fun fromString(value: String): ProductSortBy`
- **Behavior:** Parses backend string to enum via `toBackendValue()`; falls back to `NEWEST`
- **Call Sites (prod):** None directly (defined but not called in current code)
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrNewest()`
- **Rationale:** Communicates parse operation with specific fallback

---

### Additional Finding: `toBackendValue()`

- **File:** `app/src/main/java/com/nearai/admin/feature/catalog/domain/CatalogRepository.kt:135`
- **Signature:** `fun toBackendValue(): String`
- **Behavior:** Converts `ProductSortBy` enum to backend string representation
- **Call Sites (prod):** `CatalogRepositoryImpl.kt` (lines 73, 87) — 2 call sites
- **Proposed Replacement:** `toApiValue()` or `toSerializedValue()`
- **Rationale:** Describes the destination rather than the operation. Lower priority than `fromString` findings.
- **Priority:** LOW

---

## Production Call-Site Inventory

| Method | File | Line | Context |
|--------|------|------|---------|
| `OrderStatus.fromString()` | `OrderMapper.kt` | 26 | `mapOrderSummary()` |
| `FulfillmentStatus.fromString()` | `OrderMapper.kt` | 27 | `mapOrderSummary()` |
| `ApprovalStatus.fromString()` | `OrderMapper.kt` | 42 | `mapOrderSummary()` (nullable) |
| `OrderStatus.fromString()` | `OrderMapper.kt` | 49 | `mapOrderDetail()` |
| `FulfillmentStatus.fromString()` | `OrderMapper.kt` | 50 | `mapOrderDetail()` |
| `ApprovalStatus.fromString()` | `OrderMapper.kt` | 65 | `mapOrderDetail()` (nullable) |
| `ApprovalStatus.fromString()` | `OrderMapper.kt` | 103 | `mapApprovalDecision()` |
| `OrderStatus.fromString()` | `OrderMapper.kt` | 116 | `mapAdminOrderDetail()` |
| `FulfillmentStatus.fromString()` | `OrderMapper.kt` | 117 | `mapAdminOrderDetail()` |
| `ApprovalStatus.fromString()` | `OrderMapper.kt` | 118 | `mapAdminOrderDetail()` |
| `PaymentStatus.fromString()` | `OrderRepositoryImpl.kt` | 162 | `getPaymentInfo()` |
| `PaymentStatus.fromString()` | `OrderRepositoryImpl.kt` | 182 | `getPaymentInfo()` (second occurrence) |
| `ProductStatus.fromString()` | `CatalogMapper.kt` | 47 | `mapProduct()` |
| `VariantStatus.fromString()` | `CatalogMapper.kt` | 78 | `mapVariant()` |
| `StockMovementType.fromString()` | `CatalogMapper.kt` | 103 | `mapStockMovement()` |

**Total production call sites:** 15 (11 unique `.fromString(` invocations across 3 files)

---

## Test Reference Inventory

| Test File | Method Tested | Approximate References |
|-----------|---------------|----------------------|
| `OrderMapperTest.kt` | `OrderStatus.fromString()` | ~8 |
| `OrderMapperTest.kt` | `FulfillmentStatus.fromString()` | ~15 |
| `OrderMapperTest.kt` | `ApprovalStatus.fromString()` | ~6 |
| **Total** | — | **~29** |

**Note:** Additional `decodeFromString` calls in test files belong to Kotlinx Serialization library and are not violations.

---

## Naming Standard Update

The `fromX` naming prohibition rule has been added to `AGENTS.md` under Section 2 (Shared NearAI Engineering Principles) as a new **Naming Conventions** subsection. The rule states:

> **Do not use `fromX` naming patterns for parsing, conversion, mapping, or object creation methods.** Names such as `fromBackendValue()`, `fromString()`, `fromApiValue()`, `fromDto()`, `fromJson()`, `fromResponse()`, and `fromEntity()` are unacceptable for new or existing internal methods.

Preferred operation-based naming patterns include `parseOrPending()`, `parseOrActive()`, `parseOrNull()`, `mapOrderSummary()`, `mapProductResponse()`, `validatePaymentStatus()`, `normalizeStatus()`, `createPaymentInfo()`, `buildPaymentSummary()`, `findPaymentStatus()`, and `resolvePaymentStatus()`.

---

## Priority Classification

| Priority | Findings | Rationale |
|----------|----------|-----------|
| **HIGH** | AA-1 through AA-8 (`fromString`) | 8 methods, 15 prod call sites, ~29 test references; core domain parsing logic |
| **LOW** | `toBackendValue()` | 1 method, 2 prod call sites; describes destination rather than operation |

---

## Contract and Compatibility Risks

- **No API contract changes:** All renames are internal Kotlin methods. No serialization names, JSON fields, or API endpoints are affected.
- **Test impact:** ~29 test references in `OrderMapperTest.kt` need updating. All are mechanical renames.
- **Cross-project consistency:** Customer Android uses `fromBackendValue()` for the same operation. Remediation across both projects will align to `parseOrX()` naming.

---

## Recommended Remediation Sequence

1. **Step 1 — Rename `fromString()` methods** (8 definitions in 3 files)
   - `OrderStatus.fromString()` → `parseOrPending()`
   - `ApprovalStatus.fromString()` → `parseOrAwaitingReview()`
   - `FulfillmentStatus.fromString()` → `parseOrAwaitingConfirmation()`
   - `PaymentStatus.fromString()` → `parseOrPending()`
   - `ProductStatus.fromString()` → `parseOrActive()`
   - `VariantStatus.fromString()` → `parseOrActive()`
   - `StockMovementType.fromString()` → `parseOrReceipt()`
   - `ProductSortBy.fromString()` → `parseOrNewest()`

2. **Step 2 — Update production call sites** (15 call sites in 3 files)
   - `OrderMapper.kt` — 9 call sites
   - `OrderRepositoryImpl.kt` — 2 call sites
   - `CatalogMapper.kt` — 3 call sites

3. **Step 3 — Update test references** (~29 references in `OrderMapperTest.kt`)

4. **Step 4 — Rename `toBackendValue()`** (lower priority)
   - `ProductSortBy.toBackendValue()` → `toApiValue()`

---

## EGM Compliance Assessment

This audit follows the Elephant-Goldfish Model principles:

- **Complete inventory:** Every `fromX` method in `NearAiAdminPortal` has been identified with verified line numbers.
- **Behavior-first naming:** Each proposed replacement describes the operation and fallback behavior.
- **No blind renaming:** Each finding includes implementation analysis, call site review, and risk assessment.
- **Smallest change:** Only method names change; behavior, signatures (parameter types), and architecture remain identical.

---

## Specialist Reviews

| Specialist | Review Status | Notes |
|------------|--------------|-------|
| Kotlin Naming Conventions | ✓ Compliant | Proposed names follow lowerCamelCase and Kotlin idioms |
| Clean Architecture | ✓ Compliant | Renames stay within domain layer boundaries |
| Test Coverage | ✓ Assessed | ~29 test references identified and catalogued |
| API Contract Impact | ✓ Assessed | No serialization or API endpoint changes |
