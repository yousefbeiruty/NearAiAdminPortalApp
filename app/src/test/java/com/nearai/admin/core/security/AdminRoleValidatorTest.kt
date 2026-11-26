package com.nearai.admin.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [AdminRoleValidator].
 *
 * Verifies the role validation matrix:
 * - `platform_admin` — accepted (primary role)
 * - `admin` — accepted (legacy backward compatibility)
 * - `merchant_admin` — rejected
 * - `customer` — rejected
 * - Unknown role — rejected
 * - Null role — rejected
 * - Blank role — rejected
 * - Malformed strings containing "admin" — rejected
 */
class AdminRoleValidatorTest {

    @Test
    fun `platform_admin role is accepted`() {
        assertEquals(true, AdminRoleValidator.isAdminRole("platform_admin"))
    }

    @Test
    fun `legacy admin role is accepted`() {
        assertEquals(true, AdminRoleValidator.isAdminRole("admin"))
    }

    @Test
    fun `merchant_admin role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("merchant_admin"))
    }

    @Test
    fun `customer role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("customer"))
    }

    @Test
    fun `unknown role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("super_admin"))
    }

    @Test
    fun `another unknown role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("administrator"))
    }

    @Test
    fun `null role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole(null))
    }

    @Test
    fun `blank role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole(""))
    }

    @Test
    fun `whitespace role is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("   "))
    }

    @Test
    fun `case sensitive - Platform_Admin is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("Platform_Admin"))
    }

    @Test
    fun `case sensitive - ADMIN is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("ADMIN"))
    }

    @Test
    fun `malformed string containing admin is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("not_an_admin"))
    }

    @Test
    fun `malformed string with admin prefix is rejected`() {
        assertEquals(false, AdminRoleValidator.isAdminRole("admin_user"))
    }
}
