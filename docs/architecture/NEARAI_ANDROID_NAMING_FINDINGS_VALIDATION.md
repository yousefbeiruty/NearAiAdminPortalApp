# NearAI — Android Naming Audit Findings Validation

**Date:** 2026-11-15
**Validator:** Junie (Automated Validation)
**Source Report:** `docs/architecture/NEARAI_ANDROID_NAMING_AUDIT.md`
**Status:** Complete

---

## 1. Executive Summary

This document validates all nine findings from `NEARAI_ANDROID_NAMING_AUDIT.md` by inspecting the actual source code declarations, usage sites, and cross-platform context. The validation confirms that **most findings are accurately identified but incorrectly classified** — several are presented as naming defects when they are actually type-safety or domain-modeling issues.

**Validation Verdict:** `VALIDATED WITH CONDITIONS`

The source report requires the following corrections:
1. **Misclassification:** AA-3, AA-4, and AA-7 are type-safety/domain-modeling issues, not naming defects.
2. **Missing semantic detail:** Cross-1 (`FulfillmentStatus` vs `OrderFulfillmentStatus`) has a semantic mismatch beyond naming — Android includes `UNFULFILLED` which the backend does not have.
3. **Severity adjustment:** AA-5 and AA-6 (`value` properties) are correctly classified as LOW but are defensible in their narrow contexts.
4. **Structural alternative:** AA-1 and AA-2 could be resolved structurally (move to `companion object`) rather than by renaming.

---

## 2. Finding-by-Finding Validation

### AA-1: `MAX_IMAGE_FILE_SIZE` — UPPER_SNAKE_CASE Instance Val

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/catalog/presentation/ProductDetailsScreen.kt:305` ✓ Verified |
| **Declaration** | `private val MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024L` ✓ Verified |
| **Usage sites** | Line 345: `if (bytes.size > MAX_IMAGE_FILE_SIZE)` — 1 reference |
| **Classification** | **Naming Convention Defect** |
| **Severity** | MEDIUM ✓ Correct |
| **Proposed fix** | Rename to `maxImageFileSize` |
| **Alternative fix** | Move to `companion object` as `const val MAX_IMAGE_FILE_SIZE = 10_240_000L` (compile-time constant) |
| **Rationale** | UPPER_SNAKE_CASE implies a compile-time constant. This is an instance-level `private val`. Kotlin convention requires lowerCamelCase for instance properties. The value IS conceptually constant, so moving it to a `companion object` would make UPPER_SNAKE_CASE appropriate. |
| **Implementation risk** | Low — private property, 1 internal reference |
| **Verdict** | **VALID** — finding is correct; structural fix is preferable to renaming |

---

### AA-2: `ALLOWED_IMAGE_MIME_TYPES` — UPPER_SNAKE_CASE Instance Val

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/catalog/presentation/ProductDetailsScreen.kt:310` ✓ Verified |
| **Declaration** | `private val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")` ✓ Verified |
| **Usage sites** | Line 326: `!ALLOWED_IMAGE_MIME_TYPES.contains(mimeType.lowercase())` — 1 reference |
| **Classification** | **Naming Convention Defect** |
| **Severity** | MEDIUM ✓ Correct |
| **Proposed fix** | Rename to `allowedImageMimeTypes` |
| **Alternative fix** | Move to `companion object` as `val ALLOWED_IMAGE_MIME_TYPES = setOf(...)` |
| **Rationale** | Same as AA-1. `setOf()` cannot be a `const val`, but placing it in a `companion object` would make UPPER_SNAKE_CASE conventionally appropriate as a class-level constant. |
| **Implementation risk** | Low — private property, 1 internal reference |
| **Verdict** | **VALID** — finding is correct; structural fix is preferable to renaming |

---

### AA-3: `PaymentInfo.status` — String Instead of Enum

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/orders/domain/OrderRepository.kt:92` ✓ Verified |
| **Declaration** | `val status: String` inside `data class PaymentInfo` ✓ Verified |
| **Backend enum** | `PaymentStatus` enum exists in `app/modules/payments/models/entities.py` with 6 values: PENDING, COMPLETED, COLLECTED, FAILED, CANCELLED, REFUNDED |
| **Classification** | **Type-Safety Issue** (NOT a naming defect) |
| **Severity** | MEDIUM ✓ Correct |
| **Proposed fix** | Create `PaymentStatus` enum in Android; change `PaymentInfo.status` from `String` to `PaymentStatus` |
| **Rationale** | This is not a naming problem. The property name `status` is correct and clear. The issue is that the type is `String` when a `PaymentStatus` enum already exists in the backend. Using String loses compile-time type safety and allows invalid values. The backend has 6 payment status values; Android should mirror this. |
| **Affected references** | `OrderMapper.kt` — needs `PaymentStatus.fromString()` mapping; `OrderDetailsScreen.kt` — may reference status string for display |
| **Implementation risk** | Low — internal domain model; mapper handles conversion |
| **Verdict** | **VALID but MISCLASSIFIED** — this is a type-safety improvement, not a naming defect |

---

### AA-4: `StatusHistoryEntry.statusType` — String Instead of Enum

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/orders/domain/OrderRepository.kt:77` ✓ Verified |
| **Declaration** | `val statusType: String` (comment: `"order" or "fulfillment"`) ✓ Verified |
| **Backend** | `OrderStatusHistory.status_type: str` — the backend ALSO uses a string, not an enum |
| **Classification** | **Domain-Modeling Issue** (NOT a naming defect) |
| **Severity** | MEDIUM ✓ Correct |
| **Proposed fix** | Create `StatusHistoryType` enum with `ORDER`, `FULFILLMENT`; change type from `String` to `StatusHistoryType` |
| **Rationale** | This is not a naming problem. The property name `statusType` is correct. The issue is that only two valid values exist (`"order"` or `"fulfillment"`), yet both backend and Android use a plain String. The backend validates via `__post_init__` (`if self.status_type not in ("order", "fulfillment")`). Android should create an enum for type safety even though the backend does not have one. |
| **Affected references** | `OrderMapper.kt` — needs mapping; `OrderDetailsScreen.kt` — may display status type |
| **Implementation risk** | Low — internal domain model |
| **Verdict** | **VALID but MISCLASSIFIED** — this is a domain-modeling improvement, not a naming defect |

---

### AA-5: `QuotaSafeguard.value` — Generic `value` Property

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/aiusage/domain/AiUsageRepository.kt:50` ✓ Verified |
| **Declaration** | `data class QuotaSafeguard(val title: String, val value: String, val description: String, val status: String)` ✓ Verified |
| **Context** | AI usage feature — the backend API is not yet implemented; this is a placeholder model |
| **Classification** | **Naming Convention Defect** (generic identifier) |
| **Severity** | LOW ✓ Correct |
| **Proposed fix** | Rename to `displayValue` or `quotaValue` |
| **Rationale** | `value` is generic. In the `QuotaSafeguard` context with `title`, `value`, `description`, and `status`, the `value` field represents the quota display string (e.g., "80%", "500/1000 requests"). `displayValue` would be clearer. However, this is a placeholder model for an unimplemented API — the field may change when the real API is available. |
| **Implementation risk** | Negligible — feature is not yet implemented; no UI consumes this data |
| **Verdict** | **VALID but DEFERRABLE** — the AI usage API is not yet implemented; renaming can wait until the real data model is known |

---

### AA-6: `ProductKpi.value` — Generic `value` Property

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/inventory/presentation/InventoryScreen.kt:75` ✓ Verified |
| **Declaration** | `data class ProductKpi(val title: String, val value: String, val delta: String, val deltaColor: Color, val icon: ImageVector, val iconTint: Color)` ✓ Verified |
| **Usage sites** | 3 `ProductKpi` instances created at lines 436, 444, 452; displayed at line 788: `text = kpi.value` |
| **Context** | KPI cards show "Active SKUs", "Total Products", "Low Stock" with numeric values |
| **Classification** | **Naming Convention Defect** (generic identifier) |
| **Severity** | LOW ✓ Correct |
| **Proposed fix** | Rename to `displayValue` |
| **Rationale** | `value` is generic. In the `ProductKpi` context with `title`, `value`, `delta`, `deltaColor`, `icon`, `iconTint`, the `value` field is the primary metric display string (e.g., "42", "156", "3"). `displayValue` would be clearer. However, the surrounding context (`title` already describes what the value represents) makes `value` defensible. |
| **Affected references** | 3 creation sites + 1 display site in `InventoryScreen.kt` |
| **Implementation risk** | Negligible — internal UI model, single file |
| **Verdict** | **VALID but LOW PRIORITY** — context makes `value` understandable; rename is cosmetic |

---

### AA-7: `RecentOrder.status` — String Instead of Enum

| Field | Validation Result |
|-------|-------------------|
| **File** | `feature/dashboard/domain/DashboardRepository.kt:20` ✓ Verified |
| **Declaration** | `val status: String` inside `data class RecentOrder` ✓ Verified |
| **Existing enum** | `OrderStatus` enum exists in `feature/orders/domain/OrderStatus.kt` with 4 values: PENDING, COMPLETED, CANCELLED, REJECTED |
| **Classification** | **Type-Safety Issue** (NOT a naming defect) |
| **Severity** | LOW ✓ Correct |
| **Proposed fix** | Change `RecentOrder.status` from `String` to `OrderStatus`; update `DashboardMapper.kt` |
| **Rationale** | This is not a naming problem. The property name `status` is correct. The issue is that `OrderStatus` enum already exists but `RecentOrder` uses `String`, losing type safety for a dashboard summary. The dashboard is a lower-priority consumer than the orders feature, so this is LOW. |
| **Affected references** | `DashboardMapper.kt` — needs `OrderStatus.fromString()` mapping; `DashboardScreen.kt` — may display status |
| **Implementation risk** | Low — internal domain model |
| **Verdict** | **VALID but MISCLASSIFIED** — this is a type-safety improvement, not a naming defect |

---

### Cross-1: `FulfillmentStatus` vs `OrderFulfillmentStatus`

| Field | Validation Result |
|-------|-------------------|
| **Admin Android** | `FulfillmentStatus` enum in `OrderStatus.kt` — 6 entries: UNFULFILLED, AWAITING_CONFIRMATION, CONFIRMED, PROCESSING, OUT_FOR_DELIVERY, DELIVERED |
| **Backend** | `OrderFulfillmentStatus` enum in `orders/models/entities.py` — 5 entries: AWAITING_CONFIRMATION, CONFIRMED, PROCESSING, OUT_FOR_DELIVERY, DELIVERED |
| **Classification** | **Cross-Platform Terminology + Semantic Mismatch** |
| **Severity** | HIGH ✓ Correct |
| **Key finding** | **Android includes `UNFULFILLED` which the backend does NOT have.** This is a semantic mismatch, not just a naming difference. The Android enum has 6 entries; the backend has 5. |
| **Rationale** | Two issues: (1) naming mismatch — `FulfillmentStatus` vs `OrderFulfillmentStatus`; (2) semantic mismatch — Android's `UNFULFILLED` has no backend equivalent. The `UNFULFILLED` entry appears to be an Android-side default/fallback. The backend starts orders at `AWAITING_CONFIRMATION`. |
| **Proposed fix** | (1) Align enum names to one canonical form; (2) Decide whether `UNFULFILLED` should exist in the backend or be removed from Android |
| **Implementation risk** | Medium — cross-platform change affecting API contract interpretation |
| **Verdict** | **VALID with ADDITIONAL FINDING** — the semantic mismatch (UNFULFILLED) was not documented in the source report |

---

### C-1: Customer Android Package Naming

| Field | Validation Result |
|-------|-------------------|
| **Current package** | `com.yousef.nearaidescovery` ✓ Verified |
| **Proposed package** | `com.nearai.discovery` |
| **Classification** | **Compatibility-Sensitive Change** |
| **Severity** | HIGH ✓ Correct |
| **Three issues** | (1) Developer name `yousef` in package, (2) typo `nearaidescovery` (missing `i` in "Discovery"), (3) inconsistent with `com.nearai.admin` |
| **Migration implications** | |
| Impact | Detail |
|-------|--------|
| Application ID | Must change in `build.gradle.kts`; affects Play Store listing |
| AndroidManifest | Package attribute must update |
| Deep links | Any existing deep links would break |
| Firebase/Analytics | Project configuration may need updating |
| Hilt modules | `@Module` annotations with `@InstallIn` may reference the package |
| Navigation | `NearAiNavHost` package reference must update |
| **Risk assessment** | Medium — skeleton project with minimal code, but namespace changes have broad ripple effects |
| **Verdict** | **VALID** — all three issues confirmed; migration risk is acceptable for a skeleton project |

---

## 3. Finding Classification Summary

The source report presents all findings as "naming" issues. This validation separates them into their true categories:

| Finding | Source Classification | Correct Classification | Severity | Status |
|---------|----------------------|------------------------|----------|--------|
| AA-1 | Naming Convention | **Naming Convention Defect** | MEDIUM | Valid |
| AA-2 | Naming Convention | **Naming Convention Defect** | MEDIUM | Valid |
| AA-3 | Naming Convention | **Type-Safety Issue** | MEDIUM | Valid, misclassified |
| AA-4 | Naming Convention | **Domain-Modeling Issue** | MEDIUM | Valid, misclassified |
| AA-5 | Naming Convention | **Naming Convention Defect** | LOW | Valid, deferrable |
| AA-6 | Naming Convention | **Naming Convention Defect** | LOW | Valid, low priority |
| AA-7 | Naming Convention | **Type-Safety Issue** | LOW | Valid, misclassified |
| Cross-1 | Cross-Platform | **Cross-Platform + Semantic Mismatch** | HIGH | Valid, incomplete |
| C-1 | Compatibility | **Compatibility-Sensitive Change** | HIGH | Valid |

**Category Breakdown:**
- **Naming Convention Defects:** 4 (AA-1, AA-2, AA-5, AA-6)
- **Type-Safety Issues:** 2 (AA-3, AA-7)
- **Domain-Modeling Issues:** 1 (AA-4)
- **Cross-Platform Terminology:** 1 (Cross-1)
- **Compatibility-Sensitive Changes:** 1 (C-1)

---

## 4. Severity Reassessment

| Finding | Original Severity | Validated Severity | Change Reason |
|---------|------------------|-------------------|---------------|
| AA-1 | MEDIUM | MEDIUM | No change |
| AA-2 | MEDIUM | MEDIUM | No change |
| AA-3 | MEDIUM | MEDIUM | No change — type-safety gap for payment status is meaningful |
| AA-4 | MEDIUM | MEDIUM | No change — only two values, easy enum to create |
| AA-5 | LOW | LOW | No change — placeholder model for unimplemented API |
| AA-6 | LOW | LOW | No change — context makes `value` understandable |
| AA-7 | LOW | LOW | No change — dashboard summary, lower priority |
| Cross-1 | HIGH | HIGH | No change — now includes semantic mismatch (UNFULFILLED) |
| C-1 | HIGH | HIGH | No change — package rename has broad implications |

**All severity ratings are confirmed correct.**

---

## 5. CourierName Fix Verification

| Check | Result |
|-------|--------|
| DTO `@SerialName` | `@SerialName("courier_name") val courierName` ✓ Correct |
| Mapper | `courierName = dto.courierName` ✓ Correct |
| Domain model | `val courierName: String?` in `ShipmentInfo` ✓ Correct |
| UI reference | `shipment.courierName?.let { courierName ->` ✓ Correct |
| Backend field | `courier_name` in `OrderShipment` ✓ Matches |
| **Verdict** | **FIX CONFIRMED** — no regression |

---

## 6. Revised Implementation Roadmap

The source report's three phases are correct but need reorganization based on proper classification.

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

## 7. Project Coverage Confirmation

| Project | Status | Notes |
|---------|--------|-------|
| Admin Android (`NearAiAdminPortal`) | ✓ Fully audited | 102 production Kotlin files inspected |
| Customer Android (`NearAiDescovery`) | ✗ Not accessible | Findings referenced from prior cross-project audit |
| FastAPI Backend | ✓ Audited in prior report | Referenced for cross-platform comparison |
| Next.js Web Admin | ✓ Audited in prior report | Referenced for cross-platform comparison |

**Important:** Only Admin Android was fully audited in this report. Customer Android findings are referenced from `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`. The Backend and Web Admin were audited in the cross-project report, not this Android-specific audit.

---

## 8. Corrections Required in Source Report

The following corrections should be applied to `NEARAI_ANDROID_NAMING_AUDIT.md`:

1. **Add semantic mismatch to Cross-1:** Document that Android's `UNFULFILLED` has no backend equivalent.
2. **Reclassify AA-3, AA-4, AA-7:** Mark as type-safety/domain-modeling issues, not naming defects.
3. **Add structural alternative for AA-1, AA-2:** Note that moving to `companion object` is a valid alternative to renaming.
4. **Mark AA-5 as deferrable:** The AI usage API is not yet implemented.
5. **Update finding summary table:** Separate naming defects from type-safety and domain-modeling issues.

---

## 9. Final Verdict

### VALIDATED WITH CONDITIONS

The Android naming audit report is substantially accurate. All nine findings reference real code at correct file paths and line numbers. No already-resolved defects are presented as open. The `courierName` fix is confirmed in place.

**Conditions:**
1. The source report must reclassify AA-3, AA-4, and AA-7 as type-safety/domain-modeling issues rather than naming defects.
2. Cross-1 must document the `UNFULFILLED` semantic mismatch.
3. AA-1 and AA-2 should offer the structural fix (companion object) as the preferred remediation.
4. AA-5 should be marked as deferrable until the AI usage API is implemented.

**No findings were fabricated.** All file paths, line numbers, and declarations were verified against the current source code.

---

*This validation report supplements `NEARAI_ANDROID_NAMING_AUDIT.md` and `NEARAI_CROSS_PROJECT_NAMING_AUDIT.md`. It corrects misclassifications, adds missing semantic detail, and provides a revised implementation roadmap.*
