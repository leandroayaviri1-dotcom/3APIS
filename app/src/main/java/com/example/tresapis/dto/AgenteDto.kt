package com.example.tresapis.dto

// DTO = copia exacta del JSON de https://valorant-api.com/v1/agents
data class RespuestaAgentesDto(
    val status: Int,
    val data: List<AgenteDto>
)

data class AgenteDto(
    val uuid: String,
    val displayName: String,
    val description: String,
    val displayIcon: String?,
    val fullPortrait: String?,
    val background: String?,
    val backgroundGradientColors: List<String>?,
    val role: RolDto?,
    val abilities: List<HabilidadDto>?
)

data class RolDto(val displayName: String, val displayIcon: String?)

data class HabilidadDto(
    val slot: String,
    val displayName: String,
    val description: String,
    val displayIcon: String?
)
