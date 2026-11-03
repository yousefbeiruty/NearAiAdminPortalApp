package com.nearai.admin.core.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents the current authentication state of the admin session.
 */
sealed interface SessionState {
    object Unauthenticated : SessionState
    object Authenticating : SessionState
    data class Authenticated(val email: String, val role: String) : SessionState
    object SessionExpired : SessionState
    object NotAdmin : SessionState
}

/**
 * Manages the admin session lifecycle: restoration, login, logout,
 * and session expiry detection. Exposes a single [StateFlow] that
 * the navigation layer observes to gate protected destinations.
 *
 * Session restoration validates the stored user has the admin role.
 * The backend is the authoritative source for role and account status.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage
) {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unauthenticated)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    val isAuthenticated: Boolean
        get() = _sessionState.value is SessionState.Authenticated

    /**
     * Called on app startup to restore any existing session from secure storage.
     * Validates that the stored user has the admin role.
     * Runs on Dispatchers.IO to prevent UI main thread blocking and ANRs.
     *
     * Note: This is a local validation. The backend is the authoritative source.
     * If the access token is expired, the OkHttp Authenticator will attempt to
     * refresh it automatically. If refresh fails, [onSessionExpired] will be called.
     */
    suspend fun restoreSession() = withContext(Dispatchers.IO) {
        if (tokenStorage.hasValidAdminSession()) {
            val role = tokenStorage.getUserRole() ?: "admin"
            val email = tokenStorage.getUserEmail() ?: "admin"
            _sessionState.value = SessionState.Authenticated(email = email, role = role)
        } else {
            // Clear invalid non-admin sessions
            if (tokenStorage.hasValidSession() && !tokenStorage.hasValidAdminSession()) {
                clearSession()
            }
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    /**
     * Called after successful authentication. Persists tokens and updates state.
     * Rejects non-admin users.
     */
    fun login(accessToken: String, refreshToken: String, email: String, role: String) {
        if (!role.equals("admin", ignoreCase = true)) {
            // Non-admin user attempted to access admin app — clear any tokens
            tokenStorage.clearAll()
            _sessionState.value = SessionState.NotAdmin
            return
        }
        tokenStorage.saveAccessToken(accessToken)
        tokenStorage.saveRefreshToken(refreshToken)
        tokenStorage.saveUserRole(role)
        tokenStorage.saveUserEmail(email)
        _sessionState.value = SessionState.Authenticated(email = email, role = role)
    }

    /**
     * Called when the server returns 401 or the session is determined to be expired.
     */
    fun onSessionExpired() {
        clearSession()
        _sessionState.value = SessionState.SessionExpired
    }

    /**
     * Explicitly logs out the user, clearing all stored credentials.
     */
    fun logout() {
        clearSession()
        _sessionState.value = SessionState.Unauthenticated
    }

    private fun clearSession() {
        tokenStorage.clearAll()
    }
}
