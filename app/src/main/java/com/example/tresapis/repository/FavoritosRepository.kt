package com.example.tresapis.repository

import com.example.tresapis.local.FavoritoDao
import com.example.tresapis.mapper.aEntidad
import com.example.tresapis.mapper.aResumen
import com.example.tresapis.modelo.PokemonResumen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Hermano de PokemonRepository: uno habla con la API y este con la base de datos.
class FavoritosRepository(private val dao: FavoritoDao) {
    fun observarFavoritos(): Flow<List<PokemonResumen>> =
        dao.observarFavoritos().map { entidades -> entidades.map { it.aResumen() } }

    fun observarIds(): Flow<Set<Int>> = dao.observarIdsFavoritos().map { it.toSet() }

    suspend fun agregar(pokemon: PokemonResumen) = dao.insertar(pokemon.aEntidad())

    suspend fun quitar(id: Int) = dao.eliminarPorId(id)
}
