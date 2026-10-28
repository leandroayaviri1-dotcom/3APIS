package com.example.tresapis.dto

// Una fila de la tabla "favoritos" de SQLite Cloud, tal como la envían y reciben las Edge Functions.
data class FavoritoDto(
    val id: Int,
    val nombre: String,
    val imagen: String,
    val agregadoEn: Long
)

// Cuerpo de "quitar_favorito": solo hace falta el id.
data class IdFavoritoDto(val id: Int)

// Toda Edge Function responde {"data": ...}.
data class RespuestaSqliteCloud<T>(val data: T)
