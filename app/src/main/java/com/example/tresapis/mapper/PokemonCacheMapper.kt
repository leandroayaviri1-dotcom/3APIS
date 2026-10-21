package com.example.tresapis.mapper

import com.example.tresapis.local.PokemonCacheEntity
import com.example.tresapis.modelo.PokemonResumen

// Mapper: convierte la fila de la caché en el modelo de la app, y al revés.
fun PokemonCacheEntity.aResumen() = PokemonResumen(id = id, nombre = nombre, imagen = imagen)

fun PokemonResumen.aEntidadCache() = PokemonCacheEntity(id = id, nombre = nombre, imagen = imagen)
