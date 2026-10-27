package com.nearai.admin.feature.auth.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.common.toAppError
import com.nearai.admin.core.security.TokenStorage
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import com.nearai.admin.feature.auth.domain.MfaChallenge
import com.nearai.admin.feature.auth.domain.SignInResponse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Development-only fake implementation of [AuthRepository].
 *
 * ⚠️ This repository simulates authentication without a real backend.
 * It is used only when the backend API is not yet connected.
 * DO NOT USE IN PRODUCTION — replace with [RealAuthRepository] when the API is ready.
 */
@Singleton
class FakeAuthRepository @Inject constructor(
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<SignInResponse> {
        return try {
            // Simulate network latency
            delay(800)

            // Simulate MFA challenge for any sign-in attempt
            val challenge = MfaChallenge(
                challengeId = "dev-challenge-001",
                method = "totp",
                hint = "Enter 6-digit code from your authenticator app"
            )
            Result.Success(SignInResponse.MfaRequired(challenge))
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    override suspend fun verifyMfa(challengeId: String, code: String): Result<AdminSession> {
        return try {
            // Simulate network latency
            delay(500)

            // Accept any 6-digit code in dev mode
            if (code.length != 6 || !code.all { it.isDigit() }) {
                return Result.Error(AppError.ValidationError("MFA code must be 6 digits"))
            }

            val session = AdminSession(
                email = "admin@nearai.dev",
                accessToken = "dev-access-token-placeholder",
                refreshToken = "dev-refresh-token-placeholder"
            )

            // Store tokens in secure storage
            tokenStorage.saveAccessToken(session.accessToken)
            tokenStorage.saveRefreshToken(session.refreshToken)

            Result.Success(session)
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    override suspend fun refreshToken(): Result<AdminSession> {
        // In dev mode, simply return the existing session
        val accessToken = tokenStorage.getAccessToken()
        val refreshToken = tokenStorage.getRefreshToken()

        return if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            Result.Success(
                AdminSession(
                    email = "admin@nearai.dev",
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        } else {
            Result.Error(AppError.AuthenticationError("No stored session to refresh"))
        }
    }

    override suspend fun signOut() {
        tokenStorage.clearAll()
    }
}

// --- DTO to Domain Mappers ---

fun SessionDto.toDomain(): AdminSession {
    return AdminSession(
        email = this.email ?: "admin",
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}

fun SignInResponseDto.toDomain(): SignInResponse {
    return when (this) {
        is SignInResponseDto.Session -> SignInResponse.Session(this.session.toDomain())
        is SignInResponseDto.MfaRequired -> SignInResponse.MfaRequired(
            challenge = MfaChallenge(
                challengeId = this.challengeId,
                method = this.method,
                hint = this.hint
            )
        )
    }
}
