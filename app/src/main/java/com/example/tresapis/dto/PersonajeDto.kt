package com.example.tresapis.dto

// DTO = copia exacta del JSON de https://rickandmortyapi.com/api/character (nombres en inglés como en la API).
data class RespuestaPersonajesDto(
    val info: InfoPaginaDto,
    val results: List<PersonajeDto>
)

data class InfoPaginaDto(
    val count: Int,
    val pages: Int,
    val next: String?
)

data class PersonajeDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String?,
    val gender: String,
    val origin: LugarDto,
    val location: LugarDto,
    val image: String,
    val episode: List<String>
)

data class LugarDto(val name: String)
