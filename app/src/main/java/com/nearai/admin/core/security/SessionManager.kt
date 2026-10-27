package com.nearai.admin.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents the current authentication state of the admin session.
 */
sealed interface SessionState {
    object Unauthenticated : SessionState
    object Authenticating : SessionState
    data class Authenticated(val email: String) : SessionState
    object SessionExpired : SessionState
}

/**
 * Manages the admin session lifecycle: restoration, login, logout,
 * and session expiry detection. Exposes a single [StateFlow] that
 * the navigation layer observes to gate protected destinations.
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
     */
    fun restoreSession() {
        if (tokenStorage.hasValidSession()) {
            _sessionState.value = SessionState.Authenticated(email = "admin")
        } else {
            _sessionState.value = SessionState.Unauthenticated
        }
    }

    /**
     * Called after successful authentication. Persists tokens and updates state.
     */
    fun login(accessToken: String, refreshToken: String, email: String) {
        tokenStorage.saveAccessToken(accessToken)
        tokenStorage.saveRefreshToken(refreshToken)
        _sessionState.value = SessionState.Authenticated(email = email)
    }

    /**
     * Development-only: mark the session as authenticated without real tokens.
     * This is used only when the backend API is not yet connected.
     * ⚠️ DO NOT USE IN PRODUCTION.
     */
    fun devAuthenticate(email: String = "admin@nearai.dev") {
        _sessionState.value = SessionState.Authenticated(email = email)
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
