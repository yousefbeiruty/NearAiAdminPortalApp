package com.nearai.admin.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Securely stores and retrieves session tokens using Android Keystore-backed
 * EncryptedSharedPreferences. Tokens are encrypted at rest and never stored
 * in plaintext.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        try {
            createEncryptedSharedPreferences()
        } catch (_: Exception) {
            // Clear corrupted shared preferences and recreate if KeyStore/encryption state is invalid
            context.deleteSharedPreferences("nearai_auth_prefs")
            createEncryptedSharedPreferences()
        }
    }

    private fun createEncryptedSharedPreferences() = EncryptedSharedPreferences.create(
        context,
        "nearai_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun hasValidSession(): Boolean = !getAccessToken().isNullOrBlank()

    /**
     * Returns true if the stored session exists and the user has the admin role.
     */
    fun hasValidAdminSession(): Boolean =
        !getAccessToken().isNullOrBlank() && getUserRole()?.equals("admin", ignoreCase = true) == true
}
