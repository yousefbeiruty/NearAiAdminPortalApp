package com.nearai.admin.core.network

import com.nearai.admin.BuildConfig
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.toAppError
import com.nearai.admin.core.security.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val JSON_CONTENT_TYPE = "application/json"

/**
 * Returns true if the given URL path is an authentication endpoint.
 * Authentication endpoints should never have their request bodies logged
 * (contain passwords, tokens) and should never be auto-retried on 401.
 */
private fun isAuthEndpoint(path: String): Boolean {
    return path.contains("auth/login") ||
        path.contains("auth/refresh") ||
        path.contains("auth/logout")
}

/**
 * Hilt module providing networking dependencies: Json, OkHttpClient, and Retrofit.
 * Installed in the SingletonComponent so these instances are app-wide singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    @Provides
    fun provideJson(): Json = json

    @Provides
    fun provideOkHttpClient(
        tokenStorage: TokenStorage
    ): OkHttpClient {
        // Secure logging interceptor:
        // - In debug builds, log headers only (never request/response bodies)
        // - This prevents passwords, tokens, and sensitive data from appearing in logs
        // - Use HttpLoggingInterceptor.Level.HEADERS for debugging network issues
        //   without exposing sensitive request bodies
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                // Filter out Authorization headers from logs
                if (!message.contains("Authorization", ignoreCase = true)) {
                    android.util.Log.d("OkHttp", message)
                }
            }
        }).apply {
            level = if (BuildConfig.DEBUG) {
                // HEADERS level logs method, URL, and headers — but we filter Authorization above
                // Do NOT use BODY level as it logs the entire request/response body including
                // passwords (login), refresh tokens (refresh), and sensitive user data
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // Authorization header interceptor — reads access token from secure storage
            .addInterceptor { chain ->
                val token = tokenStorage.getAccessToken()
                val request = if (!token.isNullOrBlank()) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            // Note: OkHttp Authenticator removed because Retrofit suspend functions
            // cannot be called synchronously. Token refresh on 401 is handled at the
            // repository/ViewModel level instead. See RealAuthRepository.refreshToken().
            .callTimeout(30, TimeUnit.SECONDS)
            // Connect timeout set to 3 seconds to stay safely under Android's 5-second ANR
            // threshold. DNS resolution failures (UnknownHostException) are the most common
            // cause of login ANRs. With 3s, the network error surfaces ~2 seconds before the
            // system watchdog reports "Application Not Responding", giving the UI time to
            // transition from loading state to error state and remain responsive to input.
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(JSON_CONTENT_TYPE.toMediaType()))
            .build()
    }
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
            422 -> {
                // FastAPI Pydantic 422 errors return {"detail": [{"loc": [...], "msg": "...", "type": "..."}]}
                // Parse the array of validation errors into a readable message
                val validationErrors = extractPydanticValidationErrors(httpException)
                AppError.ServerError(
                    message = validationErrors ?: "Validation failed. Please check your input.",
                    statusCode = 422
                )
            }
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

    /**
     * Extracts a human-readable message from a FastAPI Pydantic 422 validation error response.
     *
     * Pydantic 422 responses have this structure:
     * ```json
     * {
     *   "detail": [
     *     {"loc": ["body", "email"], "msg": "field required", "type": "missing"},
     *     {"loc": ["header", "authorization"], "msg": "value is not a valid uuid", "type": "type_error.uuid"}
     *   ]
     * }
     * ```
     *
     * Returns a joined string like: "Field required in body: 'email'. Value is not a valid uuid in header: 'authorization'"
     */
    private fun extractPydanticValidationErrors(httpException: HttpException): String? {
        val localJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val body = httpException.response()?.errorBody()?.string() ?: return null

        return try {
            val jsonElement = localJson.parseToJsonElement(body)
            val jsonObject = jsonElement as? kotlinx.serialization.json.JsonObject ?: return null
            val detailElement = jsonObject["detail"] ?: return null

            when (detailElement) {
                is kotlinx.serialization.json.JsonArray -> {
                    // Pydantic validation error array
                    val messages = detailElement.mapNotNull { errorObj ->
                        try {
                            val obj = errorObj as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
                            val loc = obj["loc"] as? kotlinx.serialization.json.JsonArray
                            val msg = obj["msg"]?.toString()?.removeSurrounding("\"")
                            val fieldPath = loc?.map { it.toString().removeSurrounding("\"") }?.joinToString(".") ?: ""
                            if (fieldPath.isNotBlank() && msg != null) {
                                "$msg in $fieldPath"
                            } else if (msg != null) {
                                msg
                            } else {
                                null
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (messages.isNotEmpty()) {
                        messages.joinToString(". ")
                    } else {
                        null
                    }
                }
                is kotlinx.serialization.json.JsonPrimitive -> {
                    // Some FastAPI configs return detail as a plain string even for 422
                    detailElement.content.removeSurrounding("\"")
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractDetail(httpException: HttpException): String? {
        // FastAPI returns {"detail": "message"} on errors
        val localJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        return httpException.response()?.errorBody()?.string()?.let { body ->
            try {
                val jsonElement = localJson.parseToJsonElement(body)
                (jsonElement as? kotlinx.serialization.json.JsonObject)?.get("detail")?.toString()?.removeSurrounding("\"")
            } catch (_: Exception) {
                null
            }
        }
    }
}
