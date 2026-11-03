package com.nearai.admin.core.network

import com.nearai.admin.BuildConfig
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.toAppError
import com.nearai.admin.core.security.TokenStorage
import com.nearai.admin.feature.auth.data.AuthApiService
import com.nearai.admin.feature.auth.data.RefreshRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val JSON_CONTENT_TYPE = "application/json"

/**
 * OkHttp Authenticator that handles automatic token refresh on 401 responses.
 *
 * Key behaviors:
 * - Only retries once per request to prevent infinite loops.
 * - Serializes concurrent refresh attempts so only one refresh happens at a time.
 * - Does not retry authentication endpoints (login, refresh, logout) to avoid loops.
 * - On refresh failure, clears the stored tokens so the app can redirect to login.
 *
 * Uses a lazy [authApiServiceProvider] to avoid circular dependencies:
 * OkHttpClient → Authenticator → AuthApiService → Retrofit → OkHttpClient
 */
class TokenRefreshAuthenticator(
    private val tokenStorage: TokenStorage,
    private val authApiServiceProvider: () -> AuthApiService,
    private val tokenCleared: AtomicBoolean = AtomicBoolean(false)
) : Authenticator {

    private var isRefreshing = false

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        // Never retry authentication endpoints to avoid infinite loops
        val path = response.request.url.encodedPath
        if (path.contains("auth/login") || path.contains("auth/refresh") || path.contains("auth/logout")) {
            return null
        }

        // Only retry once per response to prevent infinite loops
        if (response.priorResponse != null) {
            return null
        }

        val refreshToken = tokenStorage.getRefreshToken()
            ?: return null

        var newAccessToken: String?

        synchronized(this) {
            if (isRefreshing) {
                // Another thread is already refreshing — just return null to let this
                // request fail; the caller will see the 401 and handle it.
                return null
            }
            isRefreshing = true
        }

        try {
            newAccessToken = runBlocking {
                try {
                    val apiService = authApiServiceProvider()
                    val refreshResponse = apiService.refreshToken(RefreshRequest(refreshToken))
                    // Store rotated tokens atomically
                    tokenStorage.saveAccessToken(refreshResponse.accessToken)
                    tokenStorage.saveRefreshToken(refreshResponse.refreshToken)
                    refreshResponse.accessToken
                } catch (e: Exception) {
                    // Refresh failed — clear tokens so the app can redirect to login
                    if (tokenCleared.compareAndSet(false, true)) {
                        tokenStorage.clearAll()
                    }
                    null
                }
            }
        } finally {
            synchronized(this) {
                isRefreshing = false
            }
        }

        return if (!newAccessToken.isNullOrBlank()) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else {
            null
        }
    }
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
        tokenStorage: TokenStorage,
        retrofitProvider: dagger.Lazy<Retrofit>
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Flag to ensure tokens are cleared only once on refresh failure
        val tokenCleared = AtomicBoolean(false)

        // Lazy provider for AuthApiService to break circular dependency:
        // OkHttpClient → Authenticator → AuthApiService → Retrofit → OkHttpClient
        val refreshAuthenticator = TokenRefreshAuthenticator(
            tokenStorage = tokenStorage,
            authApiServiceProvider = { retrofitProvider.get().create(AuthApiService::class.java) },
            tokenCleared = tokenCleared
        )

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
            .authenticator(refreshAuthenticator)
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
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
