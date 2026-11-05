package com.nearai.admin.feature.aiusage.data

import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.aiusage.domain.AiUsageData
import com.nearai.admin.feature.aiusage.domain.AiUsageRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AiUsageRepository] that reports the AI usage feature as unavailable.
 *
 * The backend AI usage API is not yet implemented. This repository returns an honest
 * unavailable state instead of fabricated data, in compliance with the engineering
 * constitution rule: "Never fabricate dashboard, analytics, or system-health metrics."
 *
 * When the backend API becomes available, this implementation should be updated to:
 * 1. Add an AiUsageApiService with the appropriate Retrofit endpoints.
 * 2. Add an AiUsageMapper to transform DTOs to domain models.
 * 3. Call the API via TokenRefreshManager and handle errors via ApiErrorParser.
 */
@Singleton
class AiUsageRepositoryImpl @Inject constructor() : AiUsageRepository {

    override suspend fun getAiUsageData(): Result<AiUsageData> {
        return Result.Error(
            AppError.ValidationError(
                message = "AI usage data is not yet available. This feature is coming soon.",
                errorCode = "FEATURE_UNAVAILABLE"
            )
        )
    }
}
