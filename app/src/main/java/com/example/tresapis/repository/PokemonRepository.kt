package com.example.tresapis.repository

import com.example.tresapis.api.PokeApi
import com.example.tresapis.local.PokemonCacheDao
import com.example.tresapis.mapper.aEntidadCache
import com.example.tresapis.mapper.aModelo
import com.example.tresapis.mapper.aResumen
import com.example.tresapis.modelo.PaginaPokemon
import com.example.tresapis.modelo.PokemonDetalle
import com.example.tresapis.modelo.PokemonResumen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Coordina la API y la caché: la API solo llena Room; la pantalla siempre lee de Room.
class PokemonRepository(private val api: PokeApi, private val cacheDao: PokemonCacheDao) {

    // La lista que ve el usuario: lo que haya guardado en Room, con o sin Internet.
    fun observarPokemon(): Flow<List<PokemonResumen>> =
        cacheDao.observarTodos().map { entidades -> entidades.map { it.aResumen() } }

    suspend fun hayPokemonGuardados(): Boolean = cacheDao.contar() > 0

    // Pide a la API la página que sigue a lo ya guardado y la GUARDA en Room.
    // Room emite la lista nueva por su cuenta; aquí solo se devuelve total y si hay más.
    suspend fun cargarSiguientePagina(limite: Int = 30): PaginaPokemon {
        val desde = cacheDao.contar()
        val pagina = llamar("PokéAPI", "No se encontró la lista de Pokémon.") { api.listar(limite, desde).aModelo() }
        cacheDao.insertarTodos(pagina.pokemon.map { it.aEntidadCache() })
        return pagina
    }

    suspend fun detalle(nombreOId: String): PokemonDetalle {
        val consulta = nombreOId.trim().lowercase().replace(' ', '-')
        if (consulta.isEmpty()) throw ApiException("Escribe el nombre o número de un Pokémon.")
        return llamar("PokéAPI", "No existe un Pokémon llamado \"${nombreOId.trim()}\".") {
            api.detalle(consulta).aModelo()
        }
    }
}
