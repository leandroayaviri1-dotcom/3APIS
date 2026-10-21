package com.example.tresapis.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity = una tabla. Cada propiedad es una columna y cada objeto, una fila.
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    // El número del Pokémon ya es único en la API: sirve como clave primaria.
    @PrimaryKey val id: Int,
    val nombre: String,
    val imagen: String,
    // Momento en que se marcó como favorito; sirve para ordenar.
    val agregadoEn: Long = System.currentTimeMillis()
)
