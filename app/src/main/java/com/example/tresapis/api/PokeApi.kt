package com.example.tresapis.api

import com.example.tresapis.dto.PokemonDetalleDto
import com.example.tresapis.dto.RespuestaPokemonDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {
    @GET("pokemon")
    suspend fun listar(
        @Query("limit") limite: Int,
        @Query("offset") desde: Int
    ): RespuestaPokemonDto

    @GET("pokemon/{nombre}")
    suspend fun detalle(@Path("nombre") nombre: String): PokemonDetalleDto
}
