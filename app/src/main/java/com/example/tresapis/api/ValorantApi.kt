package com.example.tresapis.api

import com.example.tresapis.dto.RespuestaAgentesDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ValorantApi {
    @GET("agents")
    suspend fun agentes(
        @Query("isPlayableCharacter") jugables: Boolean = true,
        @Query("language") idioma: String = "es-ES"
    ): RespuestaAgentesDto
}
