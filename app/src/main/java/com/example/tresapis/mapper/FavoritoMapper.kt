package com.example.tresapis.mapper

import com.example.tresapis.local.FavoritoEntity
import com.example.tresapis.modelo.PokemonResumen

// Mapper: convierte la fila de Room en el modelo de la app, y al revés.
fun FavoritoEntity.aResumen() = PokemonResumen(id = id, nombre = nombre, imagen = imagen)

fun PokemonResumen.aEntidad() = FavoritoEntity(id = id, nombre = nombre, imagen = imagen)
