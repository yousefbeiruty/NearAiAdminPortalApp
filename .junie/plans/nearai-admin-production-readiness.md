---
sessionId: session-261104-151120-tnbq
---

# ADMIN_BACKEND_READINESS_AUDIT.md

## NearAI Admin Backend — Readiness Audit

**Date**: 2025-11-04  
**Backend**: FastAPI at `/Users/yousefbeiruty/PycharmProjects/NearAi-Back-End`  
**Database**: PostgreSQL (asyncpg), Alembic migrations  
**Scope**: Remaining Admin modules — Users Management, Audit Logs, System Health, Discovery Analytics, AI Usage Analytics

---

## 1. Backend Architecture Summary

### Module Structure
```
app/
  api/
    router.py              — aggregates all module routers under /api/v1
    health.py              — /health/liveness, /health/readiness
    exception_handlers.py  — global exception handling
  core/
    config.py              — Settings (Pydantic), JWT, DB, CORS, email, storage
    exceptions.py          — AuthenticationException, AuthorizationException, ValidationException
    logging.py             — structured logging setup
  infrastructure/
    database/              — engine, session, base ORM
    email/                 — email provider interface + mock
    scheduler.py           — APScheduler for reservation expiration
  middleware/
    request_id.py          — X-Request-ID generation
    request_logging.py     — request timing, status, no body/params logged
    security_headers.py    — security response headers
  modules/
    admin/                 — dashboard only
    auth/                  — register, login, refresh, logout, verify email, me
    catalog/               — products, categories, images, variants
    inventory/             — stock balances, movements, receipts, adjustments
    orders/                — order lifecycle, status history, fulfillment
    payments/              — payment creation, webhook, COD cash collection
    addresses/             — saved delivery addresses
    cart/                  — shopping cart
```

### Database Tables (18 total)
| Table | Module | Purpose |
|-------|--------|---------|
| `users` | auth | User accounts, roles, email verification |
| `refresh_sessions` | auth | Refresh token sessions with rotation |
| `verification_challenges` | auth | OTP verification codes |
| `products` | catalog | Product catalog |
| `categories` | catalog | Product categories (hierarchical) |
| `product_images` | catalog | Product image storage |
| `product_variants` | inventory | Product variants (SKU, attributes) |
| `stock_balances` | inventory | On-hand and reserved quantities |
| `stock_movements` | inventory | Stock movement history |
| `orders` | orders | Order records with delivery address |
| `order_items` | orders | Order line items |
| `order_status_history` | orders | Audit trail for order status changes |
| `idempotency_keys` | orders | Idempotency for order creation |
| `payments` | payments | Payment records |
| `webhook_events` | payments | Payment provider webhook log |
| `addresses` | addresses | Saved delivery addresses |
| `cart_items` | cart | Shopping cart items |

### Authorization
- Role-based via `require_roles("admin")` dependency
- Roles: `admin`, `customer` (UserRole enum)
- User role is loaded from database on each request (not cached in JWT)
- `get_current_user()` validates JWT, loads user from DB, checks `is_active`
- `require_authenticated_user()` additionally checks `email_verified`

---

## 2. Existing API Endpoints (Complete Inventory)

### Auth (`/api/v1/auth/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | Public | Register new user (customer role) |
| POST | `/auth/login` | Public | Login, get access + refresh tokens |
| POST | `/auth/refresh` | Public | Refresh access token |
| POST | `/auth/logout` | Public | Revoke refresh session |
| GET | `/auth/me` | Authenticated | Get current user profile |
| POST | `/auth/email-verification/send` | Public | Send verification code |
| POST | `/auth/email-verification/verify` | Public | Verify email with OTP |
| GET | `/auth/email-verification/mock-outbox/{recipient}` | Dev only | Mock email retrieval |

### Admin (`/api/v1/admin/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/admin/dashboard` | Admin | Aggregated dashboard metrics |

### Products (`/api/v1/products/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/products/` | Public | List products (paginated, filtered) |
| GET | `/products/search` | Public | Search products by query |
| GET | `/products/{id}` | Public | Get product by ID |
| POST | `/products/` | Admin | Create product |

### Categories (`/api/v1/categories/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/categories/` | Public | List categories |
| GET | `/categories/{id}` | Public | Get category by ID |
| POST | `/categories/` | Admin | Create category |

### Variants (`/api/v1/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/products/{id}/variants` | Public | List variants for product |
| GET | `/variants/by-sku/{sku}` | Public | Get variant by SKU |
| GET | `/variants/{id}` | Public | Get variant by ID |
| POST | `/products/{id}/variants` | Admin | Create variant |

### Stock (`/api/v1/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/variants/{id}/stock/receipts` | Admin | Receive stock |
| POST | `/variants/{id}/stock/adjustments` | Admin | Adjust stock |
| GET | `/variants/{id}/stock` | Public | Get stock availability |
| GET | `/variants/{id}/stock/movements` | Admin | Stock movement history |

### Orders (`/api/v1/orders/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/orders/` | Authenticated | Create order from cart |
| GET | `/orders/` | Authenticated | List user's orders (paginated) |
| GET | `/orders/{id}` | Authenticated | Get order (owner or admin) |
| GET | `/orders/admin/orders` | Admin | List all orders (paginated, filtered) |
| POST | `/orders/admin/orders/{id}/confirm` | Admin | Confirm order |
| POST | `/orders/admin/orders/{id}/process` | Admin | Start processing |
| POST | `/orders/admin/orders/{id}/dispatch` | Admin | Dispatch for delivery |
| POST | `/orders/admin/orders/{id}/deliver` | Admin | Mark delivered |
| POST | `/orders/admin/orders/{id}/cancel` | Admin | Cancel order |
| POST | `/orders/admin/orders/{id}/reject` | Admin | Reject order with reason |
| POST | `/orders/admin/expiry/process` | Admin | Expire reservations |
| POST | `/orders/checkout` | Authenticated | COD checkout |
| POST | `/orders/{id}/cancel` | Authenticated | Customer cancel own order |
| GET | `/orders/{id}/status-history` | Authenticated | Order status history |

### Payments (`/api/v1/payments/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/payments/for-order/{id}` | Authenticated | Create payment for order |
| GET | `/payments/{id}` | Authenticated | Get payment details |
| POST | `/payments/webhook` | Public (signature) | Payment provider webhook |
| POST | `/payments/{id}/collect-cash` | Admin | Collect COD cash |

### Health (`/api/v1/health/*`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/health/liveness` | Public | Liveness probe |
| GET | `/health/readiness` | Public | Readiness probe (DB check) |

---

## 3. Capability Matrix for Remaining Admin Modules

### 3.1 Users Management

| Feature | Status | Details |
|---------|--------|---------|
| List all users | **MISSING** | No admin endpoint exists |
| Search users | **MISSING** | No admin endpoint exists |
| Filter by role | **MISSING** | No admin endpoint exists |
| Filter by status | **MISSING** | No admin endpoint exists |
| Get user by ID | **MISSING** | No admin endpoint exists |
| Update user role | **MISSING** | No admin endpoint exists |
| Suspend/activate user | **MISSING** | No admin endpoint exists |
| View user orders | **PARTIAL** | `GET /orders/admin/orders` lists all orders, but no user-specific admin view |
| View user sessions | **PARTIAL** | `refresh_sessions` table exists, no admin endpoint |
| Revoke user sessions | **PARTIAL** | `revoke_all_user_sessions()` exists in repository, no admin endpoint |

**Database**: `users` table has all needed fields (id, email, role, is_active, email_verified, created_at, updated_at). No schema changes needed.

**Repository**: `SQLAlchemyUserRepository` has `get_by_id`, `get_by_email`, `create_user`, `email_exists`, `update_email_verified`. Missing: list_all, search, update_role, update_active.

### 3.2 Audit Logs

| Feature | Status | Details |
|---------|--------|---------|
| Order status changes | **PARTIAL** | `order_status_history` table + `GET /orders/{id}/status-history` endpoint exists, but only per-order, not global |
| Admin action audit trail | **MISSING** | No general audit log for admin actions |
| Stock change audit | **PARTIAL** | `stock_movements` table exists with reason, but no admin endpoint for global view |
| User management audit | **MISSING** | No audit of role changes, suspensions |
| Product change audit | **MISSING** | No audit of product creates/updates/deletes |
| Audit log export | **MISSING** | No export functionality |
| Filter by actor | **MISSING** | No global audit log |
| Filter by action type | **MISSING** | No global audit log |
| Filter by time range | **MISSING** | No global audit log |
| Paginated audit stream | **MISSING** | No global audit log |

**Database**: `order_status_history` exists with actor_id, actor_role, reason. `stock_movements` exists with reason. No general `audit_logs` table.

**Key finding**: The backend has domain-specific audit trails (order status, stock movements) but no centralized audit log. A general audit log table is needed.

### 3.3 System Health

| Feature | Status | Details |
|--------|--------|---------|
| Liveness probe | **IMPLEMENTED** | `GET /health/liveness` — always returns 200 |
| Readiness probe | **IMPLEMENTED** | `GET /health/readiness` — checks DB connectivity |
| Service health dashboard | **MISSING** | No admin endpoint for service status |
| Deployment info | **MISSING** | No endpoint for version, commit, uptime |
| Database health | **PARTIAL** | Readiness probe checks DB, but no admin endpoint |
| Error rate monitoring | **MISSING** | No error rate tracking |
| Request latency | **MISSING** | Request logging exists but no aggregation |
| Incident journal | **MISSING** | No incident tracking |
| Background job status | **PARTIAL** | APScheduler runs expiration job, no status endpoint |
| Infrastructure integrations | **MISSING** | No health checks for email, storage, payment providers |

**Database**: No changes needed. Health data is runtime/infrastructure, not stored.

**Infrastructure**: `RequestLoggingMiddleware` logs timing but doesn't aggregate. No metrics collection.

### 3.4 Discovery Analytics

| Feature | Status | Details |
|--------|--------|---------|
| Discovery pipeline | **MISSING** | No discovery module exists |
| Search telemetry | **MISSING** | No search tracking |
| Trace inspection | **MISSING** | No trace infrastructure |
| Discovery events | **MISSING** | No event collection |

**Database**: No discovery-related tables exist.

**Key finding**: The backend is a pure e-commerce platform. There is no AI-powered discovery, search, or recommendation system. The Android app's Discovery screen has no backend to connect to.

### 3.5 AI Usage Analytics

| Feature | Status | Details |
|--------|--------|---------|
| AI provider integration | **MISSING** | No AI module exists |
| Usage tracking | **MISSING** | No AI usage tracking |
| Cost tracking | **MISSING** | No AI cost tracking |
| Budget management | **MISSING** | No budget system |
| Provider distribution | **MISSING** | No multi-provider setup |
| Telemetry | **MISSING** | No AI telemetry |

**Database**: No AI-related tables exist.

**Key finding**: The backend has no AI integration whatsoever. The Android app's AI Usage screen has no backend to connect to.

---

## 4. Security Gaps

### 4.1 Critical

1. **No admin action audit log** — Admin actions (stock adjustments, order status changes, product edits) are not centrally logged. An admin could make changes without any traceable record beyond domain-specific tables.

2. **No user management endpoints** — There is no way for an admin to manage users through the API. Admins cannot suspend compromised accounts, change roles, or view user lists.

3. **No rate limiting** — No rate limiting on any endpoint, including auth endpoints. Login and registration are vulnerable to brute-force attacks.

4. **No input sanitization on admin endpoints** — Admin endpoints accept user input (reasons, descriptions) without sanitization. Potential for XSS if rendered in admin UI.

### 4.2 Moderate

5. **Customer email exposed in dashboard** — `GET /admin/dashboard` returns `user_email` in recent orders. This is PII exposure. Consider masking or removing.

6. **No audit log tampering protection** — `order_status_history` and `stock_movements` can be modified by any admin via the database. No row-level protection against deletion.

7. **No session management for admins** — While `revoke_all_user_sessions()` exists in the repository, there is no admin endpoint to revoke sessions for a specific user.

8. **No IP-based access control** — No restriction on admin access by IP or network.

### 4.3 Low

9. **Mock email endpoint in dev** — `GET /auth/email-verification/mock-outbox/{recipient}` is correctly gated behind `DEBUG=true` and `EMAIL_PROVIDER=mock`. No production risk.

10. **Request logging excludes sensitive data** — `RequestLoggingMiddleware` correctly excludes query params, bodies, and auth headers. Good practice.

---

## 5. Recommended Implementation Phases

### ✓ Step 1: Users Management — Backend Endpoints

**Why**: Required to operate a real e-commerce marketplace. Admins need to manage user accounts.

**Database changes**: None. The `users` table has all needed fields.

**Repository changes**:
- Add `list_users(page, size, search, role, is_active)` to `UserRepository`
- Add `update_role(user_id, role)` to `UserRepository`
- Add `update_active(user_id, is_active)` to `UserRepository`
- Add `get_by_id()` already exists

**New endpoints**:
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/admin/users` | Admin | List users (paginated, search, filters) |
| GET | `/admin/users/{id}` | Admin | Get user by ID |
| PATCH | `/admin/users/{id}` | Admin | Update user (role, active status) |
| POST | `/admin/users/{id}/revoke-sessions` | Admin | Revoke all sessions for a user |

**Authorization**: `require_roles("admin")` on all endpoints.

**Business logic**:
- List users with pagination (page, size), search by email, filter by role and active status
- Update role: validate target role is valid enum value
- Update active: deactivate prevents login
- Revoke sessions: calls existing `revoke_all_user_sessions()`

**Android integration**:
- New `UsersApiService`, `UsersRepositoryImpl`, `UsersMapper`, `UsersModule`
- New `UsersViewModel` with StateFlow for users list, search, filters
- Connect `UsersScreen` to ViewModel

**Tests**:
- Unit tests for repository methods
- Integration tests for endpoints with admin/non-admin auth
- ViewModel tests for loading, filtering, admin actions

### ✓ Step 2: Audit Logs (High Priority)

**Why**: Required for accountability and compliance. Admin actions must be traceable.

**Database changes**:
- New `audit_logs` table:
  - `id` (UUID, PK)
  - `actor_id` (UUID, FK → users.id, nullable for system actions)
  - `actor_role` (String(20))
  - `action` (String(100)) — e.g., "user.suspend", "stock.adjust", "order.cancel"
  - `target_type` (String(50)) — e.g., "user", "order", "product", "stock"
  - `target_id` (UUID, nullable)
  - `details` (JSON) — action-specific details (before/after values)
  - `ip_address` (String(45)) — request IP
  - `created_at` (DateTime, indexed)

**New endpoints**:
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/admin/audit-logs` | Admin | List audit logs (paginated, filters) |
| POST | `/admin/audit-logs/export` | Admin | Export audit logs (CSV/JSON) |

**Authorization**: `require_roles("admin")`. Admins cannot delete their own logs.

**Implementation approach**:
- Create `AuditLogService` as a dependency-injected service
- Call `audit_log.record(action, target_type, target_id, details)` from handlers after sensitive operations
- Stock adjustments already record in `stock_movements` — also write to `audit_logs`
- Order status changes already record in `order_status_history` — also write to `audit_logs`
- New: user management actions write to `audit_logs`
- Product create/update writes to `audit_logs`

**Android integration**:
- New `AuditLogsApiService`, `AuditLogsRepositoryImpl`, `AuditLogsMapper`, `AuditLogsModule`
- New `AuditLogsViewModel` with pagination and filtering
- Connect `AuditLogsScreen` to ViewModel

**Tests**:
- Unit tests for audit log recording
- Integration tests for audit log retrieval
- Verify audit logs are created for stock adjustments, order changes, user management

### ✓ Step 3: System Health (Medium Priority)

**Why**: Admins need visibility into system status for operational awareness.

**Database changes**: None.

**New endpoints**:
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/admin/health` | Admin | System health overview |

**Response schema**:
```json
{
  "app_version": "1.0.0",
  "uptime_seconds": 86400,
  "database": {"status": "healthy", "last_check": "2025-11-04T12:00:00Z"},
  "scheduler": {"status": "running", "last_expiration_run": "2025-11-04T11:55:00Z", "orders_expired": 0},
  "storage": {"status": "healthy", "backend": "local"},
  "email": {"status": "healthy", "provider": "mock"}
}
```

**Authorization**: `require_roles("admin")`.

**Implementation**:
- Check DB connectivity (reuse readiness probe logic)
- Check scheduler status (APScheduler.running)
- Check storage backend (config + test write)
- Check email provider (config)
- Report app start time for uptime calculation

**Android integration**:
- New `SystemHealthApiService`, `SystemHealthRepositoryImpl`, `SystemHealthMapper`, `SystemHealthModule`
- New `SystemHealthViewModel`
- Connect `SystemHealthScreen` to ViewModel

**Tests**:
- Unit tests for health checks
- Integration test for endpoint

### ✓ Step 3: System Health (Medium Priority)

**Finding**: The backend is a pure e-commerce platform with no AI, discovery, search, or recommendation features. These modules do not exist and are not planned.

**Recommendation**:
- **Option A**: Remove these screens from the Android app if they are not part of the product roadmap.
- **Option B**: Keep the screens as placeholders with "Feature not available" messages until the backend implements AI/discovery capabilities.
- **Option C**: Implement the AI/discovery backend first, then connect the Android app.

**Decision required from product team**: Whether AI/discovery features are in scope for the NearAI platform.

---

## 6. Summary of Findings

| Module | Backend Status | Android Status | Effort |
|--------|---------------|----------------|--------|
| Users Management | Missing endpoints, DB ready | UI-only, hardcoded | Medium — 4 new endpoints, repository extensions |
| Audit Logs | Partial (domain-specific), needs central table | UI-only, hardcoded | High — new table, service, 2 endpoints, handler integration |
| System Health | Partial (probes exist), needs admin endpoint | UI-only, hardcoded | Low — 1 new endpoint, runtime checks |
| Discovery Analytics | **Does not exist** | UI-only, hardcoded | N/A — requires backend first |
| AI Usage Analytics | **Does not exist** | UI-only, hardcoded | N/A — requires backend first |

### Immediate Priorities
1. **Users Management** — Required for marketplace operations
2. **Audit Logs** — Required for accountability and security
3. **System Health** — Low effort, high operational value
4. **Discovery/AI** — Decision needed on product roadmap

### Security Recommendations
1. Implement centralized audit logging before deploying admin actions to production
2. Add rate limiting to auth endpoints
3. Mask customer email in dashboard responses
4. Add admin session management endpoint
5. Protect audit log tables from admin deletion (row-level security or separate schema)