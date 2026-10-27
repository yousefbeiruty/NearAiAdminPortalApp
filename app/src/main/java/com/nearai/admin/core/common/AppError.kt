package com.nearai.admin.core.common

/**
 * Represents application-level errors with user-facing messages.
 * Each subtype corresponds to a distinct error category so the UI
 * can present appropriate feedback.
 */
sealed interface AppError {
    val message: String
    val errorCode: String?

    data class NetworkError(override val message: String = "No internet connection", override val errorCode: String? = "NETWORK_ERROR") : AppError
    data class ServerError(override val message: String, val statusCode: Int, override val errorCode: String? = "SERVER_ERROR") : AppError
    data class AuthenticationError(override val message: String, override val errorCode: String? = "AUTH_ERROR") : AppError
    data class UnauthorizedError(override val message: String = "Session expired. Please sign in again.", override val errorCode: String? = "UNAUTHORIZED") : AppError
    data class NotFoundError(override val message: String, override val errorCode: String? = "NOT_FOUND") : AppError
    data class ValidationError(override val message: String, override val errorCode: String? = "VALIDATION_ERROR") : AppError
    data class UnknownError(override val message: String = "An unexpected error occurred", override val errorCode: String? = "UNKNOWN") : AppError
}

/**
 * Converts common exceptions to [AppError] instances.
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is java.net.UnknownHostException -> AppError.NetworkError("No internet connection")
        is java.net.SocketTimeoutException -> AppError.ServerError("Request timed out", 0, "TIMEOUT")
        else -> AppError.UnknownError(this.message ?: "An unexpected error occurred")
    }
}
