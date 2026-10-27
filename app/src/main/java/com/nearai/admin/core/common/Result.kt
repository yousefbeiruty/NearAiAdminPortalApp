package com.nearai.admin.core.common

/**
 * A generic wrapper for operations that can succeed or fail.
 * Used throughout the app to provide consistent error handling
 * without throwing exceptions for expected failure cases.
 */
sealed interface Result<out T> {

    data class Success<out T>(val data: T) : Result<T>
    data class Error(val exception: AppError) : Result<Nothing>
    object Loading : Result<Nothing>

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): AppError? = when (this) {
        is Error -> exception
        else -> null
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
        is Loading -> Loading
    }

    fun onError(action: (AppError) -> Unit): Result<T> = when (this) {
        is Error -> {
            action(exception)
            this
        }
        else -> this
    }
}
