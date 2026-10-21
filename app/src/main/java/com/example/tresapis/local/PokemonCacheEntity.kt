package com.example.tresapis.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla distinta de "favoritos" a propósito: la caché se puede borrar o reconstruir,
// pero los favoritos son decisiones del usuario y nunca deben perderse.
@Entity(tableName = "pokemon_cache")
data class PokemonCacheEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val imagen: String
)
