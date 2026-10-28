package com.example.tresapis.repository

import com.example.tresapis.api.PokeApi
import com.example.tresapis.mapper.aModelo
import com.example.tresapis.modelo.PaginaPokemon
import com.example.tresapis.modelo.PokemonDetalle

class PokemonRepository(private val api: PokeApi) {
    suspend fun listar(desde: Int, limite: Int = 30): PaginaPokemon =
        llamar("PokéAPI", "No se encontró la lista de Pokémon.") { api.listar(limite, desde).aModelo() }

    suspend fun detalle(nombreOId: String): PokemonDetalle {
        val consulta = nombreOId.trim().lowercase().replace(' ', '-')
        if (consulta.isEmpty()) throw ApiException("Escribe el nombre o número de un Pokémon.")
        return llamar("PokéAPI", "No existe un Pokémon llamado \"${nombreOId.trim()}\".") {
            api.detalle(consulta).aModelo()
        }
    }
}
