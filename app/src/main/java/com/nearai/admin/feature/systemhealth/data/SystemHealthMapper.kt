package com.nearai.admin.feature.systemhealth.data

import com.nearai.admin.feature.systemhealth.domain.ApplicationHealth
import com.nearai.admin.feature.systemhealth.domain.ComponentHealth
import com.nearai.admin.feature.systemhealth.domain.SystemHealthData
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Mapper functions that convert API DTOs to domain models.
 */
object SystemHealthMapper {

    fun mapApplication(dto: ApplicationHealthDto): ApplicationHealth = ApplicationHealth(
        status = dto.status,
        environment = dto.environment,
        uptimeSeconds = dto.uptimeSeconds
    )

    fun mapComponent(name: String, dto: ComponentHealthDto): ComponentHealth = ComponentHealth(
        name = name,
        status = dto.status,
        details = dto.details?.mapValues { (_, value) ->
            when (value) {
                is JsonPrimitive -> value.content
                else -> value.toString()
            }
        }
    )

    fun mapSystemHealth(dto: SystemHealthResponseDto): SystemHealthData = SystemHealthData(
        overallStatus = dto.status,
        checkedAt = dto.checkedAt,
        application = mapApplication(dto.application),
        components = dto.components.map { (name, componentDto) ->
            mapComponent(name, componentDto)
        }
    )
}
