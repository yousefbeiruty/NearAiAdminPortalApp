package com.nearai.admin.core.network

import com.nearai.admin.BuildConfig
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.toAppError
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

private const val JSON_CONTENT_TYPE = "application/json"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
}

/**
 * Parses an HTTP error response into an [AppError].
 * FastAPI returns JSON error bodies with a "detail" field by convention.
 */
object ApiErrorParser {

    fun parse(httpException: HttpException): AppError {
        return when (httpException.code()) {
            401 -> AppError.UnauthorizedError()
            403 -> AppError.AuthenticationError("Access denied. Insufficient permissions.")
            404 -> AppError.NotFoundError("The requested resource was not found.")
            in 400..499 -> AppError.ServerError(
                message = extractDetail(httpException) ?: "Request failed with status ${httpException.code()}",
                statusCode = httpException.code()
            )
            in 500..599 -> AppError.ServerError(
                message = extractDetail(httpException) ?: "Server error",
                statusCode = httpException.code()
            )
            else -> httpException.toAppError()
        }
    }

    private fun extractDetail(httpException: HttpException): String? {
        // FastAPI returns {"detail": "message"} on errors
        return httpException.response()?.errorBody()?.string()?.let { body ->
            try {
                val jsonElement = json.parseToJsonElement(body)
                (jsonElement as? kotlinx.serialization.json.JsonObject)?.get("detail")?.toString()?.removeSurrounding("\"")
            } catch (_: Exception) {
                null
            }
        }
    }
}

@Singleton
fun provideJson(): Json = json

@Singleton
fun provideOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
}

@Singleton
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(JSON_CONTENT_TYPE.toMediaType()))
        .build()
}
