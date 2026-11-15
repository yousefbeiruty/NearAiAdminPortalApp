---
sessionId: session-261115-101713-122a
---

# Audit Findings Summary

## Executive Summary

This audit identifies all internally defined `fromX` naming patterns across the four NearAI projects. A total of **8 unique `fromX` method definitions** were found across 3 projects (Admin Android, Customer Android, Web Admin). The FastAPI Backend has 1 borderline case (`from_name`) that is acceptable.

### Total `fromX` Methods Identified

| Project | Pattern | Definitions | Call Sites (prod) | Call Sites (test) | Files Affected |
|---------|---------|-------------|-------------------|-------------------|----------------|
| Admin Android | `fromString` | 8 | 19 | 80 | 4 prod, 1 test |
| Customer Android | `fromBackendValue` | 3 | 6 | 91 | 2 prod, 2 test |
| Web Admin | `fromResponse` | 1 | 1 | 1 | 1 prod, 1 test |
| FastAPI Backend | `from_name` | 1 | 3 | 0 | 1 prod |
| **Total** | — | **13** | **29** | **173** | **8 prod, 4 test** |

### Final Verdict: **APPROVED FOR NAMING REMEDIATION**

All identified `fromX` methods are clear violations of the naming standard. They describe where the input originates rather than what the function does. Each has a clear, descriptive replacement.

---

## Project Coverage Confirmation

| # | Project | Path | Language | Status |
|---|---------|------|----------|--------|
| 1 | Admin Android | `NearAiAdminPortal` | Kotlin | ✓ Fully inspected (102 prod `.kt` files) |
| 2 | Customer Android | `NearAiDescovery` | Kotlin | ✓ Fully inspected (all `.kt` files via filesystem) |
| 3 | FastAPI Backend | `NearAi-Back-End` | Python | ✓ Inspected (`app/` and `tests/`, excluding `.venv`) |
| 4 | Web Admin | `NearAi-Web-Admin` | TypeScript/React | ✓ Inspected (`src/`, excluding `node_modules`) |

---

## Findings by Project

### Project 1: Admin Android (`NearAiAdminPortal`)

**Pattern:** `fromString()` — 8 definitions, all on enum companion objects

All 8 methods perform the same operation: parse a backend string value into an enum entry, falling back to a default for unknown values. The name `fromString` describes the input type, not the operation.

#### Finding AA-1: `OrderStatus.fromString()`
- **File:** `feature/orders/domain/OrderStatus.kt:19`
- **Signature:** `fun fromString(value: String): OrderStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderMapper.kt` (lines 26, 49, 116), `OrderRepositoryImpl.kt` (lines 162, 182)
- **Call Sites (test):** `OrderMapperTest.kt` (~20 references)
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** The name communicates both the operation (parse) and the fallback behavior (defaults to PENDING)

#### Finding AA-2: `ApprovalStatus.fromString()`
- **File:** `feature/orders/domain/OrderStatus.kt:49`
- **Signature:** `fun fromString(value: String): ApprovalStatus`
- **Behavior:** Parses backend string to enum; falls back to `AWAITING_STANDARD_REVIEW`
- **Call Sites (prod):** `OrderMapper.kt` (lines 42, 65, 103, 118)
- **Call Sites (test):** `OrderMapperTest.kt` (~10 references)
- **Proposed Replacement:** `parseOrAwaitingReview()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-3: `FulfillmentStatus.fromString()`
- **File:** `feature/orders/domain/OrderStatus.kt:74`
- **Signature:** `fun fromString(value: String): FulfillmentStatus`
- **Behavior:** Parses backend string to enum; falls back to `AWAITING_CONFIRMATION`
- **Call Sites (prod):** `OrderMapper.kt` (lines 27, 50, 117)
- **Call Sites (test):** `OrderMapperTest.kt` (~30 references)
- **Proposed Replacement:** `parseOrAwaitingConfirmation()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-4: `PaymentStatus.fromString()`
- **File:** `feature/orders/domain/OrderStatus.kt:104`
- **Signature:** `fun fromString(value: String): PaymentStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderRepositoryImpl.kt` (lines 162, 182)
- **Call Sites (test):** `OrderMapperTest.kt` (~15 references)
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** Same pattern as OrderStatus; name communicates operation and fallback

#### Finding AA-5: `ProductStatus.fromString()`
- **File:** `feature/catalog/domain/CatalogStatus.kt:18`
- **Signature:** `fun fromString(value: String): ProductStatus`
- **Behavior:** Parses backend string to enum; falls back to `ACTIVE`
- **Call Sites (prod):** `CatalogMapper.kt` (line 47)
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrActive()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-6: `VariantStatus.fromString()`
- **File:** `feature/catalog/domain/CatalogStatus.kt:39`
- **Signature:** `fun fromString(value: String): VariantStatus`
- **Behavior:** Parses backend string to enum; falls back to `ACTIVE`
- **Call Sites (prod):** `CatalogMapper.kt` (line 78)
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrActive()`
- **Rationale:** Same pattern as ProductStatus

#### Finding AA-7: `StockMovementType.fromString()`
- **File:** `feature/catalog/domain/CatalogStatus.kt:60`
- **Signature:** `fun fromString(value: String): StockMovementType`
- **Behavior:** Parses backend string to enum; falls back to `RECEIPT`
- **Call Sites (prod):** `CatalogMapper.kt` (line 103)
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrReceipt()`
- **Rationale:** Communicates parse operation with specific fallback

#### Finding AA-8: `ProductSortBy.fromString()`
- **File:** `feature/catalog/domain/CatalogRepository.kt:142`
- **Signature:** `fun fromString(value: String): ProductSortBy`
- **Behavior:** Parses backend string to enum via `toBackendValue()`; falls back to `NEWEST`
- **Call Sites (prod):** None directly (only defined, not called in current code)
- **Call Sites (test):** None directly
- **Proposed Replacement:** `parseOrNewest()`
- **Rationale:** Communicates parse operation with specific fallback

**Additional Admin Android Finding — `toBackendValue()`:**
- **File:** `feature/catalog/domain/CatalogRepository.kt:135`
- **Signature:** `fun toBackendValue(): String`
- **Note:** This is the inverse operation (enum → string for API calls). While not a `fromX` pattern, it describes the destination rather than the operation. Proposed: `toApiValue()` or `toSerializedValue()` — lower priority.

---

### Project 2: Customer Android (`NearAiDescovery`)

**Pattern:** `fromBackendValue()` — 3 definitions, all on enum companion objects

#### Finding CA-1: `OrderStatus.fromBackendValue()`
- **File:** `feature-home/src/main/.../domain/model/Order.kt:93`
- **Signature:** `fun fromBackendValue(value: String?): OrderStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderRepositoryImpl.kt` (line 121, 155)
- **Call Sites (test):** `OrderEnumTest.kt`, `CheckoutAndOrderIntegrationTest.kt` (~30 references)
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** Same operation as Admin Android; consistent naming across projects

#### Finding CA-2: `FulfillmentStatus.fromBackendValue()`
- **File:** `feature-home/src/main/.../domain/model/Order.kt:125`
- **Signature:** `fun fromBackendValue(value: String?): FulfillmentStatus?`
- **Behavior:** Parses backend string to enum; returns nullable, falls back to `null`
- **Call Sites (prod):** `OrderRepositoryImpl.kt` (line 122, 156)
- **Call Sites (test):** `OrderEnumTest.kt`, `CheckoutAndOrderIntegrationTest.kt` (~35 references)
- **Proposed Replacement:** `parseOrNull()`
- **Rationale:** The nullable return is a key behavioral difference; name must communicate this

#### Finding CA-3: `PaymentStatus.fromBackendValue()`
- **File:** `feature-home/src/main/.../domain/model/Order.kt:169`
- **Signature:** `fun fromBackendValue(value: String?): PaymentStatus`
- **Behavior:** Parses backend string to enum; falls back to `PENDING`
- **Call Sites (prod):** `OrderRepositoryImpl.kt` (line 111, 161)
- **Call Sites (test):** `OrderEnumTest.kt`, `CheckoutAndOrderIntegrationTest.kt` (~26 references)
- **Proposed Replacement:** `parseOrPending()`
- **Rationale:** Same operation as Admin Android

---

### Project 3: FastAPI Backend (`NearAi-Back-End`)

**Pattern:** `from_name()` — 1 definition

#### Finding BE-1: `ProductSlug.from_name()`
- **File:** `app/modules/catalog/models/value_objects.py:98`
- **Signature:** `def from_name(cls, name: str) -> str`
- **Behavior:** Generates a slug string from a human-readable name by cleaning it
- **Call Sites (prod):** `schemas.py` (lines 26, 122), `category_handlers.py` (line 230)
- **Proposed Replacement:** `generate_slug()` or `slugify()`
- **Rationale:** The function generates a slug from a name. `from_name` describes the input source; `generate_slug` or `slugify` describes the operation. This is a borderline case — `from_name` is common Python factory convention — but still violates the NearAI standard.
- **Priority:** LOW — this is a well-established Python factory pattern; the name is not misleading in context.

---

### Project 4: Web Admin (`NearAi-Web-Admin`)

**Pattern:** `fromResponse()` — 1 definition

#### Finding WA-1: `ApiErrorClass.fromResponse()`
- **File:** `src/lib/errors/api-error.ts:22`
- **Signature:** `static fromResponse(error: ApiError, status: number): ApiErrorClass`
- **Behavior:** Constructs an `ApiErrorClass` instance from a raw API error object and HTTP status
- **Call Sites (prod):** `src/lib/api/client.ts:98`
- **Call Sites (test):** `src/tests/unit/api-error.test.ts:23`
- **Proposed Replacement:** `fromApiError()` or `wrapApiError()`
- **Rationale:** `fromResponse` is vague — it could mean HTTP response, GraphQL response, etc. `fromApiError` is more specific about the input type. `wrapApiError` describes the operation (wrapping a raw error in a typed class).
- **Priority:** LOW — the name is not misleading in context; the parameter type `ApiError` makes the input clear.

---

## Naming Standard Update

The following rule must be added to `AGENTS.md` (and equivalent docs in other projects):

> **Do not use `fromX` naming patterns for parsing, conversion, mapping, or object creation methods.** Names such as `fromBackendValue()`, `fromString()`, `fromApiValue()`, `fromDto()`, `fromJson()`, `fromResponse()`, and `fromEntity()` are unacceptable. The preferred name must describe what the function actually does, not merely where its input originates.
>
> Use descriptive operation-based names:
> - Parsing with fallback: `parseOrPending()`, `parseOrActive()`, `parseOrNull()`
> - DTO mapping: `mapOrderSummary()`, `mapProductResponse()`
> - Validation: `validatePaymentStatus()`
> - Normalization: `normalizeStatus()`
> - Object creation: `createPaymentInfo()`, `buildPaymentSummary()`
> - Lookup: `findPaymentStatus()`
> - Resolution: `resolvePaymentStatus()`

---

## Priority Classification

| Priority | Findings | Rationale |
|----------|----------|-----------|
| **HIGH** | AA-1 through AA-8 (Admin Android `fromString`) | 8 methods, 19 prod call sites, 80 test references; core domain parsing logic |
| **HIGH** | CA-1 through CA-3 (Customer Android `fromBackendValue`) | 3 methods, 6 prod call sites, 91 test references; inconsistent with Admin Android naming |
| **LOW** | BE-1 (Backend `from_name`) | 1 method, 3 call sites; established Python factory pattern |
| **LOW** | WA-1 (Web Admin `fromResponse`) | 1 method, 1 prod call site; input type is clear from signature |

---

## Contract and Compatibility Risks

- **No API contract changes:** All renames are internal Kotlin/TypeScript/Python methods. No serialization names, JSON fields, or API endpoints are affected.
- **Test impact:** ~173 test references need updating. All are mechanical renames.
- **Cross-project consistency:** Admin Android and Customer Android use different names (`fromString` vs `fromBackendValue`) for the same operation. Remediation will align both to `parseOrX()` naming.

---

## Recommended Implementation Phases

1. **Phase 1 — Admin Android `fromString` → `parseOrX()`** (8 methods, ~100 references)
2. **Phase 2 — Customer Android `fromBackendValue` → `parseOrX()`** (3 methods, ~100 references)
3. **Phase 3 — Web Admin `fromResponse` → `wrapApiError()`** (1 method, 2 references)
4. **Phase 4 — Backend `from_name` → `slugify()`** (1 method, 3 references)
5. **Phase 5 — Update `AGENTS.md` and naming documentation**

---

## EGM Compliance Assessment

This audit follows the Elephant-Goldfish Model principles:
- **Complete inventory:** Every `fromX` method across all 4 projects has been identified.
- **Behavior-first naming:** Each proposed replacement describes the operation and fallback behavior.
- **No blind renaming:** Each finding includes implementation analysis, call site review, and risk assessment.
- **Cross-project consistency:** The remediation will align naming conventions across all projects.
- **Smallest change:** Only method names change; behavior, signatures (parameter types), and architecture remain identical.

# Updated Naming Standard

## Naming Standard Addition for `AGENTS.md`

The following section should be added to the **Code Quality** subsection of Section 2 (Shared NearAI Engineering Principles) in `AGENTS.md`:

### Naming Conventions

- Follow existing naming and formatting conventions.
- **Do not use `fromX` naming patterns for parsing, conversion, mapping, or object creation methods.** Names such as `fromBackendValue()`, `fromString()`, `fromApiValue()`, `fromDto()`, `fromJson()`, `fromResponse()`, and `fromEntity()` are unacceptable for new or existing internal methods.
- **Use descriptive operation-based names** that communicate what the function does, not where its input originates:
  - **Parsing with fallback:** `parseOrPending()`, `parseOrActive()`, `parseOrNull()`
  - **Parsing (strict):** `parsePaymentStatus()`
  - **DTO mapping:** `mapOrderSummary()`, `mapProductResponse()`
  - **Domain mapping:** `mapToDomain()` or a more descriptive name
  - **Validation:** `validatePaymentStatus()`
  - **Normalization:** `normalizeStatus()`
  - **Object creation:** `createPaymentInfo()`, `buildPaymentSummary()`
  - **Lookup:** `findPaymentStatus()`
  - **Resolution:** `resolvePaymentStatus()`
- Every identifier must communicate:
  1. What operation it performs.
  2. What responsibility it owns.
  3. What kind of result it produces.
  4. Whether it can fail or use a fallback, where relevant.
- Do not accept ambiguous names, generic names when a meaningful name is available, or names based on the origin of input rather than function behavior.
- This rule applies to all four NearAI projects: Admin Android, Customer Android, FastAPI Backend, and Web Admin.
- Language-specific conventions still apply (lowerCamelCase for Kotlin, snake_case for Python, camelCase for TypeScript).

---

## Files to Update

| File | Change |
|------|--------|
| `AGENTS.md` (NearAiAdminPortal) | Add naming conventions subsection to Section 2 |
| `AGENTS.md` (NearAiDescovery) — if exists | Add same rule |
| `AGENTS.md` (NearAi-Back-End) — if exists | Add same rule |
| `AGENTS.md` (NearAi-Web-Admin) — if exists | Add same rule |
| `docs/architecture/NEARAI_FROMX_NAMING_AUDIT.md` | Create this audit report |

# Delivery Steps

### ✓ Step 1: Create comprehensive audit report documents
Create `docs/architecture/NEARAI_FROMX_NAMING_AUDIT.md` with the complete inventory of all `fromX` methods across all four NearAI projects.

- Write executive summary with total counts and verdict.
- Document all 13 `fromX` method definitions with file paths, line numbers, signatures, behaviors, and call sites.
- Include proposed replacements with rationale for each finding.
- Include priority classification (HIGH for Android projects, LOW for Backend and Web Admin).
- Include contract and compatibility risk assessment.
- Include recommended implementation phases.
- Include EGM compliance assessment.
- Cross-reference existing audit reports (`NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`, `NEARAI_ANDROID_NAMING_AUDIT.md`).

### ✓ Step 2: Update AGENTS.md with fromX naming prohibition rule
Add explicit `fromX` naming prohibition to the Code Quality section of `AGENTS.md`.

- Add a new **Naming Conventions** subsection under Section 2 (Shared NearAI Engineering Principles).
- State the rule: "Do not use `fromX` naming patterns for parsing, conversion, mapping, or object creation methods."
- List preferred operation-based naming patterns (parse, map, normalize, resolve, validate, create, build).
- Include the four naming quality requirements (operation, responsibility, result type, fallback behavior).
- Note that the rule applies across all four NearAI projects.
- Preserve existing Code Quality content; this is an additive change only.