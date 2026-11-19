package com.nearai.admin.core.security

/**
 * Canonical role validation for the Admin application.
 *
 * The backend UserRole enum defines four roles:
 * - `customer` — storefront user
 * - `admin` — legacy tenant-level admin (preserved for backward compatibility)
 * - `merchant_admin` — tenant-level merchant admin
 * - `platform_admin` — global platform operator
 *
 * This Admin application serves as the **Platform Admin Console**.
 * Only `platform_admin` and legacy `admin` are accepted.
 * `merchant_admin` is explicitly rejected — merchant administrators
 * should use the merchant-facing application, not the global console.
 *
 * The backend remains the security authority. These client-side checks
 * are a convenience layer that prevents non-admin users from proceeding
 * past the login screen. They do not substitute for server authorization.
 */
object AdminRoleValidator {

    /**
     * Returns true if the given role string is authorized for the Admin application.
     *
     * Accepted roles:
     * - `"platform_admin"` — global platform operator (primary role)
     * - `"admin"` — legacy tenant-level admin (backward compatibility)
     *
     * Rejected roles:
     * - `"merchant_admin"` — merchant-level admin
     * - `"customer"` — storefront user
     * - Any unknown, null, blank, or malformed role string
     */
    fun isAdminRole(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        return role == "platform_admin" || role == "admin"
    }
}
