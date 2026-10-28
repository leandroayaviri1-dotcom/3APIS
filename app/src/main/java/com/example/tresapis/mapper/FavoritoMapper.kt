package com.example.tresapis.mapper

import com.example.tresapis.dto.FavoritoDto
import com.example.tresapis.modelo.PokemonResumen

// Mapper: convierte la fila de SQLite Cloud en el modelo de la app, y al revés.
fun FavoritoDto.aResumen() = PokemonResumen(id = id, nombre = nombre, imagen = imagen)

fun PokemonResumen.aFavoritoDto() =
    FavoritoDto(id = id, nombre = nombre, imagen = imagen, agregadoEn = System.currentTimeMillis())
