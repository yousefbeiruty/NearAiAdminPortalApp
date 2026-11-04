# Discovery Analytics — Backend Investigation & Readiness Audit

> **Date**: 2026-11-04
> **Scope**: Backend investigation. No source code modified.
> **Backend**: FastAPI at `/Users/yousefbeiruty/PycharmProjects/NearAi-Back-End/`
> **Database**: PostgreSQL (confirmed via SQLAlchemy ORM models and Alembic migrations)
> **Admin Portal**: Android app at `/Users/yousefbeiruty/AndroidStudioProjects/NearAiAdminPortal/`

---

## 0. Root Cause: Why the Endpoint Returns 404

**Request**: `GET /api/v1/admin/analytics/discovery/overview?days=7&compare=true`

**Response**: `{"detail": "Not Found"}`

**Root cause**: The endpoint does not exist. The backend has no analytics router, no analytics controller, no analytics handler, and no analytics database tables.

### Evidence

The backend router (`app/api/router.py`) registers exactly 12 routers under `/api/v1`:

| Router | Prefix | Tags | Controller file |
|---|---|---|---|
| `health_router` | (none) | health | `app/api/health.py` |
| `auth_router` | (none) | auth | `app/modules/auth/controllers/auth_controller.py` |
| `admin_router` | `/admin` | admin | `app/modules/admin/controllers/dashboard_controller.py` |
| `admin_system_health_router` | `/admin` | admin-health | `app/modules/admin/controllers/system_health_controller.py` |
| `admin_users_router` | `/admin` | admin-users | `app/modules/admin/controllers/users_controller.py` |
| `admin_audit_router` | `/admin` | admin-audit | `app/modules/admin/controllers/audit_controller.py` |
| `addresses_router` | `/addresses` | addresses | `app/modules/addresses/controllers/address_controller.py` |
| `product_router` | `/products` | products | `app/modules/catalog/controllers/product_controller.py` |
| `image_controller` | `/products` | product images | `app/modules/catalog/controllers/image_controller.py` |
| `category_router` | `/categories` | categories | `app/modules/catalog/controllers/category_controller.py` |
| `variant_router` | (none) | variants | `app/modules/inventory/controllers/variant_controller.py` |
| `stock_router` | (none) | stock | `app/modules/inventory/controllers/stock_controller.py` |
| `cart_router` | `/cart` | cart | `app/modules/cart/controllers/cart_controller.py` |
| `orders_router` | `/orders` | orders | `app/modules/orders/controllers/order_controller.py` |
| `payments_router` | `/payments` | payments | `app/modules/payments/controllers/payment_controller.py` |

**None of these routers contains an `/analytics` path.** The path `/api/v1/admin/analytics/discovery/overview` would require a router with prefix `/admin/analytics` or a route at `/admin/analytics/discovery/overview` — neither exists.

### Admin module structure

The admin module (`app/modules/admin/`) contains:

- **4 controllers**: `dashboard_controller.py`, `audit_controller.py`, `system_health_controller.py`, `users_controller.py`
- **3 handlers**: `dashboard_handlers.py`, `audit_handlers.py`, `users_handlers.py`, `health_handlers.py`
- **1 infrastructure service**: `audit_service.py`, `health_service.py`
- **1 ORM model**: `audit_logs` table
- **1 DTO file**: `dtos.py` (DashboardMetricsDTO, RecentOrderDTO, RecentUserDTO, LowStockAlertDTO)

**No analytics controller, handler, or model exists.**

---

## 1. Existing Analytics Endpoints

### 1.1 What exists today

The only admin analytics-like endpoint is the **dashboard**:

| Endpoint | Method | Description |
|---|---|---|
| `GET /api/v1/admin/dashboard` | GET | Aggregate counts: total users, products, categories, orders by status, total revenue, recent orders, recent registrations, low stock alerts |
| `GET /api/v1/admin/system-health` | GET | Application uptime, database connectivity, file storage, scheduler, email provider status |
| `GET /api/v1/admin/users` | GET | Paginated user listing with search, role filter, active status filter |
| `GET /api/v1/admin/users/{id}` | GET | Single user detail |
| `PATCH /api/v1/admin/users/{id}` | PATCH | Update user role or active status |
| `POST /api/v1/admin/users/{id}/revoke-sessions` | POST | Revoke all refresh sessions for a user |
| `GET /api/v1/admin/audit-logs` | GET | Paginated audit logs with filters (actor, action, resource, date range) |

### 1.2 Dashboard metrics detail

`GET /api/v1/admin/dashboard` returns (from `DashboardMetricsResponse` schema):

```json
{
  "total_users": 0,
  "total_products": 0,
  "total_categories": 0,
  "total_orders": 0,
  "pending_orders": 0,
  "completed_orders": 0,
  "cancelled_orders": 0,
  "rejected_orders": 0,
  "total_revenue": 0.0,
  "revenue_currency": "USD",
  "recent_orders": [...],
  "recent_users": [...],
  "low_stock_alerts": [...]
}
```

These are **static aggregate counts** computed via SQL `COUNT()` queries at request time. There is no time-series data, no date-range filtering, and no period comparison.

### 1.3 What does NOT exist

| Capability | Status |
|---|---|
| **Customer discovery events** | ❌ None |
| **Search event tracking** | ❌ None |
| **Product view tracking** | ❌ None |
| **Cart operations tracking** | ❌ None |
| **Checkout / purchase event tracking** | ❌ None |
| **Wishlist tracking** | ❌ None |
| **Product comparison tracking** | ❌ None |
| **Category view tracking** | ❌ None |
| **Conversion funnel analytics** | ❌ None |
| **Anonymous session tracking** | ❌ None |
| **Event ingestion endpoint** | ❌ None |
| **Aggregated analytics storage** | ❌ None |
| **Background aggregation jobs** | ❌ None |
| **Analytics admin API** | ❌ None |
| **`/api/v1/admin/analytics/*` endpoints** | ❌ None |

### 1.4 Key finding

The existing audit log infrastructure tracks **admin actions only** (who did what to which resource). It is not designed for customer-facing behavioral events. The dashboard returns **static aggregate counts** from database queries, not time-series analytics. There is no event ingestion pipeline, no session tracking, and no customer behavioral data collection.

---

## 2. Existing Database Schema

### 2.1 All database tables (from Alembic migrations and ORM models)

| Table | Module | Purpose | Key fields |
|---|---|---|---|
| `users` | auth | Customer and admin accounts | id, email, role, is_active, email_verified, created_at |
| `verification_challenges` | auth | OTP/MFA challenges | user_id, purpose, otp_hash, expires_at, consumed |
| `refresh_sessions` | auth | JWT refresh token storage | user_id, token_hash, expires_at, revoked |
| `products` | catalog | Product catalog | id, name, slug, price_amount, price_currency, status, category_id |
| `categories` | catalog | Product categories | id, name, slug, parent_id |
| `product_images` | catalog | Product image storage | product_id, url, is_primary, display_order |
| `product_variants` | inventory | Product variants (SKUs) | product_id, sku, attributes, status |
| `stock_balances` | inventory | Current stock levels | variant_id, on_hand, reserved |
| `stock_movements` | inventory | Stock change history | variant_id, quantity_delta, movement_type, reason |
| `carts` | cart | User shopping carts | user_id (unique), created_at, updated_at |
| `cart_items` | cart | Cart line items | cart_id, variant_id, quantity, unit_price_amount |
| `orders` | orders | Customer orders | user_id, status, fulfillment_status, total_amount, currency, delivery address fields |
| `order_items` | orders | Order line items | order_id, variant_id, product_name, sku, quantity, unit_price_amount |
| `order_status_history` | orders | Order status transitions | order_id, previous_status, new_status, status_type, actor_id, reason |
| `idempotency_keys` | orders | Order creation idempotency | user_id, key, order_id |
| `payments` | payments | Payment records | order_id (unique), payment_method, provider, amount, status, collected_at |
| `webhook_events` | payments | Payment webhook deduplication | provider, provider_event_id, event_type |
| `audit_logs` | admin | Admin action audit trail | actor_id, actor_role, action, resource_type, resource_id, changes (JSONB) |
| `addresses` | addresses | Customer delivery addresses | user_id, recipient_name, phone, city, area, street, etc. |

### 2.2 Tables relevant to analytics

| Table | Analytics value | Available data |
|---|---|---|
| `orders` | **Purchase volume, revenue, conversion** | user_id, status, total_amount, currency, created_at, fulfillment_status |
| `order_items` | **Product-level sales data** | order_id, variant_id, product_name, sku, quantity, unit_price_amount |
| `order_status_history` | **Order lifecycle tracking** | Status transitions with timestamps |
| `payments` | **Payment confirmation** | order_id, status, collected_at, provider |
| `carts` / `cart_items` | **Current cart state** | user_id, variant_id, quantity, unit_price_amount |
| `products` | **Product catalog reference** | id, name, category_id, status, price |
| `categories` | **Category reference** | id, name, parent_id |
| `users` | **User registration data** | id, email, role, created_at |
| `audit_logs` | **Admin action history** | action, resource_type, changes (JSONB) |

### 2.3 What is NOT in the database

| Missing table | Purpose |
|---|---|
| `discovery_events` | Raw customer behavioral events |
| `discovery_sessions` | Session identification and tracking |
| `discovery_daily_metrics` | Pre-aggregated daily analytics metrics |
| `wishlists` / `wishlist_items` | Wishlist tracking |
| `product_comparisons` | Product comparison tracking |

---

## 3. Available Business Data for Analytics

### 3.1 Data that CAN power analytics today

Even without event tracking, the existing database contains **rich business data** that can answer several analytics questions:

#### Order-based analytics (available now)

| Metric | Source | Query approach |
|---|---|---|
| **Total orders by date** | `orders.created_at` | `COUNT(*) GROUP BY DATE(created_at)` |
| **Revenue by date** | `orders.total_amount` | `SUM(total_amount) GROUP BY DATE(created_at)` |
| **Orders by status** | `orders.status` | `COUNT(*) FILTER (WHERE status = 'completed')` |
| **Orders by fulfillment** | `orders.fulfillment_status` | `COUNT(*) GROUP BY fulfillment_status` |
| **Average order value** | `orders.total_amount` | `AVG(total_amount)` |
| **Orders per user** | `orders.user_id` | `COUNT(DISTINCT orders) / COUNT(DISTINCT users)` |
| **Revenue by currency** | `orders.currency` | `SUM(total_amount) GROUP BY currency` |
| **Order lifecycle time** | `order_status_history` | Time between status transitions |
| **Payment success rate** | `payments.status` | `COUNT(paid) / COUNT(total)` |

#### Product sales analytics (available now)

| Metric | Source | Query approach |
|---|---|---|
| **Best-selling products** | `order_items` JOIN `product_variants` | `SUM(quantity) GROUP BY variant_id` |
| **Revenue by product** | `order_items.subtotal_amount` | `SUM(subtotal_amount) GROUP BY variant_id` |
| **Revenue by category** | `order_items` → `product_variants` → `products` → `categories` | `SUM(subtotal_amount) GROUP BY category_id` |
| **Products per order** | `order_items` | `COUNT(DISTINCT variant_id) / COUNT(DISTINCT order_id)` |

#### User analytics (available now)

| Metric | Source | Query approach |
|---|---|---|
| **New registrations by date** | `users.created_at` | `COUNT(*) GROUP BY DATE(created_at)` |
| **Active customers** | `orders.user_id` + `orders.created_at` | `COUNT(DISTINCT user_id)` in date range |
| **Repeat purchase rate** | `orders.user_id` | Users with >1 order / total ordering users |
| **Customer lifetime value** | `orders` per user | `SUM(total_amount) GROUP BY user_id` |

#### Inventory analytics (available now)

| Metric | Source | Query approach |
|---|---|---|
| **Low stock products** | `stock_balances` | `on_hand - reserved < threshold` |
| **Stock movement trends** | `stock_movements` | `SUM(quantity_delta) GROUP BY DATE(created_at)` |
| **Variant availability** | `product_variants.status` | `COUNT(*) FILTER (WHERE status = 'active')` |

### 3.2 Data that CANNOT be derived from existing tables

| Metric | Why it's missing |
|---|---|
| **Search volume** | No search event tracking. The product search endpoint (`GET /api/v1/products/?search=...`) does not log queries. |
| **Zero-result searches** | No search event tracking. |
| **Popular search terms** | No search event tracking. |
| **Product views** | No view tracking. The product detail endpoint does not log views. |
| **Most viewed products** | No view tracking. |
| **Category views** | No category browsing tracking. |
| **Cart add events** | Cart items exist, but there is no history of adds/removes — only current state. |
| **Cart abandonment rate** | No checkout start event. Carts exist but there is no "checkout started" marker. |
| **Conversion funnel** | No behavioral events to build a funnel from search → view → cart → purchase. |
| **Search-to-view rate** | No search or view events. |
| **Product-view-to-cart rate** | No view or cart add events. |
| **Guest customer behavior** | All existing data requires `user_id`. Guest sessions are not tracked. |
| **Session duration** | No session tracking. |
| **Page navigation patterns** | No pageview tracking. |

### 3.3 Summary: What NearAI can measure today

| Category | Available | Missing |
|---|---|---|
| **Business outcomes** | ✅ Orders, revenue, payments, fulfillment | — |
| **Product sales** | ✅ Best-sellers, revenue by product/category | — |
| **User growth** | ✅ Registrations, active customers | — |
| **Inventory** | ✅ Stock levels, movements, low stock alerts | — |
| **Customer discovery** | ❌ None | Search, views, clicks, cart adds, funnel |
| **Behavioral analytics** | ❌ None | All behavioral events |
| **Guest analytics** | ❌ None | All guest session data |
| **Time-series analytics** | ❌ None | No historical metrics storage |
| **Period comparison** | ❌ None | Dashboard has no date-range filtering |

---

## 4. Events We Need to Collect

### 4.1 Event taxonomy

Each event is classified as either a **behavioral event** (something the customer did) or a **business event** (something the system confirmed). This distinction is critical: recording an event is not the same as a business operation succeeding.

#### Behavioral events (customer intent)

| Event | Trigger | Data captured |
|---|---|---|
| `search_performed` | Customer submits a search query | query, category filter, price range, location context |
| `product_viewed` | Customer opens product detail | product_id, variant_id, referrer (search, category, direct) |
| `product_clicked` | Customer taps a product in a list | product_id, position in list, source (search results, category page, recommendations) |
| `category_viewed` | Customer browses a category | category_id, filters applied |
| `product_added_to_cart` | Customer adds product to cart | product_id, variant_id, quantity, price at time of add |
| `product_removed_from_cart` | Customer removes product from cart | product_id, variant_id, quantity, time_in_cart_seconds |
| `product_saved_to_wishlist` | Customer saves product | product_id, variant_id |
| `comparison_performed` | Customer compares products | product_ids compared, attributes compared |
| `checkout_started` | Customer proceeds to checkout | cart_total, item_count, currency |

#### Business events (system-confirmed outcomes)

| Event | Trigger | Data captured |
|---|---|---|
| `search_results_returned` | Backend returns search results | query, result_count, latency_ms, has_products |
| `purchase_completed` | Order created AND payment confirmed (derived from order lifecycle) | order_id, total_amount, currency, item_count, products |
| `order_confirmed` | Admin/system confirms pending order | order_id, confirmed_by |
| `order_delivered` | Order marked as delivered | order_id, delivered_at |

### 4.2 Critical distinction: event recorded vs operation succeeded

- **`product_added_to_cart`** is recorded when the customer clicks "Add to Cart" — it does not mean the cart operation succeeded (stock may be unavailable, session may expire).
- **`purchase_completed`** must be derived from the order lifecycle: an order exists with status `confirmed` or beyond AND payment is confirmed. It is **not** triggered by a "Place Order" button click.
- **`search_results_returned`** is recorded by the backend after search completes — it captures whether results were found and how long it took.

### 4.3 Events NOT needed (out of scope)

- Raw pageview tracking (no need to store every navigation event)
- Complete raw search history without aggregation (privacy risk, no clear business need)
- Mouse movement or scroll depth tracking
- Device fingerprinting beyond session identification

---

## 5. Proposed Event Architecture

### 5.1 Event schema

```json
{
  "event_id": "uuid-v4",
  "event_type": "search_performed",
  "session_id": "uuid-v4",
  "customer_id": "uuid | null",
  "timestamp": "2025-11-04T10:30:00Z",
  "metadata": {
    "query": "coffee near me",
    "category_id": "uuid | null",
    "product_id": "uuid | null",
    "variant_id": "uuid | null",
    "result_count": 0,
    "referrer": "search|category|direct|recommendation",
    "position": 0,
    "quantity": 0,
    "price_amount": 0.0,
    "price_currency": "USD",
    "cart_total": 0.0,
    "item_count": 0,
    "order_id": "uuid | null",
    "latency_ms": 0,
    "has_products": true,
    "platform": "web|ios|android",
    "user_agent": "string | null"
  }
}
```

### 5.2 Database model

#### Raw events table

```sql
CREATE TABLE discovery_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type      VARCHAR(50) NOT NULL,
    session_id      UUID NOT NULL,
    customer_id     UUID REFERENCES customers(id),  -- nullable for guests
    timestamp       TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata        JSONB NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes
CREATE INDEX idx_discovery_events_type_ts ON discovery_events(event_type, timestamp DESC);
CREATE INDEX idx_discovery_events_session ON discovery_events(session_id);
CREATE INDEX idx_discovery_events_customer ON discovery_events(customer_id, timestamp DESC);
CREATE INDEX idx_discovery_events_gin ON discovery_events USING GIN(metadata);
```

#### Sessions table (for session identification)

```sql
CREATE TABLE discovery_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID REFERENCES customers(id),  -- nullable for guests
    user_agent      VARCHAR(500),
    platform        VARCHAR(20),  -- web, ios, android
    ip_hash         VARCHAR(64),  -- hashed IP for geo, not stored raw
    first_event_at  TIMESTAMPTZ NOT NULL,
    last_event_at   TIMESTAMPTZ NOT NULL,
    is_converted    BOOLEAN DEFAULT FALSE  -- true if session led to purchase
);

CREATE INDEX idx_discovery_sessions_customer ON discovery_sessions(customer_id);
CREATE INDEX idx_discovery_sessions_first ON discovery_sessions(first_event_at DESC);
```

#### Aggregated daily metrics table (for fast dashboard queries)

```sql
CREATE TABLE discovery_daily_metrics (
    id              SERIAL PRIMARY KEY,
    metric_date     DATE NOT NULL,
    metric_key      VARCHAR(50) NOT NULL,  -- e.g., "daily_active_users", "search_volume"
    metric_value    BIGINT NOT NULL DEFAULT 0,
    details         JSONB NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(metric_date, metric_key)
);

CREATE INDEX idx_discovery_daily_metrics_date ON discovery_daily_metrics(metric_date DESC);
CREATE INDEX idx_discovery_daily_metrics_key ON discovery_daily_metrics(metric_key);
```

### 5.3 Event ingestion endpoint

```
POST /api/v1/events
Content-Type: application/json
Authorization: Bearer <token>  (optional — guests send session_id only)

Request body:
{
  "event_type": "search_performed",
  "session_id": "uuid",
  "metadata": { ... }
}

Response:
202 Accepted
{ "event_id": "uuid", "accepted": true }
```

**Design decisions:**
- Returns `202 Accepted` — ingestion is fire-and-forget from the client perspective.
- `customer_id` is derived server-side from the auth token; the client does not send it.
- `session_id` is required. For authenticated users, the server links it to the customer. For guests, the session is anonymous.
- Batch ingestion supported: `POST /api/v1/events/batch` accepts an array of events.

### 5.4 Session identification

| Scenario | Session handling |
|---|---|
| **Authenticated customer** | `session_id` sent by client. Server links session to `customer_id` from JWT. |
| **Guest customer** | Client generates and persists `session_id` (UUID) in local storage. Server creates anonymous session. |
| **Guest → authenticated** | On login, server merges anonymous session events with the customer record. Future events use the customer's session. |
| **Session expiry** | Sessions are considered active if there is an event within the last 30 minutes. A new session starts after 30 minutes of inactivity. |

### 5.5 Event validation

- **Event type**: must be one of the predefined event types (enum validation).
- **Session ID**: must be a valid UUID.
- **Metadata**: validated against a per-event-type schema (Pydantic models).
- **Timestamp**: if not provided, server assigns `now()`. Client-provided timestamps must be within ±5 minutes of server time.
- **Rate limiting**: max 60 events per session per minute. Excess events are dropped with a `429` response.

### 5.6 Idempotency and duplicate prevention

- Each event includes an `event_id` (UUID v4 generated by the client).
- The ingestion endpoint checks for duplicate `event_id` before inserting.
- If a duplicate is detected, returns `200 OK` with the existing event (no new insert).
- Clients should cache the `event_id` and retry with the same ID on network failure.

### 5.7 Timestamps and timezone conventions

- All timestamps stored as `TIMESTAMPTZ` (UTC) in PostgreSQL.
- Client sends timestamps in ISO 8601 format with timezone offset (e.g., `2025-11-04T10:30:00+03:00`).
- Server normalizes to UTC on ingestion.
- Analytics API returns timestamps in UTC; clients format for local display.

### 5.8 Data retention

| Data | Retention | Rationale |
|---|---|---|
| **Raw events** | 90 days | Sufficient for trend analysis, debugging, and re-aggregation. |
| **Sessions** | 90 days | Tied to raw event retention. |
| **Daily aggregated metrics** | Indefinite | Small row count, supports long-term trend analysis. |
| **Search queries in metadata** | 90 days (with raw events) | Retained in aggregated form (popular terms) indefinitely. |

A scheduled job runs daily to:
1. Aggregate raw events into `discovery_daily_metrics`.
2. Delete raw events older than 90 days.

### 5.9 Privacy and sensitive-data controls

- **No PII in events**: customer email, name, phone, or address are never stored in event metadata. The `customer_id` foreign key is sufficient for joins.
- **Search queries**: stored as-is in event metadata but excluded from any export or third-party integration. Aggregated search terms (popular queries) are stored without associated customer IDs.
- **IP addresses**: stored as a one-way hash (SHA-256) for geo-approximation only. Raw IP is never stored.
- **User agent**: stored for platform detection only. Truncated to 500 characters.
- **GDPR/CCPA compliance**: events are linked to `customer_id` via foreign key. On customer data deletion request, all events for that customer are deleted. Anonymous events (no `customer_id`) are not subject to deletion requests.
- **No complete raw search history exposed**: the admin API returns aggregated search analytics (volume, popular terms, zero-result searches) — never individual search histories.

---

## 6. Analytics Definitions

### 6.1 Core metrics

All metrics use **UTC date boundaries** unless otherwise specified. A "day" is `00:00:00 UTC` to `23:59:59 UTC`.

#### Daily Active Users (DAU)
- **Definition**: Count of distinct `customer_id` OR `session_id` (for guests) that generated at least one event on the given date.
- **Query**: `SELECT COUNT(DISTINCT COALESCE(customer_id, session_id)) FROM discovery_events WHERE timestamp::date = :date`
- **Time window**: Single day.

#### Weekly Active Users (WAU)
- **Definition**: Count of distinct `customer_id` OR `session_id` that generated at least one event in the given 7-day window (Monday–Sunday UTC).
- **Query**: `SELECT COUNT(DISTINCT COALESCE(customer_id, session_id)) FROM discovery_events WHERE timestamp >= :start AND timestamp < :end`

#### Search Volume
- **Definition**: Count of `search_performed` events in the time window.
- **Note**: Each search submission counts as one, regardless of result count.

#### Searches with No Results
- **Definition**: Count of `search_results_returned` events where `metadata.has_products = false` OR `metadata.result_count = 0`.
- **Zero-result rate**: `no_results / total_searches * 100`

#### Popular Search Terms
- **Definition**: Top N search queries by frequency, derived from `search_performed` events.
- **Aggregation**: `SELECT metadata->>'query' AS query, COUNT(*) FROM discovery_events WHERE event_type = 'search_performed' GROUP BY query ORDER BY COUNT(*) DESC LIMIT :n`
- **Normalization**: queries lowercased, trimmed, whitespace-normalized before grouping.

#### Most Viewed Products
- **Definition**: Products with the highest count of `product_viewed` events in the time window.
- **Query**: `SELECT metadata->>'product_id', COUNT(*) FROM discovery_events WHERE event_type = 'product_viewed' AND timestamp >= :start GROUP BY product_id ORDER BY COUNT(*) DESC`

#### Most Added-to-Cart Products
- **Definition**: Products with the highest count of `product_added_to_cart` events.

#### Search-to-Product-View Rate
- **Definition**: Percentage of sessions that performed a search and then viewed at least one product within the same session.
- **Calculation**: `sessions_with_search_and_view / sessions_with_search * 100`
- **Window**: Per session, not per event.

#### Product-View-to-Cart Rate
- **Definition**: Percentage of sessions that viewed a product and then added at least one product to cart within the same session.
- **Calculation**: `sessions_with_view_and_cart / sessions_with_view * 100`

#### Cart-to-Order Conversion
- **Definition**: Percentage of sessions that added to cart and then completed a purchase (`purchase_completed` event) within the same session.
- **Calculation**: `sessions_with_cart_and_purchase / sessions_with_cart * 100`

#### Category Popularity
- **Definition**: Categories with the highest count of `category_viewed` events.
- **Secondary metric**: Products viewed within each category (from `product_viewed` events where the product belongs to the category).

#### Product Discovery Conversion
- **Definition**: Percentage of sessions that started with a search and ended with a purchase.
- **Calculation**: `sessions_with_search_and_purchase / sessions_with_search * 100`

### 6.2 Consistency rules

- **No double-counting**: Each event is counted once. Aggregated metrics are derived from raw events or from the daily aggregation table, never both.
- **Consistent time windows**: All date-range comparisons use the same UTC boundaries.
- **Guest vs authenticated**: Active user counts include both. Conversion funnels track by session, not by customer, to include guest journeys.
- **Metric source of truth**: Daily aggregated metrics (`discovery_daily_metrics`) are the source of truth for dashboard display. Raw events are used for detailed drill-down and re-aggregation.

---

## 7. Admin API Design

### 7.1 Endpoints

All endpoints require admin authentication (`Authorization: Bearer <admin_token>`).

#### Discovery Overview

```
GET /api/v1/admin/analytics/discovery/overview?days=7&compare=true

Response:
{
  "period": { "start": "2025-10-28", "end": "2025-11-04" },
  "previous_period": { "start": "2025-10-21", "end": "2025-10-28" },
  "daily_active_users": { "current": 1250, "previous": 1100, "change_pct": 13.6 },
  "weekly_active_users": { "current": 4800, "previous": 4200, "change_pct": 14.3 },
  "total_searches": { "current": 8420, "previous": 7100, "change_pct": 18.6 },
  "total_product_views": { "current": 3200, "previous": 2800, "change_pct": 14.3 },
  "total_cart_adds": { "current": 890, "previous": 750, "change_pct": 18.7 },
  "total_purchases": { "current": 320, "previous": 280, "change_pct": 14.3 },
  "conversion_rate": { "current": 3.8, "previous": 3.9, "change_pct": -2.6 }
}
```

#### Search Analytics

```
GET /api/v1/admin/analytics/search?days=7&page=1&size=20

Response:
{
  "total_searches": 8420,
  "zero_result_searches": 340,
  "zero_result_rate_pct": 4.0,
  "avg_results_per_search": 12.4,
  "avg_latency_ms": 240,
  "popular_terms": [
    { "query": "coffee", "count": 890, "avg_results": 15 },
    { "query": "pharmacy", "count": 650, "avg_results": 8 }
  ],
  "zero_result_terms": [
    { "query": "underwater basket weaving", "count": 12 },
    { "query": "24 hour laundromat", "count": 8 }
  ],
  "pagination": { "page": 1, "size": 20, "total": 2 }
}
```

#### Popular Products

```
GET /api/v1/admin/analytics/products/popular?days=7&metric=views&size=20

Query params:
- metric: views | cart_adds | purchases (default: views)
- days: 1, 7, 30 (default: 7)
- size: 10–50 (default: 20)

Response:
{
  "metric": "views",
  "period_days": 7,
  "products": [
    {
      "product_id": "uuid",
      "name": "Product Name",
      "category_name": "Category",
      "count": 450,
      "unique_viewers": 320,
      "cart_add_rate_pct": 19.4
    }
  ]
}
```

#### Popular Categories

```
GET /api/v1/admin/analytics/categories/popular?days=7&size=20

Response:
{
  "period_days": 7,
  "categories": [
    {
      "category_id": "uuid",
      "name": "Category Name",
      "views": 1200,
      "product_views": 890,
      "cart_adds": 120,
      "purchases": 45
    }
  ]
}
```

#### Conversion Funnel

```
GET /api/v1/admin/analytics/funnel?days=7

Response:
{
  "period_days": 7,
  "steps": [
    { "step": "search", "count": 8420, "rate": 100.0 },
    { "step": "product_view", "count": 3200, "rate": 38.0 },
    { "step": "cart_add", "count": 890, "rate": 27.8 },
    { "step": "checkout_start", "count": 520, "rate": 58.4 },
    { "step": "purchase", "count": 320, "rate": 61.5 }
  ],
  "overall_conversion_pct": 3.8
}
```

#### Date-Range Comparison

All endpoints support an optional `compare=true` parameter that returns the previous period's metrics alongside the current period, with percentage change calculated.

### 7.2 Pagination and filtering

- All list endpoints support `page` (default 1) and `size` (default 20, max 50).
- Date range controlled by `days` parameter (1, 7, 30) or explicit `start_date` / `end_date` (ISO 8601).
- Admin authorization enforced via existing JWT role check (`role == "admin"`).

### 7.3 Response schema conventions

- All responses use camelCase keys.
- Timestamps in ISO 8601 UTC format.
- Counts are integers; rates are floats with one decimal place.
- Pagination wrapper: `{ "items": [...], "pagination": { "page", "size", "total", "total_pages" } }`

---

## 8. Scalability Considerations

### 8.1 Event ingestion throughput

- **Expected load**: At early-stage marketplace scale, the backend should handle up to 100 events/second peak.
- **Approach**: Synchronous insertion into PostgreSQL with a single `INSERT` per event. No message queue needed at this scale.
- **Batch endpoint**: `POST /api/v1/events/batch` accepts up to 50 events per request, reducing HTTP overhead for mobile clients.
- **If scale exceeds 1000 events/sec**: Introduce a background worker with a simple queue (e.g., PostgreSQL `LISTEN/NOTIFY` or a dedicated queue service). Kafka is not justified at current scale.

### 8.2 Database indexing

- `discovery_events(event_type, timestamp DESC)` — primary index for time-range queries by event type.
- `discovery_events(session_id)` — session-level aggregation.
- `discovery_events(customer_id, timestamp DESC)` — customer-level analytics.
- `discovery_events USING GIN(metadata)` — JSONB metadata queries (e.g., popular search terms).
- `discovery_daily_metrics(metric_date DESC)` — fast dashboard queries.

### 8.3 Aggregation strategy

- **Raw events** are the source of truth but are not queried directly for dashboard display.
- **Daily aggregation job** (scheduled via `apscheduler` or similar, running at 00:05 UTC):
  1. Aggregates yesterday's raw events into `discovery_daily_metrics`.
  2. Updates session conversion flags.
  3. Deletes raw events older than 90 days.
- **Dashboard queries** read from `discovery_daily_metrics` only — no full table scans on raw events.
- **Real-time needs**: If a real-time dashboard is required, the daily metrics table can be updated hourly instead of daily.

### 8.4 Raw vs aggregated storage

- **Raw events**: Stored for 90 days. Used for detailed drill-down, debugging, and re-aggregation.
- **Aggregated metrics**: Stored indefinitely. Used for dashboard display and trend analysis.
- **Separation benefit**: Dashboard queries remain fast regardless of raw event volume. Raw event deletion does not affect historical metrics.

### 8.5 Avoiding customer-facing API slowdown

- Events are ingested via a dedicated endpoint (`/api/v1/events`) separate from business operations.
- The ingestion endpoint returns `202 Accepted` immediately — no blocking aggregation.
- Business endpoints (search, product detail, cart) do **not** emit events synchronously. Events are emitted by the client or by background tasks after the business operation completes.
- If server-side event emission is needed (e.g., `search_results_returned`), it happens in a background task or after the response is sent.

### 8.6 Infrastructure assumptions

- **No Redis required** for event storage. Redis may already be used for caching (mentioned in DiscoveryScreen), but events are stored in PostgreSQL.
- **No Kafka required** at current scale. A simple scheduled job handles aggregation.
- **PostgreSQL is sufficient** for both raw events and aggregated metrics at early-stage scale.

---

## 9. Privacy and Security Considerations

### 9.1 Data minimization

- Events store only what is needed for analytics. No PII beyond `customer_id` (foreign key).
- Search queries are stored in event metadata but never exposed in admin APIs individually — only as aggregated counts.
- IP addresses are hashed (SHA-256) for geo-approximation. Raw IPs are never stored.

### 9.2 Access control

- Event ingestion endpoint: public for authenticated customers, semi-public for guests (requires `session_id` only).
- Analytics admin endpoints: admin-only. Enforced via existing JWT role check.
- Raw event data is never exposed to non-admin users.

### 9.3 Data deletion

- On customer deletion request (GDPR/CCPA), all events with the `customer_id` are deleted.
- Anonymous events (no `customer_id`) are not subject to individual deletion but are covered by the 90-day retention policy.
- Aggregated metrics are not deleted on customer deletion — they represent population-level statistics, not individual data.

### 9.4 Audit trail

- Event ingestion is logged in the existing audit log system for admin visibility.
- Bulk operations (e.g., raw event deletion) are logged with counts and timestamps.

---

## 10. Implementation Phases

### Phase 1: Foundation (Weeks 1–2)

**Goal**: Event ingestion pipeline and basic storage.

- Create `discovery_events` and `discovery_sessions` tables (SQLAlchemy models + Alembic migration).
- Implement `POST /api/v1/events` endpoint with validation, idempotency, and rate limiting.
- Implement `POST /api/v1/events/batch` endpoint.
- Session identification logic (authenticated + guest support).
- Event type enum and per-type metadata validation (Pydantic models).
- Unit tests for event ingestion, validation, and idempotency.

### Phase 2: Aggregation and Retention (Weeks 3–4)

**Goal**: Daily aggregation and data lifecycle management.

- Create `discovery_daily_metrics` table.
- Implement daily aggregation scheduled job (search volume, active users, product views, cart adds, purchases).
- Implement 90-day raw event deletion job.
- Session conversion flag update job.
- Unit tests for aggregation logic.

### Phase 3: Admin Analytics API (Weeks 5–6)

**Goal**: Admin-facing analytics endpoints.

- Implement `GET /api/v1/admin/analytics/discovery/overview`.
- Implement `GET /api/v1/admin/analytics/search`.
- Implement `GET /api/v1/admin/analytics/products/popular`.
- Implement `GET /api/v1/admin/analytics/categories/popular`.
- Implement `GET /api/v1/admin/analytics/funnel`.
- Date-range comparison support.
- Admin authorization on all endpoints.
- Integration tests for analytics queries.

### Phase 4: Client Integration (Weeks 7–8)

**Goal**: Emit events from customer-facing clients.

- Client-side event emitter (SDK or utility module).
- Session management on client (generate/persist `session_id`).
- Event emission for: search performed, product viewed, product clicked, category viewed, add to cart, remove from cart, wishlist, comparison, checkout started.
- Batch event sending on app background/exit.
- Guest-to-authenticated session merge on login.

### Phase 5: Dashboard Integration (Weeks 9–10)

**Goal**: Admin portal displays discovery analytics.

- Update DiscoveryScreen (currently uses hardcoded data) to consume real analytics API.
- Add analytics tab or section in the admin portal.
- Connect to the new admin analytics endpoints.
- Update DashboardScreen to include discovery metrics.

---

## 11. Recommended Tests

### 11.1 Event ingestion

| Test | Description |
|---|---|
| Valid event accepted | Single event with valid type and metadata returns 202 |
| Batch events accepted | Multiple events in batch return 202 |
| Duplicate event rejected | Same `event_id` returns 200 with existing event |
| Invalid event type rejected | Unknown event type returns 422 |
| Missing session ID rejected | No `session_id` returns 422 |
| Metadata validation per type | `search_performed` without `query` returns 422 |
| Rate limiting | 61st event in a minute returns 429 |
| Guest event accepted | Event without auth token but with `session_id` is accepted |
| Authenticated event links customer | Event with valid JWT sets `customer_id` |

### 11.2 Aggregation

| Test | Description |
|---|---|
| Daily active users | Correct distinct count for a day |
| Search volume | Correct count of `search_performed` events |
| Zero-result searches | Correct count of searches with no results |
| Popular terms | Correct top-N queries by frequency |
| Conversion funnel | Correct step counts and rates |
| Date comparison | Previous period metrics calculated correctly |
| Raw event deletion | Events older than 90 days are deleted |
| Aggregation idempotency | Running aggregation twice produces the same result |

### 11.3 Admin API

| Test | Description |
|---|---|
| Admin access granted | Admin JWT can access analytics endpoints |
| Non-admin access denied | Non-admin JWT returns 403 |
| Date range filtering | Correct metrics for custom date range |
| Pagination | Correct page/size/total in response |
| Empty data handling | Returns zero counts, not errors, when no events exist |
| Comparison mode | `compare=true` returns previous period data |

### 11.4 Privacy

| Test | Description |
|---|---|
| No PII in events | Event metadata does not contain email, name, or phone |
| Customer deletion cascades | Deleting a customer deletes their events |
| IP hash stored, not raw | IP address is hashed before storage |
| Anonymous events survive customer deletion | Events without `customer_id` are not deleted |

---

## 12. Summary

### Root cause of the 404 error

`GET /api/v1/admin/analytics/discovery/overview` returns `{"detail": "Not Found"}` because:

1. **No analytics router** is registered in `app/api/router.py`.
2. **No analytics controller** exists in `app/modules/admin/controllers/`.
3. **No analytics handler** exists in `app/modules/admin/handlers/`.
4. **No analytics database tables** exist (`discovery_events`, `discovery_sessions`, `discovery_daily_metrics`).
5. **No event ingestion endpoint** exists anywhere in the backend.

The proposed endpoint was designed in the audit document but **never implemented on the backend**.

### Current state

- **No customer discovery event tracking exists.** The backend has 18 database tables covering users, products, orders, payments, carts, inventory, and admin audit — but zero tables for customer behavioral events.
- **The backend has rich business data** (orders, payments, product sales, user registrations) that can power business outcome analytics today.
- **The backend has zero behavioral data** (searches, views, clicks, cart adds, funnels) — these require new event tracking infrastructure.
- **The admin dashboard** (`GET /api/v1/admin/dashboard`) returns static aggregate counts with no date-range filtering or period comparison.
- **The DiscoveryScreen** in the Android admin portal was implemented with API calls to non-existent endpoints.

### What NearAI can measure today (from existing data)

| Category | Available |
|---|---|
| Orders by date, status, fulfillment | ✅ |
| Revenue by date, currency, product, category | ✅ |
| Best-selling products and categories | ✅ |
| User registrations and active customers | ✅ |
| Payment success rate | ✅ |
| Order lifecycle times | ✅ |
| Stock levels and movements | ✅ |
| Search volume, product views, conversion funnel | ❌ |

### What is needed for discovery analytics
1. **Event ingestion pipeline** — `POST /api/v1/events` with validation, idempotency, and rate limiting.
2. **Session tracking** — `discovery_sessions` table for authenticated and anonymous customers.
3. **Raw event storage** — `discovery_events` table with JSONB metadata.
4. **Aggregation infrastructure** — `discovery_daily_metrics` table + daily scheduled job.
5. **Admin analytics API** — 5 new endpoints under `/api/v1/admin/analytics/`.
6. **Client integration** — Event emission from the customer-facing app (NearAiDescovery).
7. **Data lifecycle management** — 90-day raw event retention with indefinite aggregated metrics.

### What is NOT needed
- Kafka, Redis, or other message queue infrastructure at current scale.
- Real-time event streaming.
- Complete raw search history exposure.
- PII storage in events.

### Recommended next step

Before building the full event tracking system, consider implementing a **Phase 0: Business Analytics API** that exposes the existing order, revenue, and product sales data through time-series endpoints with date-range filtering and period comparison. This would immediately provide value to the admin portal without requiring any client-side changes.

### Risk assessment
| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Event ingestion slows down customer APIs | Low | High | Dedicated endpoint, async ingestion, no blocking on business operations |
| Raw event table grows too large | Medium | Medium | 90-day retention, daily deletion job, proper indexing |
| Aggregation queries are slow | Medium | Medium | Pre-aggregated daily metrics table, GIN index on metadata |
| Privacy compliance issues | Low | High | No PII in events, hashed IPs, customer deletion cascade, 90-day retention |
| Duplicate events inflate metrics | Medium | Medium | Idempotency via `event_id`, deduplication on ingestion |
