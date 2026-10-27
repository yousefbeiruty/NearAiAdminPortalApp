package com.nearai.admin.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.feature.auth.domain.AuthRepository
import com.nearai.admin.feature.auth.domain.SignInResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the authentication screen.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val totpDigits: List<String> = List(6) { "" },
    val isLoading: Boolean = false,
    val error: String? = null,
    val isMfaRequired: Boolean = false,
    val mfaHint: String? = null,
    val isDevelopmentMode: Boolean = true
)

/**
 * One-time events emitted from the ViewModel to the UI.
 */
sealed interface AuthEvent {
    object NavigateToDashboard : AuthEvent
    object NavigateToLogin : AuthEvent
}

/**
 * ViewModel for the Admin Sign-In screen.
 * Manages authentication state, MFA flow, and navigation events.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<AuthEvent?>(null)
    val event: StateFlow<AuthEvent?> = _event.asStateFlow()

    // Holds the challenge ID when MFA is required
    private var currentChallengeId: String? = null

    /**
     * Updates the email field.
     */
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    /**
     * Updates the password field.
     */
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    /**
     * Updates a single TOTP digit at the given index.
     */
    fun onTotpDigitChanged(index: Int, digit: String) {
        if (digit.length > 1) return
        if (digit.isNotEmpty() && !digit.all { it.isDigit() }) return

        _uiState.update { state ->
            val digits = state.totpDigits.toMutableList()
            digits[index] = digit
            state.copy(totpDigits = digits, error = null)
        }
    }

    /**
     * Submits the sign-in credentials.
     * If MFA is required, transitions to the MFA input state.
     */
    fun onSignIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.signIn(state.email, state.password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    when (result.data) {
                        is SignInResponse.Session -> {
                            // Direct session — sign in successful
                            val session = result.data.session
                            sessionManager.login(
                                accessToken = session.accessToken,
                                refreshToken = session.refreshToken,
                                email = session.email
                            )
                            _event.value = AuthEvent.NavigateToDashboard
                        }
                        is SignInResponse.MfaRequired -> {
                            // MFA required — show TOTP input
                            currentChallengeId = result.data.challenge.challengeId
                            _uiState.update {
                                it.copy(
                                    isMfaRequired = true,
                                    mfaHint = result.data.challenge.hint
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Submits the MFA/TOTP code.
     */
    fun onVerifyMfa() {
        val state = _uiState.value
        val totpCode = state.totpDigits.joinToString("")

        if (totpCode.length != 6) {
            _uiState.update { it.copy(error = "Please enter all 6 digits.") }
            return
        }

        val challengeId = currentChallengeId
        if (challengeId == null) {
            _uiState.update { it.copy(error = "MFA challenge expired. Please sign in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.verifyMfa(challengeId, totpCode)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    val session = result.data
                    sessionManager.login(
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken,
                        email = session.email
                    )
                    _event.value = AuthEvent.NavigateToDashboard
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Development-only: bypasses the real auth flow and navigates to dashboard.
     * ⚠️ DO NOT USE IN PRODUCTION.
     */
    fun onDevSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Simulate brief loading state
            kotlinx.coroutines.delay(800)

            sessionManager.devAuthenticate()
            _uiState.update { it.copy(isLoading = false) }
            _event.value = AuthEvent.NavigateToDashboard
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
