---
sessionId: session-261119-101239-ouxu
---

# Overview & Scope

## WP-18 — Cross-Application Development Integration and First Merchant COD E2E

### Objective
Execute a complete cross-application integration verification of NearAI using the existing development database and dev API on port `8081`. Prove that two independent merchant tenants can operate through their assigned storefronts and administrative interfaces, with correct tenant isolation, and that a customer can complete a COD order through the complete supported business lifecycle.

**Environment:** DEVELOPMENT only. This is not staging or production verification.

### Scope

**In Scope:**
- Create two disposable merchant tenants (wp18-merchant-a, wp18-merchant-b) via HTTP API
- Verify tenant resolution and storefront routing via backend middleware
- Execute complete COD order lifecycle for both tenants via HTTP API
- Verify cross-tenant isolation (customer, merchant admin, platform admin)
- Verify Customer Android app (NearAiDescovery) against dev API via emulator
- Verify Admin Android app (NearAiAdminPortal) against dev API via emulator
- Verify Web Admin app against dev API via browser
- Verify monetary precision (JOD, exact decimal, no floating-point rounding)
- Create comprehensive report at `docs/architecture/wp-18-cross-application-development-integration-report.md`
- Clean up test resources safely

**Out of Scope:**
- E2E database migration fix (separate work item)
- New feature implementation
- Staging or production deployment
- Web Admin code changes
- Customer Android code changes
- Backend code changes (unless genuine defect found in integration scope)

### Applications Under Test

| Application | Repository | Test Method |
|---|---|---|
| FastAPI Backend | NearAi-Back-End | HTTP API requests (curl) |
| Customer Android | NearAiDescovery | Emulator run against dev API |
| Admin Android | NearAiAdminPortal | Emulator run against dev API |
| Web Admin | NearAi-Web-Admin | Browser-based verification |

### Key Architectural Components

**Tenant Resolution** (Backend):
- `app/middleware/tenant_resolution.py` — resolves tenant from Host/X-Forwarded-Host header
- `app/modules/tenants/services/resolution.py` — TenantResolutionService with cache
- `app/core/config.py` — `TENANT_STOREFRONT_DOMAIN` config
- Routes bypassing tenant resolution: `/health`, `/api/v1/auth/*`, `/api/v1/platform/*`, `/api/v1/admin/tenants/*`

**Customer Android** (NearAiDescovery):
- `core/di/AppModule.kt` — Retrofit configured with `BuildConfig.STOREFRONT_URL`
- `gradle/wrapper/dev.properties` — `STOREFRONT_URL=http://10.0.2.2:8081/api/v1/`
- No client-side `tenant_id`; tenant derived server-side from Host header

**Admin Android** (NearAiAdminPortal):
- `core/network/NetworkModule.kt` — Retrofit with `BuildConfig.BASE_URL`
- `gradle/wrapper/dev.properties` — `BASE_URL=http://10.0.2.2:8081/`
- `core/security/AdminRoleValidator.kt` — validates `platform_admin` and `admin` roles only
- `core/security/SessionManager.kt` — session state management, rejects non-admin roles
- Platform-only app: dashboard, analytics, audit logs, system health, user inspection

**Web Admin** (NearAi-Web-Admin):
- Merchant-admin focused: dashboard, products, orders, customers, tenant settings
- Browser-based verification against dev API

# Delivery Steps

### ✓ Step 1: Stage 1: Verify dev environment and create test tenants
Verify the dev API is reachable, confirm database state, and create two disposable merchant tenants with products and test accounts.

- Confirm dev API on port 8081 responds to `/docs` and auth endpoints
- Check existing tenant records to avoid conflicts with wp18-merchant-a and wp18-merchant-b names
- Create Tenant A (wp18-merchant-a) via `POST /api/v1/admin/tenants/` or equivalent admin endpoint
- Create Tenant B (wp18-merchant-b) via the same endpoint
- Create merchant admin accounts for each tenant with unique test emails
- Create at least 2 active products per tenant with JOD prices, stock, and variants if required
- Verify tenant storefront hostnames resolve correctly using existing TENANT_STOREFRONT_DOMAIN mechanism
- Document all created resource IDs, tenant IDs, storefront hostnames, and account roles in a temporary tracking file
- Record the backend commit hash and database migration state (Alembic heads)