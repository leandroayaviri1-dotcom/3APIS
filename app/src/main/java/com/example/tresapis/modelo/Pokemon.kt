package com.example.tresapis.modelo

// Modelos que usa la app. Los crea PokemonMapper a partir de los DTO.
data class PokemonResumen(
    val id: Int,
    val nombre: String,
    val imagen: String
)

data class PaginaPokemon(
    val pokemon: List<PokemonResumen>,
    val total: Int,
    val hayMas: Boolean
)

data class PokemonDetalle(
    val id: Int,
    val nombre: String,
    val imagen: String,
    val alturaMetros: Double,
    val pesoKg: Double,
    /** Claves en inglés ("fire", "water"…); la pantalla les pone nombre y color. */
    val tipos: List<String>,
    val habilidades: List<String>,
    val estadisticas: List<Estadistica>
)

data class Estadistica(val nombre: String, val valor: Int)
