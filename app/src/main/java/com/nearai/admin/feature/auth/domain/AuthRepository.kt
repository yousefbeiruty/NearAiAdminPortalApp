package com.nearai.admin.feature.auth.domain

import com.nearai.admin.core.common.Result

/**
 * Domain model representing an authenticated admin session.
 */
data class AdminSession(
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

/**
 * Domain model representing the MFA challenge state returned by the backend
 * when the initial sign-in requires multi-factor authentication.
 */
data class MfaChallenge(
    val challengeId: String,
    val method: String, // e.g. "totp", "sms", "email"
    val hint: String? = null
)

/**
 * Repository interface for authentication operations.
 * Belongs in the domain layer so the presentation layer depends on an abstraction,
 * not on Retrofit or concrete data sources.
 */
interface AuthRepository {

    /**
     * Submits administrator credentials and returns either an [AdminSession]
     * or an [MfaChallenge] if MFA is required.
     */
    suspend fun signIn(email: String, password: String): Result<SignInResponse>

    /**
     * Verifies the MFA code and returns an [AdminSession] on success.
     */
    suspend fun verifyMfa(challengeId: String, code: String): Result<AdminSession>

    /**
     * Refreshes the access token using the stored refresh token.
     */
    suspend fun refreshToken(): Result<AdminSession>

    /**
     * Clears the local session. Called on logout.
     */
    suspend fun signOut()
}

/**
 * Sealed representation of the sign-in response.
 * The backend may return a session directly or challenge for MFA.
 */
sealed interface SignInResponse {
    data class Session(val session: AdminSession) : SignInResponse
    data class MfaRequired(val challenge: MfaChallenge) : SignInResponse
}
