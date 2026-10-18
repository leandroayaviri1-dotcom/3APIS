package com.example.tresapis.api

import com.example.tresapis.dto.RespuestaPersonajesDto
import retrofit2.http.GET
import retrofit2.http.Query

interface RickMortyApi {
    @GET("character")
    suspend fun personajes(
        @Query("page") pagina: Int,
        @Query("name") nombre: String? = null,
        @Query("status") estado: String? = null
    ): RespuestaPersonajesDto
}
